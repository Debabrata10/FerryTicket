package com.amtron.ferryticket.ui

import android.annotation.SuppressLint
import android.content.SharedPreferences.Editor
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.amtron.ferryticket.databinding.ActivityCurrentDateStatisticsBinding
import com.amtron.ferryticket.helper.DateAndTimeHelper
import com.amtron.ferryticket.helper.NotificationHelper
import com.amtron.ferryticket.helper.ResponseHelper
import com.amtron.ferryticket.helper.Util
import com.amtron.ferryticket.network.Client
import com.amtron.ferryticket.network.RetrofitHelper
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@DelicateCoroutinesApi
class CurrentDateStatisticsActivity : AppCompatActivity() {
	private lateinit var binding: ActivityCurrentDateStatisticsBinding
	private lateinit var editor: Editor

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityCurrentDateStatisticsBinding.inflate(layoutInflater)
		setContentView(binding.root)

		val sharedPreferences = this.getSharedPreferences("IWTCounter", MODE_PRIVATE)
		editor = sharedPreferences.edit()

		val dialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
		dialog.progressHelper.barColor = Color.parseColor("#2E74A0")
		dialog.titleText = "Getting Today's Report..."
		dialog.setCancelable(false)
		dialog.show()
		val api = RetrofitHelper.getInstance(this)!!.create(Client::class.java)
		GlobalScope.launch {
			val call: Call<JsonObject> = api.getTodayStatistics(
				Util().getJwtToken(
					sharedPreferences.getString("user", "").toString()
				)
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
							dialog.titleText = "Data Fetched"
							dialog.hideConfirmButton()
							dialog.dismissWithAnimation()
							val todayOverview: TodayOverview = Gson().fromJson(
								helper.getDataAsString(),
								TodayOverview::class.java
							)
							binding.currentDate.text = DateAndTimeHelper().changeDateFormat(
								"dd MMM, yyyy",
								todayOverview.current_date
							)
							binding.totalCashByHand.text =
								todayOverview.mode_date[0].amounts.toString()
							binding.totalTicketsByHand.text =
								todayOverview.mode_date[0].tickets.toString()
							binding.totalCashByCardP.text =
								todayOverview.mode_date[1].amounts.toString()
							binding.totalCardPTickets.text =
								todayOverview.mode_date[1].tickets.toString()
							binding.totalCashByCardO.text =
								todayOverview.mode_date[2].amounts.toString()
							binding.totalCardOTickets.text =
								todayOverview.mode_date[2].tickets.toString()
							binding.totalCashByOnline.text =
								todayOverview.mode_date[3].amounts.toString()
							binding.totalTicketsOnline.text =
								todayOverview.mode_date[3].tickets.toString()
							binding.totalPaymentReceived.text =
								todayOverview.total_data.amounts.toString()
							binding.totalTicketsCollected.text =
								todayOverview.total_data.tickets.toString()
						} else {
							dialog.dismiss()
							NotificationHelper().getErrorAlert(
								this@CurrentDateStatisticsActivity,
								helper.getErrorMsg()
							)
						}
					} else {
						dialog.dismiss()
						NotificationHelper().getErrorAlert(
							this@CurrentDateStatisticsActivity,
							"Response Error Code" + response.message()
						)
					}
				}

				override fun onFailure(call: Call<JsonObject>, t: Throwable) {
					NotificationHelper().getErrorAlert(
						this@CurrentDateStatisticsActivity,
						"Server Error"
					)
					dialog.dismiss()
				}
			})
		}
	}
}

data class TodayOverview(
	val current_date: String,
	val mode_date: List<OverviewStructure>,
	val total_data: OverviewStructure,
	val cash_data: OverviewStructure,
)

data class OverviewStructure(
	val mode_of_payment: String,
	val tickets: Int,
	val amounts: Double,
)
