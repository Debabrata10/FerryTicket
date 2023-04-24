package com.amtron.ferryticket.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.amtron.ferryticket.R
import com.amtron.ferryticket.databinding.ActivityProfileBinding
import com.amtron.ferryticket.helper.NotificationHelper
import com.amtron.ferryticket.helper.ResponseHelper
import com.amtron.ferryticket.helper.Util
import com.amtron.ferryticket.model.User
import com.amtron.ferryticket.network.Client
import com.amtron.ferryticket.network.RetrofitHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@DelicateCoroutinesApi
class ProfileActivity : AppCompatActivity() {
	private lateinit var binding: ActivityProfileBinding
	private lateinit var sharedPreferences: SharedPreferences
	private lateinit var tidSharedPreferences: SharedPreferences
	private lateinit var editor: SharedPreferences.Editor
	private lateinit var tidEditor: SharedPreferences.Editor
	private lateinit var user: User

	@SuppressLint("SetTextI18n")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityProfileBinding.inflate(layoutInflater)
		setContentView(binding.root)

		sharedPreferences = this.getSharedPreferences("IWTCounter", MODE_PRIVATE)
		tidSharedPreferences = this.getSharedPreferences("IWT_TID", MODE_PRIVATE)
		editor = sharedPreferences.edit()
		tidEditor = tidSharedPreferences.edit()

		val userString = sharedPreferences.getString("user", "").toString()
		user = Gson().fromJson(
			userString,
			object : TypeToken<User>() {}.type
		)
		binding.name.text = "${user.name} ${user.last_name}"
		binding.phone.text = user.mobile
		binding.email.text = user.email
		binding.role.text = user.role

		binding.tid.text = tidSharedPreferences.getString("tid", "").toString()
		binding.version.text = sharedPreferences.getString("version", "").toString()
		binding.buildDate.text = sharedPreferences.getString("build_date", "").toString()
		//To change tid from inside app
		/*try {
			val tid = sharedPreferences.getString("tid", "").toString()
			if (tid.isEmpty()) {
				binding.tidLl.visibility = View.GONE
				binding.enterTidLl.visibility = View.GONE
				binding.btnEnterTid.visibility = View.VISIBLE
				binding.btnEnterTid.setOnClickListener {
					getTid()
				}
			} else {
				binding.tid.text = tid
				binding.btnEnterTid.text = "CHANGE TID"
				binding.btnEnterTid.setOnClickListener {
					getTid()
				}
				binding.textInputLayout.visibility = View.GONE
			}
		} catch (e: Exception) {
			binding.tidLl.visibility = View.GONE
			binding.btnEnterTid.visibility = View.VISIBLE
			binding.btnEnterTid.setOnClickListener {
				getTid()
			}
		}*/

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

	private fun getTid() {
		binding.enterTidLl.visibility = View.VISIBLE
		binding.profileInfoLl.visibility = View.GONE
		binding.buttonsLl.visibility = View.GONE
		binding.btnGetTid.setOnClickListener {
			if (binding.serialNumber.text.isEmpty()) {
				Toast.makeText(
					this@ProfileActivity,
					"Please enter serial number",
					Toast.LENGTH_SHORT
				).show()
			} else {
				val dialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
				dialog.progressHelper.barColor = Color.parseColor("#2E74A0")
				dialog.titleText = "Getting TID..."
				dialog.setCancelable(false)
				dialog.show()
				val api = RetrofitHelper.getInstance().create(Client::class.java)
				GlobalScope.launch {
					val call: Call<JsonObject> = api.getTid(
						Util().getJwtToken(sharedPreferences.getString("user", "").toString()),
						binding.serialNumber.text.toString().toLong()
					)
					call.enqueue(object : Callback<JsonObject> {
						@SuppressLint("CommitPrefEdits", "NotifyDataSetChanged", "SetTextI18n")
						override fun onResponse(
							call: Call<JsonObject>,
							response: Response<JsonObject>
						) {
							if (response.isSuccessful) {
								val helper = ResponseHelper()
								helper.responseHelper(response.body())
								if (helper.isStatusSuccessful()) {
									dialog.titleText = "TID Fetched"
									dialog.dismissWithAnimation()
									val obj = JSONObject(helper.getDataAsString())
									val receivedTid = obj.get("tid") as String
									editor.putString("tid", receivedTid)
									editor.apply()
									binding.tid.text = receivedTid
									binding.btnEnterTid.visibility = View.GONE
									binding.enterTidLl.visibility = View.GONE
									binding.tid.text = receivedTid
									binding.profileInfoLl.visibility = View.VISIBLE
									binding.tidLl.visibility = View.GONE
								} else {
									dialog.dismiss()
									NotificationHelper().getErrorAlert(
										this@ProfileActivity,
										helper.getErrorMsg()
									)
								}
							} else {
								dialog.dismiss()
								NotificationHelper().getErrorAlert(
									this@ProfileActivity,
									"Response Error Code" + response.message()
								)
							}
						}

						override fun onFailure(call: Call<JsonObject>, t: Throwable) {
							NotificationHelper().getErrorAlert(this@ProfileActivity, "Server Error")
							dialog.dismiss()
						}
					})
				}
			}
		}
	}
}