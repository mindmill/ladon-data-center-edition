/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package de.ladon.cmis

import org.apache.chemistry.opencmis.commons.PropertyIds
import org.apache.chemistry.opencmis.commons.definitions.MutablePropertyIdDefinition
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList
import org.apache.chemistry.opencmis.commons.enums.CmisVersion
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException
import org.apache.chemistry.opencmis.commons.impl.IOUtils
import org.apache.chemistry.opencmis.commons.impl.XMLConverter
import org.apache.chemistry.opencmis.commons.impl.XMLUtils
import org.apache.chemistry.opencmis.commons.server.CallContext
import org.apache.chemistry.opencmis.server.support.TypeDefinitionFactory
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.math.BigInteger
import java.util.*
import javax.xml.stream.XMLStreamException
import javax.xml.stream.XMLStreamReader

/**
 * Manages the type definitions for all FileShare repositories.
 */
class LadonCmisTypeManager {

    private val typeDefinitionFactory: TypeDefinitionFactory
    private val typeDefinitions: MutableMap<String, TypeDefinition>

    /**
     * Returns all internal type definitions.
     */
    val internalTypeDefinitions: Collection<TypeDefinition>
        @Synchronized get() = typeDefinitions.values

    init {
        // set up TypeDefinitionFactory
        typeDefinitionFactory = TypeDefinitionFactory.newInstance()
        typeDefinitionFactory.defaultNamespace = NAMESPACE
        typeDefinitionFactory.defaultControllableAcl = false
        typeDefinitionFactory.defaultControllablePolicy = false
        typeDefinitionFactory.defaultQueryable = false
        typeDefinitionFactory.defaultFulltextIndexed = false
        typeDefinitionFactory.defaultTypeMutability = typeDefinitionFactory.createTypeMutability(false, false, false)

        // set up definitions map
        typeDefinitions = HashMap()

        // add base folder type
        val folderType = typeDefinitionFactory
                .createBaseFolderTypeDefinition(CmisVersion.CMIS_1_1)
        (folderType.propertyDefinitions[PropertyIds.OBJECT_ID] as MutablePropertyIdDefinition)
                .setIsOrderable(java.lang.Boolean.TRUE)
        (folderType.propertyDefinitions[PropertyIds.BASE_TYPE_ID] as MutablePropertyIdDefinition)
                .setIsOrderable(java.lang.Boolean.TRUE)
        typeDefinitions[folderType.id] = folderType

        // add base document type
        val documentType = typeDefinitionFactory
                .createBaseDocumentTypeDefinition(CmisVersion.CMIS_1_1)
        (documentType.propertyDefinitions[PropertyIds.OBJECT_ID] as MutablePropertyIdDefinition)
                .setIsOrderable(java.lang.Boolean.TRUE)
        (documentType.propertyDefinitions[PropertyIds.BASE_TYPE_ID] as MutablePropertyIdDefinition)
                .setIsOrderable(java.lang.Boolean.TRUE)
        typeDefinitions[documentType.id] = documentType
    }

    /**
     * Adds a type definition.
     */
    @Synchronized
    fun addTypeDefinition(type: TypeDefinition?) {
        if (type == null) {
            throw IllegalArgumentException("Type must be set!")
        }

        if (type.id == null || type.id.trim { it <= ' ' }.length == 0) {
            throw IllegalArgumentException("Type must have a valid id!")
        }

        if (type.parentTypeId == null || type.parentTypeId.trim { it <= ' ' }.length == 0) {
            throw IllegalArgumentException("Type must have a valid parent id!")
        }

        val parentType = typeDefinitions[type.parentTypeId]
                ?: throw IllegalArgumentException("Parent type doesn't exist!")

        val newType = typeDefinitionFactory.copy(type, true)

        // copy parent type property definitions and mark them as inherited
        for (propDef in parentType.propertyDefinitions.values) {
            val basePropDef = typeDefinitionFactory.copy(propDef)
            basePropDef.setIsInherited(true)
            newType.addPropertyDefinition(basePropDef)
        }

        typeDefinitions[newType.id] = newType

        if (LOG.isDebugEnabled) {
            LOG.debug("Added type '{}'.", type.id)
        }
    }

    @Throws(IOException::class, XMLStreamException::class)
    fun loadTypeDefinitionFromFile(filename: String) {
        loadTypeDefinitionFromStream(BufferedInputStream(FileInputStream(filename), 64 * 1024))
    }

    @Throws(IOException::class, XMLStreamException::class)
    fun loadTypeDefinitionFromResource(name: String) {
        loadTypeDefinitionFromStream(this.javaClass.getResourceAsStream(name))
    }

    @Throws(IOException::class, XMLStreamException::class)
    fun loadTypeDefinitionFromStream(stream: InputStream?) {
        if (stream == null) {
            throw IllegalArgumentException("Stream is null!")
        }

        var type: TypeDefinition? = null

        var parser: XMLStreamReader? = null
        try {
            parser = XMLUtils.createParser(stream)
            if (!XMLUtils.findNextStartElemenet(parser!!)) {
                return
            }

            type = XMLConverter.convertTypeDefinition(parser)
        } finally {
            parser?.close()
            IOUtils.closeQuietly(stream)
        }

        addTypeDefinition(type)
    }

    /**
     * Returns the internal type definition.
     */
    @Synchronized
    fun getInternalTypeDefinition(typeId: String): TypeDefinition? {
        return typeDefinitions[typeId]
    }

    // --- service methods ---

    fun getTypeDefinition(context: CallContext, typeId: String): TypeDefinition {
        val type = typeDefinitions[typeId] ?: throw CmisObjectNotFoundException("Type '$typeId' is unknown!")

        return typeDefinitionFactory.copy(type, true, context.cmisVersion)
    }

    fun getTypeChildren(context: CallContext, typeId: String, includePropertyDefinitions: Boolean?,
                        maxItems: BigInteger, skipCount: BigInteger): TypeDefinitionList {
        return typeDefinitionFactory.createTypeDefinitionList(typeDefinitions, typeId, includePropertyDefinitions,
                maxItems, skipCount, context.cmisVersion)
    }

    fun getTypeDescendants(context: CallContext, typeId: String, depth: BigInteger,
                           includePropertyDefinitions: Boolean?): List<TypeDefinitionContainer> {
        return typeDefinitionFactory.createTypeDescendants(typeDefinitions, typeId, depth, includePropertyDefinitions,
                context.cmisVersion)
    }

    override fun toString(): String {
        val sb = StringBuilder(128)

        for (type in typeDefinitions.values) {
            sb.append('[')
            sb.append(type.id)
            sb.append(" (")
            sb.append(type.baseTypeId.value())
            sb.append(")]")
        }

        return sb.toString()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(LadonCmisTypeManager::class.java)
        private val NAMESPACE = "http://ladon.org/opencmis"
    }
}
