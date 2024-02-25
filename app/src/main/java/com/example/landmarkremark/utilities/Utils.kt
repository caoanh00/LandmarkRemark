package com.example.landmarkremark.utilities

import android.app.Activity
import android.content.Context
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import java.util.regex.Pattern

class Utils {

    companion object {
        // Hide keyboard with activity
        fun hideKeyboard(activity: Activity) {
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            var view = activity.currentFocus
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        // Hide keyboard with view
        fun hideKeyboard(view: View) {
            val imm =
                view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm!!.hideSoftInputFromWindow(view.windowToken, 0)
        }

        /**
         * Helper method to check if enter has been clicked
         */
        fun isEnterPressed(keyCode: Int, event: KeyEvent): Boolean {
            return event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER
        }

        /**
         * Helper method to check email format
         */
        fun isEmailInvalid(email: String): Boolean {
            val str = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
            val p = Pattern.compile(str)
            val m = p.matcher(email)
            return !m.matches()
        }
    }
}
