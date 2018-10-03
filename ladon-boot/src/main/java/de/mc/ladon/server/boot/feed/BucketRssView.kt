package de.mc.ladon.server.boot.feed

import com.rometools.rome.feed.synd.SyndContentImpl
import com.rometools.rome.feed.synd.SyndEntryImpl
import com.rometools.rome.feed.synd.SyndFeedImpl
import com.rometools.rome.io.SyndFeedOutput
import de.mc.ladon.server.boot.controller.humanReadable
import de.mc.ladon.server.boot.controller.pages.toUrlString
import de.mc.ladon.server.core.api.persistence.dao.ChangeTokenDAO
import de.mc.ladon.server.core.api.persistence.dao.MetadataDAO
import de.mc.ladon.server.core.persistence.entities.impl.LadonResourceKey
import de.mc.ladon.server.core.request.impl.AnonymousCallContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.servlet.http.HttpServletRequest


/**
 * @author Ralf Ulrich
 * 24.10.16
 */
@RestController
class BucketRssView @Autowired constructor(
        val metadataDAO: MetadataDAO,
        val changeTokenDAO: ChangeTokenDAO
) {

    @RequestMapping(value = ["/feed/{repoid}/atom"], method = [RequestMethod.GET])
    @ResponseBody
    fun repoFeed(@PathVariable("repoid") repoid: String, request: HttpServletRequest): String {

        val token = changeTokenDAO.getLatestChangeToken(AnonymousCallContext(), repoid, 100L)

        val baseUrl = String.format("%s://%s:%d/admin/", request.scheme, request.serverName, request.serverPort)


        val lastUpdates = token
                .map { metadataDAO.getMetadata(AnonymousCallContext(), LadonResourceKey(it.repoId!!, it.versionseriesId!!, it.changeToken!!)) }
                .map {
                    SyndEntryImpl()
                            .apply { title = it!!.key().versionSeriesId }
                            .apply { contents = listOf(SyndContentImpl().apply { value = "ID: " + it!!.content().id + "  SIZE: " + (it.content().length.humanReadable()) }) }
                            .apply { publishedDate = Date(it!!.key().changeToken.timestamp()) }
                            .apply { link = baseUrl + "debug?key=${it!!.key().toUrlString()}" }
                            .apply { author = it!!.content().deletedBy ?: it.content().createdBy }
                }


        val feed = SyndFeedImpl()
        feed.feedType = "atom_1.0"
        feed.title = "$repoid Ladon Bucket"
        feed.entries = lastUpdates
        return SyndFeedOutput().outputString(feed)

    }

}