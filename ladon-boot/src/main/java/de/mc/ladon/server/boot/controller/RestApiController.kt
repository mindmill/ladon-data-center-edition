//package de.mc.ladon.server.boot.controller
//
//import de.mc.ladon.rest.api.RestApi
//import de.mc.ladon.rest.api.model.DefaultResponse
//import de.mc.ladon.rest.api.model.Document
//import de.mc.ladon.server.core.api.LadonRepository
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.core.io.InputStreamResource
//import org.springframework.core.io.Resource
//import org.springframework.http.HttpStatus
//import org.springframework.http.ResponseEntity
//import org.springframework.security.core.context.SecurityContextHolder
//import org.springframework.web.bind.annotation.*
//
//@RestController
//open class RestApiController : RestApi {
//    @Autowired
//    lateinit var ladonRepository: LadonRepository
//
//
//    fun getUserId() = SecurityContextHolder.getContext().authentication.name
//
//    fun <T> response(body: () -> T) = ResponseEntity(body(), HttpStatus.OK)
//
//    override fun createNewBucket(@PathVariable("bucket") bucket: String) = response {
//        ladonRepository.createNewBucket(getUserId(), bucket)
//        DefaultResponse().message("success")
//    }
//
//
//    override fun deleteDocument(@PathVariable("bucket") bucket: String,
//                                @PathVariable("key") key: String) = response {
//        ladonRepository.deleteDocument(getUserId(), bucket, key).toApi()
//    }
//
//    override fun deleteDocumentVersion(@PathVariable("bucket") bucket: String,
//                                       @PathVariable("key") key: String,
//                                       @PathVariable("version") version: String) = response {
//        ladonRepository.deleteDocumentVersion(getUserId(), bucket, key, version).toApi()
//    }
//
//    override fun deleteEmptyBucket(@PathVariable("bucket") bucket: String): ResponseEntity<DefaultResponse> {
//        return response {
//            ladonRepository.deleteEmptyBucket(getUserId(), bucket)
//            DefaultResponse().message("success")
//        }
//    }
//
//    override fun getDocument(@PathVariable("bucket") bucket: String,
//                             @PathVariable("key") key: String) = response {
//        ladonRepository.getDocument(getUserId(), bucket, key)
//                .let { InputStreamResource(it.content) as Resource }
//    }
//
//
//    override fun getDocumentVersion(@PathVariable("bucket") bucket: String,
//                                    @PathVariable("key") key: String,
//                                    @PathVariable("version") version: String) = response {
//        ladonRepository.getDocumentVersion(getUserId(), bucket, key, version)
//                .let { InputStreamResource(it.content) as Resource }
//    }
//
//    override fun getDocumentVersionMetadata(@PathVariable("bucket") bucket: String,
//                                            @PathVariable("key") key: String,
//                                            @PathVariable("version") version: String) = response {
//        ladonRepository.getDocumentVersionMetadata(getUserId(), bucket, key, version).toApi()
//    }
//
//
//    override fun getMetadata(@PathVariable("bucket") bucket: String,
//                             @PathVariable("key") key: String) = response {
//        ladonRepository.getMetadata(getUserId(), bucket, key).toApi()
//    }
//
//    override fun listBuckets() = response {
//        ladonRepository.listBuckets(getUserId())
//    }
//
//
//    override fun listDocumentVersions(@PathVariable("bucket") bucket: String,
//                                      @PathVariable("key") key: String) = response {
//        ladonRepository.listDocumentVersions(getUserId(), bucket, key).map { it.toApi() }
//    }
//
//    override fun listDocuments(@PathVariable("bucket") bucket: String,
//                               @RequestParam("limit") limit: Long?,
//                               @RequestParam("prefix") prefix: String?,
//                               @RequestParam("marker") marker: String?,
//                               @RequestParam("delimiter") delimiter: String?) = response {
//        ladonRepository.listDocuments(getUserId(), bucket, prefix,marker,delimiter,limit).map { it.toApi() }
//    }
//
//    @RequestMapping(
//            value = ["/rest/v1/{bucket}/{key:.+}"],
//            produces = ["application/json"],
//            consumes = ["application/octet-stream", "application/_*", "text/plain"],
//            method = [RequestMethod.PUT])
//    override fun putDocument(@PathVariable("bucket") bucket: String,
//                             @PathVariable("key") key: String,
//                             @RequestBody content: Resource) = response {
//        ladonRepository.putDocument(getUserId(), bucket, key, content.inputStream).toApi()
//    }
//
//
//    override fun updateDocumentMetadata(@PathVariable("bucket") bucket: String,
//                                        @PathVariable("key") key: String,
//                                        @PathVariable("version") version: String,
//                                        @RequestBody meta: Map<String, String>): ResponseEntity<Document> {
//        return super.updateDocumentMetadata(bucket, key, version, meta)
//    }
//
//    private fun de.mc.ladon.server.core.api.Document.toApi(): Document {
//        return Document()
//                .bucket(bucket)
//                .key(key)
//                .version(version)
//                .contentType(contentType)
//                .created(created.toString())
//                .latest(latest)
//                .etag(etag)
//                .size(size)
//                .usermeta(userMetadata)
//    }
//}
