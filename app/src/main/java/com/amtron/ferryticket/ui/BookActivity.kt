package com.amtron.ferryticket.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.amtron.ferryticket.adapter.OnRecyclerViewItemClickListener
import com.amtron.ferryticket.adapter.SummaryAdapter
import com.amtron.ferryticket.databinding.ActivityBookBinding
import com.amtron.ferryticket.helper.NotificationHelper
import com.amtron.ferryticket.helper.ResponseHelper
import com.amtron.ferryticket.helper.Util
import com.amtron.ferryticket.model.*
import com.amtron.ferryticket.network.Client
import com.amtron.ferryticket.network.RetrofitHelper
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
class BookActivity : AppCompatActivity(), OnRecyclerViewItemClickListener {
	private lateinit var sharedPreferences: SharedPreferences
	private lateinit var editor: SharedPreferences.Editor
	private lateinit var binding: ActivityBookBinding
	private lateinit var masterData: MasterData
	private lateinit var ferryService: FerryService
	private lateinit var passenger: PassengerDetails
	private lateinit var vehicle: Vehicle
	private lateinit var others: Others
	private lateinit var genderRG: RadioGroup
	private lateinit var passengerTypeRG: RadioGroup
	private lateinit var genderList: ArrayList<Gender>
	private lateinit var passengerList: ArrayList<PassengerDetails>
	private lateinit var vehicleList: ArrayList<Vehicle>
	private lateinit var othersList: ArrayList<Others>
	private lateinit var passengerTypeList: ArrayList<PassengerType>
	private lateinit var allDataList: ArrayList<Any>
	private lateinit var adapter: SummaryAdapter
	private var selectedVehicleType: VehicleType? = null
	private var selectedGoodsType: OtherType? = null
	private var masterDataString: String = ""
	private var serviceString: String = ""
	private var scanResult: String = ""
	private var isAddPassengerCardVisible: Boolean = false
	private var isAddVehicleCardVisible: Boolean = false
	private var isAddGoodsCardVisible: Boolean = false
	private var isSummaryVisible: Boolean = false
	private var totalPassengerCount: Int = 0
	private var totalVehiclesCount: Int = 0
	private var totalGoodsCount: Int = 0
	private var genderIdCount = 0
	private var passengerTypeIdCount = 0

	@SuppressLint("SetTextI18n")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityBookBinding.inflate(layoutInflater)
		setContentView(binding.root)

		sharedPreferences = this.getSharedPreferences("IWTCounter", MODE_PRIVATE)
		editor = sharedPreferences.edit()

		val bundleString = intent.extras
		try {
			try {
				scanResult = bundleString!!.getString("scanResult", "").toString()
			} catch (e: Exception) {
				Log.d("scan result", "is empty")
			}
		} catch (e: Exception) {
			Log.d("extras", "is empty")
		}

		if (scanResult != "") {
			Log.d("scan result", scanResult)
			//Paste data into cards
		}

		binding.refresh.setOnClickListener {
			getUpdatedAvailabilities()
		}

		binding.scan.setOnClickListener {
			startActivity(
				Intent(this, ScannerActivity::class.java)
			)
		}

		val vehicleTypeList = ArrayList<VehicleType>()
		val vehicleTypeNameList = ArrayList<String>()
		val goodsTypeList = ArrayList<OtherType>()
		val goodsTypeNameList = ArrayList<String>()
		passengerList = ArrayList()
		vehicleList = ArrayList()
		othersList = ArrayList()
		allDataList = ArrayList()

		genderRG = binding.rgGender
		passengerTypeRG = binding.rgPassengerType
		genderRG.orientation = RadioGroup.HORIZONTAL
		passengerTypeRG.orientation = RadioGroup.HORIZONTAL

