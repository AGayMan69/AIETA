package com.example.examplebluetooth

import android.content.*
import android.os.Bundle
import android.os.IBinder
import android.os.PersistableBundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity

abstract class ServiceActivity : AppCompatActivity() {
    protected lateinit var mtts: TextToSpeech
    protected lateinit var mDetector: GestureDetector
    protected var serviceName = "Undefined"

    protected var mBtReceiver: BtReceiver? = null

    protected lateinit var mService: BluetoothService
    protected var mBound: Boolean = false
    protected var connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BluetoothService.BlueBinder
            mService = binder.getService()
            Log.i(BluetoothService.TAG, "${serviceName}: Bind")
            mBound = true
            mBtReceiver ?: run {
                registerStatusReceiver()
            }
            startDetection()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mBound = false
            Log.i(BluetoothService.TAG, "${serviceName}: service connection lost")
        }
    }

    protected fun registerStatusReceiver() {
        mBtReceiver = BtReceiver()
        IntentFilter(BluetoothService.CONNECT_FILTER).also {
            it.addAction(BluetoothService.RECEIVE_FILTER)
            Log.i("StatusReceiver", "${serviceName}: receiver registered")
            registerReceiver(mBtReceiver, it)
        }
    }

    protected inner class BtReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action.equals(BluetoothService.CONNECT_FILTER)) {
                intent.getStringExtra("connection")?.also { intent ->
                    when (intent) {
                        "disconnected" -> {
                            setResult(10)
                            finish()
                        }
                    }
                }
            } else if (intent.action.equals(BluetoothService.RECEIVE_FILTER)) {
                intent.getStringExtra("action")?.also {
                    val command = intent.getStringExtra("message")
                    receiveMessage(it, command)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mtts = TTSFactory.createTTS(this)
        bindBlueService()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
        mBound = false
        Log.i(BluetoothService.TAG, "${serviceName}: Unbind")
        unregisterReceiver(mBtReceiver)
        Log.i("BtReceiver", "${serviceName}: receiver unregistered")
    }

    private fun bindBlueService() {
        Intent(this, BluetoothService::class.java).also {
            bindService(it, connection, BIND_AUTO_CREATE)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        mDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }
    protected abstract fun startDetection()
    protected abstract fun receiveMessage(it: String, command: String?)
    protected abstract fun swipeLeft()
    protected abstract fun swipeRight()
}