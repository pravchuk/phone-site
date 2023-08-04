package app.phonesite.manager

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.URL
import java.util.*
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private var serverUp = false
    private lateinit var serverButton: Button
    private lateinit var serverTextView: TextView


    private val handler = Handler(Looper.getMainLooper())
    private val pingIntervalMillis: Long = 10000 // 10 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        serverButton = findViewById(R.id.serverButton);
        serverTextView = findViewById(R.id.serverTextView);
        val port = 5000

        serverButton.setOnClickListener {
            serverUp = if (!serverUp) {
                showWebsiteInputDialog(port)
                true
            } else {
                stopServer()
                stopPingingServer()
                false
            }

        }

    }

    private fun showWebsiteInputDialog(port: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_input_website, null)
        val websiteEditText = dialogView.findViewById<EditText>(R.id.websiteEditText)
        val submitButton = dialogView.findViewById<Button>(R.id.submitButton)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Enter Website URL")
            .setView(dialogView)
            .setCancelable(false)
            .create()

        submitButton.setOnClickListener {
            val websiteName = websiteEditText.text.toString().trim()
            if (websiteName.isNotEmpty()) {
                startServer(port)
                startPingingServer(websiteName)
                dialog.dismiss()
            } else {
                websiteEditText.error = "Website URL cannot be empty."
            }
        }

        dialog.show()
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        menuInflater.inflate(R.menu.menu_main, menu)
//        return true
//    }

    private fun startPingingServer(websiteUrl: String) {
        pingRunnable.websiteUrl = websiteUrl
        handler.postDelayed(pingRunnable, pingIntervalMillis)
    }

    private fun stopPingingServer() {
        handler.removeCallbacks(pingRunnable)
    }

    private val pingRunnable = object : Runnable {
        var websiteUrl: String = ""

        override fun run() {
            // Send HTTP request to your server here
            // For example, you can use the Java HttpURLConnection
            Thread {
                pingServer(websiteUrl)
                handler.postDelayed(this, pingIntervalMillis)
            }.start()
        }
    }

    private fun pingServer(websiteUrl: String) {
        try {
            val serverAddress =
                URL("http://[2400:6180:100:d0::873:5001]:999/ping") // Replace with your server URL
            val websiteName = "website=" + websiteUrl
            val urlString = "$serverAddress?$websiteName"

            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Handle successful response here if needed
            } else {
                // Handle error response here if needed
            }
            connection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle exception here if needed
        }
    }

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


    private fun startServer(port: Int) {
        try {
            mHttpServer = HttpServer.create(InetSocketAddress(port), 0)
            mHttpServer!!.executor = Executors.newCachedThreadPool()

            mHttpServer!!.createContext("/", rootHandler)
            mHttpServer!!.createContext("/index", rootHandler)
            // Handle /messages endpoint
            mHttpServer!!.createContext("/messages", messageHandler)
            mHttpServer!!.start()//startServer server;
            serverTextView.text = getString(R.string.server_running)
            serverButton.text = getString(R.string.stop_server)
        } catch (e: IOException) {
            e.printStackTrace()
        }

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
                    sendResponse(exchange, BAKERY_HTML)
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
}


var TECH_VERSE_LANDING_PAGE = "<!DOCTYPE html>\n" +
        "<html lang=\"en\">\n" +
        "<head>\n" +
        "    <meta charset=\"UTF-8\">\n" +
        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
        "    <title>Techverse Conference</title>\n" +
        "    <style>\n" +
        "        body {\n" +
        "            font-family: Arial, sans-serif;\n" +
        "            margin: 0;\n" +
        "            padding: 0;\n" +
        "            background-color: #f5f5f5;\n" +
        "        }\n" +
        "\n" +
        "        header {\n" +
        "            background-color: #3498db;\n" +
        "            color: #fff;\n" +
        "            padding: 20px;\n" +
        "            text-align: center;\n" +
        "        }\n" +
        "\n" +
        "        h1 {\n" +
        "            margin: 0;\n" +
        "        }\n" +
        "\n" +
        "        section {\n" +
        "            padding: 30px;\n" +
        "            text-align: center;\n" +
        "        }\n" +
        "\n" +
        "        .cta-button {\n" +
        "            background-color: #e74c3c;\n" +
        "            color: #fff;\n" +
        "            padding: 10px 20px;\n" +
        "            border: none;\n" +
        "            border-radius: 4px;\n" +
        "            font-size: 16px;\n" +
        "            cursor: pointer;\n" +
        "        }\n" +
        "    </style>\n" +
        "</head>\n" +
        "<body>\n" +
        "    <header>\n" +
        "        <h1>Techverse Conference</h1>\n" +
        "        <p>Join us at the most exciting tech event of the year!</p>\n" +
        "    </header>\n" +
        "\n" +
        "    <section>\n" +
        "        <h2>About Techverse</h2>\n" +
        "        <p>Techverse is an annual conference bringing together the brightest minds in technology and innovation. Our goal is to foster collaboration, share knowledge, and inspire new ideas.</p>\n" +
        "    </section>\n" +
        "\n" +
        "    <section>\n" +
        "        <h2>Event Details</h2>\n" +
        "        <p>Date: October 15-17, 2023</p>\n" +
        "        <p>Location: Convention Center, 123 Tech Street, Silicon Valley</p>\n" +
        "        <p>Early Bird Tickets: \$99 (Limited availability)</p>\n" +
        "        <button class=\"cta-button\" onclick=\"registerNow()\">Register Now</button>\n" +
        "    </section>\n" +
        "\n" +
        "    <section>\n" +
        "        <h2>Featured Speakers</h2>\n" +
        "        <ul>\n" +
        "            <li>John Doe - CEO of Techverse Corp</li>\n" +
        "            <li>Jane Smith - AI Researcher</li>\n" +
        "            <li>Michael Johnson - Cybersecurity Expert</li>\n" +
        "        </ul>\n" +
        "    </section>\n" +
        "\n" +
        "    <script>\n" +
        "        function registerNow() {\n" +
        "            alert(\"Registration successful! We look forward to seeing you at Techverse!\");\n" +
        "        }\n" +
        "    </script>\n" +
        "</body>\n" +
        "</html>";

