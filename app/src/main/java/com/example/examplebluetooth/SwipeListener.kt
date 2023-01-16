package com.example.examplebluetooth

import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import kotlin.math.abs
class SwipeListener(val swipeLeft: ()->Unit, val swipeRight: () -> Unit) :
    SimpleOnGestureListener() {
    private val swipeThreshold = 100
    private val swipeVelocityThreshold = 100

    override fun onDown(e: MotionEvent): Boolean {
        return true
    }

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        try {
            val diffY = e2.y - e1.y
            val diffX = e2.x - e1.x
            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > swipeThreshold && abs(velocityX) > swipeVelocityThreshold) {
                    if (diffX > 0) {
                        swipeLeft()
                    } else {
                        swipeRight()
                    }
                }
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return true
    }

}
