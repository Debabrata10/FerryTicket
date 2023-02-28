package com.amtron.ferryticket.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amtron.ferryticket.adapter.FerryServiceAdapter
import com.amtron.ferryticket.adapter.OnRecyclerViewItemClickListener
import com.amtron.ferryticket.databinding.ActivityFerryListBinding
import com.amtron.ferryticket.helper.DateHelper
import com.amtron.ferryticket.model.FerryService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class FerryListActivity : AppCompatActivity(), OnRecyclerViewItemClickListener {
	private lateinit var binding: ActivityFerryListBinding
	private lateinit var adapter: FerryServiceAdapter
	private lateinit var recyclerView: RecyclerView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityFerryListBinding.inflate(layoutInflater)
		setContentView(binding.root)

		val bundleString = intent.extras
		try {
			val ferryServicesString = bundleString!!.getString("ferryServices", "")
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

			binding.date.text = DateHelper().getTodayOrTomorrow("today", "dd MMM, yyyy")
			binding.srcGhat.text = bundleString.getString("sourceGhat", "")
			binding.destGhat.text = bundleString.getString("destinationGhat", "")
		} catch (e: Exception) {
			Log.d("msg", "nothing found")
		}

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
		val i = Intent(this, BookActivity::class.java)
		startActivity(i)
	}
}