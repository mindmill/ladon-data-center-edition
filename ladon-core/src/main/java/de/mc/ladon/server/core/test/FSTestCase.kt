/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.test

/**
 * File System test
 * Created by Ralf Ulrich on 18.08.16.
 */
open class FSTestCase(val startState: FSSnapshot, val resultState: FSSnapshot, val calls: List<ServiceCall>)

