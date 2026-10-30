package com.dergoogler.mmrl.stub

import com.dergoogler.mmrl.model.online.ModulesJson
import com.dergoogler.mmrl.network.NetworkUtils
import retrofit2.Call
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Url

interface IRepoManager {
    @get:GET("json/modules.json")
    val modules: Call<ModulesJson>

    @GET
    fun modulesDirect(@Url url: String): Call<ModulesJson>

    companion object {
        fun build(repoUrl: String): IRepoManager =
            NetworkUtils
                .createRetrofit()
                .baseUrl(repoUrl)
                .build()
                .create()

        fun buildDirect(): IRepoManager =
            NetworkUtils
                .createRetrofit()
                .baseUrl("https://raw.githubusercontent.com/")
                .build()
                .create()
    }
}
