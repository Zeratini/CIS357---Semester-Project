package com.example.myapplication.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.TimeText
import com.example.myapplication.R
import com.example.myapplication.presentation.theme.MyApplicationTheme

@Composable
fun MainScreenPreview() {
    MyApplicationTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Water Image
                Image(
                    painterResource(id = R.drawable.water_full_16dp),
                    contentDescription = null,
                    modifier = Modifier.size(70.dp),
                    colorFilter = ColorFilter.tint(color = MaterialTheme.colors.primary)
                )

                // Plus and Minus buttons
                Row(horizontalArrangement = Arrangement.Center) {
                    Button(
                        onClick = { /* Absolutely Nothing */ },
                        modifier = Modifier.size(40.dp),
                        colors = ButtonDefaults.secondaryButtonColors(),
                    )
                    {
                        Image(
                            painterResource(id = R.drawable.remove_30dp),
                            contentDescription = null,
                            modifier = Modifier.size(70.dp),
                            colorFilter = ColorFilter.tint(color = MaterialTheme.colors.primary)
                        )
                    }
                    Spacer(modifier = Modifier.size(10.dp))
                    Button(
                        onClick = { /* Absolutely Nothing */ },
                        modifier = Modifier.size(40.dp),
                        colors = ButtonDefaults.secondaryButtonColors(),
                    )
                    {
                        Image(
                            painterResource(id = R.drawable.add_30dp),
                            contentDescription = null,
                            modifier = Modifier.size(70.dp),
                            colorFilter = ColorFilter.tint(color = MaterialTheme.colors.primary)
                        )
                    }
                }
            }
        }
    }
}