package com.amtron.ferryticket.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amtron.ferryticket.R
import com.amtron.ferryticket.adapter.OnRecentTicketsRecyclerViewItemClickListener
import com.amtron.ferryticket.adapter.OnSourceDestinationRecyclerViewItemClickListener
import com.amtron.ferryticket.adapter.RecentTicketAdapter
import com.amtron.ferryticket.adapter.SourceDestinationAdapter
import com.amtron.ferryticket.databinding.ActivityHomeBinding
import com.amtron.ferryticket.helper.DateHelper
import com.amtron.ferryticket.model.Ghat
import com.amtron.ferryticket.model.SourceDestinationData
import com.amtron.ferryticket.model.Ticket
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class HomeActivity : AppCompatActivity(), OnRecentTicketsRecyclerViewItemClickListener,
    OnSourceDestinationRecyclerViewItemClickListener {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var recentTicketsLayoutManager: LinearLayoutManager
    private lateinit var srcDestLayoutManager: LinearLayoutManager
    private lateinit var sourceDestinationRecyclerView: RecyclerView
    private lateinit var recentTicketsRecyclerView: RecyclerView
    private lateinit var sourceDestinationAdapter: SourceDestinationAdapter
    private lateinit var recentTicketAdapter: RecentTicketAdapter

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recentTicketsLayoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        srcDestLayoutManager = LinearLayoutManager(this)

//        getTotalTicketsToday()
        getRecentTickets()
        getSourceDestinationCards()

        binding.profileCard.setOnClickListener {
            startActivity(
                Intent(this, ProfileActivity::class.java)
            )
        }

        binding.recentFerry.ferryCard.setOnClickListener {
            //send ferry
            val i = Intent(this, BookActivity::class.java)
            startActivity(i)
        }

        onBackPressedDispatcher.addCallback(this) {
            val exitBottomSheet = BottomSheetDialog(this@HomeActivity)
            exitBottomSheet.setCancelable(false)
            exitBottomSheet.setContentView(R.layout.exit_bottom_sheet_layout)
            val title = exitBottomSheet.findViewById<TextView>(R.id.title)
            val header = exitBottomSheet.findViewById<TextView>(R.id.header)
            val success = exitBottomSheet.findViewById<Button>(R.id.success)
            val cancel = exitBottomSheet.findViewById<Button>(R.id.cancel)
            title?.text = "Are you sure you want to exit the app?"
            header?.text = "EXIT?"
            success?.text = "EXIT"
            cancel?.text = "CANCEL"
            exitBottomSheet.show()

            success?.setOnClickListener {
                finishAffinity()
            }
            cancel?.setOnClickListener { exitBottomSheet.dismiss() }
        }
    }

    /*private fun getTotalTicketsToday() {

    }*/

    private fun getRecentTickets() {
        val ticket1 = Ticket(
            "IWT10236547",
            "Debabrata Sarma",
            DateHelper().getTodayOrTomorrow("today", "dd MMM, yyyy")
        )
        val ticket2 = Ticket(
            "IWT10245878",
            "Lionel Messi",
            DateHelper().getTodayOrTomorrow("today", "dd MMM, yyyy")
        )
        val ticket3 = Ticket(
            "IWT10234448",
            "Cristiano Ronaldo",
            DateHelper().getTodayOrTomorrow("today", "dd MMM, yyyy")
        )
        val ticketList = ArrayList<Ticket>()
        ticketList.add(ticket1)
        ticketList.add(ticket2)
        ticketList.add(ticket3)

        recentTicketAdapter = RecentTicketAdapter(ticketList)
        recentTicketsRecyclerView = binding.recentTicketsRecyclerView
        recentTicketsRecyclerView.adapter = recentTicketAdapter
        recentTicketsRecyclerView.layoutManager = recentTicketsLayoutManager
        recentTicketsRecyclerView.isNestedScrollingEnabled = false
    }

    private fun getSourceDestinationCards() {
        //check for empty list
        val srcDestGhat1 =
            SourceDestinationData(1, Ghat(1, "GHAT1"), Ghat(2, "GHAT2"), "12:00pm--4:00pm")
        val srcDestGhat2 =
            SourceDestinationData(2, Ghat(2, "GHAT2"), Ghat(3, "GHAT3"), "1:00pm--5:00pm")
        val srcDestGhat3 =
            SourceDestinationData(3, Ghat(3, "GHAT3"), Ghat(1, "GHAT1"), "8:00am--11:00am")
        val srcDestList = ArrayList<SourceDestinationData>()
        srcDestList.add(srcDestGhat1)
        srcDestList.add(srcDestGhat2)
        srcDestList.add(srcDestGhat3)

        sourceDestinationAdapter = SourceDestinationAdapter(srcDestList)
        sourceDestinationAdapter.setOnItemClickListener(this)
        sourceDestinationRecyclerView = binding.srcDestRecyclerView
        sourceDestinationRecyclerView.adapter = sourceDestinationAdapter
        sourceDestinationRecyclerView.layoutManager = srcDestLayoutManager
        sourceDestinationRecyclerView.isNestedScrollingEnabled = false
    }

    override fun onRecentTicketsItemClickListener(position: Int, type: String) {
        TODO("Not yet implemented")
    }

    override fun onSrcDestItemClickListener(position: Int, type: String) {
        startActivity(
            Intent(this, FerryListActivity::class.java)
        )
    }
}