var BAKERY_HTML = "<!DOCTYPE html>\n" +
        "<html lang=\"en\">\n" +
        "<head>\n" +
        "    <meta charset=\"UTF-8\">\n" +
        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
        "    <title>Delicious Bakery Store</title>\n" +
        "    <style>\n" +
        "        body {\n" +
        "            font-family: Arial, sans-serif;\n" +
        "            margin: 0;\n" +
        "            padding: 0;\n" +
        "            background-color: #f5f5f5;\n" +
        "        }\n" +
        "\n" +
        "        header {\n" +
        "            background-color: #e67e22;\n" +
        "            color: #fff;\n" +
        "            padding: 20px;\n" +
        "            text-align: center;\n" +
        "        }\n" +
        "\n" +
        "        h1 {\n" +
        "            margin: 0;\n" +
        "        }\n" +
        "\n" +
        "        section {\n" +
        "            padding: 30px;\n" +
        "            text-align: center;\n" +
        "        }\n" +
        "\n" +
        "        .item {\n" +
        "            display: inline-block;\n" +
        "            width: 200px;\n" +
        "            margin: 10px;\n" +
        "            padding: 10px;\n" +
        "            background-color: #fff;\n" +
        "            border: 1px solid #ddd;\n" +
        "            border-radius: 4px;\n" +
        "        }\n" +
        "\n" +
        "        .cart {\n" +
        "            text-align: right;\n" +
        "        }\n" +
        "\n" +
        "        .cart-item {\n" +
        "            margin: 5px;\n" +
        "        }\n" +
        "\n" +
        "        .cart-button {\n" +
        "            background-color: #e74c3c;\n" +
        "            color: #fff;\n" +
        "            padding: 10px 20px;\n" +
        "            border: none;\n" +
        "            border-radius: 4px;\n" +
        "            font-size: 16px;\n" +
        "            cursor: pointer;\n" +
        "        }\n" +
        "    </style>\n" +
        "</head>\n" +
        "<body>\n" +
        "    <header>\n" +
        "        <h1>Delicious Bakery Store</h1>\n" +
        "        <p>Welcome to our bakery! Browse our tasty treats and add them to your cart.</p>\n" +
        "    </header>\n" +
        "\n" +
        "    <section>\n" +
        "        <div class=\"item\">\n" +
        "            <h2>Bread</h2>\n" +
        "            <p>Price: \$3.99</p>\n" +
        "            <button class=\"cart-button\" onclick=\"addToCart('Bread', 3.99)\">Add to Cart</button>\n" +
        "        </div>\n" +
        "\n" +
        "        <div class=\"item\">\n" +
        "            <h2>Biscuits</h2>\n" +
        "            <p>Price: \$2.49</p>\n" +
        "            <button class=\"cart-button\" onclick=\"addToCart('Biscuits', 2.49)\">Add to Cart</button>\n" +
        "        </div>\n" +
        "\n" +
        "        <div class=\"item\">\n" +
        "            <h2>Cake</h2>\n" +
        "            <p>Price: \$12.99</p>\n" +
        "            <button class=\"cart-button\" onclick=\"addToCart('Cake', 12.99)\">Add to Cart</button>\n" +
        "        </div>\n" +
        "    </section>\n" +
        "\n" +
        "    <section class=\"cart\">\n" +
        "        <h2>Your Cart</h2>\n" +
        "        <div class=\"cart-items\">\n" +
        "            <!-- Cart items will be added here dynamically using JavaScript -->\n" +
        "        </div>\n" +
        "        <button class=\"cart-button\" onclick=\"checkout()\">Checkout</button>\n" +
        "    </section>\n" +
        "\n" +
        "    <script>\n" +
        "        let cartItems = [];\n" +
        "\n" +
        "        function addToCart(itemName, itemPrice) {\n" +
        "            cartItems.push({ name: itemName, price: itemPrice });\n" +
        "            updateCart();\n" +
        "        }\n" +
        "\n" +
        "        function updateCart() {\n" +
        "            const cartItemsElement = document.querySelector('.cart-items');\n" +
        "            cartItemsElement.innerHTML = '';\n" +
        "\n" +
        "            cartItems.forEach((item) => {\n" +
        "                const cartItemElement = document.createElement('div');\n" +
        "                cartItemElement.className = 'cart-item';\n" +
        "                cartItemElement.innerHTML = `\${item.name} - \$\${item.price.toFixed(2)}`;\n" +
        "                cartItemsElement.appendChild(cartItemElement);\n" +
        "            });\n" +
        "        }\n" +
        "\n" +
        "        function checkout() {\n" +
        "            let total = 0;\n" +
        "            cartItems.forEach((item) => {\n" +
        "                total += item.price;\n" +
        "            });\n" +
        "\n" +
        "            alert(`Thank you for your purchase! Your total is \$\${total.toFixed(2)}`);\n" +
        "            cartItems = [];\n" +
        "            updateCart();\n" +
        "        }\n" +
        "    </script>\n" +
        "</body>\n" +
        "</html>\n"