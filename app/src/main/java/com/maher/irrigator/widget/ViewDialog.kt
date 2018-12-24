package com.maher.irrigator.widget

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.maher.irrigator.R

class ViewDialog {


    fun showErrorDialog(activity: Context, data: String) {
        val dialog = Dialog(activity, R.style.Dialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.item_dialog)

        val content = dialog.findViewById<TextView>(R.id.content)
        val ok = dialog.findViewById<TextView>(R.id.ok)

        content.text = data
        ok.text = activity.getString(R.string.no)

        ok.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }



}