package de.mc.ladon.server.boot.controller.pages

import com.datastax.driver.core.utils.UUIDs
import com.google.common.base.Strings
import com.google.common.io.BaseEncoding
import de.mc.ladon.s3server.common.S3Constants
import de.mc.ladon.server.boot.controller.FrameController
import de.mc.ladon.server.boot.tables.Color
import de.mc.ladon.server.boot.tables.TableCell
import de.mc.ladon.server.boot.tables.TableObject
import de.mc.ladon.server.boot.tables.TableRow
import de.mc.ladon.server.core.exceptions.LadonObjectNotFoundException
import de.mc.ladon.server.core.persistence.dao.api.BinaryDataDAO
import de.mc.ladon.server.core.persistence.dao.api.ChangeTokenDAO
import de.mc.ladon.server.core.persistence.dao.api.MetadataDAO
import de.mc.ladon.server.core.persistence.entities.api.ChangeToken
import de.mc.ladon.server.core.persistence.entities.api.Metadata
import de.mc.ladon.server.core.persistence.entities.impl.Acl
import de.mc.ladon.server.core.persistence.entities.impl.Content
import de.mc.ladon.server.core.persistence.entities.impl.HistoryKey
import de.mc.ladon.server.core.persistence.entities.impl.ResourceKey
import de.mc.ladon.server.core.request.LadonCallContext
import de.mc.ladon.server.core.util.humanReadable
import de.mc.ladon.server.core.util.toUUID
import org.apache.tomcat.util.http.fileupload.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import java.io.InputStream
import java.math.BigInteger
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.servlet.http.HttpServletResponse

/**
 * DebugPageController
 * Created by Ralf Ulrich on 13.12.15.
 */
@Controller
class DebugPageController : FrameController() {

    val log = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var dataDAO: MetadataDAO

    @Autowired
    lateinit var tokenDao: ChangeTokenDAO

    @Autowired
    lateinit var binaryDao: BinaryDataDAO


    @RequestMapping(value = ["debug"])
    fun showId(model: MutableMap<String, Any>, callContext: LadonCallContext, @RequestParam(required = false) key: String?, @RequestParam(required = false) repoid: String?, @RequestParam(required = false) objectid: String?): String {

        val resource = key?.toResourceKey() ?: dataDAO.getMetadataLatest(callContext, HistoryKey(repoid!!, objectid
                ?: ""))?.key() ?: ResourceKey(repoid!!, objectid ?: "", UUID.randomUUID())
        model["objectid"] = resource.versionSeriesId
        //model.put("df", SimpleDateFormat(datePattern))
        model["objects"] = listOf(toTableObject(dataDAO.getMetadataHistory(callContext, HistoryKey(resource.repositoryId, resource.versionSeriesId)), resource.changeToken.toString(), true))

        val obj = try {
            dataDAO.getMetadata(callContext, resource)
        } catch (e: Exception) {
            null
        }
        if (obj == null) {
            model.flashDanger("object not found")

        } else {
            val properties = obj.properties()
            // val type = repoService.getTypeDefinition(callContext, repoid, properties.getFirstValue<String>(PropertyIds.OBJECT_TYPE_ID), null)

            val tables = listOf(
                    obj[Acl::class]?.toTableObject(),
                    propToProps(properties.content).toTableObject(),
                    obj.key().toTableObject(),
                    obj.content().toTableObject(obj.key())
            ).filterNotNull()

            model["tables"] = tables

            val ct = obj.key().changeToken

            model["objectid"] = resource.versionSeriesId
            model["token"] = ct
        }
        return super.updateModel(model, "debug", resource.repositoryId)
    }

    @RequestMapping(value = ["remove-version"], method = [RequestMethod.GET])
    fun getRemoveVersion(model: MutableMap<String, Any>, callContext: LadonCallContext, @RequestParam key: String): String {
        val resourceKey = key.toResourceKey()
        model["key"] = resourceKey
        model["urlkey"] = key
        model["changeDate"] = fromDate(Date(UUIDs.unixTimestamp(resourceKey.changeToken)))
        return super.updateModel(model, "remove-version", resourceKey.repositoryId)
    }

