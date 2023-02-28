package com.amtron.ferryticket.helper

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.amtron.ferryticket.model.User
import com.google.gson.Gson

class Util {
	fun isOnline(context: Context): Boolean {
		val connectivityManager =
			context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
		val capabilities =
			connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
		if (capabilities != null) {
			if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
				Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
				return true
			} else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
				Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
				return true
			} else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
				Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
				return true
			}
		}
		return false
	}

	fun getJwtToken(json: String?): String {
		val user: User = Gson().fromJson(json, User::class.java)
		return "Bearer " + user.token
	}
}