package com.example.examplebluetooth

import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.GestureDetector
import com.example.examplebluetooth.databinding.ActivityElevatorBinding

class ElevatorActivity : ServiceActivity() {
    private lateinit var binding: ActivityElevatorBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityElevatorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        serviceName = "Elevator"
        binding.lottieDownDirection.rotation = 180f
        mDetector = GestureDetector(this, SwipeListener(::swipeLeft, ::swipeRight))
    }

    override fun startDetection() {
        mService.switchServiceMode(RequestObject("elevator"))
    }

    override fun receiveMessage(it: String, command: String?) {
        if (it =="elevator direction") {
            mtts.speak(command, TextToSpeech.QUEUE_FLUSH, null, null)
            binding.commandTv.text = command
            when (command) {
                "電梯向上" ->
                    binding.lottieUpDirection.playAnimation()
                "電梯向下" ->
                    binding.lottieDownDirection.playAnimation()
                "電梯靜止" ->
                    binding.lottieStop.playAnimation()
            }
        } else if (it == "switch mode") {
            if (command == "電梯模式") {
                Log.i("TTSSpeech", "switch mode: Elevator")
                mtts.speak(command, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }

    override fun swipeLeft() {
        Log.i("Swipe", "swipe left")
        Intent(this@ElevatorActivity, ObstacleActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_FORWARD_RESULT
//            unregisterReceiver(mBtReceiver)
            startActivity(it)
            finish()
        }
    }

    override fun swipeRight() {
        Log.i("Swipe", "swipe right")
        return
    }
}