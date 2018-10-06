package de.mc.ladon.server.boot.controller.pages

import de.mc.ladon.s3server.common.Validator
import de.mc.ladon.server.boot.controller.FrameController
import de.mc.ladon.server.core.api.exceptions.LadonIllegalArgumentException
import de.mc.ladon.server.core.api.persistence.dao.MetadataDAO
import de.mc.ladon.server.core.api.persistence.entities.Repository
import de.mc.ladon.server.core.api.request.LadonCallContext
import de.mc.ladon.server.core.config.BoxConfig
import de.mc.ladon.server.persistence.cassandra.entities.impl.DbRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam

/**
 * RepositoriesPageController
 * Created by Ralf Ulrich on 13.12.15.
 */
@Controller
class RepositoriesPageController : FrameController() {


    @Autowired
    lateinit var metaDao: MetadataDAO


    @RequestMapping("repositories", method = [RequestMethod.GET, RequestMethod.POST])
    fun repos(model: MutableMap<String, Any>, @RequestParam(required = false) repoid: String?, @RequestParam(required = false) repoprefix: String?): String {
        val repo: String = repoid ?: BoxConfig.SYSTEM_REPO
        model["repoprefix"] = repoprefix ?: ""
        return super.updateModel(model, "repositories", repo)
    }

    @RequestMapping("alterrepo", method = [RequestMethod.GET])
    fun alterRepoGet(model: MutableMap<String, Any>, callContext: LadonCallContext, @RequestParam(required = true) repoid: String): String {
        val repo: Repository = repoDao.getRepository(callContext, repoid)!!
        model.put("repo", repo)
        return super.updateModel(model, "repositories-alter", repoid)
    }

    @RequestMapping("alterrepo", method = [RequestMethod.POST])
    fun alterRepoPost(model: MutableMap<String, Any>, callContext: LadonCallContext, @ModelAttribute repository: DbRepository): String {
        repoDao.setVersioned(callContext, repository.repoId!!, repository.versioned!!)
        return super.updateModel(model, "repositories", repository.repoId!!)
    }

    @RequestMapping("newrepo", method = [RequestMethod.POST])
    fun newrepo(model: MutableMap<String, Any>, callContext: LadonCallContext, @RequestParam(required = true) newrepoid: String): String {
        try {
            if (!Validator.isValidBucketName(newrepoid)) throw LadonIllegalArgumentException("Bucket Name $newrepoid is not valid, bucketnames have to be < 64 characters, a-z 0-9 . _ : - ")
            if (repoDao.getRepository(callContext, newrepoid) != null) throw LadonIllegalArgumentException("Bucket with id $newrepoid already exists")
            repoDao.addRepository(callContext, newrepoid)
            model.put("repos", repoDao.getRepositories(callContext))
        } catch (e: Exception) {
            model.put("repos", repoDao.getRepositories(callContext))
            model.flashDanger(e.message ?: "Error while creating new Bucket")
            return super.updateModel(model, "repositories", BoxConfig.SYSTEM_REPO)
        }

        return super.updateModel(model, "repositories", newrepoid)
    }


    @RequestMapping("deleterepo", method = [RequestMethod.POST])
    fun deleteRepo(model: MutableMap<String, Any>, callContext: LadonCallContext, @RequestParam(required = true) repoid: String): String {

        var empty = metaDao.listAllMetadata(callContext, repoid, limit = 1, marker = null, delimiter = null).first.first.isEmpty()


        try {
            if (!empty) throw LadonIllegalArgumentException("Bucket with id $repoid is not empty")
            if (repoDao.getRepository(callContext, repoid) == null) throw LadonIllegalArgumentException("Bucket with id $repoid doesn't exists")
            repoDao.deleteRepository(callContext, repoid)
            model.put("repos", repoDao.getRepositories(callContext))
        } catch (e: Exception) {
            model.put("repos", repoDao.getRepositories(callContext))
            model.flashDanger(e.message ?: "Error while deleting bucket $repoid")
            return super.updateModel(model, "repositories", BoxConfig.SYSTEM_REPO)
        }

        return super.updateModel(model, "repositories", repoid)
    }


}
