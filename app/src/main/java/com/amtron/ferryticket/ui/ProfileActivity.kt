package com.amtron.ferryticket.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.amtron.ferryticket.R
import com.amtron.ferryticket.databinding.ActivityProfileBinding
import com.amtron.ferryticket.model.User
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class ProfileActivity : AppCompatActivity() {
	private lateinit var binding: ActivityProfileBinding
	private lateinit var sharedPreferences: SharedPreferences
	private lateinit var editor: SharedPreferences.Editor
	private lateinit var user: User

	@SuppressLint("SetTextI18n")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityProfileBinding.inflate(layoutInflater)
		setContentView(binding.root)

		sharedPreferences = this.getSharedPreferences("IWTCounter", MODE_PRIVATE)
		editor = sharedPreferences.edit()

		val userString = sharedPreferences.getString("user", "").toString()

		if (userString.isNotEmpty()) {
			user = Gson().fromJson(
				userString,
				object : TypeToken<User>() {}.type
			)
			binding.name.text = "${user.name} ${user.last_name}"
			binding.phone.text = user.mobile
			binding.email.text = user.email
			binding.role.text = user.role
		}

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
				editor.clear().apply()
				startActivity(Intent(this, LoginActivity::class.java))
			}
			cancel?.setOnClickListener { logoutBottomSheet.dismiss() }
		}

		onBackPressedDispatcher.addCallback(this) {
			startActivity(
				Intent(
					this@ProfileActivity,
					HomeActivity::class.java
				)
			)
		}
	}
}