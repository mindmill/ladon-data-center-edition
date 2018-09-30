/*
 * Copyright (c) 2015 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.config


/**
 * A collection for the keys used in the <code>application.properties</code> file
 */
class BoxConfig private constructor(private val parameters: Map<String, String>) {

    private fun getStringIntern(key: String): String? {
        return parameters.get(key)

    }


    companion object {
        val LADON_MAX_CONTENT_SIZE_MB: String = "ladon.max.content.size.mb"

        val ROOT_NAME: String = "/"
        val PATH_SEPARATOR: String = "/"

        val SYSTEM_REPO: String = "_system"

        val SYSTEM_PATH_REPOS: String = "repositories"
        val SYSTEM_PATH_TYPES: String = "typedefinitions"
        val SYSTEM_PATH_POLICIES: String = "policies"

        val ANONYMOUS_USER = "anonymous"
        val ANYONE_USER = "anyone"
        val UNKNOWN_USER = "unknown"
        val ROLE_PREFIX = "ROLE_"
        val ROLE_USER = "ROLE_user"
        val PRINCIPAL_KEY = "principal"


        private var singleInstance: BoxConfig? = null

        fun getString(key: String): String? {
            return singleInstance!!.getStringIntern(key)
        }


        fun init(parameters: Map<String, String>) {
            singleInstance = BoxConfig(parameters)
        }

        fun getParameters(): Map<String, String> {
            return singleInstance!!.parameters
        }
    }


}
