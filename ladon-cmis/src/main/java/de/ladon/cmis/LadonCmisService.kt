/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package de.ladon.cmis

import org.apache.chemistry.opencmis.commons.data.*
import org.apache.chemistry.opencmis.commons.data.Properties
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships
import org.apache.chemistry.opencmis.commons.enums.UnfileObject
import org.apache.chemistry.opencmis.commons.enums.VersioningState
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectListImpl
import org.apache.chemistry.opencmis.commons.impl.server.AbstractCmisService
import org.apache.chemistry.opencmis.commons.server.CallContext
import org.apache.chemistry.opencmis.commons.spi.Holder
import org.apache.chemistry.opencmis.server.support.wrapper.CallContextAwareCmisService
import java.math.BigInteger
import java.util.*

/**
 * FileShare Service implementation.
 */
class LadonCmisService(private val repositoryManager: LadonCmisRepositoryManager) : AbstractCmisService(), CallContextAwareCmisService {
    private lateinit var context: CallContext

    /**
     * Gets the repository for the current call.
     */
    val repository: LadonCmisRepository
        get() = repositoryManager.getRepository(callContext!!.repositoryId)

    // --- Call Context ---

    /**
     * Sets the call context.
     *
     * This method should only be called by the service factory.
     */
    override fun setCallContext(context: CallContext) {
        this.context = context
    }

    /**
     * Gets the call context.
     */
    override fun getCallContext(): CallContext? {
        return context
    }

    // --- repository service ---

    override fun getRepositoryInfo(repositoryId: String?, extension: ExtensionsData): RepositoryInfo {
        for (fsr in repositoryManager.getRepositories()) {
            if (fsr.repositoryId == repositoryId) {
                return fsr.getRepositoryInfo(callContext!!)
            }
        }

        throw CmisObjectNotFoundException("Unknown repository '$repositoryId'!")
    }

    override fun getRepositoryInfos(extension: ExtensionsData): List<RepositoryInfo> {
        val result = ArrayList<RepositoryInfo>()

        for (fsr in repositoryManager.getRepositories()) {
            result.add(fsr.getRepositoryInfo(callContext!!))
        }

        return result
    }

    override fun getTypeChildren(repositoryId: String, typeId: String, includePropertyDefinitions: Boolean?,
                                 maxItems: BigInteger, skipCount: BigInteger, extension: ExtensionsData): TypeDefinitionList {
        return repository.getTypeChildren(callContext!!, typeId, includePropertyDefinitions, maxItems,
                skipCount)
    }

    override fun getTypeDefinition(repositoryId: String, typeId: String, extension: ExtensionsData): TypeDefinition {
        return repository.getTypeDefinition(callContext!!, typeId)
    }

    override fun getTypeDescendants(repositoryId: String, typeId: String, depth: BigInteger,
                                    includePropertyDefinitions: Boolean?, extension: ExtensionsData?): List<TypeDefinitionContainer> {
        return repository.getTypeDescendants(callContext!!, typeId, depth, includePropertyDefinitions)
    }

    // --- navigation service ---

    override fun getChildren(repositoryId: String, folderId: String, filter: String, orderBy: String,
                             includeAllowableActions: Boolean?, includeRelationships: IncludeRelationships, renditionFilter: String,
                             includePathSegment: Boolean?, maxItems: BigInteger, skipCount: BigInteger, extension: ExtensionsData): ObjectInFolderList {
        return repository.getChildren(callContext!!, folderId, filter, orderBy, includeAllowableActions,
                includePathSegment, maxItems, skipCount, this)
    }

    override fun getDescendants(repositoryId: String?, folderId: String?, depth: BigInteger?,
                                filter: String?, includeAllowableActions: Boolean?, includeRelationships: IncludeRelationships?,
                                renditionFilter: String?, includePathSegment: Boolean?, extension: ExtensionsData?): List<ObjectInFolderContainer> {
        return repository.getDescendants(callContext!!, folderId!!, depth, filter!!, includeAllowableActions,
                includePathSegment, this, false)
    }

    override fun getFolderParent(repositoryId: String?, folderId: String?, filter: String?, extension: ExtensionsData?): ObjectData {
        return repository.getFolderParent(callContext!!, folderId!!, filter!!, this)
    }

