package com.amtron.ferryticket.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.amtron.ferryticket.R
import com.amtron.ferryticket.databinding.ActivityProfileBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogOut.setOnClickListener {
            val logoutBottomSheet = BottomSheetDialog(this)
            logoutBottomSheet.setCancelable(false)
            logoutBottomSheet.setContentView(R.layout.exit_bottom_sheet_layout)
            val title = logoutBottomSheet.findViewById<TextView>(R.id.title)
            val header = logoutBottomSheet.findViewById<TextView>(R.id.header)
            val success = logoutBottomSheet.findViewById<Button>(R.id.success)
            val cancel = logoutBottomSheet.findViewById<Button>(R.id.cancel)
            title?.text = "Are you sure you want to logout?"
            header?.text = "LOGOUT?"
            success?.text = "YES"
            cancel?.text = "CANCEL"
            logoutBottomSheet.show()

            success?.setOnClickListener {
                finishAffinity()
            }
            cancel?.setOnClickListener { logoutBottomSheet.dismiss() }
        }
    }
}