package com.dergoogler.mmrl.stub

import com.dergoogler.mmrl.model.online.RepoCatalogEntry
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface IRepoCatalogManager {
    @GET
    fun entries(@Url url: String): Call<List<RepoCatalogEntry>>
}
