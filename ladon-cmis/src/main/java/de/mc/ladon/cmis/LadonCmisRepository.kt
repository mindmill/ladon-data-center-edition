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
package de.mc.ladon.cmis

import de.mc.ladon.cmis.FileShareUtils.*
import de.mc.ladon.server.core.api.Document
import de.mc.ladon.server.core.api.DocumentNotFound
import de.mc.ladon.server.core.api.LadonRepository
import org.apache.chemistry.opencmis.commons.BasicPermissions
import org.apache.chemistry.opencmis.commons.PropertyIds
import org.apache.chemistry.opencmis.commons.data.*
import org.apache.chemistry.opencmis.commons.data.Properties
import org.apache.chemistry.opencmis.commons.definitions.*
import org.apache.chemistry.opencmis.commons.enums.*
import org.apache.chemistry.opencmis.commons.exceptions.*
import org.apache.chemistry.opencmis.commons.impl.Base64
import org.apache.chemistry.opencmis.commons.impl.MimeTypes
import org.apache.chemistry.opencmis.commons.impl.dataobjects.*
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl
import org.apache.chemistry.opencmis.commons.server.CallContext
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler
import org.apache.chemistry.opencmis.commons.spi.Holder
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class LadonCmisRepository(

        val repositoryId: String?, rootPath: String?,
        private val typeManager: LadonCmisTypeManager) {

    val rootDirectory: File
    val userManager = lazy { LadonServicesHolder.userDetailsManager() }
    val ladonRepo = lazy { LadonServicesHolder.getService(LadonRepository::class.java) }


    private val readWriteUserMap: MutableMap<String, Boolean>

    private val repositoryInfo11: RepositoryInfo

    init {
        // check repository id
        if (repositoryId == null || repositoryId.trim { it <= ' ' }.isEmpty()) {
            throw IllegalArgumentException("Invalid repository id!")
        }

        // check root folder
        if (rootPath == null || rootPath.trim { it <= ' ' }.length == 0) {
            throw IllegalArgumentException("Invalid root folder!")
        }

        rootDirectory = File(rootPath)
        if (!rootDirectory.isDirectory) {
            throw IllegalArgumentException("Root is not a directory!")
        }

        // set up read-write user map
        readWriteUserMap = HashMap()

        repositoryInfo11 = createRepositoryInfo()
    }// set type manager objects

    private fun createRepositoryInfo(): RepositoryInfo {

        val repositoryInfo = RepositoryInfoImpl()

        repositoryInfo.id = repositoryId
        repositoryInfo.name = repositoryId
        repositoryInfo.description = repositoryId

        repositoryInfo.cmisVersionSupported = "1.1"

        repositoryInfo.productName = "Ladon Datacenter Edition"
        repositoryInfo.productVersion = "3.0"
        repositoryInfo.vendorName = "Mind Consulting"

        repositoryInfo.setRootFolder(ROOT_ID)

        repositoryInfo.thinClientUri = ""
        repositoryInfo.changesIncomplete = true

        val capabilities = RepositoryCapabilitiesImpl()
        capabilities.setCapabilityAcl(CapabilityAcl.DISCOVER)
        capabilities.setAllVersionsSearchable(false)
        capabilities.setCapabilityJoin(CapabilityJoin.NONE)
        capabilities.setSupportsMultifiling(false)
        capabilities.setSupportsUnfiling(true)
        capabilities.setSupportsVersionSpecificFiling(false)
        capabilities.setIsPwcSearchable(false)
        capabilities.setIsPwcUpdatable(false)
        capabilities.setCapabilityQuery(CapabilityQuery.NONE)
        capabilities.setCapabilityChanges(CapabilityChanges.NONE)
        capabilities.setCapabilityContentStreamUpdates(CapabilityContentStreamUpdates.ANYTIME)
        capabilities.setSupportsGetDescendants(true)
        capabilities.setSupportsGetFolderTree(true)
        capabilities.setCapabilityRendition(CapabilityRenditions.NONE)


        capabilities.setCapabilityOrderBy(CapabilityOrderBy.COMMON)

        val typeSetAttributes = NewTypeSettableAttributesImpl()
        typeSetAttributes.setCanSetControllableAcl(false)
        typeSetAttributes.setCanSetControllablePolicy(false)
        typeSetAttributes.setCanSetCreatable(false)
        typeSetAttributes.setCanSetDescription(false)
        typeSetAttributes.setCanSetDisplayName(false)
        typeSetAttributes.setCanSetFileable(false)
        typeSetAttributes.setCanSetFulltextIndexed(false)
        typeSetAttributes.setCanSetId(false)
        typeSetAttributes.setCanSetIncludedInSupertypeQuery(false)
        typeSetAttributes.setCanSetLocalName(false)
        typeSetAttributes.setCanSetLocalNamespace(false)
        typeSetAttributes.setCanSetQueryable(false)
        typeSetAttributes.setCanSetQueryName(false)

        capabilities.newTypeSettableAttributes = typeSetAttributes

        val creatablePropertyTypes = CreatablePropertyTypesImpl()
        capabilities.creatablePropertyTypes = creatablePropertyTypes


        repositoryInfo.capabilities = capabilities

        val aclCapability = AclCapabilitiesDataImpl()
        aclCapability.supportedPermissions = SupportedPermissions.BASIC
        aclCapability.aclPropagation = AclPropagation.OBJECTONLY

        // permissions
        val permissions = ArrayList<PermissionDefinition>()
        permissions.add(createPermission(BasicPermissions.READ, "Read"))
        permissions.add(createPermission(BasicPermissions.WRITE, "Write"))
        permissions.add(createPermission(BasicPermissions.ALL, "All"))
        aclCapability.setPermissionDefinitionData(permissions)

        // mapping
        val list = ArrayList<PermissionMapping>()
        list.add(createMapping(PermissionMapping.CAN_CREATE_DOCUMENT_FOLDER, BasicPermissions.READ))
        list.add(createMapping(PermissionMapping.CAN_CREATE_FOLDER_FOLDER, BasicPermissions.READ))
        list.add(createMapping(PermissionMapping.CAN_DELETE_CONTENT_DOCUMENT, BasicPermissions.WRITE))
        list.add(createMapping(PermissionMapping.CAN_DELETE_OBJECT, BasicPermissions.ALL))
        list.add(createMapping(PermissionMapping.CAN_DELETE_TREE_FOLDER, BasicPermissions.ALL))
        list.add(createMapping(PermissionMapping.CAN_GET_ACL_OBJECT, BasicPermissions.READ))
        list.add(createMapping(PermissionMapping.CAN_GET_ALL_VERSIONS_VERSION_SERIES, BasicPermissions.READ))
        list.add(createMapping(PermissionMapping.CAN_GET_CHILDREN_FOLDER, BasicPermissions.READ))
        list.add(createMapping(PermissionMapping.CAN_GET_DESCENDENTS_FOLDER, BasicPermissions.READ))
        list.add(createMapping(PermissionMapping.CAN_GET_FOLDER_PARENT_OBJECT, BasicPermissions.READ))
        list.add(createMapping(PermissionMapping.CAN_GET_PARENTS_FOLDER, BasicPermissions.READ))
        list.add(createMapping(PermissionMapping.CAN_GET_PROPERTIES_OBJECT, BasicPermissions.READ))
        list.add(createMapping(PermissionMapping.CAN_MOVE_OBJECT, BasicPermissions.WRITE))
        list.add(createMapping(PermissionMapping.CAN_MOVE_SOURCE, BasicPermissions.READ))
        list.add(createMapping(PermissionMapping.CAN_MOVE_TARGET, BasicPermissions.WRITE))
        list.add(createMapping(PermissionMapping.CAN_SET_CONTENT_DOCUMENT, BasicPermissions.WRITE))
        list.add(createMapping(PermissionMapping.CAN_UPDATE_PROPERTIES_OBJECT, BasicPermissions.WRITE))
        list.add(createMapping(PermissionMapping.CAN_VIEW_CONTENT_OBJECT, BasicPermissions.READ))
        val map = LinkedHashMap<String, PermissionMapping>()
        for (pm in list) {
            map[pm.key] = pm
        }
        aclCapability.setPermissionMappingData(map)

        repositoryInfo.aclCapabilities = aclCapability

        return repositoryInfo
    }

    private fun createPermission(permission: String, description: String): PermissionDefinition {
        val pd = PermissionDefinitionDataImpl()
        pd.id = permission
        pd.description = description

        return pd
    }

    private fun createMapping(key: String, permission: String): PermissionMapping {
        val pm = PermissionMappingDataImpl()
        pm.key = key
        pm.permissions = listOf(permission)

        return pm
    }

    /**
     * Sets read-only flag for the given user.
     */
    fun setUserReadOnly(user: String?) {
        if (user == null || user.length == 0) {
            return
        }

        readWriteUserMap[user] = true
    }

    /**
     * Sets read-write flag for the given user.
     */
    fun setUserReadWrite(user: String?) {
        if (user == null || user.length == 0) {
            return
        }

        readWriteUserMap[user] = false
    }

    // --- CMIS operations ---

    /**
     * CMIS getRepositoryInfo.
     */
    fun getRepositoryInfo(context: CallContext): RepositoryInfo {
        debug("getRepositoryInfo")

        checkUser(context, false)

        return repositoryInfo11

    }

    /**
     * CMIS getTypesChildren.
     */
    fun getTypeChildren(context: CallContext, typeId: String?, includePropertyDefinitions: Boolean?,
                        maxItems: BigInteger, skipCount: BigInteger): TypeDefinitionList {
        debug("getTypesChildren")
        checkUser(context, false)

        return typeManager.getTypeChildren(context, typeId, includePropertyDefinitions, maxItems, skipCount)
    }

    /**
     * CMIS getTypesDescendants.
     */
    fun getTypeDescendants(context: CallContext, typeId: String, depth: BigInteger,
                           includePropertyDefinitions: Boolean?): List<TypeDefinitionContainer> {
        debug("getTypesDescendants")
        checkUser(context, false)

        return typeManager.getTypeDescendants(context, typeId, depth, includePropertyDefinitions)
    }

    /**
     * CMIS getTypeDefinition.
     */
    fun getTypeDefinition(context: CallContext, typeId: String): TypeDefinition {
        debug("getTypeDefinition")
        checkUser(context, false)

        return typeManager.getTypeDefinition(context, typeId)
    }

//    /**
//     * Create* dispatch for AtomPub.
//     */
//    fun create(context: CallContext, properties: Properties, folderId: String, contentStream: ContentStream?,
//               versioningState: VersioningState?, objectInfos: ObjectInfoHandler): ObjectData {
//        debug("create")
//        val userReadOnly = checkUser(context, true)
//
//        val typeId = FileShareUtils.getObjectTypeId(properties)
//        val type = typeManager.getInternalTypeDefinition(typeId)
//                ?: throw CmisObjectNotFoundException("Type '$typeId' is unknown!")
//
//        var objectId: String? = null
//        if (type.baseTypeId == BaseTypeId.CMIS_DOCUMENT) {
//            objectId = createDocument(context, properties, folderId, contentStream, versioningState)
//        } else if (type.baseTypeId == BaseTypeId.CMIS_FOLDER) {
//            if (contentStream != null || versioningState != null) {
//                throw CmisInvalidArgumentException("Cannot create a folder with content or a versioning state!")
//            }
//
//            objectId = createFolder(context, properties, folderId)
//        } else {
//            throw CmisObjectNotFoundException("Cannot create object of type '$typeId'!")
//        }
//
//        return compileObjectData(context, getFile(objectId), null, false, false, userReadOnly, objectInfos)
//    }

    /**
     * CMIS createDocument.
     */
    fun createDocument(context: CallContext, properties: Properties?, folderId: String,
                       contentStream: ContentStream?, versioningState: VersioningState?): String {
        debug("createDocument")
        checkUser(context, true)

        // check properties
        if (properties == null || properties.properties == null) {
            throw CmisInvalidArgumentException("Properties must be set!")
        }

        // check versioning state
        if (!(VersioningState.NONE == versioningState || versioningState == null)) {
            throw CmisConstraintException("Versioning not supported!")
        }

        // check type
        val typeId = getObjectTypeId(properties)
        val type = typeManager.getInternalTypeDefinition(typeId)
                ?: throw CmisObjectNotFoundException("Type '$typeId' is unknown!")
        if (type.baseTypeId != BaseTypeId.CMIS_DOCUMENT) {
            throw CmisInvalidArgumentException("Type must be a document type!")
        }

        // compile the properties
        val props = compileWriteProperties(typeId, context.username, context.username, properties)

        // check the name
        val name = getStringProperty(properties, PropertyIds.NAME)
        if (!isValidName(name)) {
            throw CmisNameConstraintViolationException("Name is not valid!")
        }

        // get parent File
        val parent = getDocument(context.username, folderId.fromBase64Id())
        if (!parent.isFolder) {
            throw CmisObjectNotFoundException("Parent is not a folder!")
        }

        // check the file
        val file = folderId.fromBase64Id() + "/" + name
        if (documentExists(context.username, file)) {
            throw CmisNameConstraintViolationException("Document already exists!")
        }

        // set creation date
        addPropertyDateTime(props, typeId, null, PropertyIds.CREATION_DATE,
                millisToCalendar(System.currentTimeMillis()))

        // write content, if available
        val newId = if (contentStream != null && contentStream.stream != null) {
            putDocumentContent(context.username, file, contentStream, props)
        } else {
            putDocument(context.username, file, props)
        }

        return newId
    }

    /**
     * CMIS createDocumentFromSource.
     */
    fun createDocumentFromSource(context: CallContext, sourceId: String, properties: Properties?,
                                 folderId: String, versioningState: VersioningState?): String {
        debug("createDocumentFromSource")
        checkUser(context, true)

        // check versioning state
        if (!(VersioningState.NONE == versioningState || versioningState == null)) {
            throw CmisConstraintException("Versioning not supported!")
        }

        // get parent File
        val parent = getDocument(context.username, folderId.fromBase64Id())
        if (!parent.isFolder) {
            throw CmisObjectNotFoundException("Parent is not a folder!")
        }

        // check the file
        val sourceFile = sourceId.fromBase64Id()


        // get source File
        val source = getDocument(context.username, sourceFile)
        if (!source.isFolder) {
            throw CmisObjectNotFoundException("Source is not a document!")
        }

        // file name
        var name = source.getName()
        val targetFile = folderId.fromBase64Id() + "/" + name


        // get properties
        val sourceProperties = PropertiesImpl()
        readCustomProperties(source, sourceProperties, null)

        // get the type id
        var typeId = getIdProperty(sourceProperties, PropertyIds.OBJECT_TYPE_ID)
        if (typeId == null) {
            typeId = BaseTypeId.CMIS_DOCUMENT.value()
        }

        // copy properties
        val newProperties = PropertiesImpl()
        for (prop in sourceProperties.properties.values) {
            if (prop.id == PropertyIds.OBJECT_TYPE_ID || prop.id == PropertyIds.CREATED_BY
                    || prop.id == PropertyIds.CREATION_DATE
                    || prop.id == PropertyIds.LAST_MODIFIED_BY) {
                continue
            }

            newProperties.addProperty(prop)
        }

        // replace properties
        if (properties != null) {
            // find new name
            val newName = getStringProperty(properties, PropertyIds.NAME)
            if (newName != null) {
                if (!isValidName(newName)) {
                    throw CmisNameConstraintViolationException("Name is not valid!")
                }
                name = newName
            }

            // get the property definitions
            val type = typeManager.getInternalTypeDefinition(typeId!!)
                    ?: throw CmisObjectNotFoundException("Type '$typeId' is unknown!")
            if (type.baseTypeId != BaseTypeId.CMIS_DOCUMENT) {
                throw CmisInvalidArgumentException("Type must be a document type!")
            }

            // replace with new values
            for (prop in properties.properties.values) {
                val propType = type.propertyDefinitions[prop.id]
                        ?: throw CmisConstraintException("Property '" + prop.id + "' is unknown!")

                // do we know that property?

                // can it be set?
                if (propType.updatability != Updatability.READWRITE) {
                    throw CmisConstraintException("Property '" + prop.id + "' cannot be updated!")
                }

                // empty properties are invalid
                if (isEmptyProperty(prop)) {
                    throw CmisConstraintException("Property '" + prop.id + "' must not be empty!")
                }

                newProperties.addProperty(prop)
            }
        }

        addPropertyId(newProperties, typeId, null, PropertyIds.OBJECT_TYPE_ID, typeId)
        addPropertyString(newProperties, typeId, null, PropertyIds.CREATED_BY, context.username)
        addPropertyDateTime(newProperties, typeId, null, PropertyIds.CREATION_DATE,
                millisToCalendar(System.currentTimeMillis()))
        addPropertyString(newProperties, typeId, null, PropertyIds.LAST_MODIFIED_BY, context.username)

        // check the file
        if (documentExists(context.username, targetFile)) {
            throw CmisNameConstraintViolationException("Document already exists.")
        }

        // create the file
        return copyDocument(context.username, sourceFile, targetFile, newProperties)
    }


    /**
     * CMIS createFolder.
     */
    fun createFolder(context: CallContext, properties: Properties?, folderId: String): String {
        debug("createFolder")
        checkUser(context, true)

//        // check properties
//        if (properties == null || properties.properties == null) {
//            throw CmisInvalidArgumentException("Properties must be set!")
//        }
//
//        // check type
//        val typeId = getObjectTypeId(properties)
//        val type = typeManager.getInternalTypeDefinition(typeId)
//                ?: throw CmisObjectNotFoundException("Type '$typeId' is unknown!")
//        if (type.baseTypeId != BaseTypeId.CMIS_FOLDER) {
//            throw CmisInvalidArgumentException("Type must be a folder type!")
//        }
//
//        // compile the properties
//        val props = compileWriteProperties(typeId, context.username, context.username, properties)
//
//        // check the name
//        val name = getStringProperty(properties, PropertyIds.NAME)
//        if (!isValidName(name)) {
//            throw CmisNameConstraintViolationException("Name is not valid.")
//        }
//
//        // get parent File
//        val parent = getFile(folderId)
//        if (!parent.isDirectory) {
//            throw CmisObjectNotFoundException("Parent is not a folder!")
//        }
//
//        // create the folder
//        val newFolder = File(parent, name!!)
//        if (!newFolder.mkdir()) {
//            throw CmisStorageException("Could not create folder!")
//        }
//
//        // set creation date
//        addPropertyDateTime(props, typeId, null, PropertyIds.CREATION_DATE,
//                millisToCalendar(newFolder.lastModified()))
//
//        // write properties
//        writePropertiesFile(newFolder, props)
//
//        return getId(newFolder)
        TODO()
    }

    /**
     * CMIS moveObject.
     */
    fun moveObject(context: CallContext, objectId: Holder<String>?, targetFolderId: String,
                   objectInfos: ObjectInfoHandler): ObjectData {
        debug("moveObject")
        val userReadOnly = checkUser(context, true)

        if (objectId == null) {
            throw CmisInvalidArgumentException("Id is not valid!")
        }

//        // get the file and parent
//        val file = getFile(objectId.value)
//        val parent = getFile(targetFolderId)
//
//        // build new path
//        val newFile = File(parent, file.name)
//        if (newFile.exists()) {
//            throw CmisStorageException("Object already exists!")
//        }
//
//        // move it
//        if (!file.renameTo(newFile)) {
//            throw CmisStorageException("Move failed!")
//        } else {
//            // set new id
//            objectId.value = getId(newFile)
//
//            // if it is a file, move properties file too
//            if (newFile.isFile) {
//                val propFile = getPropertiesFile(file)
//                if (propFile.exists()) {
//                    val newPropFile = File(parent, propFile.name)
//                    if (!propFile.renameTo(newPropFile)) {
//                        LOG.error("Could not rename properties file: {}", propFile.name)
//                    }
//                }
//            }
//        }
//
//        return compileObjectData(context, newFile, null, false, false, userReadOnly, objectInfos)
        TODO()
    }

    /**
     * CMIS setContentStream, deleteContentStream, and appendContentStream.
     */
    fun changeContentStream(context: CallContext, objectId: Holder<String>?, overwriteFlag: Boolean?,
                            contentStream: ContentStream?, append: Boolean) {
        debug("setContentStream or deleteContentStream or appendContentStream")
        checkUser(context, true)

        if (objectId == null) {
            throw CmisInvalidArgumentException("Id is not valid!")
        }

//        // get the file
//        val file = getFile(objectId.value)
//        if (!file.isFile) {
//            throw CmisStreamNotSupportedException("Not a file!")
//        }
//
//        // check overwrite
//        val owf = getBooleanParameter(overwriteFlag, true)
//        if (!owf && file.length() > 0) {
//            throw CmisContentAlreadyExistsException("Content already exists!")
//        }
//
//        var out: OutputStream? = null
//        var `in`: InputStream? = null
//        try {
//            out = FileOutputStream(file, append)
//
//            if (contentStream == null || contentStream.stream == null) {
//                // delete content
//                out.write(ByteArray(0))
//            } else {
//                // set content
//                `in` = contentStream.stream
//                IOUtils.copy(`in`!!, out, BUFFER_SIZE)
//            }
//        } catch (e: Exception) {
//            throw CmisStorageException("Could not write content: " + e.message, e)
//        } finally {
//            IOUtils.closeQuietly(out)
//            IOUtils.closeQuietly(`in`)
//        }
        TODO()
    }

    /**
     * CMIS deleteObject.
     */
    fun deleteObject(context: CallContext, objectId: String) {
        debug("deleteObject")
        checkUser(context, true)

//        // get the file or folder
//        val file = getFile(objectId)
//        if (!file.exists()) {
//            throw CmisObjectNotFoundException("Object not found!")
//        }
//
//        // check if it is a folder and if it is empty
//        if (!isFolderEmpty(file)) {
//            throw CmisConstraintException("Folder is not empty!")
//        }
//
//        // delete properties and actual file
//        getPropertiesFile(file).delete()
//        if (!file.delete()) {
//            throw CmisStorageException("Deletion failed!")
//        }
        TODO()
    }

    /**
     * CMIS deleteTree.
     */
    fun deleteTree(context: CallContext, folderId: String, continueOnFailure: Boolean?): FailedToDeleteData {
        debug("deleteTree")
        checkUser(context, true)

        val cof = getBooleanParameter(continueOnFailure, false)

//        // get the file or folder
//        val file = getFile(folderId)
//
//        val result = FailedToDeleteDataImpl()
//        result.ids = ArrayList()
//
//        // if it is a folder, remove it recursively
//        if (file.isDirectory) {
//            deleteFolder(file, cof, result)
//        } else {
//            throw CmisConstraintException("Object is not a folder!")
//        }
//
//        return result
        TODO()
    }

//    /**
//     * Removes a folder and its content.
//     */
//    private fun deleteFolder(folder: File, continueOnFailure: Boolean, ftd: FailedToDeleteDataImpl): Boolean {
//        var success = true
//
//        for (file in folder.listFiles()!!) {
//            if (file.isDirectory) {
//                if (!deleteFolder(file, continueOnFailure, ftd)) {
//                    if (!continueOnFailure) {
//                        return false
//                    }
//                    success = false
//                }
//            } else {
//                if (!file.delete()) {
//                    ftd.ids.add(getId(file))
//                    if (!continueOnFailure) {
//                        return false
//                    }
//                    success = false
//                }
//            }
//        }
//
//        if (!folder.delete()) {
//            ftd.ids.add(getId(folder))
//            success = false
//        }
//
//        return success
//    }

    /**
     * CMIS updateProperties.
     */
    fun updateProperties(context: CallContext, objectId: Holder<String>?, properties: Properties,
                         objectInfos: ObjectInfoHandler): ObjectData {
        debug("updateProperties")
        val userReadOnly = checkUser(context, true)

        if (objectId == null || objectId.value == null) {
            throw CmisInvalidArgumentException("Id is not valid!")
        }

        // get the file or folder
        val document = getDocument(context.username, objectId.value.fromBase64Id())

        // get and check the new name
        val newName = getStringProperty(properties, PropertyIds.NAME)
        val isRename = newName != null && document.getName() != newName
        if (isRename && !isValidName(newName)) {
            throw CmisNameConstraintViolationException("Name is not valid!")
        }

        // get old properties
        val oldProperties = PropertiesImpl()
        readCustomProperties(document, oldProperties, null)

        // get the type id
        var typeId = getIdProperty(oldProperties, PropertyIds.OBJECT_TYPE_ID)
        if (typeId == null) {
            typeId = if (document.isFolder) BaseTypeId.CMIS_FOLDER.value() else BaseTypeId.CMIS_DOCUMENT.value()
        }

        // get the creator
        var creator = getStringProperty(oldProperties, PropertyIds.CREATED_BY)
        if (creator == null) {
            creator = context.username
        }

        // get creation date
        var creationDate = getDateTimeProperty(oldProperties, PropertyIds.CREATION_DATE)
        if (creationDate == null) {
            creationDate = millisToCalendar(document.created.toEpochSecond(ZoneOffset.UTC))
        }

        // compile the properties
        val props = updateProperties(typeId, creator, creationDate, context.username, oldProperties,
                properties)

        // write properties
//        writePropertiesFile(file, props)
//
//        // rename file or folder if necessary
//        var newFile = file
//        if (isRename) {
//            val parent = file.parentFile
//            val propFile = getPropertiesFile(file)
//            newFile = File(parent, newName!!)
//            if (!file.renameTo(newFile)) {
//                // if something went wrong, throw an exception
//                throw CmisUpdateConflictException("Could not rename object!")
//            } else {
//                // set new id
//                objectId.value = getId(newFile)
//
//                // if it is a file, rename properties file too
//                if (newFile.isFile) {
//                    if (propFile.exists()) {
//                        val newPropFile = File(parent, newName + SHADOW_EXT)
//                        if (!propFile.renameTo(newPropFile)) {
//                            LOG.error("Could not rename properties file: {}", propFile.name)
//                        }
//                    }
//                }
//            }
//        }
//        return compileObjectData(context, newFile, null, false, false, userReadOnly, objectInfos)
        TODO()
    }

    /**
     * Checks and updates a property set that can be written to disc.
     */
    private fun updateProperties(typeId: String, creator: String, creationDate: GregorianCalendar, modifier: String,
                                 oldProperties: Properties, properties: Properties?): Properties {
        val result = PropertiesImpl()

        if (properties == null) {
            throw CmisConstraintException("No properties!")
        }

        // get the property definitions
        val type = typeManager.getInternalTypeDefinition(typeId)
                ?: throw CmisObjectNotFoundException("Type '$typeId' is unknown!")

        // copy old properties
        for (prop in oldProperties.properties.values) {
            val propType = type.propertyDefinitions[prop.id]
                    ?: throw CmisConstraintException("Property '" + prop.id + "' is unknown!")

            // do we know that property?

            // only add read/write properties
            if (propType.updatability != Updatability.READWRITE) {
                continue
            }

            result.addProperty(prop)
        }

        // update properties
        for (prop in properties.properties.values) {
            val propType = type.propertyDefinitions[prop.id]
                    ?: throw CmisConstraintException("Property '" + prop.id + "' is unknown!")

            // do we know that property?

            // can it be set?
            if (propType.updatability == Updatability.READONLY) {
                throw CmisConstraintException("Property '" + prop.id + "' is readonly!")
            }

            if (propType.updatability == Updatability.ONCREATE) {
                throw CmisConstraintException("Property '" + prop.id + "' can only be set on create!")
            }

            // default or value
            if (isEmptyProperty(prop)) {
                addPropertyDefault(result, propType)
            } else {
                result.addProperty(prop)
            }
        }

        addPropertyId(result, typeId, null, PropertyIds.OBJECT_TYPE_ID, typeId)
        addPropertyString(result, typeId, null, PropertyIds.CREATED_BY, creator)
        addPropertyDateTime(result, typeId, null, PropertyIds.CREATION_DATE, creationDate)
        addPropertyString(result, typeId, null, PropertyIds.LAST_MODIFIED_BY, modifier)

        return result
    }

    /**
     * CMIS bulkUpdateProperties.
     */
    fun bulkUpdateProperties(context: CallContext,
                             objectIdAndChangeToken: List<BulkUpdateObjectIdAndChangeToken>?, properties: Properties?,
                             objectInfos: ObjectInfoHandler): List<BulkUpdateObjectIdAndChangeToken> {
        debug("bulkUpdateProperties")
        checkUser(context, true)

        if (objectIdAndChangeToken == null) {
            throw CmisInvalidArgumentException("No object ids provided!")
        }

        val result = ArrayList<BulkUpdateObjectIdAndChangeToken>()

        for (oid in objectIdAndChangeToken) {
            if (oid == null) {
                // ignore invalid ids
                continue
            }
            try {
                val oidHolder = Holder(oid.id)
                updateProperties(context, oidHolder, properties!!, objectInfos)

                result.add(BulkUpdateObjectIdAndChangeTokenImpl(oid.id, oidHolder.value, null))
            } catch (e: CmisBaseException) {
                // ignore exceptions - see specification
            }

        }

        return result
    }

    /**
     * CMIS getObject.
     */
    fun getObject(context: CallContext, objectId: String?, versionServicesId: String?, filter: String?,
                  includeAllowableActions: Boolean?, includeAcl: Boolean?, objectInfos: ObjectInfoHandler?): ObjectData {
        var objectId = objectId
        debug("getObject")
        val userReadOnly = checkUser(context, false)

        // check id
        if (objectId == null && versionServicesId == null) {
            throw CmisInvalidArgumentException("Object Id must be set.")
        }

        if (objectId == null) {
            // this works only because there are no versions in a file system
            // and the object id and version series id are the same
            objectId = versionServicesId
        }

        // get the file or folder
        val document = getDocument(context.username, objectId!!.fromBase64Id())

        // set defaults if values not set
        val iaa = getBooleanParameter(includeAllowableActions, false)
        val iacl = getBooleanParameter(includeAcl, false)

        // split filter
        val filterCollection = splitFilter(filter)

        // gather properties
        return compileObjectData(context, document, filterCollection, iaa, iacl, userReadOnly, objectInfos)
    }

    /**
     * CMIS getAllowableActions.
     */
    fun getAllowableActions(context: CallContext, objectId: String): AllowableActions {
        debug("getAllowableActions")
        val userReadOnly = checkUser(context, false)
        val document = getDocument(context.username, objectId.fromBase64Id())
        return compileAllowableActions(document, userReadOnly)
    }

    /**
     * CMIS getACL.
     */
    fun getAcl(context: CallContext, objectId: String): Acl {
        debug("getAcl")
        checkUser(context, false)
        val document = getDocument(context.username, objectId.fromBase64Id())
        return compileAcl(document)
    }

    /**
     * CMIS getContentStream.
     */
    fun getContentStream(context: CallContext, objectId: String, offset: BigInteger?, length: BigInteger?): ContentStream {
        debug("getContentStream")
        checkUser(context, false)

        // get the file
        val document = getDocument(context.username, objectId.fromBase64Id())
        if (document.isFolder) {
            throw CmisStreamNotSupportedException("Not a file!")
        }

        if (document.size == 0L) {
            throw CmisConstraintException("Document has no content!")
        }


        // compile data
        val result: ContentStreamImpl
        if (offset != null && offset.toLong() > 0 || length != null) {
            result = PartialContentStreamImpl()
        } else {
            result = ContentStreamImpl()
        }

        result.fileName = document.getName()
        result.setLength(BigInteger.valueOf(document.size))
        result.mimeType = document.getMimeType()
        result.stream = getDocumentContent(context.username, objectId.fromBase64Id())

        return result
    }

    /**
     * CMIS getChildren.
     */
    fun getChildren(context: CallContext, folderId: String, filter: String, orderBy: String?,
                    includeAllowableActions: Boolean?, includePathSegment: Boolean?, maxItems: BigInteger?, skipCount: BigInteger?,
                    objectInfos: ObjectInfoHandler): ObjectInFolderList {
        debug("getChildren")
        val userReadOnly = checkUser(context, false)

        // split filter
        val filterCollection = splitFilter(filter)

        // set defaults if values not set
        val iaa = getBooleanParameter(includeAllowableActions, false)
        val ips = getBooleanParameter(includePathSegment, false)

        // skip and max
        var skip = skipCount?.toInt() ?: 0
        if (skip < 0) {
            skip = 0
        }

        var max = maxItems?.toInt() ?: Integer.MAX_VALUE
        if (max < 0) {
            max = Integer.MAX_VALUE
        }

        // get the folder
        val folder = getDocument(context.username, folderId.fromBase64Id())
        if (!folder.isFolder) {
            throw CmisObjectNotFoundException("Not a folder!")
        }

        // get the children
        val children = ArrayList<Document>()
        for (child in getChildDocuments(context.username, folderId.fromBase64Id())) {
            children.add(child)
        }

        // very basic sorting
        if (orderBy != null) {
            var desc = false
            var queryName: String = orderBy

            val commaIdx = orderBy.indexOf(',')
            if (commaIdx > -1) {
                queryName = orderBy.substring(0, commaIdx)
            }

            queryName = queryName.trim { it <= ' ' }
            if (queryName.toLowerCase(Locale.ENGLISH).endsWith(" desc")) {
                desc = true
                queryName = queryName.substring(0, queryName.length - 5).trim { it <= ' ' }
            }

            var comparator: Comparator<Document>? = null

            if ("cmis:name" == queryName) {
                comparator = Comparator { f1, f2 ->
                    f1.getName().toLowerCase(Locale.ENGLISH)
                            .compareTo(f2.getName().toLowerCase(Locale.ENGLISH))
                }
            } else if ("cmis:creationDate" == queryName || "cmis:lastModificationDate" == queryName) {
                comparator = Comparator { f1, f2 -> f1.created.compareTo(f2.created) }
            } else if ("cmis:contentStreamLength" == queryName) {
                comparator = Comparator { f1, f2 -> java.lang.Long.compare(f1.size, f2.size) }
            } else if ("cmis:objectId" == queryName) {
                comparator = Comparator { f1, f2 ->
                    try {
                        return@Comparator f1.getId().compareTo(f2.getId())
                    } catch (e: IOException) {
                        return@Comparator 0
                    }
                }
            } else if ("cmis:baseTypeId" == queryName) {
                comparator = Comparator { f1, f2 ->
                    if (f1.isFolder == f2.isFolder) {
                        return@Comparator 0
                    }
                    if (f1.isFolder) -1 else 1
                }
            } else if ("cmis:createdBy" == queryName || "cmis:lastModifiedBy" == queryName) {
                // do nothing
            } else {
                throw CmisInvalidArgumentException("Cannot sort by $queryName.")
            }

            if (comparator != null) {
                Collections.sort(children, comparator)
                if (desc) {
                    Collections.reverse(children)
                }
            }
        }

        // set object info of the the folder
        if (context.isObjectInfoRequired) {
            compileObjectData(context, folder, null, false, false, userReadOnly, objectInfos)
        }

        // prepare result
        val result = ObjectInFolderListImpl()
        result.objects = ArrayList()
        result.setHasMoreItems(false)
        var count = 0

        // iterate through children
        for (child in children) {
            count++

            if (skip > 0) {
                skip--
                continue
            }

            if (result.objects.size >= max) {
                result.setHasMoreItems(true)
                continue
            }

            // build and add child object
            val objectInFolder = ObjectInFolderDataImpl()
            objectInFolder.setObject(compileObjectData(context, child, filterCollection, iaa, false, userReadOnly,
                    objectInfos))
            if (ips) {
                objectInFolder.pathSegment = child.getName()
            }

            result.objects.add(objectInFolder)
        }

        result.numItems = BigInteger.valueOf(count.toLong())

        return result
    }

    /**
     * CMIS getDescendants.
     */
    fun getDescendants(context: CallContext, folderId: String, depth: BigInteger?,
                       filter: String, includeAllowableActions: Boolean?, includePathSegment: Boolean?, objectInfos: ObjectInfoHandler,
                       foldersOnly: Boolean): List<ObjectInFolderContainer> {
        debug("getDescendants or getFolderTree")
        val userReadOnly = checkUser(context, false)

        // check depth
        var d = depth?.toInt() ?: 2
        if (d == 0) {
            throw CmisInvalidArgumentException("Depth must not be 0!")
        }
        if (d < -1) {
            d = -1
        }

        // split filter
        val filterCollection = splitFilter(filter)

        // set defaults if values not set
        val iaa = getBooleanParameter(includeAllowableActions, false)
        val ips = getBooleanParameter(includePathSegment, false)

        // get the folder
        val folder = getDocument(context.username, folderId.fromBase64Id())
        if (!folder.isFolder) {
            throw CmisObjectNotFoundException("Not a folder!")
        }

        // set object info of the the folder
        if (context.isObjectInfoRequired) {
            compileObjectData(context, folder, null, false, false, userReadOnly, objectInfos)
        }

        // get the tree
        val result = ArrayList<ObjectInFolderContainer>()
        gatherDescendants(context, folder.getAbsolutPath(), result, foldersOnly, d, filterCollection, iaa, ips, userReadOnly,
                objectInfos)

        return result
    }

    /**
     * Gather the children of a folder.
     */
    private fun gatherDescendants(context: CallContext, folder: String, list: MutableList<ObjectInFolderContainer>,
                                  foldersOnly: Boolean, depth: Int, filter: Set<String>?, includeAllowableActions: Boolean,
                                  includePathSegments: Boolean, userReadOnly: Boolean, objectInfos: ObjectInfoHandler) {

        // iterate through children
        for (child in getChildDocuments(context.username, folder)) {

            // folders only?
            if (foldersOnly && !child.isFolder) {
                continue
            }

            // add to list
            val objectInFolder = ObjectInFolderDataImpl()
            objectInFolder.setObject(compileObjectData(context, child, filter, includeAllowableActions, false,
                    userReadOnly, objectInfos))
            if (includePathSegments) {
                objectInFolder.pathSegment = child.getName()
            }

            val container = ObjectInFolderContainerImpl()
            container.setObject(objectInFolder)

            list.add(container)

            // move to next level
            if (depth != 1 && child.isFolder) {
                container.children = ArrayList()
                gatherDescendants(context, child.getAbsolutPath(), container.children, foldersOnly, depth - 1, filter,
                        includeAllowableActions, includePathSegments, userReadOnly, objectInfos)
            }
        }
    }

    /**
     * CMIS getFolderParent.
     */
    fun getFolderParent(context: CallContext,
                        folderId: String,
                        filter: String,
                        objectInfos: ObjectInfoHandler): ObjectData {
        val parents = getObjectParents(context, folderId, filter, false, false, objectInfos)

        if (parents.isEmpty()) {
            throw CmisInvalidArgumentException("The root folder has no parent!")
        }

        return parents[0].getObject()
    }

    /**
     * CMIS getObjectParents.
     */
    fun getObjectParents(context: CallContext,
                         objectId: String,
                         filter: String,
                         includeAllowableActions: Boolean?,
                         includeRelativePathSegment: Boolean?,
                         objectInfos: ObjectInfoHandler): List<ObjectParentData> {
        debug("getObjectParents")
        val userReadOnly = checkUser(context, false)

        // split filter
        val filterCollection = splitFilter(filter)

        // set defaults if values not set
        val iaa = includeAllowableActions ?: false
        val irps = includeRelativePathSegment ?: false
        // don't climb above the root folder
        if (objectId == ROOT_PATH.toBase64Id()) {
            return emptyList()
        }
        // get the file or folder
        val document = getDocument(context.username, objectId.fromBase64Id())
        // set object info of the the object
        if (context.isObjectInfoRequired) {
            compileObjectData(context, document, null, false, false, userReadOnly, objectInfos)
        }

        // get parent folder
        val parent = document.getParent(context.username)
        val `object` = compileObjectData(context, parent, filterCollection, iaa, false, userReadOnly, objectInfos)

        val result = ObjectParentDataImpl()
        result.setObject(`object`)
        if (irps) {
            result.relativePathSegment = document.getName()
        }

        return listOf<ObjectParentData>(result)
    }

    /**
     * CMIS getObjectByPath.
     */
    fun getObjectByPath(context: CallContext, folderPath: String?, filter: String,
                        includeAllowableActions: Boolean, includeACL: Boolean, objectInfos: ObjectInfoHandler?): ObjectData {
        debug("getObjectByPath")
        val userReadOnly = checkUser(context, false)

        // split filter
        val filterCollection = splitFilter(filter)

        // check path
        if (folderPath == null || folderPath.isEmpty() || folderPath[0] != '/') {
            throw CmisInvalidArgumentException("Invalid folder path!")
        }


        val document = if (folderPath.length == 1) {
            ROOT_DOCUMENT
        } else {
            val path = folderPath.replace('/', File.separatorChar)
            getDocument(context.username, path)
        }

        return compileObjectData(
                context,
                document,
                filterCollection,
                includeAllowableActions,
                includeACL,
                userReadOnly,
                objectInfos)
    }

    // --- helpers ---


    private fun compileObjectData(context: CallContext,
                                  document: Document,
                                  filter: Set<String>?,
                                  includeAllowableActions: Boolean,
                                  includeAcl: Boolean,
                                  userReadOnly: Boolean,
                                  objectInfos: ObjectInfoHandler?): ObjectData {
        val result = ObjectDataImpl()
        val objectInfo = ObjectInfoImpl()

        result.properties = compileProperties(context, document, filter, objectInfo)

        if (includeAllowableActions) {
            result.allowableActions = compileAllowableActions(document, userReadOnly)
        }

        if (includeAcl) {
            result.acl = compileAcl(document)
            result.setIsExactAcl(true)
        }

        if (context.isObjectInfoRequired) {
            objectInfo.setObject(result)
            objectInfos!!.addObjectInfo(objectInfo)
        }

        return result
    }


    private fun compileProperties(context: CallContext, document: Document?, orgfilter: Set<String>?,
                                  objectInfo: ObjectInfoImpl): Properties {
        if (document == null) {
            throw IllegalArgumentException("File must not be null!")
        }


        // copy filter
        val filter = if (orgfilter == null) null else HashSet(orgfilter)

        // find base type
        var typeId: String? = null

        if (document.isFolder) {
            typeId = BaseTypeId.CMIS_FOLDER.value()
            objectInfo.baseType = BaseTypeId.CMIS_FOLDER
            objectInfo.typeId = typeId
            objectInfo.contentType = null
            objectInfo.fileName = null
            objectInfo.setHasAcl(true)
            objectInfo.setHasContent(false)
            objectInfo.versionSeriesId = null
            objectInfo.setIsCurrentVersion(true)
            objectInfo.relationshipSourceIds = null
            objectInfo.relationshipTargetIds = null
            objectInfo.renditionInfos = null
            objectInfo.setSupportsDescendants(true)
            objectInfo.setSupportsFolderTree(true)
            objectInfo.setSupportsPolicies(false)
            objectInfo.setSupportsRelationships(false)
            objectInfo.workingCopyId = null
            objectInfo.workingCopyOriginalId = null
        } else {
            typeId = BaseTypeId.CMIS_DOCUMENT.value()
            objectInfo.baseType = BaseTypeId.CMIS_DOCUMENT
            objectInfo.typeId = typeId
            objectInfo.setHasAcl(true)
            objectInfo.setHasContent(true)
            objectInfo.setHasParent(true)
            objectInfo.versionSeriesId = null
            objectInfo.setIsCurrentVersion(true)
            objectInfo.relationshipSourceIds = null
            objectInfo.relationshipTargetIds = null
            objectInfo.renditionInfos = null
            objectInfo.setSupportsDescendants(false)
            objectInfo.setSupportsFolderTree(false)
            objectInfo.setSupportsPolicies(false)
            objectInfo.setSupportsRelationships(false)
            objectInfo.workingCopyId = null
            objectInfo.workingCopyOriginalId = null
        }

        // let's do it
        try {
            val result = PropertiesImpl()

            // id
            val id = document.getId()
            addPropertyId(result, typeId, filter, PropertyIds.OBJECT_ID, id)
            objectInfo.id = id

            // name
            val name = document.getName()
            addPropertyString(result, typeId, filter, PropertyIds.NAME, name)
            objectInfo.name = name

            // created and modified by
            addPropertyString(result, typeId, filter, PropertyIds.CREATED_BY, USER_UNKNOWN)
            addPropertyString(result, typeId, filter, PropertyIds.LAST_MODIFIED_BY, USER_UNKNOWN)
            objectInfo.createdBy = USER_UNKNOWN

            // creation and modification date
            val lastModified = millisToCalendar(document.created.toEpochSecond(ZoneOffset.UTC))
            addPropertyDateTime(result, typeId, filter, PropertyIds.CREATION_DATE, lastModified)
            addPropertyDateTime(result, typeId, filter, PropertyIds.LAST_MODIFICATION_DATE, lastModified)
            objectInfo.creationDate = lastModified
            objectInfo.lastModificationDate = lastModified

            // change token
            addPropertyString(result, typeId, filter, PropertyIds.CHANGE_TOKEN, document.version)

            // CMIS 1.1 properties
            if (context.cmisVersion != CmisVersion.CMIS_1_0) {
                addPropertyString(result, typeId, filter, PropertyIds.DESCRIPTION, null)
                addPropertyIdList(result, typeId, filter, PropertyIds.SECONDARY_OBJECT_TYPE_IDS, null)
            }

            // directory or file
            if (document.isFolder) {
                // base type and type name
                addPropertyId(result, typeId, filter, PropertyIds.BASE_TYPE_ID, BaseTypeId.CMIS_FOLDER.value())
                addPropertyId(result, typeId, filter, PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_FOLDER.value())
                val path = document.getAbsolutPath()
                addPropertyString(result, typeId, filter, PropertyIds.PATH, path)

                // folder properties
                if (document.bucket != ROOT_ID) {
                    addPropertyId(result, typeId, filter, PropertyIds.PARENT_ID, document.getParentId())
                    objectInfo.setHasParent(true)
                } else {
                    addPropertyId(result, typeId, filter, PropertyIds.PARENT_ID, null)
                    objectInfo.setHasParent(false)
                }

                addPropertyIdList(result, typeId, filter, PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, null)
            } else {
                // base type and type name
                addPropertyId(result, typeId, filter, PropertyIds.BASE_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value())
                addPropertyId(result, typeId, filter, PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value())

                // file properties
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_IMMUTABLE, false)
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_LATEST_VERSION, true)
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_MAJOR_VERSION, true)
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_LATEST_MAJOR_VERSION, true)
                addPropertyString(result, typeId, filter, PropertyIds.VERSION_LABEL, document.version)
                addPropertyId(result, typeId, filter, PropertyIds.VERSION_SERIES_ID, document.getId())
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, false)
                addPropertyString(result, typeId, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, null)
                addPropertyString(result, typeId, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, null)
                addPropertyString(result, typeId, filter, PropertyIds.CHECKIN_COMMENT, "")
                if (context.cmisVersion != CmisVersion.CMIS_1_0) {
                    addPropertyBoolean(result, typeId, filter, PropertyIds.IS_PRIVATE_WORKING_COPY, false)
                }

                if (document.size == 0L) {
                    addPropertyBigInteger(result, typeId, filter, PropertyIds.CONTENT_STREAM_LENGTH, null)
                    addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_MIME_TYPE, null)
                    addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_FILE_NAME, null)

                    objectInfo.setHasContent(false)
                    objectInfo.contentType = null
                    objectInfo.fileName = null
                } else {
                    val mime = document.getMimeType()
                    addPropertyInteger(result, typeId, filter, PropertyIds.CONTENT_STREAM_LENGTH, document.size)
                    addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_MIME_TYPE, mime)
                    addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_FILE_NAME, document.getName())

                    objectInfo.setHasContent(true)
                    objectInfo.contentType = mime
                    objectInfo.fileName = document.getName()
                }

                addPropertyId(result, typeId, filter, PropertyIds.CONTENT_STREAM_ID, null)
            }

            // read custom properties
            readCustomProperties(document, result, filter)

            if (filter != null) {
                if (!filter.isEmpty()) {
                    debug("Unknown filter properties: $filter")
                }
            }

            return result
        } catch (cbe: CmisBaseException) {
            throw cbe
        } catch (e: Exception) {
            throw CmisRuntimeException(e.message, e)
        }

    }

    private fun Document.getMimeType(): String {
        return try {
            MimeTypes.getMIMEType(getName().substringAfterLast("."))
        } catch (e: Exception) {
            "application/octet-stream"
        }
    }

    /**
     * Reads and adds properties.
     */
    private fun readCustomProperties(document: Document, properties: PropertiesImpl, filter: MutableSet<String>?) {

        // add it to properties
        for (prop in document.userMetadata) {
            // overwrite object info


            // check filter
            if (filter != null) {
                if (!filter.contains(prop.key)) {
                    continue
                } else {
                    filter.remove(prop.key)
                }
            }

            // don't overwrite id
            if (PropertyIds.OBJECT_ID == prop.key) {
                continue
            }

            // don't overwrite base type
            if (PropertyIds.BASE_TYPE_ID == prop.key) {
                continue
            }
            // add it
            properties.replaceProperty(PropertyStringImpl(prop.key, prop.value))
        }
    }

    /**
     * Checks and compiles a property set that can be written to disc.
     */
    private fun compileWriteProperties(typeId: String, creator: String, modifier: String, properties: Properties?): PropertiesImpl {
        val result = PropertiesImpl()
        val addedProps = HashSet<String>()

        if (properties == null || properties.properties == null) {
            throw CmisConstraintException("No properties!")
        }

        // get the property definitions
        val type = typeManager.getInternalTypeDefinition(typeId)
                ?: throw CmisObjectNotFoundException("Type '$typeId' is unknown!")

        // check if all required properties are there
        for (prop in properties.properties.values) {
            val propType = type.propertyDefinitions[prop.id]
                    ?: throw CmisConstraintException("Property '" + prop.id + "' is unknown!")

            // do we know that property?

            // can it be set?
            if (propType.updatability == Updatability.READONLY) {
                throw CmisConstraintException("Property '" + prop.id + "' is readonly!")
            }

            // empty properties are invalid
            // TODO: check
            // if (isEmptyProperty(prop)) {
            // throw new CmisConstraintException("Property '" + prop.getId() +
            // "' must not be empty!");
            // }

            // add it
            result.addProperty(prop)
            addedProps.add(prop.id)
        }

        // check if required properties are missing
        for (propDef in type.propertyDefinitions.values) {
            if (!addedProps.contains(propDef.id) && propDef.updatability != Updatability.READONLY) {
                if (!addPropertyDefault(result, propDef) && propDef.isRequired!!) {
                    throw CmisConstraintException("Property '" + propDef.id + "' is required!")
                }
            }
        }

        addPropertyId(result, typeId, null, PropertyIds.OBJECT_TYPE_ID, typeId)
        addPropertyString(result, typeId, null, PropertyIds.CREATED_BY, creator)
        addPropertyString(result, typeId, null, PropertyIds.LAST_MODIFIED_BY, modifier)

        return result
    }

