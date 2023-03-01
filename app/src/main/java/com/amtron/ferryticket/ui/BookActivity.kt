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
import com.amtron.ferryticket.model.FerryService
import com.amtron.ferryticket.model.MasterData
import com.amtron.ferryticket.model.VehicleType
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
	private lateinit var genderRG: RadioGroup
	private lateinit var passengerTypeRG: RadioGroup
	private var masterDataString: String = ""
	private var serviceString: String = ""
	private var isAddPassengerCardVisible: Boolean = false
	private var isAddVehicleCardVisible: Boolean = false
	private var isAddVehicleNumberVisible: Boolean = false
	private var totalPassengerCount: Int = 0
	private var totalVehiclesCount: Int = 0
	private var totalGoodsCount: Int = 0

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityBookBinding.inflate(layoutInflater)
		setContentView(binding.root)

		sharedPreferences = this.getSharedPreferences("IWTCounter", MODE_PRIVATE)
		editor = sharedPreferences.edit()

		genderRG = binding.rgGender
		passengerTypeRG = binding.rgPassengerType
		genderRG.orientation = RadioGroup.HORIZONTAL
		passengerTypeRG.orientation = RadioGroup.HORIZONTAL

		try {
			masterDataString = sharedPreferences.getString("masterData", "").toString()
			serviceString = sharedPreferences.getString("service", "").toString()
			masterData = Gson().fromJson(masterDataString, object : TypeToken<MasterData>() {}.type)
			ferryService = Gson().fromJson(serviceString, object : TypeToken<FerryService>() {}.type)
			for (gender in masterData.genders) {
				val rb = RadioButton(this)
				rb.text = gender.gender_name
				genderRG.addView(rb)
			}
			for (pType in masterData.passengerTypes) {
				val rb = RadioButton(this)
				rb.text = pType.type
				passengerTypeRG.addView(rb)
			}
		} catch (e: Exception) {
			Toast.makeText(this, "No master data found", Toast.LENGTH_SHORT).show()
		}

		val vehicleList = ArrayList<VehicleType>()
		val vehicleNameList = ArrayList<String>()
		for (vehicle in masterData.vehicleTypes) {
			vehicleList.add(vehicle)
			vehicleNameList.add(vehicle.p_name)
		}
		val vehicleAdapter = ArrayAdapter(this, androidx.transition.R.layout.support_simple_spinner_dropdown_item, vehicleNameList)
		binding.vehicleTypeDropdown.setAdapter(vehicleAdapter)
		vehicleAdapter.notifyDataSetChanged()
		binding.vehicleTypeDropdown.showSoftInputOnFocus = false
		binding.vehicleTypeDropdown.onItemClickListener = AdapterView.OnItemClickListener() { _: AdapterView<*>, _: View, position: Int, _: Long ->
			Log.d("vehicle", vehicleList[position].toString())
		};

		binding.passengerCount.text = totalPassengerCount.toString()
		binding.vehiclesCount.text = totalPassengerCount.toString()
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
				isAddPassengerCardVisible = true
				binding.addPassengerCard.visibility = View.VISIBLE
			}
		}

		binding.openAddVehiclesCard.setOnClickListener {
			if (!isAddVehicleCardVisible) {
				isAddVehicleCardVisible = true
				binding.addVehicleCard.visibility = View.VISIBLE
			}
		}

		binding.closeAddPassengerCard.setOnClickListener {
			if (isAddPassengerCardVisible) {
				resetPassenger()
				isAddPassengerCardVisible = false
				isAddVehicleCardVisible = false
				binding.addPassengerCard.visibility = View.GONE
				binding.addVehicleCard.visibility = View.GONE
			}
		}

		binding.closeAddVehicleCard.setOnClickListener {
			if (isAddVehicleCardVisible) {
				resetVehicle()
				isAddVehicleCardVisible = false
				binding.addVehicleCard.visibility = View.GONE
			}
		}

		binding.viewBookingSummaryBtn.setOnClickListener {
			openBookingSummaryBottomSheet()
		}

		binding.scan.setOnClickListener {
			openScanner()
		}
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
		TODO("Not yet implemented")
	}

	private fun resetVehicle() {
		TODO("Not yet implemented")
	}
}