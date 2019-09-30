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

import org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory
import org.apache.chemistry.opencmis.commons.server.CallContext
import org.apache.chemistry.opencmis.commons.server.CmisService
import org.apache.chemistry.opencmis.server.support.wrapper.CallContextAwareCmisService
import org.apache.chemistry.opencmis.server.support.wrapper.CmisServiceWrapperManager
import org.apache.chemistry.opencmis.server.support.wrapper.ConformanceCmisServiceWrapper
import org.slf4j.LoggerFactory
import java.math.BigInteger
import java.util.*

/**
 * FileShare Service Factory.
 */
class LadonCmisServiceFactory : AbstractServiceFactory() {

    /** Each thread gets its own [LadonCmisService] instance.  */
    private var threadLocalService: ThreadLocal<CallContextAwareCmisService>? = ThreadLocal()

    var repositoryManager: LadonCmisRepositoryManager? = null
        private set
    var userManager: LadonCmisUserManager? = null
        private set
    var typeManager: LadonCmisTypeManager? = null
        private set
    private var wrapperManager: CmisServiceWrapperManager? = null

    override fun init(parameters: Map<String, String>?) {
        repositoryManager = LadonCmisRepositoryManager()
        userManager = LadonCmisUserManager()
        typeManager = LadonCmisTypeManager()

        wrapperManager = CmisServiceWrapperManager()
        wrapperManager!!.addWrappersFromServiceFactoryParameters(parameters)
        wrapperManager!!.addOuterWrapper(ConformanceCmisServiceWrapper::class.java, DEFAULT_MAX_ITEMS_TYPES,
                DEFAULT_DEPTH_TYPES, DEFAULT_MAX_ITEMS_OBJECTS, DEFAULT_DEPTH_OBJECTS)

        readConfiguration(parameters!!)
    }

    override fun destroy() {
        threadLocalService = null
    }

    override fun getService(context: CallContext): CmisService {
        // authenticate the user
        // if the authentication fails, authenticate() throws a
        // CmisPermissionDeniedException
        userManager!!.authenticate(context)

        // get service object for this thread
        var service: CallContextAwareCmisService? = threadLocalService!!.get()
        if (service == null) {
            // there is no service object for this thread -> create one
            val fileShareService = LadonCmisService(repositoryManager!!)

            service = wrapperManager!!.wrap(fileShareService) as CallContextAwareCmisService

            threadLocalService!!.set(service)
        }

        // hand over the call context to the service object
        service.callContext = context

        return service
    }

    // ---- helpers ----

    /**
     * Reads the configuration and sets up the repositories, logins, and type
     * definitions.
     */
    private fun readConfiguration(parameters: Map<String, String>) {
        val keys = ArrayList(parameters.keys)
        Collections.sort(keys)

        for (key in keys) {
            if (key.startsWith(PREFIX_LOGIN)) {
                // get logins
                val usernameAndPassword = replaceSystemProperties(parameters[key]) ?: continue

                var username: String = usernameAndPassword
                var password = ""

                val x = usernameAndPassword.indexOf(':')
                if (x > -1) {
                    username = usernameAndPassword.substring(0, x)
                    password = usernameAndPassword.substring(x + 1)
                }

                LOG.info("Adding login '{}'.", username)

                userManager!!.addLogin(username, password)
            } else if (key.startsWith(PREFIX_TYPE)) {
                // load type definition
                val typeFile = replaceSystemProperties(parameters[key]?.trim { it <= ' ' })
                if (typeFile!!.isEmpty()) {
                    continue
                }

                LOG.info("Loading type definition: {}", typeFile)

                if (typeFile[0] == '/') {
                    try {
                        typeManager!!.loadTypeDefinitionFromResource(typeFile)
                        continue
                    } catch (e: IllegalArgumentException) {
                        // resource not found -> try it as a regular file
                    } catch (e: Exception) {
                        LOG.warn("Could not load type defintion from resource '{}': {}", typeFile, e.message, e)
                        continue
                    }

                }

                try {
                    typeManager!!.loadTypeDefinitionFromFile(typeFile)
                } catch (e: Exception) {
                    LOG.warn("Could not load type defintion from file '{}': {}", typeFile, e.message, e)
                }

            } else if (key.startsWith(PREFIX_REPOSITORY)) {
                // configure repositories
                var repositoryId = key.substring(PREFIX_REPOSITORY.length).trim { it <= ' ' }
                val x = repositoryId.lastIndexOf('.')
                if (x > 0) {
                    repositoryId = repositoryId.substring(0, x)
                }

                if (repositoryId.length == 0) {
                    throw IllegalArgumentException("No repository id!")
                }

                if (key.endsWith(SUFFIX_READWRITE)) {
                    // read-write users
                    val fsr = repositoryManager!!.getRepository(repositoryId)
                    for (user in split(parameters[key])) {
                        fsr.setUserReadWrite(replaceSystemProperties(user))
                    }
                } else if (key.endsWith(SUFFIX_READONLY)) {
                    // read-only users
                    val fsr = repositoryManager!!.getRepository(repositoryId)
                    for (user in split(parameters[key])) {
                        fsr.setUserReadOnly(replaceSystemProperties(user))
                    }
                } else {
                    // new repository
                    val root = replaceSystemProperties(parameters[key])

                    LOG.info("Adding repository '{}': {}", repositoryId, root)

                    val fsr = LadonCmisRepository(repositoryId, root, typeManager!!)
                    repositoryManager!!.addRepository(fsr)
                }
            }
        }
    }

    /**
     * Splits a string by comma.
     */
    private fun split(csl: String?): List<String> {
        if (csl == null) {
            return emptyList()
        }

        val result = ArrayList<String>()
        for (s in csl.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            result.add(s.trim { it <= ' ' })
        }

        return result
    }

    /**
     * Finds all substrings in curly braces and replaces them with the value of
     * the corresponding system property.
     */
    private fun replaceSystemProperties(s: String?): String? {
        if (s == null) {
            return null
        }

        val result = StringBuilder(128)
        var property: StringBuilder? = null
        var inProperty = false

        for (i in 0 until s.length) {
            val c = s[i]

            if (inProperty) {
                if (c == '}') {
                    val value = System.getProperty(property!!.toString())
                    if (value != null) {
                        result.append(value)
                    }
                    inProperty = false
                } else {
                    property!!.append(c)
                }
            } else {
                if (c == '{') {
                    property = StringBuilder(32)
                    inProperty = true
                } else {
                    result.append(c)
                }
            }
        }

        return result.toString()
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(LadonCmisServiceFactory::class.java)

        private val PREFIX_LOGIN = "login."
        private val PREFIX_REPOSITORY = "repository."
        private val PREFIX_TYPE = "type."
        private val SUFFIX_READWRITE = ".readwrite"
        private val SUFFIX_READONLY = ".readonly"

        /** Default maxItems value for getTypeChildren()}.  */
        private val DEFAULT_MAX_ITEMS_TYPES = BigInteger.valueOf(50)

        /** Default depth value for getTypeDescendants().  */
        private val DEFAULT_DEPTH_TYPES = BigInteger.valueOf(-1)

        /**
         * Default maxItems value for getChildren() and other methods returning
         * lists of objects.
         */
        private val DEFAULT_MAX_ITEMS_OBJECTS = BigInteger.valueOf(200)

        /** Default depth value for getDescendants().  */
        private val DEFAULT_DEPTH_OBJECTS = BigInteger.valueOf(10)
    }

}
