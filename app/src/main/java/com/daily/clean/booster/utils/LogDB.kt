package com.daily.clean.booster.utils

import android.util.Log
import com.daily.clean.booster.base.DBConfig
import java.util.*

object LogDB {

    private const val DEBUG_MODE = DBConfig.DAIBOO_USE_LOG_DUBUG
    private var TAG:String? = null
    private const val TAG_DEFAULT = "DAILY:"

    private val sCachedTag = HashMap<String, String>()


    fun t(appTag: String): LogDB {
        TAG = appTag
        return this
    }

    fun i(message: String) {
        if (DEBUG_MODE) Log.i(buildTag(TAG), buildMessage(message)!!)
    }
    fun d(message: String) {
        if (DEBUG_MODE) Log.d(buildTag(TAG), buildMessage(message)!!)
    }

    fun dLog(message: String) {
        if (DEBUG_MODE) Log.d(buildTag(TAG), buildMessage("-MM-$message")!!)
    }
    fun dEvent(message: String) {
        if (DEBUG_MODE) Log.d(buildTag("#LOG-EVENT---"), buildMessage(message)!!)
    }
    fun dService(message: String) {
        if (DEBUG_MODE) Log.d(buildTag("#SERVICE---"), buildMessage(message)!!)
    }
    fun dReport(message: String) {
        if (DEBUG_MODE) Log.d(buildTag(TAG), buildMessage("#TBA---$message")!!)
    }
    fun eReport(message: String) {
        if (DEBUG_MODE) Log.e(buildTag(TAG), buildMessage("#TBA-E--$message")!!)
    }
    fun dpop(s: String) {
        if (DEBUG_MODE) Log.i(buildTag(TAG), buildMessage("#POP-ups---$s")!!)
    }
    fun e(message: String) {
        if (DEBUG_MODE) Log.e(buildTag(TAG), buildMessage(message)!!)
    }
    fun dAD(message: String) {
        if (DEBUG_MODE) Log.d(buildTag("#ADS---"), buildMessage(message)!!)
    }
    fun eAD(message: String) {
        if (DEBUG_MODE) Log.e(buildTag("#ADS---"), buildMessage(message)!!)
    }

    fun dFile(string: String) {
        if (DEBUG_MODE) Log.d(buildTag("#FILE---"), buildMessage(string)!!)
    }

    fun dScan(string: String) {
        if (DEBUG_MODE) Log.d(buildTag("#scan---"), buildMessage(string)!!)
    }
    fun eScan(string: String) {
        if (DEBUG_MODE) Log.e(buildTag("#scan-E---"), buildMessage(string)!!)
    }

    fun dLife(string: String) {
        if (DEBUG_MODE) Log.d(buildTag("#lifeCycle#"), buildMessage(string)!!)
    }


    private fun buildTag(t: String?): String? {

        var tag = t?: TAG_DEFAULT
        val key = String.format(Locale.US, "%s@%s", tag, Thread.currentThread().name)
        if (!sCachedTag.containsKey(key)) {
            if (TAG_DEFAULT == tag) {
                sCachedTag[key] = String.format(
                    Locale.US, "|%s|%s|",
                    tag,
                    Thread.currentThread().name
                )
            } else {
                sCachedTag[key] = String.format(
                    Locale.US, "|%s%s|%s|",
                    TAG_DEFAULT,
                    tag,
                    Thread.currentThread().name
                )
            }
        }
        return sCachedTag[key]
    }

    private fun buildMessage(message: String): String? {
        val traceElements = Thread.currentThread().stackTrace
        if (traceElements == null || traceElements.size < 4) {
            return message
        }
        val traceElement = traceElements[4]
        return String.format(
            Locale.US, "(%s:%d) %s",
//            Locale.US, "%s.%s(%s:%d) %s",
//            traceElement.className.substring(traceElement.className.lastIndexOf(".") + 1),
//            traceElement.methodName,
            traceElement.fileName,
            traceElement.lineNumber,
            message
        )
    }




}