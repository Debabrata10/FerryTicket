package com.amtron.ferryticket.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import okhttp3.Authenticator
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetSocketAddress
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.util.concurrent.TimeUnit

object RetrofitHelper {
	private const val username = "ventureinfotek\\amtron"
	private const val password = "U\$er@12345"
	private const val proxyPort = 8080
	private const val proxyHost = "192.168.153.200" // Airtel
//    String proxyHost = "192.168.99.7"; //Vodafone

//	private const val apiUrl = "https://asiwt.in/StagingServer/api/counter/"
	private const val apiUrl = "http://103.8.249.24/iwtassam/StagingServer/api/counter/"
	private var mClient: OkHttpClient? = null

	private val proxyAuthenticator = Authenticator { _, response ->
		val credential = Credentials.basic(username, password)
		response.request.newBuilder()
			.header("Proxy-Authorization", credential)
			.build()
	}

	private val client: OkHttpClient
		@Throws(NoSuchAlgorithmException::class, KeyManagementException::class)
		get() {
			if (mClient == null) {
				val interceptor = HttpLoggingInterceptor()
				interceptor.level = HttpLoggingInterceptor.Level.BODY

				val httpBuilder = OkHttpClient.Builder()
				httpBuilder
					.connectTimeout(15, TimeUnit.SECONDS)
					.readTimeout(20, TimeUnit.SECONDS)
					.proxy(java.net.Proxy(java.net.Proxy.Type.HTTP, InetSocketAddress(proxyHost, proxyPort)))
					.proxyAuthenticator(proxyAuthenticator)
					.protocols(listOf(Protocol.HTTP_1_1))
					.addInterceptor(interceptor) // show all JSON in logCat
				mClient = httpBuilder.build()

			}
			return mClient!!
		}

	private val clientWithoutProxy: OkHttpClient
		@Throws(NoSuchAlgorithmException::class, KeyManagementException::class)
		get() {
			if (mClient == null) {
				val interceptor = HttpLoggingInterceptor()
				interceptor.level = HttpLoggingInterceptor.Level.BODY

				val httpBuilder = OkHttpClient.Builder()
				httpBuilder
					.connectTimeout(15, TimeUnit.SECONDS)
					.readTimeout(20, TimeUnit.SECONDS)
					.addInterceptor(interceptor)  // show all JSON in logCat
				mClient = httpBuilder.build()

			}
			return mClient!!
		}

	fun getInstance(context: Context): Retrofit? {
		var retrofit: Retrofit? = null
		val connectivityManager =
			context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
		val capabilities =
			connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
		if (capabilities!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
			retrofit = Retrofit.Builder()
				.baseUrl(apiUrl)
				.client(client)
				.addConverterFactory(GsonConverterFactory.create()).build()
		} else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
			retrofit = Retrofit.Builder()
				.baseUrl(apiUrl)
				.client(clientWithoutProxy)
				.addConverterFactory(GsonConverterFactory.create()).build()
		}
		return retrofit
	}

	class ForJava {
		companion object {
			fun getInstance(): Retrofit {
				return Retrofit.Builder()
					.baseUrl(apiUrl)
					.client(client)
					.addConverterFactory(GsonConverterFactory.create()).build()
			}
		}
	}
}