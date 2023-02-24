package com.amtron.ferryticket.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.amtron.ferryticket.databinding.ActivityBookBinding

class BookActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookBinding
    private var isAddPassengerCardVisible: Boolean = false
    private var isAddVehicleCardVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.openAddPassengerCard.setOnClickListener {
            if (!isAddPassengerCardVisible) {
                isAddPassengerCardVisible = true
                binding.addPassengerCard.visibility = View.VISIBLE
            }
        }

        binding.openAddVehicleCard.setOnClickListener {
            if (!isAddVehicleCardVisible) {
                isAddVehicleCardVisible = true
                binding.addVehicleCard.visibility = View.VISIBLE
            }
        }

        binding.closeAddPassengerCard.setOnClickListener {
            if (isAddPassengerCardVisible) {
                isAddPassengerCardVisible = false
                isAddVehicleCardVisible = false
                binding.addPassengerCard.visibility = View.GONE
                binding.addVehicleCard.visibility = View.GONE
            }
        }

        binding.closeAddVehicleCard.setOnClickListener {
            if (isAddVehicleCardVisible) {
                isAddVehicleCardVisible = false
                binding.addVehicleCard.visibility = View.GONE
            }
        }
    }
}