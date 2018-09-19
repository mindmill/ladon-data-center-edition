package de.mc.ladon.server.s3

import de.mc.ladon.s3server.auth.AuthKeyGen
import de.mc.ladon.server.core.persistence.services.api.KeyPair
import de.mc.ladon.server.core.persistence.services.api.S3KeyGen
import javax.inject.Named

/**
 * @author Ralf Ulrich
 * 21.10.16
 */
@Named
open class LadonS3KeyGen() : S3KeyGen {
    override fun generateNewKey(): KeyPair {
        val pair = AuthKeyGen.generateKeypair()
        return KeyPair(pair.awsAccessKeyId, pair.awsSecretAccessKey)
    }
}