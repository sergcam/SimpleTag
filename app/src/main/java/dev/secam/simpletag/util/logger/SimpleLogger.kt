package dev.secam.simpletag.util.logger

import org.apache.commons.io.input.ReversedLinesFileReader
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import java.io.File
import javax.inject.Inject

class SimpleLogger @Inject constructor(context: Context) {
    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }
    private val IOScope = CoroutineScope(Dispatchers.IO).plus(coroutineExceptionHandler)
    private val logFile = File(context.filesDir, "log.txt")

    fun log(message: String){
        IOScope.launch {
            val stacktrace = Thread.currentThread().stackTrace
            val simpleClassName = stacktrace[3].className.substringAfterLast(".")
            Log.d(simpleClassName, message)
            logFile.appendText(message+"\n")
        }
    }

    fun readLog(lines: Int = 100): List<String> {
        return ReversedLinesFileReader(logFile, Charsets.UTF_8).readLines(lines)
    }
    fun clearLog(){
        logFile.writeText("")
    }

}