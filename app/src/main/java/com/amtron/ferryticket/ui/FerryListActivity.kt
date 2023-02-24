package com.amtron.ferryticket.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.amtron.ferryticket.adapter.FerryListAdapter
import com.amtron.ferryticket.adapter.OnRecyclerViewItemClickListener
import com.amtron.ferryticket.databinding.ActivityFerryListBinding
import com.amtron.ferryticket.helper.DateHelper
import com.amtron.ferryticket.model.Ferry

class FerryListActivity : AppCompatActivity(), OnRecyclerViewItemClickListener {
    private lateinit var binding: ActivityFerryListBinding
    private lateinit var ferryList: ArrayList<Ferry>
    private lateinit var adapter: FerryListAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFerryListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.date.text = DateHelper().getTodayOrTomorrow("today", "dd MMM, yyyy")

        ferryList = ArrayList()
        val ferry1 = Ferry(1,
            "ROPAX",
            100,
            "public",
            "GHAT1",
            "GHAT2",
            "12:00PM",
            "4:00PM",
            20,
            1,
            3,
            1,
            1,
            23
        )
        val ferry2 = Ferry(1,
            "S.B SUBHALAXMI",
            23,
            "pubic",
            "GHAT2",
            "GHAT3",
            "12:00PM",
            "4:00PM",
            20,
            10,
            8,
            0,
            0,
            50
        )

        ferryList.add(ferry1)
        ferryList.add(ferry2)

        adapter = FerryListAdapter(ferryList)
        adapter.setOnItemClickListener(this)
        recyclerView = binding.ferryListRecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.isNestedScrollingEnabled = false
    }

    override fun onItemClickListener(position: Int, type: String) {
        val i = Intent(this, BookActivity::class.java)
        startActivity(i)
    }
}