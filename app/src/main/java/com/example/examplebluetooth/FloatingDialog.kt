package com.example.examplebluetooth

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.util.Log
import android.view.View

abstract class FloatingDialog constructor(private val mActivity: Activity, private val mLayout: Int){
    protected var mAlertDialog: AlertDialog
    protected var mView : View

    init {
        val builder = AlertDialog.Builder(mActivity)
        val inflater = mActivity.layoutInflater
        mView = inflater.inflate(mLayout, null)
        builder.setView(mView)
        builder.setCancelable(false)
        mAlertDialog = builder.create()
        Log.i(TAG, "alertDialog has been initialized")
    }

    fun showDialog() {
        mAlertDialog.show()
        startLottie()
    }

    fun dismissDialog() {
        mAlertDialog.dismiss()
    }
    abstract fun startLottie()

    fun getDialog(): AlertDialog {
        return mAlertDialog;
    }
}