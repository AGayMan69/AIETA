package com.example.examplebluetooth

import android.content.*
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.GestureDetector
import com.example.examplebluetooth.databinding.ActivityObstacleBinding

class ObstacleActivity : ServiceActivity() {
    private lateinit var binding: ActivityObstacleBinding
//    private lateinit var mtts: TextToSpeech
//    private lateinit var mDetector: GestureDetector
//
//    private var mBtReceiver: BtReceiver? = null
//
//    private lateinit var mService: BluetoothService
//    private var mBound: Boolean = false
//    private var mConnection: Boolean = true
//    private var connection = object : ServiceConnection {
//        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//            val binder = service as BluetoothService.BlueBinder
//            mService = binder.getService()
//            Log.i(BluetoothService.TAG, "Obstacle: Bind")
//            mBound = true
//            mBtReceiver ?: run {
//                registerStatusReceiver()
//            }
//            startDetection()
//        }
//
//        override fun onServiceDisconnected(name: ComponentName?) {
//            mBound = false
//            Log.i(BluetoothService.TAG, "Obstacle: service connection lost")
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityObstacleBinding.inflate(layoutInflater)
//        mDetector = GestureDetector(this, SwipeListener(::swipeLeft, ::swipeRight))
        setContentView(binding.root)
        serviceName = "Obstacle"
        mDetector = GestureDetector(this, SwipeListener(::swipeLeft, ::swipeRight))
//        mtts = TTSFactory.createTTS(this)
//        bindBlueService()
    }

    override fun startDetection() {
        mService.switchServiceMode(RequestObject("obstacle"))
    }

    //    override fun onDestroy() {
//        super.onDestroy()
//        unbindService(connection)
//        mBound = false
//        Log.i(BluetoothService.TAG, "Obstacle: Unbind")
//        unregisterReceiver(mBtReceiver)
//    }
//
//    private fun registerStatusReceiver() {
//        mBtReceiver = BtReceiver()
//        IntentFilter(CONNECT_FILTER).also {
//            it.addAction(RECEIVE_FILTER)
//            Log.i("StatusReceiver", "Obstacle: receiver registered")
//            registerReceiver(mBtReceiver, it)
//        }
//    }
//
//    private fun bindBlueService() {
//        Intent(this, BluetoothService::class.java).also {
//            bindService(it, connection, BIND_AUTO_CREATE)
//        }
//    }
//
    override fun swipeLeft() {
        Log.i("Swipe", "swipe right")
        return
    }

    override fun swipeRight() {
        // launch elevator
        Log.i("Swipe", "swipe right")
        Intent(this@ObstacleActivity, ElevatorActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_FORWARD_RESULT
//            unregisterReceiver(mBtReceiver)
            startActivity(it)
            finish()
        }
    }
//
//    private inner class BtReceiver : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent) {
//            if (intent.action.equals(CONNECT_FILTER)) {
//                intent.getStringExtra("connectoin")?.also { intent ->
//                    when (intent) {
//                        "disconnected" -> {
//                            // return to main activity
//                        }
//                    }
//                }
//            } else if (intent.action.equals(RECEIVE_FILTER)) {
//                intent.getStringExtra("action")?.also {
//                    val command = intent.getStringExtra("message")
//                    receiveMessage(it, command)
//                }
//            }
//        }
//    }

    override fun receiveMessage(it: String, command: String?) {
        if (it == "obstacle detection") {
            mtts.speak(command, TextToSpeech.QUEUE_FLUSH, null, null)
            binding.commandTv.text = command
            when (command) {
                "前方不便前行" -> binding.lottieStop.playAnimation()
                else -> binding.lottieExplosion.playAnimation()
            }
            // ui shit
        } else if (it == "switch mode") {
            if (command == "障礙物模式") {
                Log.i("TTSSpeech", "switch mode: Obstacle")
                mtts.speak(command, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }

}