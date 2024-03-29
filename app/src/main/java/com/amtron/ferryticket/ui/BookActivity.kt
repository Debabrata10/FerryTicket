package com.amtron.ferryticket.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.amtron.ferryticket.R
import com.amtron.ferryticket.adapter.OnRecyclerViewItemClickListener
import com.amtron.ferryticket.adapter.SummaryAdapter
import com.amtron.ferryticket.databinding.ActivityBookBinding
import com.amtron.ferryticket.helper.DateAndTimeHelper
import com.amtron.ferryticket.helper.NotificationHelper
import com.amtron.ferryticket.helper.ResponseHelper
import com.amtron.ferryticket.helper.Util
import com.amtron.ferryticket.model.*
import com.amtron.ferryticket.network.Client
import com.amtron.ferryticket.network.RetrofitHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

@DelicateCoroutinesApi
class BookActivity : AppCompatActivity(), OnRecyclerViewItemClickListener {
	private lateinit var sharedPreferences: SharedPreferences
	private lateinit var editor: SharedPreferences.Editor
	private lateinit var binding: ActivityBookBinding
	private var qrScanIntegrator: IntentIntegrator? = null
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
	private lateinit var savedPhoneNumbersList: ArrayList<String>
	private var selectedVehicleType: VehicleType? = null
	private var selectedGoodsType: OtherType? = null
	private var masterDataString: String = ""
	private var serviceString: String = ""
	private var isAddPassengerCardVisible: Boolean = false
	private var isAddVehicleCardVisible: Boolean = false
	private var isAddGoodsCardVisible: Boolean = false
	private var isSummaryVisible: Boolean = false
	private var totalPassengerCount: Int = 0
	private var totalVehiclesCount: Int = 0
	private var totalGoodsCount: Int = 0
	private var genderIdCount = 0
	private var passengerTypeIdCount = 0

	@SuppressLint("SetTextI18n", "InflateParams")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityBookBinding.inflate(layoutInflater)
		setContentView(binding.root)

		sharedPreferences = this.getSharedPreferences("IWTCounter", MODE_PRIVATE)
		editor = sharedPreferences.edit()

		qrScanIntegrator = IntentIntegrator(this)

		binding.scan.setOnClickListener {
			performAction()
		}

		binding.enterCardNo.setOnClickListener {
			openEnterCardNumberBottomSheet()
		}

		savedPhoneNumbersList = ArrayList()
		val vehicleTypeList = ArrayList<VehicleType>()
		val vehicleTypeNameList = ArrayList<String>()
		val goodsTypeList = ArrayList<OtherType>()
		val goodsTypeNameList = ArrayList<String>()
		passengerList = ArrayList()
		vehicleList = ArrayList()
		othersList = ArrayList()
		allDataList = ArrayList()

		genderRG = binding.passengerLayout.rgGender
		passengerTypeRG = binding.passengerLayout.rgPassengerType

		genderRG.orientation = RadioGroup.VERTICAL //get the radio group for gender
		passengerTypeRG.orientation = RadioGroup.VERTICAL //get the radio group for passenger