    override fun getFolderTree(repositoryId: String?, folderId: String?, depth: BigInteger?,
                               filter: String?, includeAllowableActions: Boolean?, includeRelationships: IncludeRelationships?,
                               renditionFilter: String?, includePathSegment: Boolean?, extension: ExtensionsData?): List<ObjectInFolderContainer> {
        return repository.getDescendants(callContext!!, folderId!!, depth, filter!!, includeAllowableActions,
                includePathSegment, this, true)
    }

    override fun getObjectParents(repositoryId: String, objectId: String, filter: String,
                                  includeAllowableActions: Boolean?, includeRelationships: IncludeRelationships, renditionFilter: String,
                                  includeRelativePathSegment: Boolean?, extension: ExtensionsData): List<ObjectParentData> {
        return repository.getObjectParents(callContext!!, objectId, filter, includeAllowableActions,
                includeRelativePathSegment, this)
    }

    override fun getCheckedOutDocs(repositoryId: String?, folderId: String?, filter: String?, orderBy: String?,
                                   includeAllowableActions: Boolean?, includeRelationships: IncludeRelationships?, renditionFilter: String?,
                                   maxItems: BigInteger?, skipCount: BigInteger?, extension: ExtensionsData?): ObjectList {
        val result = ObjectListImpl()
        result.setHasMoreItems(false)
        result.numItems = BigInteger.ZERO
        val emptyList = emptyList<ObjectData>()
        result.objects = emptyList

        return result
    }

    // --- object service ---

    override fun create(repositoryId: String, properties: Properties, folderId: String, contentStream: ContentStream,
                        versioningState: VersioningState, policies: List<String>, extension: ExtensionsData): String {
        val `object` = repository.create(callContext!!, properties, folderId, contentStream,
                versioningState, this)

        return `object`.id
    }

    override fun createDocument(repositoryId: String?, properties: Properties?, folderId: String?,
                                contentStream: ContentStream?, versioningState: VersioningState?, policies: List<String>?, addAces: Acl?,
                                removeAces: Acl?, extension: ExtensionsData?): String {
        return repository.createDocument(callContext!!, properties, folderId!!, contentStream, versioningState)
    }

    override fun createDocumentFromSource(repositoryId: String?, sourceId: String?, properties: Properties?,
                                          folderId: String?, versioningState: VersioningState?, policies: List<String>?, addAces: Acl?, removeAces: Acl?,
                                          extension: ExtensionsData?): String {
        return repository.createDocumentFromSource(callContext!!, sourceId!!, properties, folderId!!,
                versioningState)
    }

    override fun createFolder(repositoryId: String?, properties: Properties?, folderId: String?, policies: List<String>?,
                              addAces: Acl?, removeAces: Acl?, extension: ExtensionsData?): String {
        return repository.createFolder(callContext!!, properties, folderId!!)
    }

    override fun deleteObjectOrCancelCheckOut(repositoryId: String?, objectId: String?, allVersions: Boolean?,
                                              extension: ExtensionsData?) {
        repository.deleteObject(callContext!!, objectId!!)
    }

    override fun deleteTree(repositoryId: String?, folderId: String?, allVersions: Boolean?,
                            unfileObjects: UnfileObject?, continueOnFailure: Boolean?, extension: ExtensionsData?): FailedToDeleteData {
        return repository.deleteTree(callContext!!, folderId!!, continueOnFailure)
    }

    override fun getAllowableActions(repositoryId: String, objectId: String, extension: ExtensionsData): AllowableActions {
        return repository.getAllowableActions(callContext!!, objectId)
    }

    override fun getContentStream(repositoryId: String?, objectId: String?, streamId: String?, offset: BigInteger?,
                                  length: BigInteger?, extension: ExtensionsData?): ContentStream {
        return repository.getContentStream(callContext!!, objectId!!, offset, length)
    }

    override fun getObject(repositoryId: String, objectId: String, filter: String, includeAllowableActions: Boolean?,
                           includeRelationships: IncludeRelationships, renditionFilter: String, includePolicyIds: Boolean?,
                           includeAcl: Boolean?, extension: ExtensionsData): ObjectData {
        return repository.getObject(callContext!!, objectId, null, filter, includeAllowableActions, includeAcl,
                this)
    }

