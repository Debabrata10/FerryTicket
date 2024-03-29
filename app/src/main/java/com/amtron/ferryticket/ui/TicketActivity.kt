package com.amtron.ferryticket.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.amtron.ferryticket.R
import com.amtron.ferryticket.adapter.OtherDetailsTicketViewAdapter
import com.amtron.ferryticket.adapter.PassengerDetailsTicketViewAdapter
import com.amtron.ferryticket.adapter.VehicleDetailsTicketViewAdapter
import com.amtron.ferryticket.databinding.ActivityTicketBinding
import com.amtron.ferryticket.helper.DateAndTimeHelper
import com.amtron.ferryticket.helper.NotificationHelper
import com.amtron.ferryticket.helper.ResponseHelper
import com.amtron.ferryticket.helper.Util
import com.amtron.ferryticket.model.*
import com.amtron.ferryticket.network.Client
import com.amtron.ferryticket.network.RetrofitHelper.getInstance
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.DelicateCoroutinesApi
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

@DelicateCoroutinesApi
class TicketActivity : AppCompatActivity() {
	private lateinit var sharedPreferences: SharedPreferences
	private lateinit var editor: SharedPreferences.Editor
	private lateinit var binding: ActivityTicketBinding
	private lateinit var ticket: Ticket
	private lateinit var passengerDetailsRecyclerView: RecyclerView
	private lateinit var vehicleRecyclerView: RecyclerView
	private lateinit var otherRecyclerView: RecyclerView
	private lateinit var passengerDetailsTicketViewAdapter: PassengerDetailsTicketViewAdapter
	private lateinit var vehicleDetailsTicketViewAdapter: VehicleDetailsTicketViewAdapter
	private lateinit var otherDetailsTicketViewAdapter: OtherDetailsTicketViewAdapter
	private lateinit var walletPayBottomSheet: BottomSheetDialog
	private lateinit var operatorWallet: CardDetails
	lateinit var walletLoaderDialog: SweetAlertDialog
	private var passengerWallet: CardDetails? = null
	private var isUserWalletAvailable: Boolean = false
	private var isPassengerWalletAvailable: Boolean = false
	private var totalPosAmt: Double = 0.0
	private var totalWalletAmt: Double = 0.0
	private var operatorWalletAmount: String = ""

	@SuppressLint("SetTextI18n", "CommitPrefEdits")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityTicketBinding.inflate(layoutInflater)
		setContentView(binding.root)

		sharedPreferences = this.getSharedPreferences("IWTCounter", MODE_PRIVATE)
		editor = sharedPreferences.edit()
		val bundle = intent.extras

		try {
			val ticketString = sharedPreferences.getString("ticket", "").toString()
			ticket = Gson().fromJson(ticketString, object : TypeToken<Ticket>() {}.type)
			totalPosAmt = ticket.net_amt + ticket.service_amt
			totalWalletAmt = if (ticket.wallet_service_charge == 1) ticket.net_amt + ticket.service_amt else ticket.total_amt
			binding.cashPay.text = "CASH PAYMENT ₹${ticket.net_amt}"
			binding.posPay.text = "POS PAYMENT ₹$totalPosAmt"
			binding.walletPay.text = "WALLET PAYMENT ₹$totalWalletAmt"
			if (ticket.order_status == "SUCCESS") {
				binding.paymentButtons.visibility = View.GONE
				binding.printBtn.visibility = View.VISIBLE
			}
		} catch (e: Exception) {
			Log.d("ticket details", "not found")
			Toast.makeText(this, "Ticket - Something went wrong!", Toast.LENGTH_SHORT).show()
			startActivity(Intent(this, TicketListActivity::class.java))
		}

		try {
			val userString = sharedPreferences.getString("user", "").toString()
			val user: User = Gson().fromJson(userString, object : TypeToken<User>() {}.type)
			if (user.card_details != null) {
				isUserWalletAvailable = true
				operatorWallet = user.card_details
				updateOperatorWalletBalance(
					Util().getJwtToken(
						sharedPreferences.getString(
							"user",
							""
						)
					)
				)
			}
		} catch (e: Exception) {
			Log.d("user details", "not found")
		}

