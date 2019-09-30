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

import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException
import org.apache.chemistry.opencmis.commons.server.CallContext
import java.util.*

/**
 * Manages users for the FileShare repository.
 */
class LadonCmisUserManager {

    private val logins: MutableMap<String, String>

    init {
        logins = HashMap()
    }

    @Synchronized
    fun getLogins(): Collection<String> {
        return logins.keys
    }


    @Synchronized
    fun addLogin(username: String?, password: String?) {
        if (username == null || password == null) {
            return
        }

        logins[username.trim { it <= ' ' }] = password
    }


    @Synchronized
    fun authenticate(context: CallContext): String {
        // try to get the remote user first
        // HttpServletRequest request = (HttpServletRequest)
        // context.get(CallContext.HTTP_SERVLET_REQUEST);
        // if (request != null && request.getRemoteUser() != null) {
        // return request.getRemoteUser();
        // }

        // check user and password
        if (!authenticate(context.username, context.password)) {
            throw CmisPermissionDeniedException("Invalid username or password.")
        }

        return context.username
    }


    @Synchronized
    private fun authenticate(username: String, password: String): Boolean {
        val pwd = logins[username] ?: return false

        return pwd == password
    }

    override fun toString(): String {
        val sb = StringBuilder(128)

        for (user in logins.keys) {
            sb.append('[')
            sb.append(user)
            sb.append(']')
        }

        return sb.toString()
    }
}
