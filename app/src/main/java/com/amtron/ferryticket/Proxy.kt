package com.amtron.ferryticket

import android.app.Activity
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

class Proxy {

	private val username = "ventureinfotek\\amtron"
	private val password = "U\$er@12345"
	private val url = "https://serviceurl"

	private val proxyPort = 8080
	private val proxyHost = "192.168.153.200" // Airtel

	private val proxyAuthenticator = Authenticator { _, response ->
		val credential = Credentials.basic(username, password)
		response.request.newBuilder()
			.header("Proxy-Authorization", credential)
			.build()
	}

	fun execute() {
		onPreExecute()
		CoroutineScope(Dispatchers.IO).launch {
			val result = doInBackground()
			withContext(Dispatchers.Main) {
				onPostExecute(result)
			}
		}
	}

	private fun doInBackground(): String {
		var res = ""
		try {
			val client = OkHttpClient.Builder()
				.connectTimeout(60L, TimeUnit.SECONDS)
				.writeTimeout(60L, TimeUnit.SECONDS)
				.readTimeout(60L, TimeUnit.SECONDS)
				.proxy(java.net.Proxy(java.net.Proxy.Type.HTTP, InetSocketAddress(proxyHost, proxyPort)))
				.proxyAuthenticator(proxyAuthenticator)
				.protocols(listOf(Protocol.HTTP_1_1))
				.build()

			val formBody: RequestBody = FormBody.Builder().build()

			val request: Request = Request.Builder().url(url).post(formBody).build()
			val response: Response = client.newCall(request).execute()
			res = response.body?.string().orEmpty()
			val rres = response.code
			Log.d("response code: ", rres.toString());
		} catch (e: Exception) {
			res = e.toString()
			e.printStackTrace()
		}
		return res
	}

	private fun onPreExecute() {
		Log.d("msg", "Connecting to Proxy...")
	}

	private fun onPostExecute(result: String) {
		Log.d("msg", result)
	}
}