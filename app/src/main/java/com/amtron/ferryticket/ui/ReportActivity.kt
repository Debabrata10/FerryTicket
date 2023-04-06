package com.amtron.ferryticket.ui;

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amtron.ferryticket.adapter.ReportAdapter
import com.amtron.ferryticket.databinding.ActivityReportBinding
import com.amtron.ferryticket.helper.DateAndTimeHelper
import com.amtron.ferryticket.model.Report
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class ReportActivity : AppCompatActivity() {
	private lateinit var sharedPreferences: SharedPreferences
	private lateinit var editor: SharedPreferences.Editor
	private lateinit var binding: ActivityReportBinding
	private lateinit var adapter: ReportAdapter
	private lateinit var recyclerView: RecyclerView

	@SuppressLint("NotifyDataSetChanged")
	@Override
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState);
		binding = ActivityReportBinding.inflate(layoutInflater)
		setContentView(binding.root);

		sharedPreferences = this.getSharedPreferences("IWTCounter", MODE_PRIVATE)
		editor = sharedPreferences.edit()

		binding.date.text = DateAndTimeHelper().getToday("dd MMM, yyyy")
		binding.srcGhat.text = sharedPreferences.getString("sourceGhat", "").toString()
		binding.destGhat.text = sharedPreferences.getString("destinationGhat", "").toString()

		val reportListString = sharedPreferences.getString("reports", "").toString()
		val reportList: ArrayList<Report> = Gson().fromJson(
			reportListString,
			object : TypeToken<List<Report>>() {}.type
		)
		adapter = ReportAdapter(reportList)
//        adapter.setOnItemClickListener(this)
		recyclerView = binding.reportListRecyclerView
		recyclerView.adapter = adapter
		adapter.notifyDataSetChanged()
		recyclerView.layoutManager = LinearLayoutManager(this)
		recyclerView.isNestedScrollingEnabled = false

		onBackPressedDispatcher.addCallback(this) {
			startActivity(
				Intent(
					this@ReportActivity,
					HomeActivity::class.java
				)
			)
		}
	}
}