		try {
			val cardString = sharedPreferences.getString("passenger_card_details", "").toString()
			if (cardString.isNotEmpty()) {
				isPassengerWalletAvailable = true
				passengerWallet =
					Gson().fromJson(cardString, object : TypeToken<CardDetails>() {}.type)
			}
		} catch (e: Exception) {
			Log.d("passenger card details", "not found")
		}

		//code to check for card status if paid through operator/passenger card
		if (bundle != null) {
			try {
				if (bundle.getString("info", "").toString() == "check_status" ) {
					binding.checkCardStatus.visibility = View.VISIBLE
					binding.checkCardStatus.setOnClickListener {
						checkPaymentStatusByCard(
							Util().getJwtToken(sharedPreferences.getString("user", "").toString()),
							ticket.ticket_no
						)
					}
				}
			} catch (e : Exception) {
				println("Bundle is null")
			}
		}

		binding.printBtn.setOnClickListener {
			startActivity(Intent(this, InAppApprovedActivity::class.java))
		}

		binding.walletPay.setOnClickListener {
			walletPayBottomSheet = BottomSheetDialog(this)
			walletPayBottomSheet.setContentView(R.layout.wallet_pay_bottomsheet_layout)
			val walletButtonsLayout =
				walletPayBottomSheet.findViewById<LinearLayout>(R.id.wallet_buttons_layout)
			val layoutParams = LinearLayout.LayoutParams(
				350,
				ViewGroup.LayoutParams.WRAP_CONTENT
			)
			layoutParams.setMargins(5, 0, 5, 0)
			if (!isPassengerWalletAvailable && !isUserWalletAvailable) {
				walletButtonsLayout!!.visibility = View.GONE
			} else {
				if (isUserWalletAvailable) {
					val operatorWalletButton = MaterialButton(this@TicketActivity)
					operatorWalletButton.text =
						"OPERATOR WALLET ₹${operatorWalletAmount}"
					operatorWalletButton.textSize = 12F
					operatorWalletButton.setTextColor(Color.parseColor("#ffffff"))
					operatorWalletButton.layoutParams = layoutParams
					operatorWalletButton.setOnClickListener {
						payWithWallet(operatorWallet.id.toInt(), ticket.id, operatorWallet)
					}
					walletButtonsLayout!!.addView(operatorWalletButton)
				}
				if (isPassengerWalletAvailable) {
					val passengerWalletButton = MaterialButton(this@TicketActivity)
					passengerWalletButton.text =
						"PASSENGER WALLET ₹${passengerWallet!!.wallet_amount}"
					passengerWalletButton.textSize = 12F
					passengerWalletButton.setTextColor(Color.parseColor("#ffffff"))
					passengerWalletButton.layoutParams = layoutParams
					passengerWalletButton.setOnClickListener {
						payWithWallet(passengerWallet!!.id.toInt(), ticket.id, passengerWallet!!)
					}
					walletButtonsLayout!!.addView(passengerWalletButton)
				}
			}
			walletPayBottomSheet.show()
		}

		if (ticket.two_way == 0) {
			binding.isTwoWayImg.visibility = View.GONE
			binding.isTwoWayText.visibility = View.GONE
		}

		binding.ticketNo.text = ticket.ticket_no
		binding.ferryName.text = ticket.ferry.ferry_name
		binding.departureTime.text = DateAndTimeHelper().changeTimeFormat(ticket.fs_departure_time)
		binding.arrivalTime.text = DateAndTimeHelper().changeTimeFormat(ticket.fs_reached_time)
		binding.ticketDate.text =
			DateAndTimeHelper().changeDateFormat("dd MMM, yyyy", ticket.ferry_date)

		val passengerDetailsList = ArrayList<PassengerDetails>()
		passengerDetailsList.addAll(ticket.passenger)

		val vehiclesList = ArrayList<Vehicle>()
		vehiclesList.addAll(ticket.vehicle)

		val othersList = ArrayList<Others>()
		othersList.addAll(ticket.other)

