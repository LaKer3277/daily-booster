package com.daily.clean.booster.ads.conf

import android.annotation.SuppressLint
import android.util.Log
import com.daily.clean.booster.datas.RemoteConfig
import com.daily.clean.booster.ext.toAdPos
import org.json.JSONArray
import org.json.JSONObject
import java.util.ArrayList

open class LoaderConf {

    protected fun configByPos(adPos: AdPos): PosConf {
        val config = spaceMap[adPos.adPos]
        if (config == null) {
            tryParseRelease()
        }

        return spaceMap[adPos.adPos] ?: PosConf(adPos)
    }

    companion object {
        private val spaceMap = HashMap<String, PosConf>()

        fun tryParseLocal() {
            tryParse(localAds())
        }

        fun tryParseRelease() {
            var adConfig = RemoteConfig.ins.getAdsConfig()
            Log.i("AdsLoader", "RemoteConfig: $adConfig")
            if (adConfig.isNullOrEmpty()) {
                adConfig = localAds()
            }
            tryParse(adConfig)
        }

        @SuppressLint("LogNotTimber")
        @Synchronized
        private fun tryParse(adConfig: String) {
            try {
                val content = JSONObject(adConfig)
                val names = content.names() ?: return
                val hashMap = HashMap<String, PosConf>()

                var name: String
                var idArray: JSONArray
                var idJson: JSONObject
                var idList: ArrayList<AdConf>
                for (i in 0 until names.length()) {
                    name = names.optString(i)
                    if (name.isNullOrEmpty()) continue
                    idArray = content.optJSONArray(name) ?: continue
                    idList = arrayListOf()
                    for (j in 0 until idArray.length()) {
                        idJson = idArray.optJSONObject(j) ?: continue
                        idList.add(
                            AdConf(
                                Network = idJson.optString("ad_source"),
                                Id = idJson.optString("id"),
                                Form = idJson.optString("ad_type"),
                                Hierarchy = idJson.optInt("weight")
                            )
                        )
                    }
                    idList.sortBy { it.Hierarchy * -1 }
                    hashMap[name] = PosConf(name.toAdPos(), idList)
                }

                synchronized(spaceMap) {
                    spaceMap.clear()
                    spaceMap.putAll(hashMap)
                }
            } catch (e: Exception) {}
        }

        private fun localAds(): String {
            return """{
    "db_Open":[
        {
            "Id":"ca-app-pub-3940256099942544/3419835294",
            "Network":"admob",
            "Form":"open",
            "out":7200,
            "Hierarchy":2
        },
        {
            "Id":"ca-app-pub-3940256099942544/3419835294",
            "Network":"admob",
            "Form":"open",
            "out":7200,
            "Hierarchy":3
        },
        {
            "Id":"ca-app-pub-3940256099942544/3419835294",
            "Network":"admob",
            "Form":"open",
            "out":7200,
            "Hierarchy":1
        }
    ],
    "clean_IV":[
        {
            "Id":"ca-app-pub-3940256099942544/1033173712",
            "Network":"admob",
            "Form":"interstitial",
            "out":3000,
            "Hierarchy":2
        },
        {
            "Id":"ca-app-pub-3940256099942544/1033173712",
            "Network":"admob",
            "Form":"interstitial",
            "out":3000,
            "Hierarchy":1
        },
        {
            "Id":"ca-app-pub-3940256099942544/1033173712",
            "Network":"admob",
            "Form":"interstitial",
            "out":3000,
            "Hierarchy":3
        }
    ],
    "result_NV":[
        {
            "Id":"ca-app-pub-3940256099942544/2247696110",
            "Network":"admob",
            "Form":"native",
            "out":3000,
            "Hierarchy":2
        },
        {
            "Id":"ca-app-pub-3940256099942544/2247696110",
            "Network":"admob",
            "Form":"native",
            "out":3000,
            "Hierarchy":1
        },
        {
            "Id":"ca-app-pub-3940256099942544/2247696110",
            "Network":"admob",
            "Form":"native",
            "out":3000,
            "Hierarchy":3
        }
    ]
}"""
        }
    }
}