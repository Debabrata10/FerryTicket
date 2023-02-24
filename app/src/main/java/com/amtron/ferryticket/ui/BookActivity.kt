package com.amtron.ferryticket.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.amtron.ferryticket.databinding.ActivityBookBinding

class BookActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get ferry from bundle

    }
}