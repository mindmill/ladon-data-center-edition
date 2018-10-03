package de.mc.ladon.server.plugin.runtime

import de.mc.ladon.server.core.api.persistence.dao.BinaryDataDAO
import de.mc.ladon.server.core.api.persistence.dao.ChangeTokenDAO
import de.mc.ladon.server.core.api.persistence.dao.MetadataDAO
import de.mc.ladon.server.core.api.persistence.entities.Metadata
import de.mc.ladon.server.core.config.BoxConfig
import de.mc.ladon.server.core.request.impl.SystemCallContext
import de.mc.ladon.server.core.util.PathUtils.getSystemDir
import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named


@Named
class SystemRepoSync @Inject constructor(val changeTokenDAO: ChangeTokenDAO,
                                         val binaryDataDAO: BinaryDataDAO,
                                         val metadataDAO: MetadataDAO) {

    val log = LoggerFactory.getLogger(javaClass)
    val pluginDir = Paths.get(getSystemDir(), "plugins")
    var lastToken: UUID? = UUID.randomUUID()

    init {
        if (!Files.exists(pluginDir)) Files.createDirectories(pluginDir)
        val ex = Executors.newSingleThreadScheduledExecutor()
        ex.scheduleAtFixedRate({ sync() }, 30, 5, TimeUnit.SECONDS)
        Runtime.getRuntime().addShutdownHook(Thread { ex.shutdown() })
    }

    private fun downloadFile(path: Path, meta: Metadata) {
        if (!Files.exists(path) && !path.toFile().isDirectory) {
            Files.createDirectories(path.parent)
            Files.createFile(path)
        }
        if (!path.toFile().isDirectory) {
            binaryDataDAO.getContentStream(SystemCallContext(), meta.key().repositoryId, meta.content().id, null, null)
                    .use { input ->
                        path.toFile().outputStream().use {
                            input?.copyTo(it)
                        }
                    }
        }
    }


    private fun hasChanges(): Boolean {
        val latestChangeToken = changeTokenDAO.getLatestChangeToken(SystemCallContext(), BoxConfig.SYSTEM_REPO)
                .firstOrNull()?.changeToken
        val changed = latestChangeToken != lastToken
        lastToken = latestChangeToken
        return changed
    }

    fun sync() {
        try {
            if (hasChanges()) {
                log.info("running system repo sync")
                val (list, _) = metadataDAO.listAllMetadata(SystemCallContext(), BoxConfig.SYSTEM_REPO, "plugins/", null, null, Int.MAX_VALUE)
                list.first.forEach {
                    val name = it.key().versionSeriesId
                    val path = Paths.get(getSystemDir(), name)
                    if (Files.exists(path)) {
                        if (Files.size(path) != it.content().length) {
                            downloadFile(path, it)
                        }
                    } else {
                        downloadFile(path, it)
                    }
                }

                val allKeys = list.first.map { it.key().versionSeriesId }


                Files.newDirectoryStream(pluginDir).forEach {
                    val key = Paths.get(getSystemDir()).relativize(it).toString()
                    if (key !in allKeys) {
                        log.info("removing plugin resource $key")
                        Files.delete(it)
                    }
                }
                log.info("finished system repo sync")
            }
        } catch (e: Exception) {
            log.error("system repo sync error", e)
        }
    }

}