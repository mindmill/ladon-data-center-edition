package de.mc.ladon.server.s3

/**
 * @author Ralf Ulrich
 * 24.10.16
 */
interface LadonS3Config {
    var validatecontent: Boolean?
    var servletthreads: Int?
    var requesttimeout: Long?
    var disableSecurity: Boolean?

}
