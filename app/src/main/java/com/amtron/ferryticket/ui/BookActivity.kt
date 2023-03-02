package com.amtron.ferryticket.ui

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.amtron.ferryticket.R
import com.amtron.ferryticket.databinding.ActivityBookBinding
import com.amtron.ferryticket.helper.NotificationHelper
import com.amtron.ferryticket.model.*
import com.amtron.ferryticket.network.Client
import com.amtron.ferryticket.network.RetrofitHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
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
class BookActivity : AppCompatActivity() {
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
	private lateinit var bookingList: ArrayList<Booking>
	private lateinit var passengerList: ArrayList<PassengerDetails>
	private lateinit var vehicleList: ArrayList<Vehicle>
	private lateinit var othersList: ArrayList<Others>
	private lateinit var passengerTypeList: ArrayList<PassengerType>
	private var selectedVehicleType: VehicleType? = null
	private var selectedGoodsType: OtherType? = null
	private var masterDataString: String = ""
	private var serviceString: String = ""
	private var isAddPassengerCardVisible: Boolean = false
	private var isAddVehicleCardVisible: Boolean = false
	private var isAddGoodsCardVisible: Boolean = false
	private var totalPassengerCount: Int = 0
	private var totalVehiclesCount: Int = 0
	private var totalGoodsCount: Int = 0
	private var genderIdCount = 0
	private var passengerTypeIdCount = 0

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityBookBinding.inflate(layoutInflater)
		setContentView(binding.root)

		sharedPreferences = this.getSharedPreferences("IWTCounter", MODE_PRIVATE)
		editor = sharedPreferences.edit()

		val vehicleTypeList = ArrayList<VehicleType>()
		val vehicleTypeNameList = ArrayList<String>()
		val goodsTypeList = ArrayList<OtherType>()
		val goodsTypeNameList = ArrayList<String>()
		bookingList = ArrayList()
		passengerList = ArrayList()
		vehicleList = ArrayList()
		othersList = ArrayList()

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
				isAddPassengerCardVisible = true
				binding.addPassengerCard.visibility = View.VISIBLE
				binding.addPassenger.setOnClickListener {
					if (passengerTypeRG.checkedRadioButtonId == -1){
						Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show()
					} else {
						if (binding.name.text.isEmpty()) {
							Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show()
						} else {
							if (binding.phone.text.isEmpty()) {
								Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show()
							} else {
								if (binding.age.text.isEmpty()) {
									Toast.makeText(this, "Please enter age", Toast.LENGTH_SHORT).show()
								} else {
									if (genderRG.checkedRadioButtonId == -1) {
										Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show()
									} else {
										if (binding.address.text.isEmpty()) {
											Toast.makeText(this, "Please enter age", Toast.LENGTH_SHORT).show()
										} else {
											var disableBool = 0
											if (binding.disableCheckBox.isChecked) {
												disableBool = 1
											}
											passenger = PassengerDetails(
												binding.name.text.toString(),
												binding.phone.text.toString(),
												binding.age.text.toString(),
												genderList[genderRG.checkedRadioButtonId],
												passengerTypeList[passengerTypeRG.checkedRadioButtonId],
												binding.address.text.toString(),
												disableBool
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
				isAddVehicleCardVisible = true
				binding.addVehicleCard.visibility = View.VISIBLE
				binding.addVehicle.setOnClickListener {
					if (selectedVehicleType == null) {
						Toast.makeText(this, "Please select vehicle type", Toast.LENGTH_SHORT).show()
					} else {
						vehicle = Vehicle(selectedVehicleType!!, binding.vehicleNumber.text.toString())
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
				isAddGoodsCardVisible = true
				binding.addGoodsCard.visibility = View.VISIBLE
				binding.addGoods.setOnClickListener {
					if (selectedGoodsType == null) {
						Toast.makeText(this, "Please select goods type", Toast.LENGTH_SHORT).show()
					} else {
						if (binding.goodsName.text.isEmpty()) {
							Toast.makeText(this, "Please enter goods name", Toast.LENGTH_SHORT).show()
						} else {
							if (binding.goodsWeight.text.isEmpty()) {
								Toast.makeText(this, "Please enter goods weight", Toast.LENGTH_SHORT).show()
							} else {
								if (binding.goodsQuantity.text.isEmpty()) {
									Toast.makeText(this, "Please enter goods quantity", Toast.LENGTH_SHORT).show()
								} else {
									others = Others(selectedGoodsType!!,
										binding.goodsName.text.toString(),
										binding.goodsWeight.text.toString().toInt(),
										binding.goodsQuantity.text.toString().toInt()
									)
									addOthers(others)
								}
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
//			openBookingSummaryBottomSheet()
		}

		binding.scan.setOnClickListener {
			openScanner()
		}
	}

	private fun addOthers(others: Others) {
		resetGoods()
		totalGoodsCount += 1
		othersList.add(others)
		binding.goodsCount.text = totalGoodsCount.toString()
	}

	private fun addVehicle(vehicle: Vehicle) {
		resetVehicle()
		totalVehiclesCount += 1
		vehicleList.add(vehicle)
		binding.vehiclesCount.text = totalVehiclesCount.toString()
	}

	private fun addPassenger(passenger: PassengerDetails) {
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

	private fun openBookingSummaryBottomSheet() {
		val bookingSummaryBottomSheet = BottomSheetDialog(this)
		bookingSummaryBottomSheet.setContentView(R.layout.booking_summary_layout)
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
}