		try {
			masterDataString = sharedPreferences.getString("masterData", "").toString()
			serviceString = sharedPreferences.getString("service", "").toString()
			masterData = Gson().fromJson(masterDataString, object : TypeToken<MasterData>() {}.type)
			ferryService =
				Gson().fromJson(serviceString, object : TypeToken<FerryService>() {}.type)

			genderList = ArrayList()
			passengerTypeList = ArrayList()

			for (gender in masterData.genders) {
				val rb1 = RadioButton(this)
				rb1.id = genderIdCount
				rb1.text = gender.gender_name
				genderRG.addView(rb1)
				genderList.add(gender)
				genderIdCount += 1
			}
			for (pType in masterData.passengerTypes) {
				val rb2 = RadioButton(this)
				rb2.id = passengerTypeIdCount
				rb2.text = pType.type
				passengerTypeRG.addView(rb2)
				passengerTypeList.add(pType)
				passengerTypeIdCount += 1
			}
			for (vehicle in masterData.vehicleTypes) {
				vehicleTypeList.add(vehicle)
				vehicleTypeNameList.add(vehicle.p_name)
			}
			val vehicleAdapter = ArrayAdapter(
				this,
				androidx.transition.R.layout.support_simple_spinner_dropdown_item,
				vehicleTypeNameList
			)
			vehicleTypeNameList.add(0, "SELECT VEHICLE TYPE")
			binding.vehicleTypeDropdown.setSelection(0)
			binding.vehicleTypeDropdown.setAdapter(vehicleAdapter)
			vehicleAdapter.notifyDataSetChanged()
			binding.vehicleTypeDropdown.showSoftInputOnFocus = false
			binding.vehicleTypeDropdown.onItemClickListener =
				AdapterView.OnItemClickListener { _: AdapterView<*>, _: View, position: Int, _: Long ->
					if (vehicleTypeNameList[position] == "Bicycle") {
						binding.textInputLayout6.visibility = View.GONE
					} else {
						binding.textInputLayout6.visibility = View.VISIBLE
					}
					selectedVehicleType = if (position > 0) {
						vehicleTypeList[position - 1]
					} else {
						null
					}
				}
			for (goods in masterData.otherTypes) {
				goodsTypeList.add(goods)
				goodsTypeNameList.add(goods.p_name)
			}
			val goodsAdapter = ArrayAdapter(
				this,
				androidx.transition.R.layout.support_simple_spinner_dropdown_item,
				goodsTypeNameList
			)
			goodsTypeNameList.add(0, "SELECT GOODS TYPE")
			binding.othersTypeDropdown.setSelection(0)
			binding.othersTypeDropdown.setAdapter(goodsAdapter)
			goodsAdapter.notifyDataSetChanged()
			binding.othersTypeDropdown.showSoftInputOnFocus = false
			binding.othersTypeDropdown.onItemClickListener =
				AdapterView.OnItemClickListener { _: AdapterView<*>, _: View, position: Int, _: Long ->
					selectedGoodsType = if (position > 0) {
						goodsTypeList[position - 1]
					} else {
						null
					}
				}
		} catch (e: Exception) {
			Toast.makeText(this, "No master data found", Toast.LENGTH_SHORT).show()
		}

		binding.passengerCount.text = totalPassengerCount.toString()
		binding.vehiclesCount.text = totalVehiclesCount.toString()
		binding.goodsCount.text = totalGoodsCount.toString()
		binding.ferry.ferryName.text = ferryService.ferry.ferry_name
		binding.ferry.ferryNumber.text = ferryService.ferry.ferry_no
		binding.ferry.src.text = ferryService.source.ghat_name
		binding.ferry.dest.text = ferryService.destination.ghat_name
		binding.ferry.departureTime.text = ferryService.departure_time
		binding.ferry.arrivalTime.text = ferryService.reached_time
		binding.ferry.availablePerson.text = ferryService.passenger_capacity.toString()
		binding.ferry.availableCycle.text = ferryService.bicycle_capacity.toString()
		binding.ferry.availableMotorcycle.text = ferryService.two_wheeler_capacity.toString()
		binding.ferry.availableLmv.text = ferryService.four_wheeler.toString()
		binding.ferry.availableHmv.text = ferryService.hmv_capacity.toString()
		binding.ferry.availableGoods.text = ferryService.others_capacity.toString()

