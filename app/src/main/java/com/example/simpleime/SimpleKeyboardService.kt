package com.example.simpleime

import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class SimpleKeyboardService : InputMethodService() {
    companion object {
        val TAG: String = SimpleKeyboardService::class.java.simpleName
    }

    private var serverSocket: ServerSocket? = null
    private val serverPort = 12345 // Define your port here

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            android.os.Debug.waitForDebugger()
        }
        Log.i(TAG, "onCreate")
        startTcpServer()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTcpServer()
    }

    override fun onCreateInputView(): View {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        Log.i(TAG, "onCreateInputView")
        return layout
    }

    private fun startTcpServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                serverSocket = ServerSocket(serverPort)
                Log.i(TAG, "Server started on port $serverPort")
                while (!serverSocket!!.isClosed) {
                    val clientSocket: Socket = serverSocket!!.accept()
                    handleClient(clientSocket)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in server: ${e.message}")
            }
        }
    }

    private fun stopTcpServer() {
        try {
            serverSocket?.close()
            Log.i(TAG, "Server stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping server: ${e.message}")
        }
    }

    private fun handleClient(clientSocket: Socket) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
                val writer = PrintWriter(clientSocket.getOutputStream(), true)
                writer.println("Connected to SimpleKeyboardService")

                var command: String?
                while (reader.readLine().also { command = it } != null) {
                    Log.i(TAG, "Received command: $command")
                    applyCommand(command)
                }

                clientSocket.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error handling client: ${e.message}")
            }
        }
    }

    private fun applyCommand(command: String?) {
        command?.let {
            val ic = currentInputConnection
            ic?.commitText(it, 1)
        }
    }
}