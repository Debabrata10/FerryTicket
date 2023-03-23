package com.amtron.ferryticket.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amtron.ferryticket.R
import com.amtron.ferryticket.model.Others
import com.amtron.ferryticket.model.PassengerDetails
import com.amtron.ferryticket.model.Vehicle
import com.google.gson.Gson

class SummaryAdapter(private val summaryList: List<Any>, private val mContext: Context) :
	RecyclerView.Adapter<SummaryAdapter.ViewHolder>() {
	private lateinit var mItemClickListener: OnRecyclerViewItemClickListener
	private lateinit var any: Any

	fun setOnItemClickListener(mItemClickListener: OnRecyclerViewItemClickListener?) {
		this.mItemClickListener = mItemClickListener!!
	}

	override fun onCreateViewHolder(
		parent: ViewGroup,
		viewType: Int
	): ViewHolder {
		val layoutInflater = LayoutInflater.from(parent.context)
		val view = layoutInflater.inflate(R.layout.summary_layout, parent, false)
		return ViewHolder(view)
	}

	@SuppressLint("SetTextI18n")
	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		any = summaryList[position]
		if (any is PassengerDetails) {
			holder.imageLayout.setBackgroundColor(Color.parseColor(("#56A8AF")))
			holder.image.setBackgroundResource(R.drawable.ic_baseline_person_24)
			holder.image.layoutParams.height = 50
			holder.image.layoutParams.width = 50
			holder.image.maxWidth = 50
			holder.image.maxHeight = 50
			holder.name.text =
				"NAME: ${(any as PassengerDetails).passenger_name} (${(any as PassengerDetails).gender.gender_name}, ${(any as PassengerDetails).age})"
			holder.type.text = "PASSENGER TYPE: ${(any as PassengerDetails).passenger_type.type}"
			holder.phone.text = "PHONE NUMBER: ${(any as PassengerDetails).mobile_no}"
			holder.address.text = "ADDRESS: ${(any as PassengerDetails).address}"
		} else if (any is Vehicle) {
			holder.imageLayout.setBackgroundColor(Color.parseColor(("#09509A")))
			holder.image.setBackgroundResource(R.drawable.ic_baseline_directions_car_24)
			holder.image.layoutParams.height = 50
			holder.image.layoutParams.width = 50
			holder.name.text = "VEHICLE TYPE: ${(any as Vehicle).vehicle_type.p_name}"
			if ((any as Vehicle).reg_no != "") {
				holder.type.text = "VEHICLE NUMBER: ${(any as Vehicle).reg_no}"
				holder.phone.visibility = View.GONE
				holder.address.visibility = View.GONE
			} else {
				holder.type.text = "VEHICLE NUMBER: NA"
				holder.phone.visibility = View.GONE
				holder.address.visibility = View.GONE
			}
		} else if (any is Others) {
			holder.imageLayout.setBackgroundColor(Color.parseColor(("#EA3C12")))
			holder.image.setBackgroundResource(R.drawable.ic_baseline_luggage_24)
			holder.image.layoutParams.height = 50
			holder.image.layoutParams.width = 50
			holder.name.text = "GOODS NAME: ${(any as Others).other_name}"
			holder.type.text = "GOODS TYPE: ${(any as Others).other_type.p_name}"
			holder.phone.text = "QUANTITY: " + (any as Others).quantity
		}
		holder.cardDeleteBtn.setOnClickListener {
			mItemClickListener.onItemClickListener(position, Gson().toJson(summaryList[position]))
		}
	}

	override fun getItemCount(): Int {
		return summaryList.size
	}

	class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val imageLayout: LinearLayout = itemView.findViewById(R.id.image_layout)
		val cardDeleteBtn: ImageView = itemView.findViewById(R.id.delete_btn)
		val name: TextView = itemView.findViewById(R.id.name)
		val type: TextView = itemView.findViewById(R.id.type)
		val phone: TextView = itemView.findViewById(R.id.phone)
		val address: TextView = itemView.findViewById(R.id.address)
		val image: ImageView = itemView.findViewById(R.id.image_type)
	}
}