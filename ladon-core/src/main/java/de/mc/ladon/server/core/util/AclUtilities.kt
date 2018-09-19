package de.mc.ladon.server.core.util

import de.mc.ladon.server.core.persistence.entities.impl.Ace
import de.mc.ladon.server.core.persistence.entities.impl.Acl
import java.util.*

/**
 * @author Ralf Ulrich
 * 04.09.16
 */

fun Acl.addAcl(other: Acl?): Acl {
    if (other == null) return this
    val result = hashMapOf<String, HashSet<String>>()
    this.content.forEach {
        result.getOrPut(it.principal, { hashSetOf<String>() }).addAll(it.permissions)
    }
    other.content.forEach {
        result.getOrPut(it.principal, { hashSetOf<String>() }).addAll(it.permissions)
    }
    return Acl(result.entries.map { Ace(it.key, it.value.toList()) })
}

fun Acl.removeAcl(other: Acl?): Acl {
    if (other == null) return this
    val result = hashMapOf<String, HashSet<String>>()
    this.content.forEach {
        result.getOrPut(it.principal, { hashSetOf<String>() }).addAll(it.permissions)
    }
    other.content.forEach {
        val itsPermissions = result.get(it.principal)
        itsPermissions?.removeAll(it.permissions)
    }
    return Acl(result.entries
            .filter { it.value.isNotEmpty() }
            .map { Ace(it.key, it.value.toList()) })
}

