//package app.phonesite.manager.webserver
//
//import com.sun.net.httpserver.HttpServer
//import java.io.IOException
//import java.net.InetSocketAddress
//
//class WebServer {
//    private lateinit var server: HttpServer
//
//    fun startServer() {
//            try {
//                val server = HttpServer.create(InetSocketAddress(8080), 0)
//                server.createContext("/", RootHandler())
//                server.start()
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//    fun stopServer() {
//        server.stop(0)
//    }
//}
//
//class RootHandler : com.sun.net.httpserver.HttpHandler {
//    @Throws(IOException::class)
//    override fun handle(exchange: com.sun.net.httpserver.HttpExchange) {
//        val response = "Hello from your Android web server!"
//        exchange.sendResponseHeaders(200, response.length.toLong())
//        val os = exchange.responseBody
//        os.write(response.toByteArray())
//        os.close()
//    }
//}