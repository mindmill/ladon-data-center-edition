/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.api.persistence.dao

import de.mc.ladon.server.core.api.persistence.entities.ChangeToken
import de.mc.ladon.server.core.api.request.LadonCallContext
import java.math.BigInteger

/**
 * DAO for change token database objects
 * Created by Ralf Ulrich on 07.05.16.
 */
interface ChangeTokenDAO {

    fun getAllChangesSince(cc: LadonCallContext, repoId: String, token: String, maxItems: BigInteger?): List<ChangeToken>

    fun getLatestChangeToken(cc: LadonCallContext, repoId: String, maxItems: Long? = 1): List<ChangeToken>

    fun getFirstChangeToken(cc: LadonCallContext, repoId: String): ChangeToken?

}