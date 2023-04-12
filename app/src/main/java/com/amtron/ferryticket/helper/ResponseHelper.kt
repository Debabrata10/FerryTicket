package com.amtron.ferryticket.helper

import android.annotation.SuppressLint
import com.amtron.ferryticket.model.ResponseData
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser

class ResponseHelper {
	private lateinit var jsonObject: JsonObject

	@SuppressLint("NotConstructor")
	fun responseHelper(jsonObject: JsonObject?) {
		this.jsonObject = jsonObject!!
	}

	fun isStatusSuccessful(): Boolean {
		return jsonObject["status"].toString().toInt() == 1
	}

	fun getDataAsString(): String {
		return jsonObject["data"].toString()
	}

	fun getData(): ResponseData<*>? {
		val gson = Gson()
		return gson.fromJson(jsonObject.toString(), ResponseData::class.java)
	}

	fun getErrorMsg(): String {
		return jsonObject["msg"].toString()
			.substring(1, jsonObject["msg"].toString().length - 1)
	}

	fun parseFromJsonObject(jsonLine: String?, translatedText: String?): String? {
		val jElement = JsonParser().parse(jsonLine)
		var jObject = jElement.asJsonObject
		jObject = jObject.getAsJsonObject("data")
		/*val jArray = jObject.getAsJsonArray("translations")
		jObject = jArray[0].asJsonObject*/
		return jObject[translatedText].asString
	}

	fun getStringValueFromJsonObject(jsonLine: String?, translatedText: String?): String {
		val jElement = JsonParser().parse(jsonLine)
		val jObject = jElement.asJsonObject
		return jObject.asJsonObject.getAsJsonPrimitive(translatedText).toString()
	}
}