package com.example.examplebluetooth

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.*
import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.*

class BluetoothService : Service() {
    private val binder = BlueBinder()
    private var mBluetoothSocket: BluetoothSocket? = null
    private var connectBluetoothJob: Job? = null
    private lateinit var mReceiveHandlder: Handler
    private lateinit var mBlueReceive: BlueReceive

    companion object {
        private val SERVICE_UUID = UUID.fromString("00030000-0000-1000-8000-00805F9B34FB")
        val CONNECT_FILTER = "com.ivebt.CONNECT_BT"
        val RECEIVE_FILTER = "com.ivebt.RECEIVE_BT"
        val TAG = "BluetoothService"
    }


    inner class BlueBinder : Binder() {
        fun getService(): BluetoothService = this@BluetoothService
    }

    fun disconnectBluetooth() {
        mBluetoothSocket?.also {
            if (it.isConnected) {
//                mBlueReceive.terminateThread()
                it.close()
//                Intent().also { intent ->
//                    intent.action = CONNECT_FILTER
//                    intent.putExtra("connection", "disconnected")
//                    Log.i(TAG, "connection fail")
//                    sendBroadcast(intent)
//                }
            }
        }
    }

    fun switchServiceMode(requestObject: RequestObject) {
        mBluetoothSocket?.also {
            val requestJSON = Gson().toJson(requestObject)
            OutputStreamWriter(it.outputStream, StandardCharsets.UTF_8) .also { writer ->
                writer.write(requestJSON)
                writer.flush()
            }
        }
    }

    private inner class BlueReceive() : Thread() {
        private var mInputStream: InputStream? = mBluetoothSocket?.inputStream
        private var readBuff: ByteArray = ByteArray(1024)
        private var terminate = false

//        fun terminateThread() {
//            terminate = true
//        }

        override fun run() {
            mInputStream?.also {
                while (!terminate) {
                    try {
                        val buffLength = it.read(readBuff)
                        val string = String(readBuff, 0, buffLength)
                        Log.i(TAG, "Received: $string")
                        val msg = Message.obtain()
                        msg.obj = string
                        msg.target = mReceiveHandlder
                        msg.sendToTarget()
                    } catch (e: IOException) {
                        Log.e(TAG, "Buffer reading error", e)
                        // Bluetooth disconnected informing handler
                        val msg = Message.obtain()
                        msg.obj = 1
                        msg.target = mReceiveHandlder
                        msg.sendToTarget()
                        Log.i(TAG, "Bluetooth disconnected")
                        break;
                    }
                }
            }
        }
    }

    private inner class ReceiveHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            val message = msg.obj
            if (message is String) {
                val messageJson = JSONObject(message)
                val action = messageJson.getString("action")
                val actionMessage = messageJson.getString("message")
                Intent().also {
                    it.action = RECEIVE_FILTER
                    it.putExtra("action", action)
                    it.putExtra("message", actionMessage)
                    sendBroadcast(it)
                    Log.i(TAG, "broadcast receiving message")
                }
            } else if (message is Int) {
                if (message == 1) {

                    Intent().also { intent ->
                        intent.action = CONNECT_FILTER
                        intent.putExtra("connection", "disconnected")
                        Log.i(TAG, "connection disconnected")
                        sendBroadcast(intent)
                    }
                    stopSelf()
                }

            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "starting bluetooth service ...")
        try {
            this.mBluetoothSocket =
                (intent.extras?.getParcelable<BluetoothDevice>("btDevice"))?.createRfcommSocketToServiceRecord(
                    SERVICE_UUID
                )

        } catch (e: IOException) {
            Log.e(TAG, "Socket creation error")
        }
        mBluetoothSocket?.also {
            connectBluetoothJob = CoroutineScope(Dispatchers.IO).launch {
                Intent().also {
                    it.action = CONNECT_FILTER
                    it.putExtra("connection", "loading")
                    Log.i(TAG, "connecting")
                    sendBroadcast(it)
                }
                try {
                    it.connect()
                    Intent().also {
                        it.action = CONNECT_FILTER
                        it.putExtra("connection", "successful")
                        Log.i(TAG, "connection success")
                        sendBroadcast(it)

                        mReceiveHandlder = ReceiveHandler(Looper.getMainLooper())
                        mBlueReceive = BlueReceive()
                        mBlueReceive.start()
                    }
                } catch (e: IOException) {
                    it.close()
                    Intent().also {
                        it.action = CONNECT_FILTER
                        it.putExtra("connection", "failed")
                        Log.i(TAG, "connection fail")
                        sendBroadcast(it)
                    }
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "service destroyed")
    }
}