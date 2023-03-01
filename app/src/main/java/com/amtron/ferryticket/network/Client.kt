package com.amtron.ferryticket.network

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.http.*

interface Client {
	//LOGIN API
	@Headers("Accept: application/json")
	@FormUrlEncoded
	@POST("login")
	fun login(
		@Field("mobile") mobile: Long,
		@Field("pin") pin: Int
	): Call<JsonObject>

	//GET PIN API
	@Headers("Accept: application/json")
	@FormUrlEncoded
	@POST("getpin")
	fun getPin(
		@Field("mobile") mobile: Long
	): Call<JsonObject>

	//GET HOME API
	@Headers("Accept: application/json")
	@GET("home")
	fun getHomeData(
		@Header("Authorization") bearer: String
	): Call<JsonObject>

	//MASTER DATA API
	@Headers("Accept: application/json")
	@GET("masterdata")
	fun getMasterData(
		@Header("Authorization") bearer: String
	): Call<JsonObject>

	//PRICE API
	@Headers("Accept: application/json")
	@GET("get-ticket-item-rates")
	fun getPriceDetails(): Call<JsonObject>

	//GET FERRY SERVICES API
	@Headers("Accept: application/json")
	@FormUrlEncoded
	@POST("ferry-service")
	fun getFerries(
		@Header("Authorization") bearer: String,
		@Field("route_id") routeId: Int
	): Call<JsonObject>

	//GET SERVICE API
	@Headers("Accept: application/json")
	@FormUrlEncoded
	@POST("ferry-service/get-service")
	fun getService(
		@Header("Authorization") bearer: String,
		@Field("id") ferryServiceId: Int
	): Call<JsonObject>

	//SCAN API
	@Headers("Accept: application/json")
	@GET("web")
	fun getQR(
		@Query("q") qr: String
	): Call<JsonObject>
}