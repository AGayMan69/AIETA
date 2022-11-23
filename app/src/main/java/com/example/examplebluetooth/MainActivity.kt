package com.example.examplebluetooth

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.*
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.content.ContextCompat
import com.example.examplebluetooth.BluetoothService.Companion.CONNECT_FILTER
import com.example.examplebluetooth.databinding.ActivityMainBinding
import java.io.*
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var mtts: TextToSpeech
    private lateinit var binding: ActivityMainBinding

    //    Bluetooth
    private lateinit var mBtadapter: BluetoothAdapter
    private lateinit var mbtArray: Array<BluetoothDevice>

    //    private lateinit var mBlueClient: BlueClientSocket
//    private lateinit var mBlueReceive: BlueReceive
//    private lateinit var mReceiveHandler: ReceiveHandler
//    private lateinit var mBlueSocket: BluetoothSocket
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var mDetector: GestureDetector
    private lateinit var mBtDevice: BluetoothDevice
    private var mBtConnectionStatusReceiver: BtConnectionStatusReceiver? = null
    private lateinit var mService: BluetoothService
    private var mBound: Boolean = false
    private var mConnect: Boolean = false
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BluetoothService.BlueBinder
            mService = binder.getService()
            Log.i(BluetoothService.TAG, "Main: Bind")
            mBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mBound = false
            Log.i(BluetoothService.TAG, "Main: service connection lost")
        }

        override fun onBindingDied(name: ComponentName?) {
            Log.i(BluetoothService.TAG, "Main: Unbind")
        }
    }

    companion object {
        //        private val SERVICE_UUID = UUID.fromString("00030000-0000-1000-8000-00805F9B34FB")
        private const val DEVICE_ADDRESS = "DC:A6:32:A2:56:77"
        private val PERMISSION_LIST = arrayOf(
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        mDetector = GestureDetector(this, SwipeListener(::swipeLeft, ::swipeRight))
        setContentView(binding.root)
        mtts = initTTS();
        initListener();
        initBT();
    }

    override fun onResume() {
        super.onResume()
        mBtConnectionStatusReceiver ?: run {
            mBtConnectionStatusReceiver = BtConnectionStatusReceiver()
            IntentFilter(CONNECT_FILTER).also {
                Log.i("StatusReceiver", "Main: receiver registered")
                registerReceiver(mBtConnectionStatusReceiver, it)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        mBtConnectionStatusReceiver?.also {
            unregisterReceiver(mBtConnectionStatusReceiver)
            Log.i("StatusReceiver", "Main: receiver unregistered")
            mBtConnectionStatusReceiver = null
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        mDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    @SuppressLint("MissingPermission")
    private fun initBT() {
        val bluetoothManager =
            applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBtadapter = bluetoothManager.adapter
        requestBTPermission()
//        mReceiveHandler = object : ReceiveHandler(
//            context = this,
//            looper = Looper.getMainLooper(),
//            mtts = mtts
//        ) {
//            override fun doInActivity() {
//                return
//            }
//
//            override fun connectionLost() {
//                showConnectionBtn()
//            }
//        }
        mBtadapter.enable()
        if (mBtadapter.isEnabled) {
            Log.i("BlueAdapter", "Bluetooth is enabled")
            listDevice()
        }
//            SERVICE_UUID)
    }

    @SuppressLint("MissingPermission")
    private fun listDevice() {
        var btDevices = mBtadapter.bondedDevices
        btDevices.forEach {
            Log.i("ListDevice", it.address)
        }
    }

    @SuppressLint("MissingPermission")
    private inner class BlueClientSocket constructor(device: BluetoothDevice) : Thread() {
//        private lateinit var mSocket: BluetoothSocket

//        private val TAG = "BlueClient"

//        init {
////            mDevice = device
//            try {
//                mSocket = device.createRfcommSocketToServiceRecord(SERVICE_UUID)
//            } catch (e: IOException) {
//                Log.e(TAG, "socket creation error", e)
//            }
//        }
//
//        override fun run() {
//            runOnUiThread {
//                showLoadingDialog()
//            }
//            try {
//                mSocket.connect()
//                runOnUiThread {
//                    showSuccessDialog()
////                    mBlueReceive = BlueReceive(mSocket, mReceiveHandler)
////                    mBlueReceive.start()
//                }
//                Log.i(TAG, "Connection successful")
//            } catch (e: IOException) {
//                try {
//                    Log.e(TAG, "Unable to connect to the device")
//                    mSocket.close()
//                    runOnUiThread {
//                        showFailDialog()
//                    }
//                    return;
//                } catch (e2: IOException) {
//                    Log.e(TAG, "Unable to close socket during connection failure")
//                }
//            } finally {
//                runOnUiThread {
////                    mBlueSocket = mSocket
//                }
//            }
//        }
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
        binding.connectBtn.setOnClickListener {
            startConnection()
        }
        binding.disconnectBtn.setOnClickListener {
            stopConnection()
        }
    }

    private fun initTTS(): TextToSpeech {
        return TTSFactory.createTTS(this)
    }

    private fun startConnection() {
//        mBlueClient = BlueClientSocket(mbtadapter.getRemoteDevice(DEVICE_ADDRESS))
//        mBlueClient.start()
        mBtDevice = mBtadapter.getRemoteDevice(DEVICE_ADDRESS)
        Intent(this, BluetoothService::class.java).also {
            it.putExtra("btDevice", mBtDevice)
            startService(it)
        }
    }

    private fun showLoadingDialog() {
        loadingDialog = LoadingDialog(this, applicationContext)
        loadingDialog.showDialog()
        loadingDialog.startLottie()
    }

    private fun showSuccessDialog() {
        val getDialog = loadingDialog.getDialog()
        getDialog.setOnDismissListener {
            showDisconnectionBtn()
        }
        loadingDialog.loadingSuccessful()
    }

    private fun showFailDialog() {
        loadingDialog.loadFail()
    }

    private fun showDisconnectionBtn() {
        binding.disconnectBtn.visibility = View.VISIBLE
        binding.connectBtn.visibility = View.INVISIBLE
    }

    private fun stopConnection() {
        if (mBound) {
            mService.disconnectBluetooth()
        }
    }

    private fun showConnectionBtn() {
        binding.connectBtn.visibility = View.VISIBLE
        binding.disconnectBtn.visibility = View.INVISIBLE
    }

    private fun startService() {
        if (mConnect) {
            Intent(this@MainActivity, ObstacleActivity::class.java).also {
                it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                resultLauncher.launch(it)
            }
        }
    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == 10) {
            Log.e(BluetoothService.TAG, "Return to main activity", )
            mConnect = false
            showConnectionBtn()
            if (mBound) {
                unbindService(connection)
                mBound = false
                Log.i(BluetoothService.TAG, "Main: Unbind")
            }
        } else {
            RequestObject("stop").also {
                mService.switchServiceMode(it)
            }
        }
    }

    private fun swipeLeft() {
        Log.i("Swipe", "swipe left")
        startService()
    }

    private fun swipeRight() {
        Log.i("Swipe", "swipe right")
        startService()
    }

    private inner class BtConnectionStatusReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action.equals(CONNECT_FILTER)) {
                intent.getStringExtra("connection")?.also { intentString ->
                    when (intentString) {
                        "loading" -> showLoadingDialog()
                        "failed" -> showFailDialog()
                        "successful" -> {
                            mConnect = true
                            showSuccessDialog()
                            Intent(context, BluetoothService::class.java).also {
                                bindService(it, connection, Context.BIND_AUTO_CREATE)
                            }
                        }
                        "disconnected" -> {
                            mConnect = false
                            showConnectionBtn()
                            if (mBound) {
                                unbindService(connection)
                                mBound = false
                                Log.i(BluetoothService.TAG, "Main: Unbind")
                            }
                        }
                        else -> return
                    }
                }
            }
        }
    }

}