    @RequestMapping(value = ["remove-version"], method = [RequestMethod.POST])
    fun postRemoveVersion(model: MutableMap<String, Any>, callContext: LadonCallContext, @RequestParam urlkey: String): String {
        val resourceKey = urlkey.toResourceKey()
        val meta = dataDAO.getMetadata(callContext, resourceKey)
        if (meta != null) {
            log.warn("User ${callContext.getUser().name} removed object $resourceKey")
            dataDAO.removeMetadata(callContext, resourceKey)
            if (!meta.isDeleted()) binaryDao.deleteContentStream(callContext, resourceKey.repositoryId, meta.content().id)
        }
        model["repoid"] = resourceKey.repositoryId
        model["objectid"] = resourceKey.versionSeriesId
        model.flashInfo("Deleted $resourceKey")
        return "redirect:searchid?repoid=${resourceKey.repositoryId}"
    }


    @RequestMapping(value = ["searchid"])
    fun searchId(model: MutableMap<String, Any>, callContext: LadonCallContext,
                 @RequestParam repoid: String,
                 @RequestParam(required = false) time: String?,
                 @RequestParam(required = false) delimiter: Boolean? = true,
                 @RequestParam(required = false) deleted: Boolean? = true,
                 @RequestParam(required = false) searchpath: String?): String {
        val datetime = toDate(time) ?: getDateMinus1()
        model["time"] = fromDate(datetime)
        model["delimiter"] = delimiter == true
        model["deleted"] = deleted == true
        model["searchpath"] = searchpath ?: ""
        model["df"] = SimpleDateFormat(datePattern)

        val limit = 1000L
        if (searchpath != null || time == null) {
            val result = dataDAO.listAllMetadata(callContext, repoid, searchpath
                    ?: "", "", delimiter?.let { if (it) "/" else null }, limit.toInt(), deleted?:true).first
            val latestVersion = ConcurrentHashMap<String, Metadata>()
            result.first.forEach {
                latestVersion.putIfAbsent(it.key().versionSeriesId, it)
            }

            val tables = mutableListOf<TableObject>()

            if(result.second.isNotEmpty()){
             tables.add(TableObject("Folders", listOf("ID","Prefix"),result.second. mapIndexed {i, pref ->
                   TableRow(listOf(
                           TableCell("${i + 1}","searchid?repoid=$repoid&searchpath=$searchpath$pref/&delimiter=$delimiter"),
                           TableCell(pref)),
                           Color.BLUE)
               }))
            }
            if (result.first.isNotEmpty()) {
               tables.add(toTableObject(latestVersion.values.toList(), null))
            }
            model["objects"] = tables
        }

        if (!time.isNullOrEmpty()) {
            val result = tokenDao.getAllChangesSince(callContext, repoid, UUIDs.startOf(datetime.time).toString(), BigInteger.valueOf(limit)).toTableObjectList()
            if (result.isNotEmpty()) {
                model["objects"] = result
            } else {
                model.flashInfo("not found")
            }

        }

        return super.updateModel(model, "searchid", repoid)
    }

    @RequestMapping(value = ["download"])
    fun download(response: HttpServletResponse, @RequestParam key: String, callContext: LadonCallContext) {
        val resource = key.toResourceKey()
        val meta = dataDAO.getMetadata(callContext, resource) ?: throw LadonObjectNotFoundException(key)
        val content = meta.content()
        val props = meta.properties()

        val stream = binaryDao.getContentStream(callContext, resource.repositoryId, content.id, null, null)
        response.contentType = Strings.emptyToNull(props.get(S3Constants.CONTENT_TYPE)) ?: "application/octetstream"
        response.setContentLength(content.length.toInt())
        response.setHeader("Pragma", "no-cache")
        response.setHeader("content-disposition", "inline; filename=\"" + resource.versionSeriesId.split("/").last() + "\"")

        val inputStream: InputStream? = stream


        try {
            IOUtils.copyLarge(inputStream, response.outputStream)
        } finally {
            inputStream?.close()
            response.flushBuffer()
        }

    }


    private fun propToProps(properties: Map<String, String>): List<Props> {
        return properties.map { e ->
            Props(e.key, e.value.javaClass.simpleName,
                    e.value)
        }.sortedBy { p -> p.type }
    }


