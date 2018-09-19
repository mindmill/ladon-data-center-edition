/*
 * Copyright (c) 2015 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.persistence.dao.api

import de.mc.ladon.server.core.request.LadonCallContext
import de.mc.ladon.server.core.util.StreamInfo
import java.io.InputStream
import java.math.BigInteger

/**
 * DAO for binary data
 * Created by Ralf Ulrich on 31.01.15.
 */
interface BinaryDataDAO {


    fun getContentStream(cc: LadonCallContext, repoId: String, streamId: String, offset: BigInteger?, length: BigInteger?): InputStream?

    fun saveContentStream(cc: LadonCallContext, repoId: String, contentStream: InputStream?): StreamInfo

    fun copyContentStream(cc: LadonCallContext, repoId: String, streamId: String, destRepo: String) : String

    fun deleteContentStream(cc: LadonCallContext, repoId: String, streamId: String)

    fun appendContentStream(cc: LadonCallContext, repoId: String, streamId: String, contentStream: InputStream?, length: BigInteger?, isLastChunk: Boolean)


}