package app.phonesite.manager

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    private var serverUp = false
    private lateinit var serverButton: Button
    private lateinit var serverTextView: TextView

    private val PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        serverButton = findViewById(R.id.serverButton);
        serverTextView = findViewById(R.id.serverTextView);
        val port = 5000

        if (checkPermission()) {
            // Permission already granted, you can access the file directory here
            serverButton.setOnClickListener {
                serverUp = if (!serverUp) {
                    startServer(port)
                    true
                } else {
                    stopServer()
                    false
                }
            }
        } else {
            requestPermission()
        }

    }

    private fun checkPermission(): Boolean {
        // Check if the permission has already been granted
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        // Request the permission
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can access the file directory here
                Log.d("MainActivity", "Location accessible now");
            } else {
                // Permission denied, handle the case when the user denies the permission
                // You may want to show a message or disable certain functionality
            }
        }
    }


//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }


    private fun streamToString(inputStream: InputStream): String {
        val s = Scanner(inputStream).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }

    private fun sendResponse(httpExchange: HttpExchange, responseText: String) {
        httpExchange.sendResponseHeaders(200, responseText.length.toLong())
        val os = httpExchange.responseBody
        os.write(responseText.toByteArray())
        os.close()
    }

    private var mHttpServer: HttpServer? = null


    // basic handling
//    private fun startServer(port: Int) {
//        try {
//            mHttpServer = HttpServer.create(InetSocketAddress(port), 0)
//            mHttpServer!!.executor = Executors.newCachedThreadPool()
//
//            mHttpServer!!.createContext("/", rootHandler)
//            mHttpServer!!.createContext("/index", rootHandler)
//            // Handle /messages endpoint
//            mHttpServer!!.createContext("/messages", messageHandler)
//            mHttpServer!!.start()//startServer server;
//            serverTextView.text = getString(R.string.server_running)
//            serverButton.text = getString(R.string.stop_server)
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//
//    }

    private fun startServer(port: Int) {
        try {
            mHttpServer = HttpServer.create(InetSocketAddress(port), 0)
            mHttpServer!!.executor = Executors.newCachedThreadPool()

            // Define the root directory where your files are stored
            val fileRoot = File(getFilesDir(), "path_to_your_file_directory")

            // Create a custom file handler to serve files from the fileRoot directory
            val fileHandler = object : HttpHandler {
                override fun handle(exchange: HttpExchange) {
                    val requestPath = exchange.requestURI.path
                    val file = File(fileRoot, requestPath)

                    if (file.exists() && file.isFile) {
                        // Serve the requested file
                        val responseHeaders = exchange.responseHeaders
                        responseHeaders["Content-Type"] = getMimeType(file)
                        // Set Cache-Control header based on file type
                        if (isImage(file)) {
                            responseHeaders["Cache-Control"] = "public, max-age=3600" // Cache images for 1 hour
                        } else {
                            responseHeaders["Cache-Control"] = "no-cache, no-store, must-revalidate" // Don't cache other files
                        }
                        exchange.sendResponseHeaders(200, file.length())

                        val outputStream = exchange.responseBody
                        val inputStream = FileInputStream(file)
                        inputStream.copyTo(outputStream)
                        outputStream.close()
                        inputStream.close()
                    } else {
                        // File not found, send a 404 response
                        val response = "File not found."
                        exchange.sendResponseHeaders(404, response.length.toLong())
                        val outputStream = exchange.responseBody
                        outputStream.write(response.toByteArray())
                        outputStream.close()
                    }
                }
            }

            mHttpServer!!.createContext("/", fileHandler)
            mHttpServer!!.start()
            serverTextView.text = getString(R.string.server_running)
            serverButton.text = getString(R.string.stop_server)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun isImage(file: File): Boolean {
        val imageExtensions = setOf("png", "jpg", "jpeg", "gif", "bmp")
        return file.extension.toLowerCase() in imageExtensions
    }

    private fun stopServer() {
        if (mHttpServer != null) {
            mHttpServer!!.stop(0)
            serverTextView.text = getString(R.string.server_down)
            serverButton.text = getString(R.string.start_server)
        }
    }

    // Handler for root endpoint
    private val rootHandler = HttpHandler { exchange ->
        run {
            // Get request method
            when (exchange!!.requestMethod) {
                "GET" -> {
                    sendResponse(exchange, "Welcome to my server")
                }
            }
        }

    }

    private val messageHandler = HttpHandler { httpExchange ->
        run {
            when (httpExchange!!.requestMethod) {
                "GET" -> {
                    // Get all messages
                    sendResponse(httpExchange, "Would be all messages stringified json")
                }
                "POST" -> {
                    val inputStream = httpExchange.requestBody

                    val requestBody = streamToString(inputStream)
                    val jsonBody = JSONObject(requestBody)
                    // save message to database

                    //for testing
                    sendResponse(httpExchange, jsonBody.toString())

                }

            }
        }
    }

    private fun getMimeType(file: File): String {
        // Add more MIME types based on your file types
        return when (file.extension.toLowerCase()) {
            "html" -> "text/html"
            "css" -> "text/css"
            "js" -> "application/javascript"
            "json" -> "application/json"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            "gif" -> "image/gif"
            else -> "application/octet-stream"
        }
    }
}