package com.example.examplebluetooth

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.airbnb.lottie.Lottie
import com.airbnb.lottie.LottieAnimationView

class LoadingDialog(mActivity: Activity, val mContext: Context) :
    FloatingDialog(mActivity, R.layout.loading_dialog) {
    private lateinit var loadingTV: TextView
    private lateinit var loadingLottie: LottieAnimationView
    private lateinit var checkLottie: LottieAnimationView
    private lateinit var failLottie: LottieAnimationView

    init {
        loadingTV = mView.findViewById(R.id.dialog_text)
        loadingLottie = mView.findViewById(R.id.lottie_loading)
        checkLottie = mView.findViewById(R.id.lottie_check)
        failLottie = mView.findViewById(R.id.lottie_fail)
    }

    override fun startLottie() {
        checkLottie.visibility = View.INVISIBLE
        failLottie.visibility = View.INVISIBLE
        loadingLottie.visibility = View.VISIBLE
        loadingTV.setTextColor(ContextCompat.getColor(mContext, R.color.green_700))
        loadingTV.text = "連線中"
        loadingLottie.playAnimation();
    }

    fun loadingSuccessful() {
        loadingLottie.cancelAnimation()
        loadingLottie.visibility = View.INVISIBLE
        loadingTV.text = "連線成功!"
        checkLottie.visibility = View.VISIBLE
        checkLottie.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
                return
            }

            override fun onAnimationEnd(p0: Animator?) {
                Handler(Looper.getMainLooper()).postDelayed({
                    dismissDialog()
                }, 500)
            }

            override fun onAnimationCancel(p0: Animator?) {
                return
            }

            override fun onAnimationRepeat(p0: Animator?) {
                return
            }

        })
        checkLottie.playAnimation()
    }

    fun loadFail() {
        loadingLottie.cancelAnimation()
        loadingLottie.visibility = View.INVISIBLE
        loadingTV.setTextColor(ContextCompat.getColor(mContext, R.color.red_300))
        loadingTV.text = "連線失敗!"
        failLottie.visibility = View.VISIBLE
        failLottie.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
                return
            }

            override fun onAnimationEnd(p0: Animator?) {
                Handler(Looper.getMainLooper()).postDelayed({
                    dismissDialog()
                }, 1000)
            }

            override fun onAnimationCancel(p0: Animator?) {
                return
            }

            override fun onAnimationRepeat(p0: Animator?) {
                return
            }

        })
        failLottie.playAnimation()
    }
}