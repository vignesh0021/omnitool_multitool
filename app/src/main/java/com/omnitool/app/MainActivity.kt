package com.omnitool.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.omnitool.app.ui.theme.OmniToolTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Safety Uncaught Exception Logger to record any crash stack trace
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val stackTrace = android.util.Log.getStackTraceString(throwable)
            android.util.Log.e("OmniTool", "FATAL CRASH in ${thread.name}: $stackTrace")
            try {
                java.io.File("/sdcard/Download/omnitool-crash.log").writeText(stackTrace)
            } catch (_: Exception) {}
            defaultHandler?.uncaughtException(thread, throwable)
        }

        enableEdgeToEdge()

        // Keep screen on so user can level furniture and scan walls without phone sleeping
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            OmniToolTheme {
                OmniToolApp()
            }
        }
    }
}
