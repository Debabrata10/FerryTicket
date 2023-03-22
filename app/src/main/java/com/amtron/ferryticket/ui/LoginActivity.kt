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
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.amtron.ferryticket.R
import com.amtron.ferryticket.databinding.ActivityLoginBinding
import com.amtron.ferryticket.helper.NotificationHelper
import com.amtron.ferryticket.helper.ResponseHelper
import com.amtron.ferryticket.helper.Util
import com.amtron.ferryticket.model.User
import com.amtron.ferryticket.network.Client
import com.amtron.ferryticket.network.RetrofitHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.google.mlkit.common.sdkinternal.CommonUtils.getAppVersion
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@DelicateCoroutinesApi
class LoginActivity : AppCompatActivity() {
	private lateinit var sharedPreferences: SharedPreferences
	private lateinit var tidSharedPreferences: SharedPreferences
	private lateinit var editor: SharedPreferences.Editor
	private lateinit var tidEditor: SharedPreferences.Editor
	private lateinit var binding: ActivityLoginBinding
	private lateinit var userString: String
	private var pinOn: Boolean = true

	@SuppressLint("SetTextI18n")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityLoginBinding.inflate(layoutInflater)
		setContentView(binding.root)

		if (!Util().isOnline(this)) {
			val noInternetAlert = SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
			noInternetAlert.setCancelable(false)
			noInternetAlert.titleText = "No Internet."
			noInternetAlert.contentText = "Please connect to internet and try again."
			noInternetAlert.confirmText = "RETRY"
			noInternetAlert.setConfirmClickListener {
				intent
			}
			noInternetAlert.setCancelClickListener {
				finishAffinity()
			}
		} else {
			sharedPreferences = this.getSharedPreferences("IWTCounter", MODE_PRIVATE)
			tidSharedPreferences = this.getSharedPreferences("IWT_TID", MODE_PRIVATE)
			editor = sharedPreferences.edit()
			tidEditor = tidSharedPreferences.edit()

			try {
				val tid = tidSharedPreferences.getString("tid", "").toString()
				if (tid.isEmpty()) {
					showEnterTidBottomSheet()
				} else {
					proceedToLogin()
				}
			} catch (e: IllegalStateException) {
				showEnterTidBottomSheet()
			}
		}

	}

	@SuppressLint("SetTextI18n")
	private fun proceedToLogin() {
		/*try {
			val appVersion = tidSharedPreferences.getString("version", "").toString().toDouble()
			if (getAppVersion() == appVersion) {
				//let proceed
			} else {
				//update from url
			}
		} catch (e: Exception) {
			val version = getAppVersion()
			tidEditor.putString("version", version.toString())
			tidEditor.apply()
			//proceed
		}*/

		userString = sharedPreferences.getString("user", "").toString()
		if (userString.isNotEmpty()) {
			val intent = Intent(this@LoginActivity, HomeActivity::class.java)
			startActivity(intent)
		}

		binding.buttonProceed.setOnClickListener {
			if (pinOn) {
				login(
					binding.phoneNumber.text.toString(),
					binding.password.text.toString()
				)
				/*startActivity(
					Intent(this@LoginActivity, HomeActivity::class.java)
				)*/
			} else {
				getPin(binding.phoneNumber.text.toString())
			}
		}

		binding.forgotPin.setOnClickListener {
			if (pinOn) {
				pinOn = false
				binding.textInputLayout2.visibility = View.GONE
				binding.forgotPin.text = "Login with PIN"
				binding.buttonProceed.text = "SEND OTP"
			} else {
				pinOn = true
				binding.textInputLayout2.visibility = View.VISIBLE
				binding.forgotPin.text = "Forgot PIN"
				binding.buttonProceed.text = "LOGIN"
			}
		}
	}

	private fun getAppVersion(): Double {
		var version = 1.01
		binding.progressbar.show()
		val api = RetrofitHelper.getInstance().create(Client::class.java)
		GlobalScope.launch {
			val call: Call<JsonObject> = api.getAppVersion("COUNTER")
			call.enqueue(object : Callback<JsonObject> {
				@SuppressLint("CommitPrefEdits", "NotifyDataSetChanged", "SetTextI18n")
				override fun onResponse(
					call: Call<JsonObject>,
					response: Response<JsonObject>
				) {
					if (response.isSuccessful) {
						binding.progressbar.hide()
						val helper = ResponseHelper()
						helper.ResponseHelper(response.body())
						if (helper.isStatusSuccessful()) {
							val obj = JSONObject(helper.getDataAsString())
							version = obj.get("version") as Double
						} else {
							NotificationHelper().getErrorAlert(
								this@LoginActivity,
								helper.getErrorMsg()
							)
						}
					} else {
						binding.progressbar.hide()
						NotificationHelper().getErrorAlert(
							this@LoginActivity,
							"Response Error Code" + response.message()
						)
					}
				}

				override fun onFailure(call: Call<JsonObject>, t: Throwable) {
					binding.progressbar.hide()
					NotificationHelper().getErrorAlert(this@LoginActivity, "Server Error")
				}
			})
		}
		return version
	}

	private fun showEnterTidBottomSheet() {
		val enterTidBottomSheet = BottomSheetDialog(this@LoginActivity)
		enterTidBottomSheet.setCancelable(false)
		enterTidBottomSheet.setContentView(R.layout.enter_device_serial_number_layout)
		val tid = enterTidBottomSheet.findViewById<TextView>(R.id.serial_number)
		val getTidBtn = enterTidBottomSheet.findViewById<MaterialButton>(R.id.btn_getTid)
		val cancelBtn = enterTidBottomSheet.findViewById<MaterialButton>(R.id.btn_cancel)
		enterTidBottomSheet.show()

		getTidBtn?.setOnClickListener {
			if (tid!!.text.isEmpty()) {
				Toast.makeText(this@LoginActivity, "Please enter serial number", Toast.LENGTH_SHORT).show()
			} else {
				val dialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
				dialog.progressHelper.barColor = Color.parseColor("#2E74A0")
				dialog.titleText = "Getting TID..."
				dialog.setCancelable(false)
				dialog.show()
				val api = RetrofitHelper.getInstance().create(Client::class.java)
				GlobalScope.launch {
					val call: Call<JsonObject> = api.getTid(
						null,
						tid.text.toString().toLong()
					)
					call.enqueue(object : Callback<JsonObject> {
						@SuppressLint("CommitPrefEdits", "NotifyDataSetChanged", "SetTextI18n")
						override fun onResponse(
							call: Call<JsonObject>,
							response: Response<JsonObject>
						) {
							if (response.isSuccessful) {
								val helper = ResponseHelper()
								helper.ResponseHelper(response.body())
								if (helper.isStatusSuccessful()) {
									dialog.titleText = "TID Fetched"
									dialog.dismissWithAnimation()
									val obj = JSONObject(helper.getDataAsString())
									val receivedTid = obj.get("tid") as String
									tidEditor.putString("tid", receivedTid)
									tidEditor.apply()
									proceedToLogin()
								} else {
									dialog.dismiss()
									NotificationHelper().getErrorAlert(
										this@LoginActivity,
										helper.getErrorMsg()
									)
								}
							} else {
								dialog.dismiss()
								NotificationHelper().getErrorAlert(
									this@LoginActivity,
									"Response Error Code" + response.message()
								)
							}
						}

						override fun onFailure(call: Call<JsonObject>, t: Throwable) {
							NotificationHelper().getErrorAlert(this@LoginActivity, "Server Error")
							dialog.dismiss()
						}
					})
				}
			}
		}
		cancelBtn?.setOnClickListener { finishAffinity() }
	}

	private fun getPin(number: String) {
		if (number.isEmpty()) {
			NotificationHelper().getErrorAlert(this, "Please enter phone number")
		} else {
			if (!Util().isOnline(this)) {
				val noInternetAlert = SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
				noInternetAlert.titleText = "No Internet."
				noInternetAlert.contentText = "Please connect to internet and try again."
				noInternetAlert.confirmText = "RETRY"
				noInternetAlert.setConfirmClickListener {
					getPin(number)
				}
			} else {
				binding.progressbar.show()
				val api = RetrofitHelper.getInstance().create(Client::class.java)
				GlobalScope.launch {
					val call: Call<JsonObject> = api.getPin(number.toLong())
					call.enqueue(object : Callback<JsonObject> {
						@SuppressLint("CommitPrefEdits", "NotifyDataSetChanged", "SetTextI18n")
						override fun onResponse(
							call: Call<JsonObject>,
							response: Response<JsonObject>
						) {
							if (response.isSuccessful) {
								binding.progressbar.hide()
								val helper = ResponseHelper()
								helper.ResponseHelper(response.body())
								if (helper.isStatusSuccessful()) {
									Toast.makeText(
										this@LoginActivity,
										"OTP sent to mobile number",
										Toast.LENGTH_SHORT
									).show()
									pinOn = true
									binding.textInputLayout2.visibility = View.VISIBLE
									binding.forgotPin.text = "Forgot PIN"
									binding.buttonProceed.text = "LOGIN"
									binding.buttonProceed.setOnClickListener {
										login(
											binding.phoneNumber.text.toString(),
											binding.password.text.toString()
										)
									}
								} else {
									NotificationHelper().getErrorAlert(
										this@LoginActivity,
										helper.getErrorMsg()
									)
								}
							} else {
								binding.progressbar.hide()
								NotificationHelper().getErrorAlert(
									this@LoginActivity,
									"Response Error Code" + response.message()
								)
							}
						}

						override fun onFailure(call: Call<JsonObject>, t: Throwable) {
							binding.progressbar.hide()
							NotificationHelper().getErrorAlert(this@LoginActivity, "Server Error")
						}
					})
				}
			}
		}
	}

	private fun login(number: String, pin: String) {
		if (number.isEmpty() || pin.isEmpty()) {
			NotificationHelper().getErrorAlert(this, "Please enter all the fields")
		} else {
			if (!Util().isOnline(this)) {
				val noInternetAlert = SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
				noInternetAlert.titleText = "No Internet."
				noInternetAlert.contentText = "Please connect to internet and try again."
				noInternetAlert.confirmText = "RETRY"
				noInternetAlert.setConfirmClickListener {
					login(number, pin)
				}
			} else {
				binding.progressbar.show()
				val api = RetrofitHelper.getInstance().create(Client::class.java)
				GlobalScope.launch {
					val call: Call<JsonObject> = api.login(number.toLong(), pin.toInt())
					call.enqueue(object : Callback<JsonObject> {
						@SuppressLint("CommitPrefEdits", "NotifyDataSetChanged")
						override fun onResponse(
							call: Call<JsonObject>,
							response: Response<JsonObject>
						) {
							if (response.isSuccessful) {
								binding.progressbar.hide()
								val helper = ResponseHelper()
								helper.ResponseHelper(response.body())
								if (helper.isStatusSuccessful()) {
									val user: User = Gson().fromJson(
										helper.getDataAsString(),
										object : TypeToken<User>() {}.type
									)
									editor.putString("user", Gson().toJson(user))
									editor.apply()
									startActivity(
										Intent(
											this@LoginActivity,
											HomeActivity::class.java
										)
									)
								} else {
									NotificationHelper().getErrorAlert(
										this@LoginActivity,
										helper.getErrorMsg()
									)
								}
							} else {
								binding.progressbar.hide()
								NotificationHelper().getErrorAlert(
									this@LoginActivity,
									"Response Error Code" + response.message()
								)
							}
						}

						override fun onFailure(call: Call<JsonObject>, t: Throwable) {
							binding.progressbar.hide()
							NotificationHelper().getErrorAlert(this@LoginActivity, "Server Error")
						}
					})
				}
			}
		}
	}
}