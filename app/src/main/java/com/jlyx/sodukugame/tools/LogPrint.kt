package com.jlyx.app.tools

import android.util.Log

class LogPrint {
    companion object {
        fun info(msg: Any) {
            Log.i("-------------------->", msg.toString())
        }
    }
}