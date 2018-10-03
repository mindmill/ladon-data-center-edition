/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.hooks.impl

import de.mc.ladon.server.core.api.hooks.LadonHookManager
import de.mc.ladon.server.core.api.hooks.MetadataChangeHook
import javax.inject.Named

/**
 * Implementation to manage all hooks
 * Created by ralf on 07.07.15.
 */
@Named open class LadonHookManagerImpl() : LadonHookManager {

    //@Inject
    private var metadataChangeHookList: List<MetadataChangeHook> = listOf()

    //init {
    //    val names = context.getBeanNamesForType(ChangeObjectDataHook::class.java)
    //    changeObjectDataHookList = names?.map { context.getBean(it) as ChangeObjectDataHook }.orEmpty()
    //}


    override fun getChangeObjectDataHooks(): List<MetadataChangeHook> {
        return metadataChangeHookList.sortedBy { it.priority() }
    }
}
