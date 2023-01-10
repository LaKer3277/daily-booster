package com.daily.clean.booster.tba

import com.daily.clean.booster.entity.DaiBooIpBean
import okhttp3.RequestBody
import retrofit2.http.*

interface BoosterApi {

    @POST("dialect/cryptic/rototill/pane")
//    @POST("irishman/grandson/pummel")//test
    suspend fun report(
        @HeaderMap headerMap: MutableMap<String, Any> = mutableMapOf(),
        @Body body: RequestBody,
        @QueryMap queryMap: MutableMap<String, Any> = mutableMapOf()
    ): String



    @GET
    suspend fun getIp(@Url url:String = "https://api.myip.com"): DaiBooIpBean
}

