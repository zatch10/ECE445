package com.example.pollutionmapper

import android.bluetooth.BluetoothSocket
import android.util.Log

class BluetoothServer(private val activity: MapsActivity, private val socket: BluetoothSocket?): Thread() {

    private val inputStream = this.socket!!.inputStream
    private val outputStream = this.socket!!.outputStream

    override fun run() {
        try {
            val available = inputStream.available()
            val bytes = ByteArray(available)
            Log.d("server", "Reading")
            inputStream.read(bytes, 0, available)
            val text = String(bytes)
            Log.d("server", "Message received")
            Log.d("server", text)
//            activity.appendText(text)
        } catch (e: Exception) {
            Log.e("client", "Cannot read data", e)
        } finally {
            inputStream.close()
            outputStream.close()
            socket?.close()
        }
    }
}