//    /**
//     * Writes the properties for a document or folder.
//     */
//    private fun writePropertiesFile(file: File, properties: Properties?) {
//        val propFile = getPropertiesFile(file)
//
//        // if no properties set delete the properties file
//        if (properties == null || properties.properties == null || properties.properties.size == 0) {
//            propFile.delete()
//            return
//        }
//
//        // create object
//        val `object` = ObjectDataImpl()
//        `object`.properties = properties
//
//        var stream: OutputStream? = null
//        try {
//            stream = BufferedOutputStream(FileOutputStream(propFile))
//            val writer = XMLUtils.createWriter(stream)
//            XMLUtils.startXmlDocument(writer)
//            XMLConverter.writeObject(writer, CmisVersion.CMIS_1_1, true, "object", XMLConstants.NAMESPACE_CMIS, `object`)
//            XMLUtils.endXmlDocument(writer)
//            writer.close()
//        } catch (e: Exception) {
//            throw CmisStorageException("Couldn't store properties!", e)
//        } finally {
//            IOUtils.closeQuietly(stream)
//        }
//    }

    private fun isEmptyProperty(prop: PropertyData<*>?): Boolean {
        return if (prop == null || prop.values == null) {
            true
        } else prop.values.isEmpty()

    }

    private fun addPropertyId(props: PropertiesImpl, typeId: String?, filter: MutableSet<String>?, id: String, value: String?) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return
        }

        props.addProperty(PropertyIdImpl(id, value))
    }

    private fun addPropertyIdList(props: PropertiesImpl, typeId: String?, filter: MutableSet<String>?, id: String,
                                  value: List<String>?) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return
        }

        props.addProperty(PropertyIdImpl(id, value))
    }

    private fun addPropertyString(props: PropertiesImpl, typeId: String?, filter: MutableSet<String>?, id: String, value: String?) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return
        }

        props.addProperty(PropertyStringImpl(id, value))
    }

    private fun addPropertyInteger(props: PropertiesImpl, typeId: String?, filter: MutableSet<String>?, id: String, value: Long) {
        addPropertyBigInteger(props, typeId, filter, id, BigInteger.valueOf(value))
    }

    private fun addPropertyBigInteger(props: PropertiesImpl, typeId: String?, filter: MutableSet<String>?, id: String,
                                      value: BigInteger?) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return
        }

        props.addProperty(PropertyIntegerImpl(id, value))
    }

    private fun addPropertyBoolean(props: PropertiesImpl, typeId: String?, filter: MutableSet<String>?, id: String, value: Boolean) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return
        }

        props.addProperty(PropertyBooleanImpl(id, value))
    }

    private fun addPropertyDateTime(props: PropertiesImpl, typeId: String?, filter: MutableSet<String>?, id: String,
                                    value: GregorianCalendar) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return
        }

        props.addProperty(PropertyDateTimeImpl(id, value))
    }

    private fun checkAddProperty(properties: Properties?, typeId: String?, filter: MutableSet<String>?, id: String?): Boolean {
        if (properties == null || properties.properties == null) {
            throw IllegalArgumentException("Properties must not be null!")
        }

        if (id == null) {
            throw IllegalArgumentException("Id must not be null!")
        }

        val type = typeManager.getInternalTypeDefinition(typeId!!)
                ?: throw IllegalArgumentException("Unknown type: $typeId")
        if (!type.propertyDefinitions.containsKey(id)) {
            throw IllegalArgumentException("Unknown property: $id")
        }

        val queryName = type.propertyDefinitions[id]?.getQueryName()

        if (queryName != null && filter != null) {
            if (!filter.contains(queryName)) {
                return false
            } else {
                filter.remove(queryName)
            }
        }

        return true
    }

    /**
     * Adds the default value of property if defined.
     */
    private fun addPropertyDefault(props: PropertiesImpl?, propDef: PropertyDefinition<*>?): Boolean {
        if (props == null || props.properties == null) {
            throw IllegalArgumentException("Props must not be null!")
        }

        if (propDef == null) {
            return false
        }

        val defaultValue = propDef.defaultValue
        if (defaultValue != null && !defaultValue.isEmpty()) {
            when (propDef.propertyType) {
                PropertyType.BOOLEAN -> props.addProperty(PropertyBooleanImpl(propDef.id, defaultValue as List<Boolean>))
                PropertyType.DATETIME -> props.addProperty(PropertyDateTimeImpl(propDef.id, defaultValue as List<GregorianCalendar>))
                PropertyType.DECIMAL -> props.addProperty(PropertyDecimalImpl(propDef.id, defaultValue as List<BigDecimal>))
                PropertyType.HTML -> props.addProperty(PropertyHtmlImpl(propDef.id, defaultValue as List<String>))
                PropertyType.ID -> props.addProperty(PropertyIdImpl(propDef.id, defaultValue as List<String>))
                PropertyType.INTEGER -> props.addProperty(PropertyIntegerImpl(propDef.id, defaultValue as List<BigInteger>))
                PropertyType.STRING -> props.addProperty(PropertyStringImpl(propDef.id, defaultValue as List<String>))
                PropertyType.URI -> props.addProperty(PropertyUriImpl(propDef.id, defaultValue as List<String>))
                else -> assert(false)
            }

            return true
        }

        return false
    }

    /**
     * Compiles the allowable actions for a file or folder.
     */
    private fun compileAllowableActions(document: Document?, userReadOnly: Boolean): AllowableActions {
        if (document == null) {
            throw IllegalArgumentException("File must not be null!")
        }


        val isReadOnly = userReadOnly
        val isFolder = document.isFolder
        val isRoot = document.bucket == ROOT_ID

        val aas = EnumSet.noneOf(Action::class.java)

        addAction(aas, Action.CAN_GET_OBJECT_PARENTS, !isRoot)
        addAction(aas, Action.CAN_GET_PROPERTIES, true)
        addAction(aas, Action.CAN_UPDATE_PROPERTIES, !userReadOnly && !isReadOnly)
        addAction(aas, Action.CAN_MOVE_OBJECT, !userReadOnly && !isRoot)
        addAction(aas, Action.CAN_DELETE_OBJECT, !userReadOnly && !isReadOnly && !isRoot)
        addAction(aas, Action.CAN_GET_ACL, true)

        if (isFolder) {
            addAction(aas, Action.CAN_GET_DESCENDANTS, true)
            addAction(aas, Action.CAN_GET_CHILDREN, true)
            addAction(aas, Action.CAN_GET_FOLDER_PARENT, !isRoot)
            addAction(aas, Action.CAN_GET_FOLDER_TREE, true)
            addAction(aas, Action.CAN_CREATE_DOCUMENT, !userReadOnly)
            addAction(aas, Action.CAN_CREATE_FOLDER, !userReadOnly)
            addAction(aas, Action.CAN_DELETE_TREE, !userReadOnly && !isReadOnly)
        } else {
            addAction(aas, Action.CAN_GET_CONTENT_STREAM, document.size > 0)
            addAction(aas, Action.CAN_SET_CONTENT_STREAM, !userReadOnly && !isReadOnly)
            addAction(aas, Action.CAN_DELETE_CONTENT_STREAM, !userReadOnly && !isReadOnly)
            addAction(aas, Action.CAN_GET_ALL_VERSIONS, true)
        }

        val result = AllowableActionsImpl()
        result.allowableActions = aas

        return result
    }

    private fun addAction(aas: MutableSet<Action>, action: Action, condition: Boolean) {
        if (condition) {
            aas.add(action)
        }
    }

    /**
     * Compiles the ACL for a file or folder.
     */
    private fun compileAcl(document: Document): Acl {
        val result = AccessControlListImpl()
        result.aces = ArrayList()

        for ((key, value) in readWriteUserMap) {
            // create principal
            val principal = AccessControlPrincipalDataImpl(key)

            // create ACE
            val entry = AccessControlEntryImpl()
            entry.principal = principal
            entry.permissions = ArrayList()
            entry.permissions.add(BasicPermissions.READ)
            if (!value) {
                entry.permissions.add(BasicPermissions.WRITE)
                entry.permissions.add(BasicPermissions.ALL)
            }

            entry.isDirect = true

            // add ACE
            result.aces.add(entry)
        }

        return result
    }


    private fun isValidName(name: String?): Boolean {
        return name != null
                && name.isNotEmpty()
                && File.pathSeparator !in name
                && File.separator !in name


    }

    /**
     * Checks if a folder is empty. A folder is considered as empty if no files
     * or only the shadow file reside in the folder.
     *
     * @param folder
     * the folder
     *
     * @return `true` if the folder is empty.
     */
    private fun isFolderEmpty(folder: File): Boolean {
        return !folder.isDirectory
                || folder.list().orEmpty().isEmpty()

    }

    /**
     * Checks if the user in the given context is valid for this repository and
     * if the user has the required permissions.
     */
    private fun checkUser(context: CallContext?, writeRequired: Boolean): Boolean {
        if (context == null) {
            throw CmisPermissionDeniedException("No user context!")
        }
        val user = try {
            userManager.value.loadUserByUsername(context.username)
        } catch (e: Exception) {
            throw CmisPermissionDeniedException("Unknown user!",e)
        }
        val roles = user.roles
        val readOnly = !roles.contains("write")
        return true
        if (readOnly && writeRequired) {
            throw CmisPermissionDeniedException("No write permission!")
        }
        return readOnly
    }

