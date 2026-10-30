package com.dergoogler.mmrl.repository

import com.dergoogler.mmrl.database.entity.Repo
import com.dergoogler.mmrl.model.online.ModulesJson
import com.dergoogler.mmrl.model.online.TrackJson
import com.dergoogler.mmrl.network.NetworkUtils
import com.dergoogler.mmrl.network.runRequest
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.platform.model.ModId
import com.dergoogler.mmrl.stub.IMMRLApiManager
import com.dergoogler.mmrl.stub.IRepoCatalogManager
import com.dergoogler.mmrl.stub.IRepoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.create
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModulesRepository
    @Inject
    constructor(
        private val localRepository: LocalRepository,
    ) {
        suspend fun getLocalAll() =
            withContext(Dispatchers.IO) {
                with(PlatformManager.moduleManager.modules) {
                    localRepository.deleteLocalAll()
                    localRepository.insertLocal(this)
                    localRepository.clearUpdatableTag(map { it.id.id })
                }
            }

        suspend fun getLocal(id: ModId) =
            withContext(Dispatchers.IO) {
                val module = PlatformManager.moduleManager.getModuleById(id)
                localRepository.insertLocal(module)
            }

        suspend fun getRepoAll(onlyEnable: Boolean = true) =
            localRepository
                .getRepoAll()
                .filter {
                    if (onlyEnable) it.enable else true
                }.map {
                    getRepo(it)
                }

        suspend fun getRepo(repo: Repo) =
            withContext(Dispatchers.IO) {
                val url = repo.url
                val isDirectJson = url.substringBefore('?').endsWith(".json", ignoreCase = true)

                runRequest {
                    if (isDirectJson) {
                        fetchDirect(url)
                    } else {
                        IRepoManager.build(url).modules.execute()
                    }
                }.onSuccess { modulesJson ->
                    localRepository.insertRepo(repo.copy(modulesJson))
                    localRepository.deleteOnlineByUrl(url)
                    localRepository.insertOnline(
                        list = modulesJson.modules,
                        repoUrl = url,
                    )
                }.onFailure {
                    Timber.e(it, "getRepo: $url")
                }
            }

        private fun fetchDirect(url: String): ModulesJson {
            val directApi = IRepoManager.buildDirect()
            return try {
                directApi.modulesDirect(url).execute()
            } catch (e: Exception) {
                val catalogApi = NetworkUtils
                    .createRetrofit()
                    .baseUrl("https://raw.githubusercontent.com/")
                    .build()
                    .create<IRepoCatalogManager>()
                val entries = catalogApi.entries(url).execute()
                ModulesJson(
                    modules = entries.map {
                        it.toOnlineModule()
                    },
                )
            }
        }

        suspend fun getBlacklist() =
            withContext(Dispatchers.IO) {
                runRequest {
                    val api = IMMRLApiManager.build()
                    return@runRequest api.blacklist.execute()
                }.onSuccess { blacklist ->
                    blacklist.map {
                        localRepository.deleteBlacklistById(it.id)
                        localRepository.insertBlacklist(it)
                    }
                }.onFailure {
                    Timber.e(it, "getBlacklist: Failed to get blacklist")
                }
            }
    }
