/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.test

import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Representation of a state of the file system
 * Created by Ralf Ulrich on 18.08.16.
 */
class FSSnapshot(val repoid: String, val objects: Map<String, ObjectDescriptor> = emptyMap()) {


    fun equalsExpected(expected: FSSnapshot) {
        assertEquals(expected.repoid, repoid, "Repository does not match")
        for ((k, v) in expected.objects) {
            val child = objects[k]
            assertNotNull(child, "Child not found")
            v.equalsExpected(child!!)
        }

    }
}

class ObjectDescriptor(val id: String, val path: String, val type: String, val props: Map<String, Any> = emptyMap(), val children: Map<String, ObjectDescriptor> = emptyMap()) {
    fun equalsExpected(expected: ObjectDescriptor) {
        assertEquals(expected.id, id, "Id does not match")
        assertEquals(expected.path, path, "Path does not match")
        assertEquals(expected.type, type, "Type does not match")
        for ((k, v) in expected.props) {
            assertEquals(v, props[k], "Value does not match")
        }
        for ((k, v) in expected.children) {
            val child = children[k]
            assertNotNull(child, "Child not found")
            v.equalsExpected(child!!)
        }

    }
}



