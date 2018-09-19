package de.mc.ladon.server.core.exceptions

/**
 * General Ladon Runtime Exception
 * Created by Ralf Ulrich on 27.08.16.
 */
open class LadonServerException : RuntimeException {
    constructor(message: String, ex: Exception) : super(message, ex)
    constructor(message: String) : super(message)
    constructor(ex: Exception) : super(ex)

}

class LadonInternalServerError : LadonServerException {
    constructor(message: String, ex: Exception) : super(message, ex)
    constructor(message: String) : super(message)
    constructor(ex: Exception) : super(ex)
}

class LadonIllegalArgumentException : LadonServerException {
    constructor(message: String, ex: Exception) : super(message, ex)
    constructor(message: String) : super(message)
    constructor(ex: Exception) : super(ex)
}


class LadonUnsupportedOperationException : LadonServerException {
    constructor(message: String, ex: Exception) : super(message, ex)
    constructor(message: String) : super(message)
    constructor(ex: Exception) : super(ex)
}

open class LadonStorageException : LadonServerException {
    constructor(message: String, ex: Exception) : super(message, ex)
    constructor(message: String) : super(message)
    constructor(ex: Exception) : super(ex)
}

class LadonObjectNotFoundException : LadonStorageException {
    constructor(message: String, ex: Exception) : super(message, ex)
    constructor(message: String) : super(message)
    constructor(ex: Exception) : super(ex)
}