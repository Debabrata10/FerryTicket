package com.amtron.ferryticket.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.amtron.ferryticket.databinding.ActivityLoginBinding
import com.amtron.ferryticket.helper.NotificationHelper
import com.amtron.ferryticket.helper.ResponseHelper
import com.amtron.ferryticket.helper.Util
import com.amtron.ferryticket.model.User
import com.amtron.ferryticket.network.Client
import com.amtron.ferryticket.network.RetrofitHelper
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@DelicateCoroutinesApi
class LoginActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var binding: ActivityLoginBinding
    private lateinit var userString: String
    private var pinOn: Boolean = true

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = this.getSharedPreferences("IWTCounter", MODE_PRIVATE)
        editor = sharedPreferences.edit()
        userString = sharedPreferences.getString("user", "").toString()
        if (userString.isNotEmpty()) {
            val intent = Intent(this@LoginActivity, HomeActivity::class.java)
            startActivity(intent)
        }

        binding.buttonProceed.setOnClickListener {
            if (pinOn) {
                /*login(
                        binding.phoneNumber.text.toString(),
                        binding.password.text.toString()
                    )*/
                startActivity(
                    Intent(this@LoginActivity, HomeActivity::class.java)
                )
            } else {
                if (binding.phoneNumber.text.toString().isEmpty()) {
                    Toast.makeText(this, "Please enter mobile number", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    //API for sms
                    Toast.makeText(this, "OTP sent to mobile number", Toast.LENGTH_SHORT).show()
                }
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

    private fun login(number: String, password: String) {
        if (number.isEmpty() || password.isEmpty()) {
            NotificationHelper().getErrorAlert(this, "Please enter all the fields")
        } else {
            if (!Util().isOnline(this)) {
                val noInternetAlert = SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                noInternetAlert.titleText = "No Internet."
                noInternetAlert.contentText = "Please connect to internet and try again."
                noInternetAlert.confirmText = "RETRY"
                noInternetAlert.setConfirmClickListener {
                    login(number, password)
                }
            } else {
                binding.progressbar.show()
                val api = RetrofitHelper.getInstance().create(Client::class.java)
                GlobalScope.launch {
                    val call: Call<JsonObject> = api.login(number, password)
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