//    /**
//     * Returns the properties file of the given file.
//     */
//    private fun getPropertiesFile(file: File): File {
//        return if (file.isDirectory) {
//            File(file, SHADOW_FOLDER)
//        } else File(file.absolutePath + SHADOW_EXT)
//
//    }

//    /**
//     * Returns the File object by id or throws an appropriate exception.
//     */
//    private fun getFile(id: String?): File {
//        try {
//            return idToFile(id)
//        } catch (e: Exception) {
//            throw CmisObjectNotFoundException(e.message, e)
//        }
//
//    }

//    /**
//     * Converts an id to a File object. A simple and insecure implementation,
//     * but good enough for now.
//     */
//    @Throws(IOException::class)
//    private fun idToFile(id: String?): File {
//        if (id == null || id.length == 0) {
//            throw CmisInvalidArgumentException("Id is not valid!")
//        }
//
//        return if (id == ROOT_ID) {
//            rootDirectory
//        } else File(rootDirectory, String(Base64.decode(id.toByteArray(charset("US-ASCII"))), Charsets.UTF_8).replace('/',
//                File.separatorChar))
//
//    }


    private fun documentExists(userId: String, file: String): Boolean {
        return try {
            getDocument(userId, file)
            true
        } catch (e: DocumentNotFound) {
            false
        }
    }

    private fun putDocumentContent(userId: String, file: String, content: ContentStream, props: PropertiesImpl): String {
        TODO()
    }

    private fun putDocument(userId: String, file: String, props: PropertiesImpl): String {
        TODO()
    }

    private fun copyDocument(username: String, sourceFile: String, targetFile: String, newProperties: PropertiesImpl): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun getDocument(userId: String, file: String): Document {
        if (file.isRootId()) return Document(ROOT_ID, ROOT_ID, "1", true, 0, "", LocalDateTime.MIN, "", mapOf(), true)
        val (bucket, key) = splitFileToBucketKey(file)
        if(bucket == ROOT_ID)return Document(key, "", "1", true, 0, "", LocalDateTime.MIN, "", mapOf(), true)
        try {
            return ladonRepo.value.getDocument(userId, bucket, key).meta
        } catch (e: DocumentNotFound) {
            throw CmisObjectNotFoundException("Object not found!")
        }
    }


    private fun getDocumentContent(userId: String, file: String): InputStream {
        val (bucket, key) = splitFileToBucketKey(file)
        return ladonRepo.value.getDocument(userId, bucket, key).content
    }

    private fun getChildDocuments(userId: String, file: String): List<Document> {
        return if (file.isRootId()) {
            ladonRepo.value.listBuckets(userId)
                    .map { Document(ROOT_ID, it, "1", true, 0, "", LocalDateTime.MIN, "", mapOf(), true) }
        } else {
            val (bucket, key) = splitFileToBucketKey(file)
            ladonRepo.value.listDocuments(userId, bucket, key, null, "/", null)
        }
    }

    private fun Document.getParent(username: String) = getDocument(username, getParentPath())


    private fun splitFileToBucketKey(file: String) =
            if (file.isRootId()) ROOT_ID to ""
            else
                file.substring(1).let { it.substringBefore("/") to it.substringAfter("/") }

    private fun String.isRootId() = this == "/" || this == ROOT_ID || this.replace("/", "") == ROOT_ID

