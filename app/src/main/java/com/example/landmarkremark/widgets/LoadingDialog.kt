package com.example.landmarkremark.widgets

import android.app.Activity
import android.app.Dialog
import com.example.landmarkremark.R

class LoadingDialog(activity: Activity?) {

    private val loadingDialog: Dialog = Dialog(activity!!)

    init {
        loadingDialog.setContentView(R.layout.dialog_loading)
    }

    fun show(isCancelable: Boolean) {
        loadingDialog.setCancelable(isCancelable)
        loadingDialog.show()
    }

    fun dismiss() {
        loadingDialog.dismiss()
    }
}