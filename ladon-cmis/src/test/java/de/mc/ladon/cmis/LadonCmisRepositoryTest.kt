package de.mc.ladon.cmis

import de.mc.ladon.server.core.api.LadonRepository
import de.mc.ladon.server.core.api.persistence.services.LadonUserDetailsManager
import de.mc.ladon.server.core.persistence.entities.impl.LadonUser
import io.mockk.every
import io.mockk.mockk
import org.apache.chemistry.opencmis.commons.data.ObjectData
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId
import org.apache.chemistry.opencmis.commons.enums.CmisVersion
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisProperty
import org.apache.chemistry.opencmis.commons.server.CallContext
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class LadonCmisRepositoryTest {


    val repo = LadonCmisRepository("testrepo", "/", LadonCmisTypeManager())
    val ladonRpository = mockk<LadonRepository>()
    val cc = mockk<CallContext>()

    @Before
    fun setUp() {
        LadonServicesHolder.ctx = TestContext(
                LadonUserDetailsManager::class.java to TestUserDetailsManager(LadonUser("test")),
                LadonRepository::class.java to ladonRpository)

        every { cc.username } returns "test"
        every { cc.cmisVersion } returns CmisVersion.CMIS_1_1
        every { cc.isObjectInfoRequired } returns false
    }

fun ObjectData.print() = println("${if (baseTypeId == BaseTypeId.CMIS_FOLDER)"Folder" else "Document"} id: $id name: ${properties.properties["cmis:name"]?.values?.firstOrNull()}")
    @Test
    fun getRootDirectory() {
        val objectByPath = repo.getObjectByPath(cc, "/", "", false, false, null)
        assertNotNull(objectByPath)
        objectByPath.print()
    }

    @Test
    fun getUserManager() {
    }

    @Test
    fun getRepositoryInfo() {
        val repositoryInfo = repo.getRepositoryInfo(cc)
        println(repositoryInfo)
    }

    @Test
    fun getTypeChildren() {
    }

    @Test
    fun getTypeDescendants() {
    }

    @Test
    fun getTypeDefinition() {
    }

    @Test
    fun createDocument() {
    }

    @Test
    fun createDocumentFromSource() {
    }

    @Test
    fun createFolder() {
    }

    @Test
    fun moveObject() {
    }

    @Test
    fun changeContentStream() {
    }

    @Test
    fun deleteObject() {
    }

    @Test
    fun deleteTree() {
    }

    @Test
    fun updateProperties() {
    }

    @Test
    fun bulkUpdateProperties() {
    }

    @Test
    fun getObject() {
    }

    @Test
    fun getAllowableActions() {
    }

    @Test
    fun getAcl() {
    }

    @Test
    fun getContentStream() {
    }

    @Test
    fun getChildren() {
    }

    @Test
    fun getDescendants() {
    }

    @Test
    fun getFolderParent() {
    }

    @Test
    fun getObjectParents() {
    }

    @Test
    fun getObjectByPath() {
    }

    @Test
    fun getRepositoryId() {
    }
}
