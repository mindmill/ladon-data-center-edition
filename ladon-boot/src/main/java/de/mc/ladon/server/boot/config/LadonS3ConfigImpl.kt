package de.mc.ladon.server.boot.config

import de.mc.ladon.server.s3.LadonS3Config
import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author Ralf Ulrich
 * 24.10.16
 */
@ConfigurationProperties(prefix = "ladon.s3")
open class LadonS3ConfigImpl(

        override var validatecontent: Boolean? = null,
        override var servletthreads: Int? = null,
        override var requesttimeout: Long? = null,
        override var disableSecurity: Boolean? = null

) : LadonS3Config