		if (passengerDetailsList.size > 0) {
			passengerDetailsTicketViewAdapter =
				PassengerDetailsTicketViewAdapter(passengerDetailsList)
			passengerDetailsRecyclerView = binding.ticketViewPassengerDetailsRecyclerView
			passengerDetailsRecyclerView.adapter = passengerDetailsTicketViewAdapter
			passengerDetailsRecyclerView.layoutManager =
				LinearLayoutManager(this)
			passengerDetailsRecyclerView.isNestedScrollingEnabled = false
			binding.ticketViewPassengerDetailsLL.visibility = View.VISIBLE
		} else {
			binding.ticketViewPassengerDetailsLL.visibility = View.GONE
		}

		if (othersList.size > 0) {
			otherDetailsTicketViewAdapter = OtherDetailsTicketViewAdapter(othersList)
			otherRecyclerView = binding.ticketViewOtherDetailsRecyclerView
			otherRecyclerView.adapter = otherDetailsTicketViewAdapter
			otherRecyclerView.layoutManager =
				LinearLayoutManager(this)
			otherRecyclerView.isNestedScrollingEnabled = false
			binding.ticketViewOtherDetailsLL.visibility = View.VISIBLE
		} else {
			binding.ticketViewOtherDetailsLL.visibility = View.GONE
		}

		if (vehiclesList.size > 0) {
			vehicleDetailsTicketViewAdapter = VehicleDetailsTicketViewAdapter(vehiclesList)
			vehicleRecyclerView = binding.ticketViewVehicleDetailsRecyclerView
			vehicleRecyclerView.adapter = vehicleDetailsTicketViewAdapter
			vehicleRecyclerView.layoutManager =
				LinearLayoutManager(this)
			vehicleRecyclerView.isNestedScrollingEnabled = false
			binding.ticketViewVehicleDetailsLL.visibility = View.VISIBLE
		} else {
			binding.ticketViewVehicleDetailsLL.visibility = View.GONE
		}

		binding.cashPay.setOnClickListener {
			val cashPaymentBottomSheet = BottomSheetDialog(this)
			cashPaymentBottomSheet.setCancelable(false)
			cashPaymentBottomSheet.setContentView(R.layout.exit_bottom_sheet_layout)
			val title = cashPaymentBottomSheet.findViewById<TextView>(R.id.title)
			val header = cashPaymentBottomSheet.findViewById<TextView>(R.id.header)
			val success = cashPaymentBottomSheet.findViewById<Button>(R.id.success)
			val cancel = cashPaymentBottomSheet.findViewById<Button>(R.id.cancel)
			title?.text = "Press confirm after collecting cash"
			header?.visibility = View.GONE
			success?.text = "CONFIRM"
			cancel?.text = "CANCEL"
			cashPaymentBottomSheet.show()

			success?.setOnClickListener {
				cashPay()
			}
			cancel?.setOnClickListener { cashPaymentBottomSheet.dismiss() }
		}

		binding.posPay.setOnClickListener {
			val sf = this.getSharedPreferences("IWT_TID",MODE_PRIVATE)
			val tEditor = sf.edit()
			tEditor.putString("ticket",Gson().toJson(ticket))
			tEditor.apply()
			val bundle = Bundle()
			val i = Intent(this, PosActivity::class.java)
			bundle.putString("price", totalPosAmt.toString())
			i.putExtras(bundle)
			startActivity(i)
		}

