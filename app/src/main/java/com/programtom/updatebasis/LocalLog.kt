package com.programtom.updatebasis

import android.util.Log

class LocalLog {
    companion object {
        @JvmStatic
        fun e(string: String, message: String, ex: Exception) {
            Log.e(string, message, ex)
        }
        @JvmStatic
        fun d(string: String, string2: String) {
            Log.d(string, string2)
        }
        @JvmStatic
        fun e(tag: String, string: String) {
            Log.e(tag, string)
        }

    }
}
