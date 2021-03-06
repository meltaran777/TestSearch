package com.bohdan.khristov.textsearch.data.repository

import com.bohdan.khristov.textsearch.util.L
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

class SearchRepository : ISearchRepository {

    override suspend fun getText(url: String): String {
        var urlConnection: HttpURLConnection? = null
        var result = ""
        try {
            val url = URL(url)
            urlConnection = url.openConnection() as HttpURLConnection
            val code = urlConnection.responseCode
            L.log("SearchRepository", "Code = $code")
            if (code == 200) {
                val stream = BufferedInputStream(urlConnection.inputStream)
                val bufferedReader = BufferedReader(InputStreamReader(stream))
                var line: String? = null
                while ({ line = bufferedReader.readLine(); line }() != null) {
                    L.log("SearchRepository", "line = $line")
                    result += line
                }
                stream.close()
            }
            return result
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            urlConnection?.disconnect()
        }
        return result
    }
}