/*
 * Copyright (c) 2015 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.core.persistence.dao.api

import de.mc.ladon.server.core.persistence.entities.api.Metadata
import de.mc.ladon.server.core.persistence.entities.impl.HistoryKey
import de.mc.ladon.server.core.persistence.entities.impl.ResourceKey
import de.mc.ladon.server.core.request.LadonCallContext

/**
 * ObjectDataDAO
 * Created by Ralf Ulrich on 30.01.15.
 */
interface MetadataDAO {


    /**
     * save ObjectData
     */
    fun saveMetadata(cc: LadonCallContext, key: ResourceKey, obj: Metadata): ResourceKey


    /**
     * this fun marks the metadata as deleted, does not actually delete anything
     */
    fun deleteMetadata(cc: LadonCallContext, key: ResourceKey)

    /**
     * calls deleteMetadata for each version of the version series
     */
    fun deleteMetadataHistory(cc: LadonCallContext, key: HistoryKey)

    /**
     * this fun actually deletes data from the database, while deleteMetadata only marks it as deleted
     */
    fun removeMetadata(cc: LadonCallContext, key: ResourceKey)

    /**
     * returns the latest entry of the version series
     */
    fun getMetadataLatest(cc: LadonCallContext, key: HistoryKey): Metadata?

    /**
     * returns the whole version series
     */
    fun getMetadataHistory(cc: LadonCallContext, key: HistoryKey): List<Metadata>

    /**
     * returns the Metadata of the given key
     */
    fun getMetadata(cc: LadonCallContext, key: ResourceKey): Metadata?


    /**
     * returns the list of the latest versions of all metadata
     */
    fun listAllMetadata(cc: LadonCallContext, repoId: String, prefix: String = "", marker: String?, limit : Int,  includeVersions: Boolean = false): Pair<List<Metadata>, Boolean>



}