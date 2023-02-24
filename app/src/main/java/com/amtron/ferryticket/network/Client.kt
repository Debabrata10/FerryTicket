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
        @Field("mobile") mobile: String,
        @Field("password") password: String
    ): Call<JsonObject>

    //PRICE API
    @Headers("Accept: application/json")
    @GET("get-ticket-item-rates")
    fun getPriceDetails(): Call<JsonObject>
}