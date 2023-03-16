package com.amtron.ferryticket.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amtron.ferryticket.R
import com.amtron.ferryticket.adapter.OtherDetailsTicketViewAdapter
import com.amtron.ferryticket.adapter.PassengerDetailsTicketViewAdapter
import com.amtron.ferryticket.adapter.VehicleDetailsTicketViewAdapter
import com.amtron.ferryticket.databinding.ActivityTicketBinding
import com.amtron.ferryticket.model.Others
import com.amtron.ferryticket.model.PassengerDetails
import com.amtron.ferryticket.model.Ticket
import com.amtron.ferryticket.model.Vehicle
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.DelicateCoroutinesApi

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

	@SuppressLint("SetTextI18n")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityTicketBinding.inflate(layoutInflater)
		setContentView(binding.root)

		sharedPreferences = this.getSharedPreferences("IWTCounter", MODE_PRIVATE)
		editor = sharedPreferences.edit()

		val ticketString = sharedPreferences.getString("ticket", "").toString()
		ticket = Gson().fromJson(
			ticketString,
			object : TypeToken<Ticket>() {}.type
		)

		if (ticket.two_way == 0) {
			binding.isTwoWayImg.visibility = View.GONE
			binding.isTwoWayText.visibility = View.GONE
		}

		binding.ticketNo.text = ticket.ticket_no
		binding.price.text = ticket.total_amt.toString()
		binding.ticketDate.text = ticket.ferry_date

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
				startActivity(
					Intent(this, TicketListActivity::class.java)
				)
			}
			cancel?.setOnClickListener { cashPaymentBottomSheet.dismiss() }
		}

		binding.posPay.setOnClickListener {
			val bundle = Bundle()
			val i = Intent(this, PosActivity::class.java)
			bundle.putString("price", ticket.total_amt.toString())
			i.putExtras(bundle)
			startActivity(i)
		}

		onBackPressedDispatcher.addCallback(this) {
			startActivity(
				Intent(this@TicketActivity, TicketListActivity::class.java)
			)
		}
	}
}