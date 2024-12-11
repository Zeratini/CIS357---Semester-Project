package com.example.myapplication.presentation

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.myapplication.R

@Composable
@RequiresApi(Build.VERSION_CODES.S)
fun MainScreen(
    vm: WaterViewModel
) {
    val drank = vm.drank.observeAsState()
    val accelerometerData by vm.accelerometerData.collectAsState()
    val rotationData by vm.rotationData.collectAsState()
    val finishedDrinking by vm.finishedDrinking.observeAsState()

    //NOTE: Debug messes with UI a lot b/c it uses ScalingLazyColumn!
    val debug = false // Debug variable. Display sensor and boolean values (see below)
    val debugAccel = false // Debug variable. Display accelerometer values
    val debugRot = false // Debug variable. Display rotation values
    val debugGlass = true // Debug variable. Display glass boolean values

    val debugConfirmation = false // Debug variable. Adds a button to show the confirmation dialog.

    val confirmationBuilder = AlertDialog.Builder(LocalContext.current)
    confirmationBuilder.setTitle("Confirm Increment")
        .setMessage("Are you sure you want to increment the counter?")
        .setPositiveButton("Yes") { _, _ ->
            vm.autoIncrement() // Increment the counter
        }
        .setNegativeButton("No", null) // Do nothing
    val dialog = confirmationBuilder.create()

    Column (horizontalAlignment = Alignment.CenterHorizontally) {
        // Water Image
        Image(
            painterResource(id = R.drawable.water_full_16dp),
            contentDescription = null,
            modifier = Modifier.size(70.dp),
            colorFilter = ColorFilter.tint(color = MaterialTheme.colors.primary)
        )

        Text(
            text = drank.value.toString()
        )

        // Plus and Minus buttons
        Row(horizontalArrangement = Arrangement.Center) {
            Button(
                onClick = { vm.decrement() },
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
                onClick = { vm.increment() },
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

            // DIALOG DEBUG
            if (debugConfirmation) {
                Spacer(modifier = Modifier.size(10.dp))
                Button(modifier = Modifier.size(40.dp), onClick = {
                    dialog.show()
                    vm.notifyOfConfirmation()
                }) {}
            }
        }

        //Lower Text
        if (debug == false) { // Remove flavor text if not needed
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.primary,
                text = "Water Tracker"
            )
        }

        if (finishedDrinking == true) {
            ShowConfirmationDialog(context = LocalContext.current, vm = vm)
        }

        if (debug) {
            /*** DEBUG VALUES ***/

            if (debugAccel) {
                Text(
                    textAlign = TextAlign.Center,
                    text = "X:${accelerometerData.first} Y:${accelerometerData.second} Z:${accelerometerData.third}"
                )
            }

            if (debugGlass) {
                Row {
                    Text(
                        textAlign = TextAlign.Center,
                        text = "${vm.glassObtained.observeAsState().value} "
                    )
                    Text(
                        textAlign = TextAlign.Center,
                        text = "${vm.glassRaised.observeAsState().value} "
                    )
                    Text(
                        textAlign = TextAlign.Center,
                        text = "${vm.glassEmpty.observeAsState().value} "
                    )
                    Text(
                        textAlign = TextAlign.Center,
                        text = "${vm.glassLowered.observeAsState().value} "
                    )
                    Text(
                        textAlign = TextAlign.Center,
                        text = "${vm.finishedDrinking.observeAsState().value}"
                    )
                }
            }

            if (debugRot) {
                Text(
                    textAlign = TextAlign.Center,
                    text = "P:${rotationData.second} R:${rotationData.third}"
                )
            }
            /*** END  DEBUG VALUES ***/
        }
    }
}

@Composable
@RequiresApi(Build.VERSION_CODES.S)
fun ShowConfirmationDialog(context: Context, vm: WaterViewModel) {
    AlertDialog.Builder(context)
        .setTitle("Confirm Increment")
        .setMessage("Are you sure you want to increment the counter?")
        .setPositiveButton("Yes") { _, _ ->
            vm.autoIncrement() // Increment the counter
        }
        .setNegativeButton("No") {dialog, _ ->
            dialog.dismiss()
            vm.reset()
        }
        .show()
}