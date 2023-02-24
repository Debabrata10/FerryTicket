package com.amtron.ferryticket.helper

import android.content.Context
import cn.pedant.SweetAlert.SweetAlertDialog

class NotificationHelper {
    fun getSuccessAlert(context: Context, text: String) {
        SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText("SUCCESS!")
            .setContentText(text)
            .show()
    }

    fun getWarningAlert(context: Context, text: String) {
        SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("WARNING!")
            .setContentText(text)
            .show()
    }

    fun getErrorAlert(context: Context, text: String) {
        SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
            .setTitleText("ERROR!")
            .setContentText(text)
            .show()
    }
}