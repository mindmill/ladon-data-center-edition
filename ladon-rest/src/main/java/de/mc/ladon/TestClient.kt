//package de.mc.ladon
//
//import de.mc.ladon.client.rest.ApiClient
//import de.mc.ladon.client.rest.api.BucketsApi
//import de.mc.ladon.client.rest.api.DocumentsApi
//import java.io.File
//
//fun main(args: Array<String>) {
//    ApiClient().setBasePath("http://localhost:8080/admin").apply {
//        setUsername("admin")
//        setPassword("admin123")
//    }.let {
//        DocumentsApi(it)
//    }.apply {
//        try {
//            putDocument("test","geht.pdf",File("/home/ralf/Downloads/Angebot_Ladon_2018-12-18.pdf"))
//        } catch (e:Exception) {
//            println(e)
//        }
//    }
//}