    private fun getDateMinus1(): Date {
        return Date(Date().time - 1000 * 60 * 60)
    }

    private val datePattern = "yyyy-MM-dd HH:mm:ss"

    private fun toDate(dateString: String?): Date? {
        if (dateString == null) return null
        try {
            return SimpleDateFormat(datePattern, Locale.GERMANY).parse(dateString)
        } catch (e: ParseException) {
            return null
        }

    }

    private fun fromDate(date: Date): String {
        return SimpleDateFormat(datePattern).format(date)
    }

    data class Props(val id: String, val type: String, val value: String = "not set")


    private fun toTableObject(meta: List<Metadata>, selected: String?, deleteButton: Boolean = false): TableObject {
        val headers = listOf("INDEX", "ID", "Operation", "Size","LastModified")
        return TableObject("Files", headers, meta.mapIndexed { i, e ->
            TableRow(listOf(
                    TableCell("${i + 1}", "debug?key=${e.key().toUrlString()}"),
                    TableCell(e.key().versionSeriesId),
                    TableCell(if (e.isDeleted()) "DELETE" else "PUT"),
                    TableCell(e.content().length.humanReadable()),
                    TableCell(SimpleDateFormat(datePattern).format(if (e.isDeleted()) e.content().deleted else e.content().created))).let {
                if (!deleteButton) it else it +
                        TableCell("Remove", "remove-version?key=${e.key().toUrlString()}")
            },
                    if (e.key().changeToken.toString() == selected) Color.GREEN else Color.NONE)
        })
    }

    private fun List<ChangeToken>.toTableObjectList(): List<TableObject> {
        val headers = listOf("INDEX", "ID", "Change", "Time")
        return listOf(TableObject("Files", headers, mapIndexed { i, e ->
            TableRow(listOf(
                    TableCell("${i + 1}", "debug?key=${ResourceKey(e.repoId!!, e.versionseriesId!!, e.changeToken!!).toUrlString()}"),
                    TableCell(e.versionseriesId),
                    TableCell(e.operation),
                    TableCell(SimpleDateFormat(datePattern).format(Date(UUIDs.unixTimestamp(e.changeToken))))),
                    Color.NONE)
        }))
    }


    private fun List<Props>.toTableObject(): TableObject {
        val headers = listOf("ID", "Type", "Value")
        return TableObject("Properties", headers, mapIndexed { _, e ->
            TableRow(listOf(
                    TableCell(e.id),
                    TableCell(e.type),
                    TableCell(e.value)),
                    Color.NONE)
        })
    }

    private fun Acl.toTableObject(): TableObject {
        val headers = listOf("Principal", "Permissions")
        return TableObject("Acl", headers, content.mapIndexed { _, ace ->
            TableRow(listOf(
                    TableCell(ace.principal),
                    TableCell(ace.permissions.toString())),
                    Color.NONE)
        })
    }


    private fun ResourceKey.toTableObject(): TableObject {
        val headers = listOf("Bucket", "Key", "Version")
        return TableObject("Key", headers, listOf(
                TableRow(listOf(
                        TableCell(repositoryId),
                        TableCell(versionSeriesId),
                        TableCell(changeToken.toString())),
                        Color.NONE))
        )
    }

    private fun Content.toTableObject(key: ResourceKey): TableObject {
        val headers = listOf("ContentId", "Size", "Hash")
        return TableObject("Content", headers, listOf(
                TableRow(listOf(
                        TableCell(id),
                        TableCell(length.toLong().humanReadable()),
                        TableCell(hash),
                        TableCell("Download", "download?key=${key.toUrlString()}")
                ),
                        Color.NONE))
        )
    }


    private fun String.toResourceKey(): ResourceKey {
        return split(".").map { it.base64dec() }.let { ResourceKey(it[0], it[1], it[2].toUUID()) }
    }

}

fun ResourceKey.toUrlString(): String {
    return "${repositoryId.base64enc()}.${versionSeriesId.base64enc()}.${changeToken.toString().base64enc()}"
}

fun String.base64enc(): String {
    return BaseEncoding.base64().encode(this.toByteArray())
}

fun String.base64dec(): String {
    return String(BaseEncoding.base64().decode(this))
}