		onBackPressedDispatcher.addCallback(this) {
			if (sharedPreferences.getString("activity_from", "") == "ticketListActivity") {
				startActivity(Intent(this@TicketActivity, TicketListActivity::class.java))
			} else {
				val backToNewBookingBottomSheet = BottomSheetDialog(this@TicketActivity)
				backToNewBookingBottomSheet.setCancelable(false)
				backToNewBookingBottomSheet.setContentView(R.layout.exit_bottom_sheet_layout)
				val title = backToNewBookingBottomSheet.findViewById<TextView>(R.id.title)
				val header = backToNewBookingBottomSheet.findViewById<TextView>(R.id.header)
				val success = backToNewBookingBottomSheet.findViewById<Button>(R.id.success)
				val cancel = backToNewBookingBottomSheet.findViewById<Button>(R.id.cancel)
				title?.visibility = View.GONE
				header!!.text = "NEW BOOKING?"
				success!!.text = "OK"
				cancel!!.text = "CANCEL"
				backToNewBookingBottomSheet.show()

				success.setOnClickListener {
					editor.remove("ticket")
					editor.remove("passenger_card_details")
					editor.apply()
					startActivity(Intent(this@TicketActivity, BookActivity::class.java))

				}
				cancel.setOnClickListener { backToNewBookingBottomSheet.dismiss() }
			}
		}
	}

	private fun checkPaymentStatusByCard(jwtToken: String, ticketNo: String) {
		val checkStatusDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
		checkStatusDialog.progressHelper.barColor = Color.parseColor("#2E74A0")
		checkStatusDialog.contentText = "Checking previous card payment status"
		checkStatusDialog.setCancelable(false)
		checkStatusDialog.show()
		val client =
			getInstance(this)!!.create(
				Client::class.java
			)
		val call = client.checkCardStatus(
			jwtToken,
			ticketNo
		)
		call.enqueue(object : Callback<JsonObject?> {
			@SuppressLint("SetTextI18n")
			override fun onResponse(
				call: Call<JsonObject?>,
				response: Response<JsonObject?>
			) {
				if (response.isSuccessful) {
					val helper = ResponseHelper()
					helper.responseHelper(response.body())
					if (helper.isStatusSuccessful()) {
						val obj = JSONObject(helper.getDataAsString())
						val statusVal = obj.get("status") as Int
						if (statusVal == 1) {
							checkStatusDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
							checkStatusDialog.contentText = helper.getSuccessMsg()
							checkStatusDialog.setConfirmClickListener {
								checkStatusDialog.dismissWithAnimation()
							}
							binding.paymentButtons.visibility = View.GONE
							binding.printBtn.visibility = View.VISIBLE
							binding.printBtn.setOnClickListener {
								startActivity(Intent(this@TicketActivity, InAppApprovedActivity::class.java))
							}
						} else {
							checkStatusDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE)
							checkStatusDialog.contentText = helper.getSuccessMsg()
							checkStatusDialog.setConfirmClickListener {
								checkStatusDialog.dismissWithAnimation()
							}
							binding.checkCardStatus.visibility = View.GONE
						}
					} else {
						checkStatusDialog.dismiss()
						NotificationHelper().getErrorAlert(
							this@TicketActivity,
							helper.getErrorMsg()
						)
					}
				} else {
					checkStatusDialog.dismiss()
					NotificationHelper().getErrorAlert(
						this@TicketActivity,
						"Response Error Code " + response.code()
					)
				}
			}

			override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
				checkStatusDialog.dismiss()
				NotificationHelper().getErrorAlert(
					this@TicketActivity,
					"Server Error. Please try again."
				)
			}
		})
	}

	private fun cashPay() {
		val cashPayAlert = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
		cashPayAlert.titleText = "CONFIRMING.."
		cashPayAlert.contentText = "Wait till your order is confirmed"
		cashPayAlert.setCancelable(false)
		cashPayAlert.show()
		val client =
			getInstance(this)!!.create(
				Client::class.java
			)
		val call = client.cashPay(
			Util().getJwtToken(sharedPreferences.getString("user", "").toString()),
			ticket.id
		)
		call.enqueue(object : Callback<JsonObject?> {
			@SuppressLint("SetTextI18n")
			override fun onResponse(
				call: Call<JsonObject?>,
				response: Response<JsonObject?>
			) {
				if (response.isSuccessful) {
					val helper = ResponseHelper()
					helper.responseHelper(response.body())
					if (helper.isStatusSuccessful()) {
						cashPayAlert.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
						cashPayAlert.titleText = "SUCCESSFUL"
						cashPayAlert.contentText = "Booking successful"
						cashPayAlert.dismissWithAnimation()
						val obj = JSONObject(helper.getDataAsString())
						ticket.order_status = obj.get("order_status") as String
						ticket.mode_of_payment = obj.get("mode_of_payment") as String
						editor.putString("ticket", Gson().toJson(ticket))
						editor.apply()
						startActivity(
							Intent(
								this@TicketActivity,
								InAppApprovedActivity::class.java
							)
						)
					} else {
						cashPayAlert.dismiss()
						NotificationHelper().getErrorAlert(
							this@TicketActivity,
							helper.getErrorMsg()
						)
					}
				} else {
					cashPayAlert.dismiss()
					NotificationHelper().getErrorAlert(
						this@TicketActivity,
						"Response Error Code " + response.code()
					)
				}
			}

			override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
				cashPayAlert.dismiss()
				NotificationHelper().getErrorAlert(
					this@TicketActivity,
					"Server Error. Please try again."
				)
			}
		})
	}

	private fun updateOperatorWalletBalance(jwtToken: String) {
		val updateOperatorWalletLoaderDialog =
			SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
		updateOperatorWalletLoaderDialog.progressHelper.barColor = Color.parseColor("#2E74A0")
		updateOperatorWalletLoaderDialog.setCancelable(false)
		updateOperatorWalletLoaderDialog.titleText = "LOADING.."
		updateOperatorWalletLoaderDialog.show()
		val client =
			getInstance(this)!!.create(
				Client::class.java
			)
		val call = client.getOperatorUpdatedAfterPayment(jwtToken)
		call.enqueue(object : Callback<JsonObject?> {
			@SuppressLint("SetTextI18n")
			override fun onResponse(
				call: Call<JsonObject?>,
				response: Response<JsonObject?>
			) {
				if (response.isSuccessful) {
					val helper = ResponseHelper()
					helper.responseHelper(response.body())
					if (helper.isStatusSuccessful()) {
						updateOperatorWalletLoaderDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
						updateOperatorWalletLoaderDialog.titleText = "SUCCESSFUL"
						updateOperatorWalletLoaderDialog.dismissWithAnimation()
						val obj = JSONObject(helper.getDataAsString())
						operatorWalletAmount = obj.get("wallet_amount") as String
					} else {
						updateOperatorWalletLoaderDialog.dismiss()
						NotificationHelper().getErrorAlert(
							this@TicketActivity,
							helper.getErrorMsg()
						)
					}
				} else {
					/*updateOperatorWalletLoaderDialog.dismiss()
					NotificationHelper().getErrorAlert(
						this@TicketActivity,
						"Response Error Code " + response.code()
					)*/
					updateOperatorWalletBalance(jwtToken)
				}
			}

			override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
				updateOperatorWalletLoaderDialog.dismiss()
				NotificationHelper().getErrorAlert(
					this@TicketActivity,
					"Server Error. Please try again."
				)
			}
		})
	}

	private fun payWithWallet(cardId: Int, ticketId: Int, wallet: CardDetails) {
		walletLoaderDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
		walletLoaderDialog.progressHelper.barColor = Color.parseColor("#2E74A0")
		walletLoaderDialog.setCancelable(false)
		walletLoaderDialog.titleText = "LOADING.."
		walletLoaderDialog.show()
		val client =
			getInstance(this)!!.create(
				Client::class.java
			)
		val call =
			client.generateWalletOrder(
				Util().getJwtToken(sharedPreferences.getString("user", "")),
				cardId,
				ticketId
			)
		call.enqueue(object : Callback<JsonObject?> {
			@SuppressLint("SetTextI18n")
			override fun onResponse(
				call: Call<JsonObject?>,
				response: Response<JsonObject?>
			) {
				if (response.isSuccessful) {
					val helper = ResponseHelper()
					helper.responseHelper(response.body())
					if (helper.isStatusSuccessful()) {
						walletLoaderDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
						walletLoaderDialog.dismiss()
						val walletPin = walletPayBottomSheet.findViewById<TextView>(R.id.pin)!!
						val walletButtonsLayout =
							walletPayBottomSheet.findViewById<LinearLayout>(R.id.wallet_buttons_layout)
						val verifyWalletPinLayout =
							walletPayBottomSheet.findViewById<LinearLayout>(R.id.verify_wallet_pin_ll)
						try {
							val obj = JSONObject(helper.getDataAsString())
							val orderId = obj.get("ID") as String
							val verifyButtonsLinearLayout = LinearLayout(this@TicketActivity)
							val verifyButtonsLayoutParams = LinearLayout.LayoutParams(
								ViewGroup.LayoutParams.MATCH_PARENT,
								ViewGroup.LayoutParams.WRAP_CONTENT
							)
							verifyButtonsLinearLayout.gravity = Gravity.CENTER
							verifyButtonsLinearLayout.layoutParams = verifyButtonsLayoutParams
							val buttonsLayoutParams =
								LinearLayout.LayoutParams(400, ViewGroup.LayoutParams.WRAP_CONTENT)
							buttonsLayoutParams.setMargins(5, 0, 5, 0)
							val verifyPin = MaterialButton(this@TicketActivity)
							val cancelPin = MaterialButton(this@TicketActivity)
							verifyPin.text = "PAY"
							verifyPin.textSize = 12F
							verifyPin.setTextColor(Color.parseColor("#ffffff"))
							verifyPin.layoutParams = buttonsLayoutParams
							verifyPin.setOnClickListener {
								if (walletPin.text.toString().isEmpty()) {
									Toast.makeText(
										this@TicketActivity,
										"Please enter pin to continue",
										Toast.LENGTH_SHORT
									).show()
								} else {
									verifyPin(
										Util().getJwtToken(sharedPreferences.getString("user", "")),
										orderId,
										walletPin.text.toString()
									)
								}
							}
							cancelPin.text = "CANCEL"
							cancelPin.textSize = 12F
							cancelPin.setTextColor(Color.parseColor("#ffffff"))
							cancelPin.layoutParams = buttonsLayoutParams
							cancelPin.setOnClickListener {
								walletPayBottomSheet.dismiss()
							}
							verifyButtonsLinearLayout.addView(verifyPin)
							verifyButtonsLinearLayout.addView(cancelPin)
							walletButtonsLayout!!.visibility = View.GONE
							verifyWalletPinLayout!!.addView(verifyButtonsLinearLayout)
							verifyWalletPinLayout.visibility = View.VISIBLE
						} catch (e: JSONException) {
							Log.d("order confirmation", "not received")
						}
					} else {
						walletLoaderDialog.dismiss()
						NotificationHelper().getErrorAlert(
							this@TicketActivity,
							helper.getErrorMsg()
						)
					}
				} else {
					walletLoaderDialog.dismiss()
					NotificationHelper().getErrorAlert(
						this@TicketActivity,
						"Response Error Code " + response.code()
					)
				}
			}

			override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
				walletLoaderDialog.dismiss()
				NotificationHelper().getErrorAlert(
					this@TicketActivity,
					"Server Error. Please try again."
				)
			}
		})
	}

	private fun verifyPin(token: String, orderId: String, pin: String) {
		val walletLoaderDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
		walletLoaderDialog.progressHelper.barColor = Color.parseColor("#2E74A0")
		walletLoaderDialog.titleText = "LOADING.."
		walletLoaderDialog.show()
		val client =
			getInstance(this)!!.create(
				Client::class.java
			)
		val call =
			client.verifyPin(
				token,
				orderId.toInt(),
				pin.toInt()
			)
		call.enqueue(object : Callback<JsonObject?> {
			override fun onResponse(
				call: Call<JsonObject?>,
				response: Response<JsonObject?>
			) {
				if (response.isSuccessful) {
					val helper = ResponseHelper()
					helper.responseHelper(response.body())
					if (helper.isStatusSuccessful()) {
						walletLoaderDialog.dismissWithAnimation()
						val obj = JSONObject(helper.getDataAsString())
						ticket.rrn = obj.get("ORDER_ID").toString()
						ticket.mode_of_payment = obj.get("mode_of_payment").toString()
						ticket.order_status = "SUCCESS"
						editor.putString("ticket", Gson().toJson(ticket))
						editor.apply()
						startActivity(
							Intent(
								this@TicketActivity,
								InAppApprovedActivity::class.java
							)
						)
					} else {
						walletLoaderDialog.dismissWithAnimation()
						NotificationHelper().getErrorAlert(
							this@TicketActivity,
							helper.getErrorMsg()
						)
					}
				} else {
					walletLoaderDialog.dismissWithAnimation()
					NotificationHelper().getErrorAlert(
						this@TicketActivity,
						"Response Error Code " + response.code()
					)
				}
			}

			override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
				walletLoaderDialog.dismissWithAnimation()
				NotificationHelper().getErrorAlert(
					this@TicketActivity,
					"Server Error. Please try again."
				)
			}
		})
	}
}