		binding.openAddPassengerCard.setOnClickListener {
			if (!isAddPassengerCardVisible) {
				resetVehicle()
				resetGoods()
				isAddGoodsCardVisible = false
				binding.addGoodsCard.visibility = View.GONE
				isAddVehicleCardVisible = false
				binding.addVehicleCard.visibility = View.GONE
				isSummaryVisible = false
				binding.viewSummaryText.visibility = View.GONE
				binding.proceedBtn.visibility = View.GONE
				binding.summaryListRecyclerView.visibility = View.GONE
				binding.viewBookingSummaryBtn.text = "VIEW SUMMARY"
				isAddPassengerCardVisible = true
				binding.addPassengerCard.visibility = View.VISIBLE
				binding.addPassenger.setOnClickListener {
					if (passengerTypeRG.checkedRadioButtonId == -1) {
						Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show()
					} else {
						if (binding.name.text.isEmpty()) {
							Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show()
						} else {
							if (binding.phone.text.isEmpty()) {
								Toast.makeText(
									this,
									"Please enter phone number",
									Toast.LENGTH_SHORT
								).show()
							} else {
								if (binding.age.text.isEmpty()) {
									Toast.makeText(this, "Please enter age", Toast.LENGTH_SHORT)
										.show()
								} else {
									if (genderRG.checkedRadioButtonId == -1) {
										Toast.makeText(
											this,
											"Please select gender",
											Toast.LENGTH_SHORT
										).show()
									} else {
										if (binding.address.text.isEmpty()) {
											Toast.makeText(
												this,
												"Please enter age",
												Toast.LENGTH_SHORT
											).show()
										} else {
											var disableBool = 0
											if (binding.disableCheckBox.isChecked) {
												disableBool = 1
											}
											passenger = PassengerDetails(
												binding.name.text.toString(),
												binding.phone.text.toString(),
												binding.age.text.toString(),
												disableBool,
												binding.address.text.toString(),
												passengerTypeList[passengerTypeRG.checkedRadioButtonId],
												genderList[genderRG.checkedRadioButtonId]
											)
											addPassenger(passenger)
										}
									}
								}
							}
						}
					}
				}
			}
		}

		binding.closeAddPassengerCard.setOnClickListener {
			if (isAddPassengerCardVisible) {
				resetPassenger()
				isAddPassengerCardVisible = false
				binding.addPassengerCard.visibility = View.GONE
			}
		}

		binding.openAddVehiclesCard.setOnClickListener {
			if (!isAddVehicleCardVisible) {
				resetPassenger()
				resetGoods()
				isAddPassengerCardVisible = false
				binding.addPassengerCard.visibility = View.GONE
				isAddGoodsCardVisible = false
				binding.addGoodsCard.visibility = View.GONE
				isSummaryVisible = false
				binding.viewSummaryText.visibility = View.GONE
				binding.proceedBtn.visibility = View.GONE
				binding.summaryListRecyclerView.visibility = View.GONE
				binding.viewBookingSummaryBtn.text = "VIEW SUMMARY"
				isAddVehicleCardVisible = true
				binding.addVehicleCard.visibility = View.VISIBLE
				binding.addVehicle.setOnClickListener {
					if (selectedVehicleType == null) {
						Toast.makeText(this, "Please select vehicle type", Toast.LENGTH_SHORT)
							.show()
					} else {
						vehicle =
							Vehicle(selectedVehicleType!!, binding.vehicleNumber.text.toString())
						Log.d("vehicle", vehicle.toString())
						addVehicle(vehicle)
					}
				}
			}
		}

		binding.closeAddVehicleCard.setOnClickListener {
			if (isAddVehicleCardVisible) {
				resetVehicle()
				isAddVehicleCardVisible = false
				binding.addVehicleCard.visibility = View.GONE
			}
		}

