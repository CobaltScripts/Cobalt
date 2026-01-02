package org.cobalt.api.util

import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URI

/**
 * Opens an HTTP connection to the specified URL and returns its input stream.
 * 
 * This function sets up a GET request with the `Cobalt` user agent and configurable
 * timeout and caching options.
 * 
 * Implementation from OdinFabric by odtheking
 * Original work: https://github.com/odtheking/OdinFabric
 *
 * @param url The URL to request (must be a valid HTTP/HTTPS URL)
 * @param timeout Connection and read timeout in milliseconds (default: 5000ms)
 * @param useCaches Whether to use HTTP caching (default: true)
 * @return InputStream for reading the response data
 * @throws java.net.MalformedURLException if the URL is invalid
 * @throws java.io.IOException if the connection fails or times out
 * 
 * @author Odin Contributors
 */
fun setupConnection(url: String, timeout: Int = 5000, useCaches: Boolean = true): InputStream {
  val connection = URI(url).toURL().openConnection() as HttpURLConnection
  connection.setRequestMethod("GET")
  connection.setUseCaches(useCaches)
  connection.addRequestProperty("User-Agent", "Cobalt")
  connection.setReadTimeout(timeout)
  connection.setConnectTimeout(timeout)
  connection.setDoOutput(true)
  return connection.inputStream
}