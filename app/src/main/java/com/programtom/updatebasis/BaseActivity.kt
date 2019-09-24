package com.programtom.updatebasis

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File


abstract class BaseActivity : AppCompatActivity() {

    override fun onResume() {
        super.onResume()
        checkForUpdate()
    }

    private val AUTHORITY = "com.programtom.updatebasis.fileprovider"
    private var cheked = false
    private var download: String? = null
    private var async: AsyncTask<Void, Void, Boolean?>? = null
    @SuppressLint("StaticFieldLeak")
    private fun checkForUpdate() {
        if (cheked) {
            normalResume()
        } else {
            if (Request.isConnected(this)) {
                cheked = true
                async =
                    object : AsyncTask<Void, Void, Boolean?>() {
                        override fun doInBackground(vararg params: Void?): Boolean? {
                            val installFile =
                                "https://PathToYourApkFileOnTheWeb.apk"
                            val versionFile =
                                "PathToATextFileOnTheWebContainingJustANumber_TheCurrentVersion.txt"

                            try {
                                val versionStr = Request.getVersion(versionFile)
                                val v = Integer.parseInt(versionStr.trim())
                                LocalLog.d("v", versionStr)
                                if (v > MainActivity.version) {
                                    download = Request.download(installFile, this@BaseActivity)
                                    return true
                                }
                            } catch (e1: Exception) {
                                e1.message?.let { LocalLog.e("Download", it, e1) }
                            }

                            return null
                        }

                        override fun onPostExecute(result: Boolean?) {
                            if (result != null && result) {
                                if (download != null) {
                                    val newIntent = Intent(Intent.ACTION_VIEW)

                                    newIntent.setDataAndType(
                                        FileProvider.getUriForFile(
                                            this@BaseActivity,
                                            AUTHORITY,
                                            File(download)
                                        ),
                                        "application/vnd.android.package-archive"
                                    )
                                    newIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    newIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    startActivity(newIntent)

                                    finish()
                                } else {
                                    val b = AlertDialog.Builder(this@BaseActivity)
                                    b.setMessage(R.string.new_version_failed)
                                    b.setPositiveButton(
                                        R.string.ok
                                    ) { dialog, _ ->
                                        dialog?.dismiss()
                                        finish()
                                    }

                                }
                                return
                            } else {
                                normalResume()
                            }
                        }
                    }.execute()
            } else {
                normalResume()
            }
        }
    }


    override fun onPause() {
        super.onPause()
        async?.cancel(true)
    }

    open fun normalResume(){

    }
}