//    /**
//     * Returns the id of a File object or throws an appropriate exception.
//     */
//    private fun getId(file: File): String {
//        try {
//            return fileToId(file)
//        } catch (e: Exception) {
//            throw CmisRuntimeException(e.message, e)
//        }
//
//    }

//    /**
//     * Creates a File object from an id. A simple and insecure implementation,
//     * but good enough for now.
//     */
//    @Throws(IOException::class)
//    private fun fileToId(file: File?): String {
//        if (file == null) {
//            throw IllegalArgumentException("File is not valid!")
//        }
//
//        if (rootDirectory == file) {
//            return ROOT_ID
//        }
//
//        val path = getRepositoryPath(file)
//
//        return Base64.encodeBytes(path.toByteArray(charset("UTF-8")))
//    }
//
//    private fun getRepositoryPath(file: File): String {
//        var path = file.absolutePath.substring(rootDirectory.absolutePath.length)
//                .replace(File.separatorChar, '/')
//        if (path.length == 0) {
//            path = "/"
//        } else if (path[0] != '/') {
//            path = "/$path"
//        }
//        return path
//    }

    private fun debug(msg: String) {
        if (LOG.isDebugEnabled) {
            LOG.debug("<{}> {}", repositoryId, msg)
        }
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(LadonCmisRepository::class.java)

        val ROOT_DOCUMENT = Document(ROOT_ID, ROOT_ID, "", true, 0, "", LocalDateTime.MIN, "", mapOf(), true)
//        private val SHADOW_EXT = ".cmis.xml"
//        private val SHADOW_FOLDER = "cmis.xml"

        private val USER_UNKNOWN = "<unknown>"

        private val BUFFER_SIZE = 64 * 1024
    }
}

private fun Document.getId() = getAbsolutPath().toBase64Id()
private fun Document.getParentId() = if (key.isEmpty()) ROOT_ID else getParentPath().toBase64Id()
private fun Document.getName() = if(this.isRoot()) ROOT_ID else  key.split("/").last()
private fun Document.getAbsolutPath() = if(this.isRoot()) ROOT_PATH else "/${bucket}/${key}"

private fun Document.isRoot(): Boolean {
    return bucket == ROOT_ID && key == ROOT_ID
}

private fun Document.getParentPath() = getAbsolutPath().substringBeforeLast("/")

val ROOT_ID = "#root"
val ROOT_PATH = "/"
fun String.fromBase64Id() = if (this == ROOT_ID) ROOT_ID else String(Base64.decode(this.toByteArray(charset("UTF-8"))), Charsets.UTF_8)
//.replace('/', File.separatorChar)

fun String.toBase64Id() = Base64.encodeBytes(this.toByteArray(charset("UTF-8")))
