package de.mc.ladon.server.boot.controller.pages

import de.mc.ladon.server.boot.controller.FrameController
import de.mc.ladon.server.boot.tables.Color
import de.mc.ladon.server.boot.tables.TableCell
import de.mc.ladon.server.boot.tables.TableObject
import de.mc.ladon.server.boot.tables.TableRow
import de.mc.ladon.server.core.api.persistence.dao.ChangeTokenDAO
import de.mc.ladon.server.core.api.persistence.dao.MetadataDAO
import de.mc.ladon.server.core.api.persistence.dao.RepositoryDAO
import de.mc.ladon.server.core.api.persistence.entities.Metadata
import de.mc.ladon.server.core.persistence.entities.impl.LadonResourceKey
import de.mc.ladon.server.core.request.impl.SystemCallContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.text.SimpleDateFormat

/**
 * OverviewPageController
 * Created by Ralf Ulrich on 13.12.15.
 */
@Controller
open class OverviewPageController @Autowired constructor(
        val repositoryDAO: RepositoryDAO,
        val metadataDAO: MetadataDAO,
        val changeTokenDAO: ChangeTokenDAO

) : FrameController() {


    @RequestMapping("/")
    fun index(): String {
        return "redirect:overview"
    }

    @RequestMapping("overview")
    fun overview(model: MutableMap<String, Any>, @RequestParam(required = false) repoid: String?): String {
    val repositoryId = repoid?:  repositoryDAO.getRepositories(SystemCallContext()).firstOrNull()?.repoId?:"default"

        val changes = changeTokenDAO.getLatestChangeToken(SystemCallContext(),repositoryId,10L).map {
            metadataDAO.getMetadata(SystemCallContext(), LadonResourceKey(it.repoId!!, it.versionseriesId!!, it.changeToken!!)) }
        model.put("objects", listOf(toTableObject(changes.filterNotNull())))
        return super.updateModel(model, "overview", repositoryId)
    }

    @RequestMapping("exception")
    fun test(): String {
        throw IllegalArgumentException()
    }

    @RequestMapping("s3")
    fun cmis(model: MutableMap<String, Any>, @RequestParam repoid: String): String {
        return super.updateModel(model, "s3", repoid)
    }


    @RequestMapping("backup")
    fun backup(model: MutableMap<String, Any>, @RequestParam repoid: String): String {
        return super.updateModel(model, "backup", repoid)
    }


    fun toTableObject(meta: List<Metadata>): TableObject {
        val headers = listOf("INDEX", "ID", "Change", "Time")
        return TableObject("Changes", headers, meta.mapIndexed { i, e ->
            TableRow(listOf(
                    TableCell("${i + 1}", "debug?key=${e.key().toUrlString()}"),
                    TableCell(e.key().versionSeriesId),
                    TableCell(if (e.isDeleted()) "DELETE" else "PUT"),
                    TableCell(SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(if (e.isDeleted()) e.content().deleted else e.content().created))),
                    if (e.isDeleted()) Color.RED else Color.GREEN)
        })
    }
}
