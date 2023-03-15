package com.amtron.ferryticket.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.amtron.ferryticket.adapter.FerryServiceAdapter
import com.amtron.ferryticket.adapter.OnRecyclerViewItemClickListener
import com.amtron.ferryticket.databinding.ActivityFerryListBinding
import com.amtron.ferryticket.helper.DateHelper
import com.amtron.ferryticket.helper.NotificationHelper
import com.amtron.ferryticket.helper.ResponseHelper
import com.amtron.ferryticket.helper.Util
import com.amtron.ferryticket.model.FerryService
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
class FerryListActivity : AppCompatActivity(), OnRecyclerViewItemClickListener {
	private lateinit var sharedPreferences: SharedPreferences
	private lateinit var editor: SharedPreferences.Editor
	private lateinit var binding: ActivityFerryListBinding
	private lateinit var adapter: FerryServiceAdapter
	private lateinit var recyclerView: RecyclerView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityFerryListBinding.inflate(layoutInflater)
		setContentView(binding.root)

		sharedPreferences = this.getSharedPreferences("IWTCounter", MODE_PRIVATE)
		editor = sharedPreferences.edit()
		val ferryServicesString = sharedPreferences.getString("ferryServices", "").toString()
		val ferryServiceList: ArrayList<FerryService> = Gson().fromJson(
			ferryServicesString.toString(),
			object : TypeToken<List<FerryService>>() {}.type
		)
		adapter = FerryServiceAdapter(ferryServiceList)
		adapter.setOnItemClickListener(this)
		recyclerView = binding.ferryListRecyclerView
		recyclerView.adapter = adapter
		recyclerView.layoutManager = LinearLayoutManager(this)
		recyclerView.isNestedScrollingEnabled = false

		binding.date.text = DateHelper().getToday("dd MMM, yyyy")
		binding.srcGhat.text = sharedPreferences.getString("sourceGhat", "").toString()
		binding.destGhat.text = sharedPreferences.getString("destinationGhat", "").toString()

		onBackPressedDispatcher.addCallback(this) {
			startActivity(
				Intent(
					this@FerryListActivity,
					HomeActivity::class.java
				)
			)
		}
	}

	override fun onItemClickListener(position: Int, type: String) {
		val dialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
		dialog.progressHelper.barColor = Color.parseColor("#2E74A0")
		dialog.titleText = "Loading..."
		dialog.setCancelable(false)
		dialog.show()
		val selectedFerryService: FerryService = Gson().fromJson(
			type,
			object : TypeToken<FerryService>() {}.type
		)
		val api = RetrofitHelper.getInstance().create(Client::class.java)
		GlobalScope.launch {
			val call: Call<JsonObject> = api.getService(
				Util().getJwtToken(sharedPreferences.getString("user", "").toString()),
				selectedFerryService.id
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
							dialog.dismiss()
							val ferryService: FerryService = Gson().fromJson(
								helper.getDataAsString(),
								object : TypeToken<FerryService>() {}.type
							)
							editor.putString("service", Gson().toJson(ferryService))
							editor.apply()
							startActivity(
								Intent(this@FerryListActivity, BookActivity::class.java)
							)
						} else {
							dialog.dismiss()
							NotificationHelper().getErrorAlert(
								this@FerryListActivity,
								helper.getErrorMsg()
							)
						}
					} else {
						dialog.dismiss()
						NotificationHelper().getErrorAlert(
							this@FerryListActivity,
							"Response Error Code" + response.message()
						)
					}
				}

				override fun onFailure(call: Call<JsonObject>, t: Throwable) {
					dialog.dismiss()
					NotificationHelper().getErrorAlert(this@FerryListActivity, "Server Error")
				}
			})
		}
	}
}