		binding.openAddGoodsCard.setOnClickListener {
			if (!isAddGoodsCardVisible) {
				resetPassenger()
				resetVehicle()
				isAddPassengerCardVisible = false
				binding.addPassengerCard.visibility = View.GONE
				isAddVehicleCardVisible = false
				binding.addVehicleCard.visibility = View.GONE
				isSummaryVisible = false
				binding.viewSummaryText.visibility = View.GONE
				binding.proceedBtn.visibility = View.GONE
				binding.summaryListRecyclerView.visibility = View.GONE
				binding.viewBookingSummaryBtn.text = "VIEW SUMMARY"
				isAddGoodsCardVisible = true
				binding.addGoodsCard.visibility = View.VISIBLE
				binding.addGoods.setOnClickListener {
					if (selectedGoodsType == null) {
						Toast.makeText(this, "Please select goods type", Toast.LENGTH_SHORT).show()
					} else {
						if (binding.goodsName.text.isEmpty()) {
							Toast.makeText(this, "Please enter goods name", Toast.LENGTH_SHORT)
								.show()
						} else {
							if (binding.goodsWeight.text.isEmpty()) {
								Toast.makeText(
									this,
									"Please enter goods weight",
									Toast.LENGTH_SHORT
								).show()
							} else {
								others = Others(
									selectedGoodsType!!,
									binding.goodsName.text.toString(),
									binding.goodsWeight.text.toString().toInt()
								)
								addOthers(others)
							}
						}
					}
				}
			}
		}

		binding.closeAddGoodsCard.setOnClickListener {
			if (isAddGoodsCardVisible) {
				resetGoods()
				isAddGoodsCardVisible = false
				binding.addGoodsCard.visibility = View.GONE
			}
		}

