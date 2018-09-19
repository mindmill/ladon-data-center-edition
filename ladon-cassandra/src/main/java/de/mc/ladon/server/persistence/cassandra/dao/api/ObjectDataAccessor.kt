/*
 * Copyright (c) 2016 Mind Consulting UG(haftungsbeschränkt)
 */

package de.mc.ladon.server.persistence.cassandra.dao.api

import com.datastax.driver.core.ResultSet
import com.datastax.driver.mapping.Result
import com.datastax.driver.mapping.annotations.Accessor
import com.datastax.driver.mapping.annotations.Query
import de.mc.ladon.server.persistence.cassandra.entities.impl.DbObjectData
import java.util.*

/**
 * ObjectDataAccessor
 * Created by Ralf Ulrich on 05.05.16.
 */
@Accessor interface ObjectDataAccessor {

    @Query("SELECT * FROM LADON.OBJECTS WHERE REPOID = :repositoryId AND VERSIONSERIESID = :versionseriesid AND CHANGETOKEN = :changetoken ")
    fun getObjectVersion(repositoryId: String, versionSeriesId: String, changeToken: UUID): DbObjectData?

    @Query("SELECT * FROM LADON.OBJECTS WHERE REPOID = :repositoryId AND VERSIONSERIESID = :versionseriesid LIMIT 1")
    fun getObject(repositoryId: String, objectId: String): DbObjectData?

    @Query("SELECT * FROM LADON.OBJECTS WHERE REPOID = :repositoryId AND VERSIONSERIESID = :versionseriesid ")
    fun getObjectVersions(repositoryId: String, versionSeriesId: String): Result<DbObjectData?>

    @Query("DELETE FROM LADON.OBJECTS WHERE REPOID = :repositoryId AND VERSIONSERIESID = :objectId AND CHANGETOKEN = :changetoken")
    fun deleteObjectVersion(repositoryId: String, objectId: String, changetoken: UUID): ResultSet

    @Query("DELETE FROM LADON.OBJECTS WHERE REPOID = :repositoryId AND VERSIONSERIESID = :versionseriesid ")
    fun deleteObject(repositoryId: String, versionSeriesId: String): ResultSet

    @Query("SELECT * FROM LADON.OBJECTS WHERE REPOID = :repositoryId")
    fun getObjects(repositoryId: String): Result<DbObjectData>

    @Query("SELECT * FROM LADON.OBJECTS")
    fun getAllObjects(): Result<DbObjectData>

    @Query("SELECT * FROM LADON.OBJECTS WHERE REPOID = :repositoryId AND VERSIONSERIESID >= :startkey")
    fun getObjectsStartingAt(repositoryId: String, startkey: String): Result<DbObjectData>

}