		try {
			masterDataString = sharedPreferences.getString("masterData", "").toString()
			serviceString = sharedPreferences.getString("service", "").toString()
			masterData = Gson().fromJson(masterDataString, object : TypeToken<MasterData>() {}.type)
			ferryService =
				Gson().fromJson(serviceString, object : TypeToken<FerryService>() {}.type)

			genderList = ArrayList() //setting the gender objects inside an Arraylist
			passengerTypeList = ArrayList() //setting the passenger objects inside an Arraylist

			for (gender in masterData.genders) { // creating radio button for each gender objects
				val rb1 = RadioButton(this)
				rb1.id = genderIdCount //setting id of RB as the index of the objects
				rb1.text = gender.gender_name //setting text of RB as the object name
				genderRG.addView(rb1) //adding RB to the gender radio group
				//adding gender object to the gender ArrayList because if we select the
				//radio button we get the id which is the index and later on with the
				//index reference we get the object from the genderList ArrayList so that
				//the selected RB object matches with the genderList object
				genderList.add(gender)
				genderIdCount += 1 //incrementing by one so that the index aka RB id increases by 1
			}
			for (pType in masterData.passengerTypes) { // creating radio button for each passenger objects
				val rb2 = RadioButton(this)
				rb2.id = passengerTypeIdCount //setting id of RB as the index of the objects
				rb2.text = pType.type //setting text of RB as the object name
				passengerTypeRG.addView(rb2) //adding RB to the gender radio group
				//adding passenger object to the passenger ArrayList because if we select the
				//radio button we get the id which is the index and later on with the
				//index reference we get the object from the passengerList ArrayList so that
				//the selected RB object matches with the passengerList object
				passengerTypeList.add(pType)
				passengerTypeIdCount += 1 //incrementing by one so that the index aka RB id increases by 1
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
			//assigning no object to the 0th position because we need validation
			//and we need no vehicle to be selected at the beginning
			vehicleTypeNameList.add(0, "SELECT VEHICLE TYPE")
			binding.vehicleLayout.vehicleTypeDropdown.setSelection(0)
			binding.vehicleLayout.vehicleTypeDropdown.setAdapter(vehicleAdapter)
			vehicleAdapter.notifyDataSetChanged()

			binding.vehicleLayout.vehicleTypeDropdown.showSoftInputOnFocus = false
			binding.vehicleLayout.vehicleTypeDropdown.onItemClickListener =
				AdapterView.OnItemClickListener { _: AdapterView<*>, _: View, position: Int, _: Long ->
					if (vehicleTypeNameList[position] == "Bicycle") { //no regd number for bicycle logic
						binding.vehicleLayout.textInputLayout6.visibility = View.GONE
					} else {
						binding.vehicleLayout.textInputLayout6.visibility = View.VISIBLE
					}
					selectedVehicleType = if (position > 0) {
						vehicleTypeList[position - 1]
					} else {
						null
					}
				}
			//for mobile view ends

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
			binding.otherLayout.othersTypeDropdown.setSelection(0)
			binding.otherLayout.othersTypeDropdown.setAdapter(goodsAdapter)
			goodsAdapter.notifyDataSetChanged()

			binding.otherLayout.othersTypeDropdown.showSoftInputOnFocus = false
			binding.otherLayout.othersTypeDropdown.onItemClickListener =
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
		binding.ferry.departureTime.text =
			DateAndTimeHelper().changeTimeFormat(ferryService.departure_time)
		binding.ferry.arrivalTime.text =
			DateAndTimeHelper().changeTimeFormat(ferryService.reached_time)
		binding.ferry.availablePerson.text = ferryService.passenger_capacity.toString()
		binding.ferry.availableCycle.text = ferryService.bicycle_capacity.toString()
		binding.ferry.availableMotorcycle.text = ferryService.two_wheeler_capacity.toString()
		binding.ferry.availableLmv.text = ferryService.four_wheeler.toString()
		binding.ferry.availableHmv.text = ferryService.hmv_capacity.toString()
		binding.ferry.availableGoods.text = ferryService.others_capacity.toString()
		binding.ferry.refreshBtn.visibility = View.VISIBLE
		if (ferryService.special_booking == 1) {
			binding.ferry.specialFerryTxt.visibility = View.VISIBLE
		}

		binding.ferry.refreshBtn.setOnClickListener {
			//update ferry particulars count
			getUpdatedAvailabilities()
		}

		binding.openAddPassengerCard.setOnClickListener {
			//logic to show/hide views when add passenger card is opened
			//clear all unfinished data from every cards except passenger
			if (!isAddPassengerCardVisible) {
				resetVehicle()
				resetGoods()
				binding.bookingBaseLayout.visibility = View.GONE
				isAddGoodsCardVisible = false
				binding.otherLayout.addGoodsCard.visibility = View.GONE
				isAddVehicleCardVisible = false
				binding.vehicleLayout.addVehicleCard.visibility = View.GONE
				isSummaryVisible = false
				binding.summarySection.visibility = View.GONE
				binding.proceedBtn.visibility = View.GONE
				binding.viewBookingSummaryBtn.text = "VIEW SUMMARY"
				isAddPassengerCardVisible = true
				binding.passengerLayout.addPassengerCard.visibility = View.VISIBLE

				binding.passengerLayout.addPassenger.setOnClickListener {
					if (passengerTypeRG.checkedRadioButtonId == -1) {
						Toast.makeText(this, "Please select passenger type", Toast.LENGTH_SHORT).show()
					} else {
						if (binding.passengerLayout.name.text.isEmpty()) {
							Toast.makeText(this, "Please enter name", Toast.LENGTH_SHORT).show()
						} else {
							if (savedPhoneNumbersList.isEmpty()) {
								if (binding.passengerLayout.phone.text.isEmpty()) {
									Toast.makeText(
										this,
										"Please enter phone number",
										Toast.LENGTH_SHORT
									).show()
								} else {
									if (binding.passengerLayout.age.text.isEmpty()) {
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
											if (binding.passengerLayout.address.text.isEmpty()) {
												Toast.makeText(
													this,
													"Please enter age",
													Toast.LENGTH_SHORT
												).show()
											} else {
												var disableBool = 0
												if (binding.passengerLayout.disableCheckBox.isChecked) {
													disableBool = 1
												}
												passenger = PassengerDetails(
													binding.passengerLayout.name.text.toString()
														.uppercase(Locale.getDefault()),
													binding.passengerLayout.phone.text.toString(),
													binding.passengerLayout.age.text.toString(),
													disableBool,
													binding.passengerLayout.address.text.toString()
														.uppercase(Locale.getDefault()),
													passengerTypeList[passengerTypeRG.checkedRadioButtonId],
													genderList[genderRG.checkedRadioButtonId]
												)
												addPassenger(passenger)
											}
										}
									}
								}
							} else {
								if (binding.passengerLayout.age.text.isEmpty()) {
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
										if (binding.passengerLayout.address.text.isEmpty()) {
											Toast.makeText(
												this,
												"Please enter age",
												Toast.LENGTH_SHORT
											).show()
										} else {
											var disableBool = 0
											if (binding.passengerLayout.disableCheckBox.isChecked) {
												disableBool = 1
											}
											passenger = PassengerDetails(
												binding.passengerLayout.name.text.toString()
													.uppercase(Locale.getDefault()),
												binding.passengerLayout.phone.text.toString(),
												binding.passengerLayout.age.text.toString(),
												disableBool,
												binding.passengerLayout.address.text.toString()
													.uppercase(Locale.getDefault()),
												passengerTypeList[passengerTypeRG.checkedRadioButtonId],
												genderList[genderRG.checkedRadioButtonId]
											)
											addPassenger(passenger)
											//After adding passengers automatically close add passenger view
											if (isAddPassengerCardVisible) {
												resetPassenger()
												isAddPassengerCardVisible = false
												binding.bookingBaseLayout.visibility = View.VISIBLE
												binding.passengerLayout.addPassengerCard.visibility = View.GONE
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}

		binding.passengerLayout.closeAddPassengerCard.setOnClickListener {
			if (isAddPassengerCardVisible) {
				resetPassenger()
				isAddPassengerCardVisible = false
				binding.bookingBaseLayout.visibility = View.VISIBLE
				binding.passengerLayout.addPassengerCard.visibility = View.GONE
			}
		}

		binding.openAddVehiclesCard.setOnClickListener {
			//logic to show/hide views when add vehicle card is opened
			//clear all unfinished data from every cards except vehicle
			if (!isAddVehicleCardVisible) {
				resetPassenger()
				resetGoods()
				binding.bookingBaseLayout.visibility = View.GONE
				isAddPassengerCardVisible = false
				binding.passengerLayout.addPassengerCard.visibility = View.GONE
				isAddGoodsCardVisible = false
				binding.otherLayout.addGoodsCard.visibility = View.GONE
				isSummaryVisible = false
				binding.summarySection.visibility = View.GONE
				binding.proceedBtn.visibility = View.GONE
				binding.viewBookingSummaryBtn.text = "VIEW SUMMARY"
				isAddVehicleCardVisible = true
				binding.vehicleLayout.addVehicleCard.visibility = View.VISIBLE

				binding.vehicleLayout.addVehicle.setOnClickListener {
					if (selectedVehicleType == null) {
						Toast.makeText(this, "Please select vehicle type", Toast.LENGTH_SHORT)
							.show()
					} else if (selectedVehicleType!!.p_name != "Bicycle") {
						if (binding.vehicleLayout.vehicleNumber.text.isEmpty()) {
							Toast.makeText(this, "Please select vehicle number", Toast.LENGTH_SHORT)
								.show()
						} else {
							vehicle =
								Vehicle(
									selectedVehicleType!!,
									binding.vehicleLayout.vehicleNumber.text.toString()
										.uppercase(Locale.getDefault())
								)
							Log.d("vehicle", vehicle.toString())
							addVehicle(vehicle)
						}
					} else {
						vehicle =
							Vehicle(
								selectedVehicleType!!,
								"NA"
							)
						addVehicle(vehicle)
						//After adding vehicle automatically close add vehicle view
						if (isAddVehicleCardVisible) {
							resetVehicle()
							isAddVehicleCardVisible = false
							binding.bookingBaseLayout.visibility = View.VISIBLE
							binding.vehicleLayout.addVehicleCard.visibility = View.GONE
						}
					}
				}
			}
		}

		binding.vehicleLayout.closeAddVehicleCard.setOnClickListener {
			if (isAddVehicleCardVisible) {
				resetVehicle()
				isAddVehicleCardVisible = false
				binding.bookingBaseLayout.visibility = View.VISIBLE
				binding.vehicleLayout.addVehicleCard.visibility = View.GONE
			}
		}

		binding.openAddGoodsCard.setOnClickListener {
			//logic to show/hide views when add other card is opened
			//clear all unfinished data from every cards except other
			if (!isAddGoodsCardVisible) {
				resetPassenger()
				resetVehicle()
				binding.bookingBaseLayout.visibility = View.GONE
				isAddPassengerCardVisible = false
				binding.passengerLayout.addPassengerCard.visibility = View.GONE
				isAddVehicleCardVisible = false
				binding.vehicleLayout.addVehicleCard.visibility = View.GONE
				isSummaryVisible = false
				binding.summarySection.visibility = View.GONE
				binding.proceedBtn.visibility = View.GONE
				binding.viewBookingSummaryBtn.text = "VIEW SUMMARY"
				isAddGoodsCardVisible = true
				binding.otherLayout.addGoodsCard.visibility = View.VISIBLE

				binding.otherLayout.addGoods.setOnClickListener {
					if (selectedGoodsType == null) {
						Toast.makeText(this, "Please select goods type", Toast.LENGTH_SHORT).show()
					} else {
						if (binding.otherLayout.goodsName.text.isEmpty()) {
							Toast.makeText(this, "Please enter goods name", Toast.LENGTH_SHORT)
								.show()
						} else {
							if (binding.otherLayout.goodsQuantity.text.isEmpty()) {
								Toast.makeText(
									this,
									"Please enter goods quantity",
									Toast.LENGTH_SHORT
								).show()
							} else {
								others = Others(
									selectedGoodsType!!,
									binding.otherLayout.goodsName.text.toString()
										.uppercase(Locale.getDefault()),
									binding.otherLayout.goodsQuantity.text.toString().toInt()
								)
								addOthers(others)
								// After adding others automatically close add others view
								if (isAddGoodsCardVisible) {
									resetGoods()
									isAddGoodsCardVisible = false
									binding.bookingBaseLayout.visibility = View.VISIBLE
									binding.otherLayout.addGoodsCard.visibility = View.GONE
								}
							}
						}
					}
				}
			}
		}

		binding.otherLayout.closeAddGoodsCard.setOnClickListener {
			if (isAddGoodsCardVisible) {
				resetGoods()
				isAddGoodsCardVisible = false
				binding.bookingBaseLayout.visibility = View.VISIBLE
				binding.otherLayout.addGoodsCard.visibility = View.GONE
			}
		}

		binding.viewBookingSummaryBtn.setOnClickListener {
			if (isSummaryVisible) {
				binding.summarySection.visibility = View.GONE
				isSummaryVisible = false
				binding.proceedBtn.visibility = View.GONE
				binding.bookingBaseLayoutWithoutSummary.visibility = View.VISIBLE
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
					binding.passengerLayout.addPassengerCard.visibility = View.GONE
					binding.vehicleLayout.addVehicleCard.visibility = View.GONE
					binding.otherLayout.addGoodsCard.visibility = View.GONE
					binding.bookingBaseLayoutWithoutSummary.visibility = View.GONE
					binding.proceedBtn.visibility = View.VISIBLE
					adapter = SummaryAdapter(allDataList)
					adapter.setOnItemClickListener(this)
					binding.summarySection.visibility = View.VISIBLE
					isSummaryVisible = true
					binding.viewBookingSummaryBtn.text = "CLOSE"
					val recyclerView = binding.summaryListRecyclerView
					recyclerView.adapter = adapter
					recyclerView.layoutManager = LinearLayoutManager(this)
					recyclerView.isNestedScrollingEnabled = false
					binding.proceedBtn.setOnClickListener {
						if (passengerList.isEmpty()) {
							NotificationHelper().getWarningAlert(this, "No passengers found")
						} else {
							bookTicket(
								ferryService.id,
								Gson().toJson(passengerList),
								Gson().toJson(vehicleList),
								Gson().toJson(othersList)
							)
							Log.d("ferry service", ferryService.id.toString())
							Log.d("passenger list", Gson().toJson(passengerList))
							Log.d("vehicle list", Gson().toJson(vehicleList))
							Log.d("others list", Gson().toJson(othersList))
						}
					}
				}
			}
		}

		onBackPressedDispatcher.addCallback(this) {
			//if any card is opened except the initial cards than all will close
			//else if in initial stage it will go back to FerryListActivity.class
			if (isAddPassengerCardVisible) {
				resetPassenger()
				isAddPassengerCardVisible = false
				binding.bookingBaseLayout.visibility = View.VISIBLE
				binding.passengerLayout.addPassengerCard.visibility = View.GONE
			} else if (isAddVehicleCardVisible) {
				resetVehicle()
				isAddVehicleCardVisible = false
				binding.bookingBaseLayout.visibility = View.VISIBLE
				binding.vehicleLayout.addVehicleCard.visibility = View.GONE
			} else if (isAddGoodsCardVisible) {
				resetGoods()
				isAddGoodsCardVisible = false
				binding.bookingBaseLayout.visibility = View.VISIBLE
				binding.otherLayout.addGoodsCard.visibility = View.GONE
			} else if (isSummaryVisible) {
				isSummaryVisible = false
				binding.proceedBtn.visibility = View.GONE
				binding.bookingBaseLayoutWithoutSummary.visibility = View.VISIBLE
				binding.summarySection.visibility = View.GONE
				binding.viewBookingSummaryBtn.text = "VIEW SUMMARY"
			} else {
				try {
					editor.remove("passenger_card_details")
					editor.apply()
				} catch (e: Exception) {
					Log.d("msg", "No cards available to remove")
				}
				startActivity(Intent(this@BookActivity, FerryListActivity::class.java))
			}
		}
	}

	@SuppressLint("SetTextI18n")
	private fun openEnterCardNumberBottomSheet() {
		//Logic to open bottom sheet to enter card code and get card details
		val enterCardDetailsBottomSheet = BottomSheetDialog(this@BookActivity)
		enterCardDetailsBottomSheet.setCancelable(false)
		enterCardDetailsBottomSheet.setContentView(R.layout.enter_device_serial_number_layout)
		val cardCode = enterCardDetailsBottomSheet.findViewById<TextView>(R.id.serial_number)
		val getDetailsBtn =
			enterCardDetailsBottomSheet.findViewById<MaterialButton>(R.id.btn_getTid)
		val textInputLayout =
			enterCardDetailsBottomSheet.findViewById<TextInputLayout>(R.id.textInputLayout)
		textInputLayout!!.hint = "Enter Card Code"
		cardCode!!.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
		cardCode.filters = arrayOf<InputFilter>(InputFilter.AllCaps())
		getDetailsBtn!!.text = "SUBMIT"

		val cancelBtn = enterCardDetailsBottomSheet.findViewById<MaterialButton>(R.id.btn_cancel)
		enterCardDetailsBottomSheet.show()

		getDetailsBtn.setOnClickListener {
			val cardCodeString = cardCode.text.toString()
			if (cardCodeString.isEmpty()) {
				Toast.makeText(this@BookActivity, "Please enter card details", Toast.LENGTH_SHORT)
					.show()
			} else {
				val dialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
				dialog.progressHelper.barColor = Color.parseColor("#2E74A0")
				dialog.titleText = "Getting card details.."
				dialog.setCancelable(false)
				dialog.show()
				val api = RetrofitHelper.getInstance(this@BookActivity)!!.create(Client::class.java)
				GlobalScope.launch {
					val call: Call<JsonObject> = api.getCardDetailsByInput(
						Util().getJwtToken(sharedPreferences.getString("user", "").toString()),
						cardCodeString
					)
					call.enqueue(object : Callback<JsonObject> {
						@SuppressLint("CommitPrefEdits", "NotifyDataSetChanged")
						override fun onResponse(
							call: Call<JsonObject>,
							response: Response<JsonObject>
						) {
							if (response.isSuccessful) {
								val helper = ResponseHelper()
								helper.responseHelper(response.body())
								if (helper.isStatusSuccessful()) {
									dialog.titleText = "Card details found.."
									enterCardDetailsBottomSheet.dismiss()
									dialog.dismissWithAnimation()
									var isPassengerAvailable = false
									var isCardAvailable = false
									lateinit var passengerDetails: PassengerDetails
									lateinit var cardDetails: CardDetails
									val obj = JSONObject(helper.getDataAsString())
									val passengerJSONObject = obj.get("passenger") as JSONObject?
									val cardJSONObject = obj.get("card_details") as JSONObject?

									val cardDetailsBottomSheet =
										BottomSheetDialog(this@BookActivity)
									cardDetailsBottomSheet.setCancelable(false)
									cardDetailsBottomSheet.setContentView(R.layout.details_from_card_layout)
									val cardDetailsCard =
										cardDetailsBottomSheet.findViewById<MaterialCardView>(R.id.card_details_card)
									val passengerDetailsCard =
										cardDetailsBottomSheet.findViewById<MaterialCardView>(R.id.passenger_details_card)
									val cancelBtn =
										cardDetailsBottomSheet.findViewById<ImageView>(R.id.hide_bottom_sheet_image)
									val addCard =
										cardDetailsBottomSheet.findViewById<MaterialButton>(R.id.add_card)
									val addPassenger =
										cardDetailsBottomSheet.findViewById<MaterialButton>(R.id.add_passenger)
									val addBoth =
										cardDetailsBottomSheet.findViewById<MaterialButton>(R.id.add_card_and_passenger)

									if (passengerJSONObject!!.length() > 0) { //adding passenger data to views
										try {
											isPassengerAvailable = true
											val holderDetails =
												cardDetailsBottomSheet.findViewById<TextView>(R.id.holder_details)
											val mobileNo =
												cardDetailsBottomSheet.findViewById<TextView>(R.id.mobile_number)
											val holderType =
												cardDetailsBottomSheet.findViewById<TextView>(R.id.p_type)

											passengerDetails =
												Gson().fromJson(
													passengerJSONObject.toString(),
													object : TypeToken<PassengerDetails>() {}.type
												)

											holderDetails!!.text =
												"${passengerDetails.passenger_name} (${passengerDetails.age}, ${passengerDetails.gender.gender_name})"
											mobileNo!!.text = passengerDetails.mobile_no
											holderType!!.text = passengerDetails.passenger_type.type

											addPassenger!!.setOnClickListener {
												addPassenger(passengerDetails)
												cardDetailsBottomSheet.dismiss()
											}
										} catch (e: Exception) {
											Log.e("Passenger Object", "not found")
											Toast.makeText(
												this@BookActivity,
												"Something went wrong. Please scan again",
												Toast.LENGTH_SHORT
											).show()
											cardDetailsBottomSheet.dismiss()
										}
									} else {
										passengerDetailsCard!!.visibility = View.GONE
										addPassenger!!.visibility = View.GONE
									}

									if (cardJSONObject!!.length() > 0) { //adding card data to views
										try {
											isCardAvailable = true
											val cardNumber =
												cardDetailsBottomSheet.findViewById<TextView>(R.id.card_number)
											val balance =
												cardDetailsBottomSheet.findViewById<TextView>(R.id.card_balance)
											val validTo =
												cardDetailsBottomSheet.findViewById<TextView>(R.id.card_valid_till)

											cardDetails =
												Gson().fromJson(
													cardJSONObject.toString(),
													object : TypeToken<CardDetails>() {}.type
												)

											val noArr = cardDetails.card_no.toCharArray()
											val builder = StringBuilder()
											val visibleSize = noArr.size - 5
											for (i in noArr.size - 1 downTo 0) {
												if (i > visibleSize) {
													builder.append(noArr[i])
												}
											}
											val resString =
												"**** **** **** " + builder.reverse().toString()

											cardNumber!!.text = resString

											balance!!.text = "₹ ${cardDetails.wallet_amount}"
											validTo!!.text = DateAndTimeHelper().changeDateFormat(
												"dd MMM, yyyy",
												cardDetails.valid_upto
											)

											addCard!!.setOnClickListener {
												editor.putString(
													"passenger_card_details",
													Gson().toJson(cardDetails)
												)
												editor.apply()
												Toast.makeText(
													this@BookActivity,
													"Card details added",
													Toast.LENGTH_SHORT
												).show()
												cardDetailsBottomSheet.dismiss()
											}
										} catch (e: java.lang.Exception) {
											Log.e("Card Object", "not found")
											Toast.makeText(
												this@BookActivity,
												"Something went wrong. Please scan again",
												Toast.LENGTH_SHORT
											).show()
											cardDetailsBottomSheet.dismiss()
										}
									} else {
										cardDetailsCard!!.visibility = View.GONE
										addCard!!.visibility = View.GONE
									}

									if (isCardAvailable && isPassengerAvailable) {
										addBoth!!.setOnClickListener {
											addPassenger(passengerDetails)
											editor.putString(
												"passenger_card_details",
												Gson().toJson(cardDetails)
											)
											editor.apply()
											Toast.makeText(
												this@BookActivity,
												"Card and passenger details added",
												Toast.LENGTH_SHORT
											).show()
											cardDetailsBottomSheet.dismiss()
										}
									} else {
										addBoth!!.visibility = View.GONE
									}

									cancelBtn!!.setOnClickListener { cardDetailsBottomSheet.dismiss() }
									cardDetailsBottomSheet.show()
								} else {
									dialog.dismissWithAnimation()
									NotificationHelper().getErrorAlert(
										this@BookActivity,
										helper.getErrorMsg()
									)
								}
							} else {
								dialog.dismissWithAnimation()
								NotificationHelper().getErrorAlert(
									this@BookActivity,
									"Response Error Code" + response.message()
								)
							}
						}

						override fun onFailure(call: Call<JsonObject>, t: Throwable) {
							dialog.dismissWithAnimation()
							NotificationHelper().getErrorAlert(this@BookActivity, "Server Error")
						}
					})
				}
			}
		}

		cancelBtn!!.setOnClickListener {
			enterCardDetailsBottomSheet.dismiss()
		}
	}

	private fun performAction() {
		qrScanIntegrator?.initiateScan()
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
			val api = RetrofitHelper.getInstance(this@BookActivity)!!.create(Client::class.java)
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
							dialog.dismissWithAnimation()
							val helper = ResponseHelper()
							helper.responseHelper(response.body())
							if (helper.isStatusSuccessful()) {
								dialog.dismissWithAnimation()
								val ticket: Ticket = Gson().fromJson(
									helper.getDataAsString(),
									object : TypeToken<Ticket>() {}.type
								)
								editor.putString("ticket", Gson().toJson(ticket))
								editor.apply()
								startActivity(Intent(this@BookActivity, TicketActivity::class.java))
							} else {
								dialog.dismiss()
								NotificationHelper().getErrorAlert(
									this@BookActivity,
									helper.getErrorMsg()
								)
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
		val api = RetrofitHelper.getInstance(this@BookActivity)!!.create(Client::class.java)
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
						helper.responseHelper(response.body())
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
		Toast.makeText(this@BookActivity,"Goods has been added", Toast.LENGTH_SHORT).show()
		totalGoodsCount += 1
		othersList.add(others)
		binding.goodsCount.text = totalGoodsCount.toString()
		resetGoods()
	}

	private fun addVehicle(vehicle: Vehicle) {
		allDataList.add(vehicle)
		Toast.makeText(this@BookActivity,"Vehicle has been added", Toast.LENGTH_SHORT).show()
		totalVehiclesCount += 1
		vehicleList.add(vehicle)
		binding.vehiclesCount.text = totalVehiclesCount.toString()
		resetVehicle()
	}

	private fun addPassenger(passenger: PassengerDetails) {
		if (savedPhoneNumbersList.contains(passenger.mobile_no) && passenger.mobile_no!="") {
			Toast.makeText(this@BookActivity, "Passenger already exists", Toast.LENGTH_SHORT).show()
		} else {
			savedPhoneNumbersList.add(passenger.mobile_no)
			allDataList.add(passenger)
			Toast.makeText(this@BookActivity, "Passenger has been added", Toast.LENGTH_SHORT).show()
			totalPassengerCount += 1
			if (totalPassengerCount > 0) {
				binding.openAddVehiclesCard.visibility = View.VISIBLE
				binding.openAddGoodsCard.visibility = View.VISIBLE
			}
			passengerList.add(passenger)
			binding.passengerCount.text = totalPassengerCount.toString()
			resetPassenger()
		}
	}

	private fun getDataFromQr(data: String) {
		val dialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
		dialog.progressHelper.barColor = Color.parseColor("#2E74A0")
		dialog.titleText = "Getting card details.."
		dialog.setCancelable(false)
		dialog.show()
		val api = RetrofitHelper.getInstance(this@BookActivity)!!.create(Client::class.java)
		GlobalScope.launch {
			val call: Call<JsonObject> = api.getCardDetailsByScan(
				Util().getJwtToken(sharedPreferences.getString("user", "").toString()),
				data
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
							dialog.dismissWithAnimation()
							var isPassengerAvailable = false
							var isCardAvailable = false
							lateinit var passengerDetails: PassengerDetails
							lateinit var cardDetails: CardDetails
							val obj = JSONObject(helper.getDataAsString())
							val passengerJSONObject = obj.get("passenger") as JSONObject?
							val cardJSONObject = obj.get("card_details") as JSONObject?

							val cardDetailsBottomSheet = BottomSheetDialog(this@BookActivity)
							cardDetailsBottomSheet.setCancelable(false)
							cardDetailsBottomSheet.setContentView(R.layout.details_from_card_layout)
							val cardDetailsCard =
								cardDetailsBottomSheet.findViewById<MaterialCardView>(R.id.card_details_card)
							val passengerDetailsCard =
								cardDetailsBottomSheet.findViewById<MaterialCardView>(R.id.passenger_details_card)
							val cancelBtn =
								cardDetailsBottomSheet.findViewById<ImageView>(R.id.hide_bottom_sheet_image)
							val addCard =
								cardDetailsBottomSheet.findViewById<MaterialButton>(R.id.add_card)
							val addPassenger =
								cardDetailsBottomSheet.findViewById<MaterialButton>(R.id.add_passenger)
							val addBoth =
								cardDetailsBottomSheet.findViewById<MaterialButton>(R.id.add_card_and_passenger)

							if (passengerJSONObject!!.length() > 0) {
								try {
									isPassengerAvailable = true
									val holderDetails =
										cardDetailsBottomSheet.findViewById<TextView>(R.id.holder_details)
									val mobileNo =
										cardDetailsBottomSheet.findViewById<TextView>(R.id.mobile_number)
									val holderType =
										cardDetailsBottomSheet.findViewById<TextView>(R.id.p_type)

									passengerDetails =
										Gson().fromJson(
											passengerJSONObject.toString(),
											object : TypeToken<PassengerDetails>() {}.type
										)

									holderDetails!!.text =
										"${passengerDetails.passenger_name} (${passengerDetails.age}, ${passengerDetails.gender.gender_name})"
									mobileNo!!.text = passengerDetails.mobile_no
									holderType!!.text = passengerDetails.passenger_type.type

									addPassenger!!.setOnClickListener {
										addPassenger(passengerDetails)
										cardDetailsBottomSheet.dismiss()
									}
								} catch (e: Exception) {
									Log.e("Passenger Object", "not found")
									Toast.makeText(
										this@BookActivity,
										"Something went wrong. Please scan again",
										Toast.LENGTH_SHORT
									).show()
									cardDetailsBottomSheet.dismiss()
								}
							} else {
								passengerDetailsCard!!.visibility = View.GONE
								addPassenger!!.visibility = View.GONE
							}

							if (cardJSONObject!!.length() > 0) {
								try {
									isCardAvailable = true
									val cardNumber =
										cardDetailsBottomSheet.findViewById<TextView>(R.id.card_number)
									val balance =
										cardDetailsBottomSheet.findViewById<TextView>(R.id.card_balance)
									val validTo =
										cardDetailsBottomSheet.findViewById<TextView>(R.id.card_valid_till)

									cardDetails =
										Gson().fromJson(
											cardJSONObject.toString(),
											object : TypeToken<CardDetails>() {}.type
										)

									val noArr = cardDetails.card_no.toCharArray()
									val builder = StringBuilder()
									val visibleSize = noArr.size - 5
									for (i in noArr.size - 1 downTo 0) {
										if (i > visibleSize) {
											builder.append(noArr[i])
										}
									}
									val resString = "**** **** **** " + builder.reverse().toString()

									cardNumber!!.text = resString

									balance!!.text = "₹ ${cardDetails.wallet_amount}"
									validTo!!.text = DateAndTimeHelper().changeDateFormat(
										"dd MMM, yyyy",
										cardDetails.valid_upto
									)

									addCard!!.setOnClickListener {
										/*var card: String? = null
										try {
											card = sharedPreferences.getString("passenger_card_details", "").toString()
										} catch (e : Exception) {
											e.printStackTrace()
										}
										if (card != null || card != "") {

										}*/
										editor.putString(
											"passenger_card_details",
											Gson().toJson(cardDetails)
										)
										editor.apply()
										Toast.makeText(
											this@BookActivity,
											"Card details added",
											Toast.LENGTH_SHORT
										).show()
										cardDetailsBottomSheet.dismiss()
									}
								} catch (e: java.lang.Exception) {
									Log.e("Card Object", "not found")
									Toast.makeText(
										this@BookActivity,
										"Something went wrong. Please scan again",
										Toast.LENGTH_SHORT
									).show()
									cardDetailsBottomSheet.dismiss()
								}
							} else {
								cardDetailsCard!!.visibility = View.GONE
								addCard!!.visibility = View.GONE
							}

							if (isCardAvailable && isPassengerAvailable) {
								addBoth!!.setOnClickListener {
									addPassenger(passengerDetails)
									editor.putString(
										"passenger_card_details",
										Gson().toJson(cardDetails)
									)
									editor.apply()
									Toast.makeText(
										this@BookActivity,
										"Card details added",
										Toast.LENGTH_SHORT
									).show()
									cardDetailsBottomSheet.dismiss()
								}
							} else {
								addBoth!!.visibility = View.GONE
							}

							cancelBtn!!.setOnClickListener { cardDetailsBottomSheet.dismiss() }
							cardDetailsBottomSheet.show()
						} else {
							dialog.dismissWithAnimation()
							NotificationHelper().getErrorAlert(
								this@BookActivity,
								helper.getErrorMsg()
							)
						}
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
		binding.passengerLayout.name.setText("")
		binding.passengerLayout.phone.setText("")
		binding.passengerLayout.age.setText("")
		binding.passengerLayout.address.setText("")
		binding.passengerLayout.rgPassengerType.clearCheck()
		binding.passengerLayout.rgGender.clearCheck()
		binding.passengerLayout.disableCheckBox.isChecked = false
	}

	private fun resetVehicle() {
		binding.vehicleLayout.vehicleNumber.visibility = View.VISIBLE
		binding.vehicleLayout.vehicleNumber.setText("")
		selectedVehicleType = null
		binding.vehicleLayout.vehicleTypeDropdown.setText(
			binding.vehicleLayout.vehicleTypeDropdown.adapter.getItem(0).toString(), false
		)
	}

	private fun resetGoods() {
		binding.otherLayout.goodsName.setText("")
		binding.otherLayout.goodsQuantity.setText("")
		selectedGoodsType = null
		binding.otherLayout.othersTypeDropdown.setText(
			binding.otherLayout.othersTypeDropdown.adapter.getItem(0).toString(), false
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
				if (totalPassengerCount == 1 || position == 0) {
					alert.titleText = "WARNING"
					alert.contentText =
						"This is the primary passenger and deleting it will remove all data. Delete?"
					alert.setConfirmClickListener {
						binding.openAddVehiclesCard.visibility = View.GONE
						binding.openAddGoodsCard.visibility = View.GONE
						totalPassengerCount = 0
						totalVehiclesCount = 0
						totalGoodsCount = 0
						binding.passengerCount.text = totalPassengerCount.toString()
						binding.vehiclesCount.text = totalPassengerCount.toString()
						binding.goodsCount.text = totalPassengerCount.toString()
						alert.changeAlertType(SweetAlertDialog.SUCCESS_TYPE)
						allDataList.clear()
						passengerList.clear()
						vehicleList.clear()
						othersList.clear()
						savedPhoneNumbersList.clear()
						adapter.notifyDataSetChanged()
						alert.dismissWithAnimation()
						binding.summarySection.visibility = View.GONE
						binding.proceedBtn.visibility = View.GONE
						binding.viewBookingSummaryBtn.text = "VIEW SUMMARY"
//						passengerList.removeAt(position)
						Toast.makeText(this@BookActivity,"All particulars have been removed", Toast.LENGTH_SHORT).show()
						isSummaryVisible = false
						binding.proceedBtn.visibility = View.GONE
						binding.bookingBaseLayoutWithoutSummary.visibility = View.VISIBLE //for mobile view
						binding.summarySection.visibility = View.GONE
						binding.viewBookingSummaryBtn.text = "VIEW SUMMARY"
					}
					alert.setCancelClickListener {
						alert.dismiss()
					}
				} else {
					totalPassengerCount -= 1
					binding.passengerCount.text = totalPassengerCount.toString()
					if (totalPassengerCount < 1) {
						binding.openAddVehiclesCard.visibility = View.GONE
						binding.openAddGoodsCard.visibility = View.GONE
					}
					allDataList.removeAt(position)
					passengerList.removeAt(position)
					var phNo = ""
					try {
						phNo = passengerList[position].mobile_no
					} catch (e: Exception) {
						e.printStackTrace()
					}
					if (savedPhoneNumbersList.contains(phNo)) {
						savedPhoneNumbersList.remove(phNo)
					}
					Toast.makeText(this@BookActivity,"Passenger has been removed", Toast.LENGTH_SHORT).show()
					adapter.notifyDataSetChanged()
					alert.dismissWithAnimation()
				}
			} else {
				if (allDataList[position] is Vehicle) {
					totalVehiclesCount -= 1
					binding.vehiclesCount.text = totalVehiclesCount.toString()
					vehicleList.remove(allDataList[position])
					Toast.makeText(this@BookActivity,"Vehicle has been removed", Toast.LENGTH_SHORT).show()
				} else if (allDataList[position] is Others) {
					totalGoodsCount -= 1
					binding.goodsCount.text = totalGoodsCount.toString()
					othersList.remove(allDataList[position])
					Toast.makeText(this@BookActivity,"Goods has been removed", Toast.LENGTH_SHORT).show()
				}
				allDataList.removeAt(position)
				adapter.notifyDataSetChanged()
				alert.dismissWithAnimation()
			}
		}
		alert.show()
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
		if (result != null) {
			// If QRCode has no data.
			if (result.contents == null) {
				Toast.makeText(this, "Result not found", Toast.LENGTH_LONG).show()
			} else {
//				println(result.contents)
				getDataFromQr(result.contents)
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data)
		}
	}

	override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
		if (currentFocus != null) {
			val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
			imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
		}
		return super.dispatchTouchEvent(ev)
	}
}