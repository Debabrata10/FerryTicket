package com.amtron.ferryticket.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amtron.ferryticket.databinding.ActivityBookBinding

class BookActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookBinding
    private var isAddPassengerCardVisible: Boolean = false
    private var isAddVehicleCardVisible: Boolean = false
    private var isAddVehicleNumberVisible: Boolean = false

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

        binding.rgVehicleType.setOnCheckedChangeListener { _, checkedId ->
            val rb: RadioButton = findViewById(checkedId)
            Log.d("id", rb.text.toString())
            if (rb.text.toString() == "Motor Cycle" ||
                rb.text.toString() == "LMV" ||
                rb.text.toString() == "HMV") {
                isAddVehicleNumberVisible = true
                binding.textInputLayout5.visibility = View.VISIBLE
            } else {
                isAddVehicleNumberVisible = false
                binding.textInputLayout5.visibility = View.GONE
            }
        }
    }
}