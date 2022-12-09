package com.darkdemon

import com.darkdemon.SoraJioTVExtractor.invokeFH
import com.darkdemon.SoraJioTVExtractor.invokeFS
import com.darkdemon.SoraJioTVExtractor.invokeGDL
import com.darkdemon.SoraJioTVExtractor.invokeI
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.AppUtils.parseJson
import com.lagradost.cloudstream3.utils.AppUtils.toJson
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.darkdemon.SoraJioTVExtractor.invokeNP
import com.darkdemon.SoraJioTVExtractor.invokeRPK
import com.darkdemon.SoraJioTVExtractor.invokeS
import com.darkdemon.SoraJioTVExtractor.invokeTS

open class SoraJioTVProvider : MainAPI() { // all providers must be an instance of MainAPI
    override var name = "SoraJioTV"
    override val hasMainPage = true
    override val hasChromecastSupport = true
    override var lang = "hi"
    override val supportedTypes = setOf(
        TvType.Live
    )

    data class Channels(
        @JsonProperty("result") var result: ArrayList<Result> = arrayListOf()
    )

    data class Result(
        @JsonProperty("channel_id") var channelId: Int? = null,
        @JsonProperty("channel_order") var channelOrder: String? = null,
        @JsonProperty("channel_name") var channelName: String? = null,
        @JsonProperty("channelCategoryId") var channelCategoryId: Int? = null,
        @JsonProperty("channelLanguageId") var channelLanguageId: Int? = null,
        @JsonProperty("isHD") var isHD: Boolean? = null,
        @JsonProperty("broadcasterId") var broadcasterId: Int? = null,
        @JsonProperty("logoUrl") var logoUrl: String? = null

    )

    companion object {
        private const val jsonUrl =
            "https://raw.githubusercontent.com/daarkdemon/jiotvchannels/main/channels.json"
        const val NPJioTV = "https://nayeemparvez.chadasaniya.cf"
        const val GDLJioTV = "https://tv.googledrivelinks.com"
        const val IJioTV = "https://epic-austin.128-199-17-57.plesk.page"
        const val SJioTV = "https://the-nayeemparvez.ml"
        const val FSJioTV = "https://tv.freeseries.eu.org"
        const val FHJioTV = "https://filmyhub.ga"
        const val TSJioTV = "https://tvstream.fun"
        const val RPKJioTV = "http://ranapk-nxt.ml"
    }

    override suspend fun getMainPage(
        page: Int,
        request: MainPageRequest
    ): HomePageResponse {
        val categories = mapOf(
            "Sports" to 8,
            "Entertainment" to 5,
            "Movies" to 6,
            "News" to 12,
            "Music" to 13,
            "Kids" to 7,
            "Lifestyle" to 9,
            "Infotainment" to 10,
            "Devotional" to 15,
            "Business" to 16,
            "Educational" to 17,
            "Shopping" to 18,
            "JioDarshan" to 19
        )
        val items = ArrayList<HomePageList>()
        val response = app.get(jsonUrl).parsed<Channels>().result
        categories.forEach { cat ->
            val results: MutableList<SearchResponse> = mutableListOf()
            val filtered = response.filter { it.channelCategoryId == cat.value }
            filtered.forEach {
                val title = it.channelName.toString()
                val posterUrl = "http://jiotv.catchup.cdn.jio.com/dare_images/images/${it.logoUrl}"
                val quality = if (it.isHD == true) "HD" else ""
                results.add(
                    newMovieSearchResponse(title, title, TvType.Live) {
                        this.posterUrl = posterUrl
                        this.quality = getQualityFromString(quality)
                    }
                )
            }
            items.add(
                HomePageList(
                    capitalizeString(cat.key),
                    results,
                    isHorizontalImages = true
                )
            )
        }
        return HomePageResponse(items)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val response = app.get(jsonUrl).parsed<Channels>().result
        val searchResults =
            response.filter { it.channelName?.lowercase()?.contains(query.lowercase()) == true }

        return searchResults.map {
            val title = it.channelName.toString()
            val posterUrl = "http://jiotv.catchup.cdn.jio.com/dare_images/images/${it.logoUrl}"
            newMovieSearchResponse(title, title, TvType.Live) {
                this.posterUrl = posterUrl
            }
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val response = app.get(jsonUrl).parsed<Channels>().result
        val searchResults =
            response.filter { it.channelName?.contains(url.substringAfterLast("/")) == true }
        val title = searchResults[0].channelName.toString()
        val posterUrl =
            "http://jiotv.catchup.cdn.jio.com/dare_images/images/${searchResults[0].logoUrl}"
        return newMovieLoadResponse(
            title, title, TvType.Live, Result(
                channelId = searchResults[0].channelId,
                channelName = title,
                logoUrl = searchResults[0].logoUrl
            ).toJson()
        ) {
            this.posterUrl = posterUrl
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {

        val result = parseJson<Result>(data)
        argamap(
            {
                invokeNP(
                    result.channelId,
                    callback
                )
            },
            {
                invokeS(
                    result.channelId,
                    callback
                )
            },
            {
                invokeGDL(
                    result.logoUrl?.substringBefore(".png"),
                    callback
                )
            },
            {
                invokeI(
                    result.logoUrl?.substringBefore(".png"),
                    callback
                )
            },
            {
                invokeFS(
                    result.logoUrl?.substringBefore(".png"),
                    callback
                )
            },
            {
                invokeFH(
                    result.logoUrl?.substringBefore(".png"),
                    callback
                )
            },
            {
                invokeTS(
                    result.logoUrl?.substringBefore(".png"),
                    callback
                )
            },
            {
                invokeRPK(
                    result.logoUrl?.substringBefore(".png"),
                    callback
                )
            }
        )

        return true
    }
}