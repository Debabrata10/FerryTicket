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
	@FormUrlEncoded
	@POST("card-details")
	fun getCardDetailsByScan(
		@Header("Authorization") bearer: String,
		@Field("card_id") card_id: String
	): Call<JsonObject>

	//BOOK TICKET API
	@Headers("Accept: application/json")
	@FormUrlEncoded
	@POST("book-ticket")
	fun bookTicket(
		@Header("Authorization") bearer: String,
		@Field("ferry_service_id") ferryServiceId: Int,
		@Field("passenger_data") passenger: String,
		@Field("vehicle_data") vehicle: String,
		@Field("other_data") others: String
	): Call<JsonObject>

	//GENERATE WALLET ORDER API
	@Headers("Accept: application/json")
	@FormUrlEncoded
	@POST("wallet-order")
	fun generateWalletOrder(
		@Header("Authorization") bearer: String,
		@Field("card_id") cardId: Int,
		@Field("id") bookingId: Int
	): Call<JsonObject>

	//CONFIRM WALLET ORDER API
	@Headers("Accept: application/json")
	@FormUrlEncoded
	@POST("wallet-order-confirm")
	fun verifyPin(
		@Header("Authorization") bearer: String,
		@Field("id") orderId: Int,
		@Field("pin") otp: Int
	): Call<JsonObject>

	//GET TID API
	@Headers("Accept: application/json")
	@FormUrlEncoded
	@POST("device-tid")
	fun getTid(
		@Header("Authorization") bearer: String?,
		@Field("device_sl_no") serialNumber: Long
	): Call<JsonObject>

	//GET APP VERSION API
	@Headers("Accept: application/json")
	@FormUrlEncoded
	@POST("app-version")
	fun getAppVersion(
		@Field("app_type") type: String
	): Call<JsonObject>

	//GET CARD DETAILS BY INPUT API
	@Headers("Accept: application/json")
	@FormUrlEncoded
	@POST("card-details-by-code")
	fun getCardDetailsByInput(
		@Header("Authorization") bearer: String,
		@Field("card_code") card_code: String
	): Call<JsonObject>

	//GET UPDATED BALANCE FOR OPERATOR WALLET AFTER PAYMENT
	@Headers("Accept: application/json")
	@POST("operator-card-details")
	fun getOperatorUpdatedAfterPayment(
		@Header("Authorization") bearer: String
	): Call<JsonObject>

	//TO SEND POS DATA TO SERVER
	@Headers("Accept: application/json")
	@FormUrlEncoded
	@POST("pos-payment")
	fun sendPosDataToServer(
		@Header("Authorization") bearer: String,
		@Field("amount") amount: Double,
		@Field("in_app_date") inAppDate: String,
		@Field("in_app_time") inAppTime: String,
		@Field("invoice") invoice: String,
		@Field("rrn") rrn: String,
		@Field("card_no") cardNo: String,
		@Field("card_type") cardType: String,
		@Field("auth_code") authCode: String,
		@Field("booking_id") bookingId: Int,
		@Field("tid") tid: String
	): Call<JsonObject>

	//GET REPORT
	@Headers("Accept: application/json")
	@FormUrlEncoded
	@POST("report")
	fun getReport(
		@Header("Authorization") bearer: String,
		@Field("route_id") route_id: Int
	): Call<JsonObject>
}