package com.example.discountgap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.discountgap.ui.DiscountGapApp
import com.example.discountgap.ui.DiscountGapViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val discountGapViewModel: DiscountGapViewModel = viewModel(
                factory = DiscountGapViewModel.provideFactory(applicationContext)
            )
            DiscountGapApp(viewModel = discountGapViewModel)
        }
    }
}