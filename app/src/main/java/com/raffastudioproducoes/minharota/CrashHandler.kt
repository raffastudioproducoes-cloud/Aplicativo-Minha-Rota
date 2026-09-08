package com.raffastudioproducoes.minharota

import android.content.Context

/**
 * Delegates crashes to Android without persisting exception contents locally.
 * Messages and stack traces may contain user input, identifiers or internal paths.
 */
class CrashHandler(context: Context) : Thread.UncaughtExceptionHandler {
    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        defaultHandler?.uncaughtException(thread, throwable)
    }
}
