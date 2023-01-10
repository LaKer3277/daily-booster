package com.daily.clean.booster.core.clean

import android.content.pm.PackageManager
import android.text.TextUtils
import com.daily.clean.booster.BuildConfig
import com.daily.clean.booster.App
import com.daily.clean.booster.R
import com.daily.clean.booster.entity.CleanType
import com.daily.clean.booster.entity.DaiBooCleanChildBean
import com.daily.clean.booster.entity.DaiBooCleanDatas
import com.daily.clean.booster.entity.DaiBooCleanParentBean
import com.daily.clean.booster.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class DaiBooCleaner(private val parent: File) {

    private var scanTemp = DaiBooCleanDatas()
    private val filters = mutableListOf<String>()
    private val whiteList = mutableListOf(BuildConfig.APPLICATION_ID)
    private val protectedFiles = arrayOf("backup", "copy", "copies", "important", "do_not_edit")
    private val securityFiles = arrayOf("cache", "temp","download")
    private val packageRegex: Regex =
        "([a-z_][a-z0-9_]*)+([.][a-z_][a-z0-9_]*){2,}".toRegex(RegexOption.IGNORE_CASE)
    private var callback: OnScanCallback? = null
    private var scan: Job? = null


    suspend fun scanFiles() {
        CleanData.cache.clear()
        scan = GlobalScope.launch(Dispatchers.IO) {
            try {
                getAllFiles2(parent){
                    callback?.onScanFiles(it)
                    if (filterFile(it)) {
                        callback?.onJunkFiles(it)
                    }
                }
                if (scanTemp.appCache.isEmpty()) {
                    callback?.onJunkFiles(null)
                }

                formatScanCache()
//                LogClnopt.dScan("scan---complete！！")
                callback?.onScanStopped()
                DaiBooMK.reSetScanTime()
            } catch (e: Exception) {
                callback?.onScanStopped()
            }

        }
        scan?.join()
    }

    private fun formatScanCache() {
        synchronized(CleanData.cache) {
            CleanData.cache.clear()
            DaiBooCleanParentBean(
                name = App.ins.getString(R.string.ram_used),
                cleanType = CleanType.RAM_USED
            ).apply {
                fileSize = DaiBooRAMUtils.getUsedMemory()
            }.let {
                CleanData.cache.add(it)
            }
            DaiBooCleanParentBean(
                name = App.ins.getString(R.string.app_cache),
                cleanType = CleanType.APP_CACHE
            ).apply {
                childDaiBooCleans.addAll(scanTemp.appCache.values)
//                fileSize += DaiBooRAMUtils.getUsedMemory()

                if (getSelectedSize() < 1000.MB) {
                    var appCache = DaiBooMK.getAppCacheJunkSize()
                    if (appCache == 0L) {
                        appCache = (256.MB..512.MB).random()
                        DaiBooMK.saveAppCacheSize(appCache)
                    } else {
                        val diff = Date().time - DaiBooMK.getLastScanTime()

                        if(appCache < 1024.MB) {
                            when (diff) {
                                in (0 until 10.mm) -> appCache += (0.MB..1.MB).random()
                                in (10 until 60.mm) -> appCache += (1.MB..10.MB).random()
                                in (60.mm until 120.mm) -> appCache += (10.MB..50.MB).random()
                                in (120.mm until 240.mm) -> appCache += (20.MB..50.MB).random()
                                in (240.mm until 360.mm) -> appCache += (100.MB..200.MB).random()
                                else -> appCache += (500.MB..1000.MB).random()
                            }
                            DaiBooMK.saveAppCacheSize(appCache)
                        }
                    }
                    fileSize += appCache
                }

            }.let {
                CleanData.cache.add(it)
            }

//            if (scanTemp.apkFiles.isNullOrEmpty().not()) {
            DaiBooCleanParentBean(
                name = App.ins.getString(R.string.apk_files),
                cleanType = CleanType.APK_FILES
            ).apply { childDaiBooCleans.addAll(scanTemp.apkFiles) }.let {
                CleanData.cache.add(it)
            }
//            }
//            if (scanTemp.appResidual.isNullOrEmpty().not()) {
            DaiBooCleanParentBean(
                name = App.ins.getString(R.string.app_residual),
                cleanType = CleanType.APP_RESIDUAL
            ).apply { childDaiBooCleans.addAll(scanTemp.appResidual) }.let {
                CleanData.cache.add(it)
            }
//            }
//            if (scanTemp.logFiles.isNullOrEmpty().not()) {
            DaiBooCleanParentBean(
                name = App.ins.getString(R.string.log_files),
                cleanType = CleanType.LOG_FILES
            ).apply { childDaiBooCleans.addAll(scanTemp.logFiles) }.let {
                CleanData.cache.add(it)
            }
//            }
//            if (scanTemp.tempFiles.isNullOrEmpty().not()) {
            DaiBooCleanParentBean(
                name = App.ins.getString(R.string.temp_files),
                cleanType = CleanType.TEMP_FILES
            ).apply { childDaiBooCleans.addAll(scanTemp.tempFiles) }.let {
                CleanData.cache.add(it)
            }
//            }
//            if (scanTemp.otherFiles.pathList.isNullOrEmpty().not()) {
            DaiBooCleanParentBean(
                name = App.ins.getString(R.string.ad_junks),
                cleanType = CleanType.AD_JUNK
            ).apply {
                childDaiBooCleans.add(scanTemp.otherFiles)
            }.let { CleanData.cache.add(it) }
//            }
        }
    }

    fun release() {
        callback = null
        scan?.cancel()
        scan = null
    }


    fun setOnScanCallback(callback: OnScanCallback? = null) {
        this.callback = callback
    }

    private fun filterFile(file: File?): Boolean {
        try {
            if (file != null) {
                val fileName = file.name
                val filePath = file.absolutePath
                if (null != file.parentFile && null != file.parentFile?.parentFile) {
                    if ("data" == file.parentFile?.name
                        && "Android" == file.parentFile?.parentFile?.name
                        && isFileInstalled(fileName).not()
                        && ".nomedia" != fileName
                        && packageRegex.containsMatchIn(filePath)
                    ) {
                        DaiBooCleanChildBean(name = fileName, size = file.length()).apply {
                            pathList.add(filePath)
                        }.let { scanTemp.appResidual.add(it) }
                        return true
                    }
                }
                if (file.isDirectory && "cache" == fileName.lowercase()) return false

                val filterIterator = filters.iterator()
//                LogUtils.dScan("过滤--开始-->${filters.size}")
                while (filterIterator.hasNext()) {
                    val filter = filterIterator.next()
                    val filePathForLowercase = filePath.lowercase()
                    //符合规则的垃圾文件
                    if (filePathForLowercase.matches(filter.lowercase().toRegex())) {
                        LogDB.dScan("过滤--开始-->符合规则---$filePathForLowercase")
//                        LogUtils.dScan("过滤--开始-->appRegex---$appRegex")
                        if (file.isFile) {
                            if (filePathForLowercase.endsWith(".apk")) {
                                DaiBooCleanChildBean(name = fileName, size = file.length()).apply {
                                    pathList.add(filePath)
                                }.let { scanTemp.apkFiles.add(it) }
                            } else if (filePathForLowercase.endsWith(".log")
                                || filePathForLowercase.endsWith(".dog3")
                            ) {
                                DaiBooCleanChildBean(name = fileName, size = file.length()).apply {
                                    pathList.add(filePath)
                                }.let { scanTemp.logFiles.add(it) }
                            } else if (filePathForLowercase.endsWith(".tmp")) {
                                DaiBooCleanChildBean(name = fileName, size = file.length()).apply {
                                    pathList.add(filePath)
                                }.let { scanTemp.tempFiles.add(it) }
                            } else {
                                LogDB.dScan("过滤--add-others--$filePathForLowercase")
                                scanTemp.otherFiles.run {
                                    pathList.add(filePath)
                                    size += file.length()
                                }
                            }
                        }
                        //如果文件在包名文件夹中
                        else if (filePathForLowercase.matches(packageRegex)) {
                            LogDB.dScan("过滤--开始-->符合规则---packageName")
                            val packageName = packageRegex.find(filePathForLowercase)?.value ?: ""
                            LogDB.dScan("过滤--add--packageName--$packageName")
                            if (TextUtils.isEmpty(packageName)) {
                                scanTemp.otherFiles.run {
                                    pathList.add(filePath)
                                    size += file.length()
                                }
                            } else {
                                if (scanTemp.appCache.containsKey(packageName)) {
                                    scanTemp.appCache[packageName]?.run {
                                        pathList.add(filePath)
                                        size += file.length()
                                    }
                                } else {
                                    addAppCacheItem(packageName, filePath)?.let {
                                        it.size = file.length()
                                        scanTemp.appCache.put(packageName, it)
                                    }
                                }
                            }
                        }
                        LogDB.dScan("过滤--return")
                        return true
                    }
                }
            } else {
                return false
            }
        } catch (e: Exception) {
            return false
        }
        return false
    }

    private fun addAppCacheItem(packageName: String, filePath: String): DaiBooCleanChildBean? {
        return try {
            val pm = App.ins.packageManager
            val pi = pm.getPackageInfo(packageName, 0)
            val icon = pi?.applicationInfo?.loadIcon(pm)
            val name = (pi?.applicationInfo?.loadLabel(pm) ?: "").toString()
            DaiBooCleanChildBean(icon = icon, name = name).apply { pathList.add(filePath) }
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }


    private fun getAllFiles2(parentDirectory: File, onScan: (File) -> Unit) {
        val allFiles = parentDirectory.listFiles()
        for (file in allFiles) {
            if (null == file) continue
            if (isFileInWhite(file).not()) {
                if (file.isDirectory) {
                    if (isEmptyDirectory(file) || isFileProtect(file)) continue
                    if (isFileSecurity(file)) {
                        onScan(file)
                    }
                    else {
                        getAllFiles2(file, onScan)
                    }
                } else {
                    onScan(file)
                }
            }
        }
    }

    private fun getAllFiles(parentDirectory: File): MutableList<File> {
        val resultFiles: MutableList<File> = mutableListOf()
        val allFiles = parentDirectory.listFiles()
        if (allFiles.isNullOrEmpty()) return resultFiles
        for (file in allFiles) {
            if (null == file) continue
            if (isFileInWhite(file).not()) {
                if (file.isDirectory) {
                    if (isEmptyDirectory(file)) continue
                    if (isFileProtect(file).not()) resultFiles.add(file)
                    resultFiles.addAll(getAllFiles(file))
                } else resultFiles.add(file)
            }
        }
        return resultFiles
    }

    fun initFilters(aggressive: Boolean) {
        val files: MutableList<String> = mutableListOf()
        val folders: MutableList<String> = mutableListOf()

        files.addAll(App.ins.resources.getStringArray(R.array.common_filter_files))
        folders.addAll(App.ins.resources.getStringArray(R.array.common_filter_folders))
        if (aggressive) {
            files.addAll(App.ins.resources.getStringArray(R.array.aggressive_files))
            folders.addAll(App.ins.resources.getStringArray(R.array.aggressive_folders))
        }
        filters.clear()
        for (folder in folders) filters.add(getFolderRegex(folder))
        for (file in files) filters.add(getFileRegex(file))
        filters.add(getFileRegex(".apk"))
    }

    private fun getFolderRegex(folder: String): String = ".*(\\\\|/)$folder(\\\\|/|$).*"
    private fun getFileRegex(file: String): String = ".+" + file.replace(".", "\\.") + "$"

    private fun isFileInstalled(fileName: String): Boolean {
        if (installedPackageNameList.isNullOrEmpty()) getInstallPackageNames()
        return installedPackageNameList.contains(fileName)
    }

    private fun isFileInWhite(file: File?): Boolean {
        if (null == file) return true
        whiteList.forEach {
            if (it.equals(file.absolutePath, ignoreCase = true) || it.equals(
                    file.name,
                    ignoreCase = true
                )
            ) return true
        }
        return false
    }

    private fun isFileProtect(file: File?): Boolean {
        if (null == file) return true
        protectedFiles.forEach {
            if (file.name.lowercase().contains(it)) return true
        }
        return false
    }

    private fun isFileSecurity(file: File?): Boolean {
        if (null == file) return true
        securityFiles.forEach {
            if (file.name.lowercase().contains(it)) return true
        }
        return false
    }


    private fun isEmptyDirectory(directory: File): Boolean = directory.list().isNullOrEmpty()

    @Volatile
    private var installedPackageNameList = mutableListOf<String>()

    private fun getInstallPackageNames(): MutableList<String> {
        synchronized(installedPackageNameList) {
            val pm = App.ins.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            installedPackageNameList.clear()
            installedPackageNameList.addAll(packages.map { it.packageName ?: "" }
                .filter { TextUtils.isEmpty(it).not() })
            return installedPackageNameList
        }
    }


    interface OnScanCallback {
        fun onScanFiles(file: File?)
        fun onJunkFiles(file: File?)
        fun onScanStopped()
    }
}

