package com.programtom.updatebasis

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*

object Request {

    val TAG = "REQUEST"
    val POST = "POST"
    val GET = "GET"
    val charset = "utf-8"

    @Throws(Exception::class)
    fun execute(params: HashMap<String, String>, urlAddr: String, method: String): String {
        var r: BufferedReader? = null
        try {
            var question = true
            var sb = StringBuilder()
            val iterator = params.entries.iterator()
            while (iterator.hasNext()) {
                val type = iterator.next()
                sb.append(if (question) "" else "&")
                question = false

                sb.append(type.key)
                sb.append("=")
                sb.append(URLEncoder.encode(type.value, "UTF-8"))
            }
            LocalLog.d(TAG, "METHOD : $method")
            LocalLog.d(TAG, "CONTENT : $sb")

            val url = URL(urlAddr + if (method == GET) "?$sb" else "")
            LocalLog.d(TAG, "URL : $url")
            val httpCon = url.openConnection() as HttpURLConnection
            if (method == POST) {
                httpCon.doOutput = true

                httpCon.requestMethod = method
                val out = OutputStreamWriter(httpCon.outputStream)

                out.write(sb.toString())
                out.close()
            }
            httpCon.readTimeout = 5000
            r = BufferedReader(InputStreamReader(httpCon.inputStream))

            sb = StringBuilder()
            var line: String?

            do {
                line = r.readLine()
                if(line != null && line != "null")
                sb.append(line).append("\n")
            } while (line != null)
            LocalLog.d(TAG, "RESPONSE : $sb")
            return sb.toString().trim()
        } catch (e: Exception) {
            LocalLog.e(TAG, "URL ADDRESS $urlAddr")
            LocalLog.e(TAG, "HTTP REQUEST METHOD $method")
            LocalLog.e(TAG, "PARAMS $params")
            e.message?.let { LocalLog.e(TAG, it, e) }

            throw e
        } finally {
            if (r != null) {
                try {
                    r.close()
                } catch (e: Exception) {

                }
            }
        }
    }

    fun isConnected(context: Context): Boolean {
        val conMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        var isConnected = false
        @Suppress("DEPRECATION") val networks = conMgr.allNetworkInfo
        for (i in networks.indices) {
            @Suppress("DEPRECATION")
            if (networks[i].state == NetworkInfo.State.CONNECTED) {
                isConnected = true
                break
            }
        }

        return isConnected
    }

    @Throws(Exception::class)
    fun getVersion(url: String): String {
        val params = HashMap<String, String>()
        return execute(params, url, GET)

    }

    @Throws(Exception::class)
    fun download(installFile: String, context: Context): String? {
        val url = URL(installFile)
        var fos: FileOutputStream? = null
        var httpCon: HttpURLConnection? = null
        try {
            httpCon = url.openConnection() as HttpURLConnection
            httpCon.connect()

            val file =
                File(context.cacheDir, installFile.substring(installFile.lastIndexOf("/") + 1))
            fos = FileOutputStream(file)

            val iss = httpCon.inputStream

            val buffer = ByteArray(1024)
            var len1: Int
            do {
                len1 = iss.read(buffer)
                if(len1 != -1)
                fos.write(buffer, 0, len1)
            } while (len1 != -1)
            LocalLog.d(TAG, file.absolutePath)
            return file.absolutePath
        } catch (e: Exception) {
            e.message?.let { LocalLog.e(TAG, it, e) }
        } finally {
            if (fos != null) {
                try {
                    fos.close()
                } catch (e: Exception) {

                }
            }
            if (httpCon != null) {
                try {
                    httpCon.disconnect()
                } catch (e: Exception) {

                }

            }
        }
        return null

    }
}
