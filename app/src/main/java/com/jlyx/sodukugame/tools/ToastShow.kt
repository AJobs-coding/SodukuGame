package com.jlyx.app.tools

import android.content.Context
import android.widget.Toast

class ToastShow {
    companion object {
        fun show(context: Context, msg: Any): Toast {
            return Toast.makeText(context, msg.toString(), Toast.LENGTH_SHORT).apply {
                show()
            }
        }

        fun show(context: Context, resId: Int): Toast {
            return Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT).apply {
                show()
            }
        }
    }
}