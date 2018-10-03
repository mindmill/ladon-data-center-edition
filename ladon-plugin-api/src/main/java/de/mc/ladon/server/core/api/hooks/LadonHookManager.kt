/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.api.hooks

/**
 * Simple registry to add hooks and retrieve them
 * Created by ralf on 07.07.15.
 */
interface LadonHookManager {

    fun getChangeObjectDataHooks(): List<MetadataChangeHook>

}