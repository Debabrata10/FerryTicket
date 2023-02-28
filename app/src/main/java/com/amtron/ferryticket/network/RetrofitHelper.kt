package com.amtron.ferryticket.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.util.concurrent.TimeUnit

object RetrofitHelper {
	private const val apiUrl = "https://asiwt.in/api/counter/"
	private const val scanUrl = "https://tokapoisa.in/"
	private var mClient: OkHttpClient? = null

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
					.addInterceptor(interceptor)  /// show all JSON in logCat
				mClient = httpBuilder.build()

			}
			return mClient!!
		}

	fun getInstance(): Retrofit {
		return Retrofit.Builder()
			.baseUrl(apiUrl)
			.client(client)
			.addConverterFactory(GsonConverterFactory.create()).build()
	}

	fun getInstanceForScan(): Retrofit {
		return Retrofit.Builder()
			.baseUrl(scanUrl)
			.client(client)
			.addConverterFactory(GsonConverterFactory.create()).build()
	}
}