    override fun getObjectByPath(repositoryId: String?, path: String?, filter: String?, includeAllowableActions: Boolean?,
                                 includeRelationships: IncludeRelationships?, renditionFilter: String?, includePolicyIds: Boolean?,
                                 includeAcl: Boolean?, extension: ExtensionsData?): ObjectData {
        return repository.getObjectByPath(callContext!!, path, filter!!, includeAllowableActions!!, includeAcl!!,
                this)
    }

    override fun getProperties(repositoryId: String, objectId: String, filter: String, extension: ExtensionsData): Properties {
        val `object` = repository.getObject(callContext!!, objectId, null, filter, false, false, this)
        return `object`.properties
    }

    override fun getRenditions(repositoryId: String?, objectId: String?, renditionFilter: String?,
                               maxItems: BigInteger?, skipCount: BigInteger?, extension: ExtensionsData?): List<RenditionData> {
        return emptyList()
    }

    override fun moveObject(repositoryId: String?, objectId: Holder<String>?, targetFolderId: String?, sourceFolderId: String?,
                            extension: ExtensionsData?) {
        repository.moveObject(callContext!!, objectId, targetFolderId!!, this)
    }

    override fun setContentStream(repositoryId: String?, objectId: Holder<String>?, overwriteFlag: Boolean?,
                                  changeToken: Holder<String>?, contentStream: ContentStream?, extension: ExtensionsData?) {
        repository.changeContentStream(callContext!!, objectId, overwriteFlag, contentStream, false)
    }

    override fun appendContentStream(repositoryId: String?, objectId: Holder<String>?, changeToken: Holder<String>?,
                                     contentStream: ContentStream?, isLastChunk: Boolean, extension: ExtensionsData?) {
        repository.changeContentStream(callContext!!, objectId, true, contentStream, true)
    }

    override fun deleteContentStream(repositoryId: String?, objectId: Holder<String>?, changeToken: Holder<String>?,
                                     extension: ExtensionsData?) {
        repository.changeContentStream(callContext!!, objectId, true, null, false)
    }

    override fun updateProperties(repositoryId: String?, objectId: Holder<String>?, changeToken: Holder<String>?,
                                  properties: Properties?, extension: ExtensionsData?) {
        repository.updateProperties(callContext!!, objectId, properties!!, this)
    }

    override fun bulkUpdateProperties(repositoryId: String?,
                                      objectIdAndChangeToken: List<BulkUpdateObjectIdAndChangeToken>?, properties: Properties?,
                                      addSecondaryTypeIds: List<String>?, removeSecondaryTypeIds: List<String>?, extension: ExtensionsData?): List<BulkUpdateObjectIdAndChangeToken> {
        return repository.bulkUpdateProperties(callContext!!, objectIdAndChangeToken, properties, this)
    }

    // --- versioning service ---

    override fun getAllVersions(repositoryId: String?, objectId: String?, versionSeriesId: String?, filter: String?,
                                includeAllowableActions: Boolean?, extension: ExtensionsData?): List<ObjectData> {
        val theVersion = repository.getObject(callContext!!, objectId, versionSeriesId, filter,
                includeAllowableActions, false, this)

        return listOf(theVersion)
    }

    override fun getObjectOfLatestVersion(repositoryId: String?, objectId: String?, versionSeriesId: String?,
                                          major: Boolean?, filter: String?, includeAllowableActions: Boolean?, includeRelationships: IncludeRelationships?,
                                          renditionFilter: String?, includePolicyIds: Boolean?, includeAcl: Boolean?, extension: ExtensionsData?): ObjectData {
        return repository.getObject(callContext!!, objectId, versionSeriesId, filter, includeAllowableActions,
                includeAcl, this)
    }

    override fun getPropertiesOfLatestVersion(repositoryId: String, objectId: String, versionSeriesId: String,
                                              major: Boolean?, filter: String, extension: ExtensionsData): Properties {
        val `object` = repository.getObject(callContext!!, objectId, versionSeriesId, filter, false,
                false, null)

        return `object`.properties
    }

    // --- ACL service ---

    override fun getAcl(repositoryId: String?, objectId: String, onlyBasicPermissions: Boolean?, extension: ExtensionsData?): Acl {
        return repository.getAcl(callContext!!, objectId)
    }
}