		binding.viewBookingSummaryBtn.setOnClickListener {
			if (isSummaryVisible) {
				binding.summaryListRecyclerView.visibility = View.GONE
				isSummaryVisible = false
				binding.proceedBtn.visibility = View.GONE
				binding.viewSummaryText.visibility = View.GONE
				binding.viewBookingSummaryBtn.text = "VIEW SUMMARY"
			} else {
				if (allDataList.isEmpty()) {
					NotificationHelper().getWarningAlert(this, "No data found yet")
				} else {
					isAddPassengerCardVisible = false
					isAddVehicleCardVisible = false
					isAddGoodsCardVisible = false
					resetPassenger()
					resetGoods()
					resetVehicle()
					binding.addPassengerCard.visibility = View.GONE
					binding.addVehicleCard.visibility = View.GONE
					binding.addGoodsCard.visibility = View.GONE
					binding.viewSummaryText.visibility = View.VISIBLE
					binding.proceedBtn.visibility = View.VISIBLE
					adapter = SummaryAdapter(allDataList, this)
					adapter.setOnItemClickListener(this)
					binding.summaryListRecyclerView.visibility = View.VISIBLE
					isSummaryVisible = true
					binding.viewBookingSummaryBtn.text = "CLOSE SUMMARY"
					val recyclerView = binding.summaryListRecyclerView
					recyclerView.adapter = adapter
					recyclerView.layoutManager = LinearLayoutManager(this)
					recyclerView.isNestedScrollingEnabled = false
					binding.proceedBtn.setOnClickListener {
						bookTicket(
							ferryService.id,
							Gson().toJson(passengerList),
							Gson().toJson(vehicleList),
							Gson().toJson(othersList)
						)
						/*Log.d("ferry service", ferryService.id.toString())
						Log.d("passenger list", Gson().toJson(passengerList))
						Log.d("vehicle list", Gson().toJson(vehicleList))
						Log.d("others list", Gson().toJson(othersList))*/
					}
				}
			}
		}
	}

	private fun bookTicket(
		ferryServiceId: Int,
		passengerList: String,
		vehicleList: String,
		othersList: String
	) {
		if (!Util().isOnline(this)) {
			val noInternetAlert = SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
			noInternetAlert.titleText = "NO INTERNET"
			noInternetAlert.contentText = "Please retry"
			noInternetAlert.confirmText = "RETRY"
			noInternetAlert.showCancelButton(false)
			noInternetAlert.setConfirmClickListener {
				bookTicket(ferryServiceId, passengerList, vehicleList, othersList)
			}
		} else {
			val dialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
			dialog.progressHelper.barColor = Color.parseColor("#2E74A0")
			dialog.titleText = "Booking Ticket..."
			dialog.setCancelable(false)
			dialog.show()
			val api = RetrofitHelper.getInstance().create(Client::class.java)
			GlobalScope.launch {
				val call: Call<JsonObject> = api.bookTicket(
					Util().getJwtToken(sharedPreferences.getString("user", "").toString()),
					ferryServiceId,
					passengerList,
					vehicleList,
					othersList
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
								dialog.dismissWithAnimation()
								val ticket: Ticket = Gson().fromJson(
									helper.getDataAsString(),
									object : TypeToken<Ticket>() {}.type
								)
								val bundle = Bundle()
								val i = Intent(this@BookActivity, Ticket::class.java)
								bundle.putString("ticket", Gson().toJson(ticket))
								i.putExtras(bundle)
								startActivity(i)
							}
						} else {
							dialog.dismiss()
							NotificationHelper().getErrorAlert(
								this@BookActivity,
								"Response Error Code" + response.message()
							)
						}
					}

					override fun onFailure(call: Call<JsonObject>, t: Throwable) {
						dialog.dismiss()
						NotificationHelper().getErrorAlert(this@BookActivity, "Server Error")
					}
				})
			}
		}
	}

	private fun getUpdatedAvailabilities() {
		val dialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
		dialog.progressHelper.barColor = Color.parseColor("#2E74A0")
		dialog.titleText = "Getting Availabilities..."
		dialog.setCancelable(false)
		dialog.show()
		val api = RetrofitHelper.getInstance().create(Client::class.java)
		GlobalScope.launch {
			val call: Call<JsonObject> = api.getService(
				Util().getJwtToken(sharedPreferences.getString("user", "").toString()),
				ferryService.id
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
							dialog.dismissWithAnimation()
							val obj = JSONObject(helper.getDataAsString())
							binding.ferry.availablePerson.text =
								(obj.get("passenger_capacity") as Int).toString()
							binding.ferry.availableCycle.text =
								(obj.get("bicycle_capacity") as Int).toString()
							binding.ferry.availableMotorcycle.text =
								(obj.get("two_wheeler_capacity") as Int).toString()
							binding.ferry.availableLmv.text =
								(obj.get("four_wheeler") as Int).toString()
							binding.ferry.availableGoods.text =
								(obj.get("others_capacity") as Int).toString()
							binding.ferry.availableHmv.text =
								(obj.get("hmv_capacity") as Int).toString()
						}
					} else {
						dialog.dismiss()
						NotificationHelper().getErrorAlert(
							this@BookActivity,
							"Response Error Code" + response.message()
						)
					}
				}

				override fun onFailure(call: Call<JsonObject>, t: Throwable) {
					dialog.dismiss()
					NotificationHelper().getErrorAlert(this@BookActivity, "Server Error")
				}
			})
		}
	}

	private fun addOthers(others: Others) {
		allDataList.add(others)
		resetGoods()
		totalGoodsCount += 1
		othersList.add(others)
		binding.goodsCount.text = totalGoodsCount.toString()
	}

	private fun addVehicle(vehicle: Vehicle) {
		allDataList.add(vehicle)
		resetVehicle()
		totalVehiclesCount += 1
		vehicleList.add(vehicle)
		binding.vehiclesCount.text = totalVehiclesCount.toString()
	}

	private fun addPassenger(passenger: PassengerDetails) {
		allDataList.add(passenger)
		resetPassenger()
		totalPassengerCount += 1
		passengerList.add(passenger)
		binding.passengerCount.text = totalPassengerCount.toString()
	}

	private fun openScanner() {
		val api = RetrofitHelper.getInstanceForScan().create(Client::class.java)
		GlobalScope.launch {
			val call: Call<JsonObject> = api.getQR("card_details/1")
			call.enqueue(object : Callback<JsonObject> {
				@SuppressLint("CommitPrefEdits", "NotifyDataSetChanged")
				override fun onResponse(
					call: Call<JsonObject>,
					response: Response<JsonObject>
				) {
					if (response.isSuccessful) {

					} else {
						NotificationHelper().getErrorAlert(
							this@BookActivity,
							"Response Error Code" + response.message()
						)
					}
				}

				override fun onFailure(call: Call<JsonObject>, t: Throwable) {
					NotificationHelper().getErrorAlert(this@BookActivity, "Server Error")
				}
			})
		}
	}

	private fun resetPassenger() {
		binding.name.setText("")
		binding.phone.setText("")
		binding.age.setText("")
		binding.address.setText("")
		binding.rgPassengerType.clearCheck()
		binding.rgGender.clearCheck()
		binding.disableCheckBox.isChecked = false
	}

	private fun resetVehicle() {
		binding.vehicleNumber.setText("")
		binding.vehicleTypeDropdown.setText(
			binding.vehicleTypeDropdown.adapter.getItem(0).toString(), false
		)
	}

	private fun resetGoods() {
		binding.goodsName.setText("")
		binding.goodsWeight.setText("")
		binding.goodsQuantity.setText("")
		binding.othersTypeDropdown.setText(
			binding.othersTypeDropdown.adapter.getItem(0).toString(), false
		)
	}

	@SuppressLint("NotifyDataSetChanged", "SetTextI18n")
	override fun onItemClickListener(position: Int, type: String) {
		val alert = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
		alert.titleText = "CONFIRM"
		alert.contentText = "Are you sure you want to delete?"
		alert.setCancelable(false)
		alert.confirmText = "YES"
		alert.cancelText = "NO"
		alert.setCancelClickListener { alert.dismiss() }
		alert.setConfirmClickListener {
			if (allDataList[position] is PassengerDetails) {
				if (totalPassengerCount == 1) {
					alert.titleText = "WARNING"
					alert.contentText =
						"This is the last passenger. Deleting will delete all data. Delete?"
					alert.setConfirmClickListener {
						totalPassengerCount = 0
						totalVehiclesCount = 0
						totalGoodsCount = 0
						binding.passengerCount.text = totalPassengerCount.toString()
						binding.vehiclesCount.text = totalPassengerCount.toString()
						binding.goodsCount.text = totalPassengerCount.toString()
						alert.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
						allDataList.clear()
						Log.d("size", passengerList.size.toString())
						adapter.notifyDataSetChanged()
						alert.dismissWithAnimation()
						binding.viewSummaryText.visibility = View.GONE
						binding.proceedBtn.visibility = View.GONE
						binding.summaryListRecyclerView.visibility = View.GONE
						binding.viewBookingSummaryBtn.text = "VIEW SUMMARY"
					}
					alert.setCancelClickListener {
						alert.dismiss()
					}
				} else {
					totalPassengerCount -= 1
					binding.passengerCount.text = totalPassengerCount.toString()
					allDataList.removeAt(position)
					adapter.notifyDataSetChanged()
					Log.d("size", passengerList.size.toString())
					alert.dismissWithAnimation()
				}
			} else {
				if (allDataList[position] is Vehicle) {
					totalVehiclesCount -= 1
					binding.vehiclesCount.text = totalPassengerCount.toString()
				} else if (allDataList[position] is Others) {
					totalGoodsCount -= 1
					binding.goodsCount.text = totalPassengerCount.toString()
				}
				allDataList.removeAt(position)
				adapter.notifyDataSetChanged()
				Log.d("size", passengerList.size.toString())
				alert.dismissWithAnimation()
			}
		}
		alert.show()
	}

	override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
		if (currentFocus != null) {
			val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
			imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
		}
		return super.dispatchTouchEvent(ev)
	}
}