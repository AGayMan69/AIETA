package com.example.examplebluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.AsyncQueryHandler
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.examplebluetooth.databinding.ActivityMainBinding
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var mtts: TextToSpeech
    private lateinit var binding: ActivityMainBinding

//    Bluetooth
    private lateinit var mbtadapter: BluetoothAdapter
    private lateinit var mbtArray: Array<BluetoothDevice>
    private lateinit var mBlueClient: BlueClientSocket
    private lateinit var mBlueSendReceive: BlueSendReceive
    private lateinit var mReceiveHandler: ReceiveHandler

    companion object {
        private val SERVICE_UUID = UUID.fromString("00030000-0000-1000-8000-00805F9B34FB")
        private const val DEVICE_ADDRESS = "DC:A6:32:A2:56:77"
        private const val STATE_LISTENING = 1
        private const val STATE_CONNECTING = 1
        private const val STATE_CONNECTED = 1
        private const val STATE_CONNECTION_FAILED = 1
        private const val STATE_MESSAGE_RECEIVED = 1
        private val PERMISSION_LIST = arrayOf(
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        mtts = initTTS();
        initListener();
        setContentView(binding.root)
        initBT();
    }

    @SuppressLint("MissingPermission")
    private fun initBT() {
        val bluetoothManager = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mbtadapter = bluetoothManager.adapter
        requestBTPermission()
        mReceiveHandler = ReceiveHandler(this, Looper.getMainLooper())
        mbtadapter.enable()
        if (mbtadapter.isEnabled) {
            Log.i("BlueAdapter", "Bluetooth is enabled")
            listDevice()
            mBlueClient = BlueClientSocket(mbtadapter.getRemoteDevice(DEVICE_ADDRESS))
            mBlueClient.start()
        }
    }

    @SuppressLint("MissingPermission")
    private fun listDevice() {
        var btDevices = mbtadapter.bondedDevices
        btDevices.forEach {
            Log.i("ListDevice", it.address)
        }
    }

    @SuppressLint("MissingPermission")
    private inner class BlueClientSocket constructor(device: BluetoothDevice): Thread() {
        private var mDevice: BluetoothDevice
        private lateinit var mSocket: BluetoothSocket
        private val TAG = "BlueClient"

        init {
            mDevice = device
            try {
                mSocket = mDevice.createRfcommSocketToServiceRecord(SERVICE_UUID)
            } catch (e: IOException) {
                Log.e(TAG, "socket creation error", e)
            }
        }

        override fun run() {
            try {
                mSocket.connect()
            } catch (e: IOException) {
                try {
                    mSocket.close()
                } catch (e2: IOException) {
                    Log.e(TAG, "Unable to close socket during connection failure", )
                }
            }
            var request = "hello from Android"
            var requestBuffer = request.toByteArray();
//            creating output stream to talk to server
            var outStream = mSocket.outputStream
//             sending the request to the server
            try {
                outStream.write(requestBuffer)
                outStream.flush()
            } catch (e: IOException) {
                Log.e(TAG, "Exception occurred during sending the output stream", e)
                Log.i(TAG, "UUID $SERVICE_UUID")
            }
            mBlueSendReceive = BlueSendReceive(mSocket)
            mBlueSendReceive.start()
        }
    }

    private inner class BlueSendReceive(socket: BluetoothSocket): Thread() {
        private var mSocket: BluetoothSocket
        private var mInputStream: InputStream
        private var mOutputStream: OutputStream
        private val TAG = "BlueReceive"

        init {
            mSocket = socket
            mInputStream = mSocket.inputStream
            mOutputStream = mSocket.outputStream
        }

        override fun run() {
            val readBuff = ByteArray(1024)
            var bufLength = 0
            while (true) {
                try {
                    bufLength = mInputStream.read(readBuff)
                    val msg = Message.obtain()
                    msg.obj = readBuff.toString()
                    msg.target = mReceiveHandler
                    msg.sendToTarget()
                } catch (e: IOException) {
                    Log.e(TAG, "Buffer reading error", e)
                }
            }
        }
    }

    private inner class ReceiveHandler(private val context: Context, looper: Looper): Handler(looper) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val message = msg.obj
            if(message is String) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                Log.i("BlueReceived", message)
            }
        }
    }

    private fun requestBTPermission() {
        //        for Android 12
        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        //            val requestMultiplePermissions = registerForActivityResult(
        //                ActivityResultContracts.RequestMultiplePermissions()
        //            )
        //            {
        //                if (!btadapter.isEnabled) {
        //                    btadapter.enable()
        //                    Toast.makeText(applicationContext, "Bluetooth enabled", Toast.LENGTH_SHORT).show()
        //                }
        //            }
        //            requestMultiplePermissions.launch(
        //                arrayOf(
        //                    android.Manifest.permission.BLUETOOTH_SCAN,
        //                    android.Manifest.permission.BLUETOOTH_CONNECT
        //                )
        //            )
        //        }
        PERMISSION_LIST.forEach {
            Log.i("requestPermission", it)
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    this,
                    it,
                ) -> {
                    // Permission is granted
                }
                else -> {
//                    requestPermissionLauncher.launch(it)
                    requestPermissions(arrayOf(it), 1)
                }
            }
        }
    }

    private fun initListener() {
        binding.eButton.setOnClickListener {
            switchToElevator();
        }
        binding.oaButton.setOnClickListener {
            switchToObstacleAvoidance();
        }
    }

    private fun initTTS(): TextToSpeech {
        lateinit var tts: TextToSpeech;
        tts = TextToSpeech(this, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale("zh", "hk"))

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported!")
                } else {
                    Toast.makeText(
                        applicationContext,
                        "TTS initialized",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("TTS", "tts ready")
                }
            }
        })
        return tts
    }

    private fun switchToElevator() {
        Log.e(TAG, "switchToElevator: starting")
        Toast.makeText(
            this,
            "Switching to elevator mode",
            Toast.LENGTH_SHORT
        ).show();
        val text = "電梯模式"
//        val text = "elevator"
        mtts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    private fun switchToObstacleAvoidance() {
        Log.e(TAG, "switchToObstacleAvoidance: starting")
        Toast.makeText(
            this,
            "Switching to obstacle avoidance mode",
            Toast.LENGTH_SHORT
        ).show();
        val text = "障礙物模式"
        mtts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }
}