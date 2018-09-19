package de.mc.ladon.storage

import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.S3ClientOptions
import com.amazonaws.services.s3.model.ObjectMetadata
import org.junit.Test
import java.io.InputStream
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * @author Ralf Ulrich
 * 17.03.17
 */

class StorageTest {

   // @Test
    fun testPut() {

        val s3Client = getS3Client("aMJlOJnVG2VW65DoR_z6", "APyBVPf_mPG9kkC6XlC79VuBzcoTRhgq5ZrbTYG9")

        Executors.newFixedThreadPool(10).run {
            for (i in 1..1000) {
                execute {
                    s3Client.putObject("test", "demo$i.txt", RandomIS(Random().nextInt(1024 * 1024 * 10)), ObjectMetadata())
                    println("demo$i.txt")
                }
            }
            shutdown()
            awaitTermination(100, TimeUnit.MINUTES)

        }


    }

    fun getS3Client(accessKey: String, secretKey: String): AmazonS3Client {
        val credentials = BasicAWSCredentials(accessKey, secretKey)
        val newClient = AmazonS3Client(credentials,
                ClientConfiguration())
        newClient.setS3ClientOptions(S3ClientOptions().withPathStyleAccess(true))
        newClient.setEndpoint("http://vpf.mind-score.de:8000/services/s3")
        newClient.signerRegionOverride = "S3SignerType"
        return newClient
    }

    class RandomIS(lenght: Int) : InputStream() {
        val random = Random()
        val seq = generateSequence { random.nextInt(127) }.take(lenght).iterator()
        override fun read(): Int {
            return if (seq.hasNext()) seq.next() else -1
        }

        override fun markSupported() = false

    }
}
