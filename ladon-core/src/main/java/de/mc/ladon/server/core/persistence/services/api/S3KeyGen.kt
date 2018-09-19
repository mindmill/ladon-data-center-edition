/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.persistence.services.api

/**
 * @author Ralf Ulrich
 * on 21.10.16.
 */
interface S3KeyGen {

    fun generateNewKey(): KeyPair
}

data class KeyPair(val publicKey: String, val privateKey: String)