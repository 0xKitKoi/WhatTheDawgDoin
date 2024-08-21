package com.example.whatthedawgdoin

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MainActivity : ComponentActivity() {
    private val client = OkHttpClient()

    // State to hold the server response text
    private var responseText by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }

    private fun sendJsonToServer(name: String, desc: String) {
        val url = "http://scuzzy.space/vagary.py"

        // Create a JSON object with specified name and desc
        val jsonObject = JSONObject().apply {
            put("name", name)
            put("desc", desc)
        }

        // Convert JSON object to string
        val jsonString = jsonObject.toString()
        val body = jsonString.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    responseText = "Error: ${e.message}"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        runOnUiThread {
                            responseText = responseBody // Update the response text
                            Toast.makeText(this@MainActivity, "$name sent!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        responseText = "Error: ${response.code} - ${response.message}"
                    }
                }
            }
        })
    }

    @Composable
    fun MyApp() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF808080))
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Define the JSON objects for each button
            val jsonObjects = listOf(
                JSONObject().apply { put("name", "Working"); put("desc", "2") },
                JSONObject().apply { put("name", "Sleeping"); put("desc", "3") },
                JSONObject().apply { put("name", "Error"); put("desc", "5") },
                JSONObject().apply { put("name", "gaeming"); put("desc", "4") },
                JSONObject().apply { put("name", "programming"); put("desc", "6") }
            )

            // Create buttons dynamically
            for (json in jsonObjects) {
                Button(
                    onClick = { sendJsonToServer(json.getString("name"), json.getString("desc")) },
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF696969)) // Set button color
                ) {
                    Text("${json.getString("name")}", color = Color.White) // Set text color to white for contrast
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Text fields for manual input
            var manualName by remember { mutableStateOf("") }
            var manualDesc by remember { mutableStateOf("") }

            OutlinedTextField(
                value = manualName,
                onValueChange = { manualName = it },
                label = { Text("Enter Name") },
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            )

            OutlinedTextField(
                value = manualDesc,
                onValueChange = { manualDesc = it },
                label = { Text("Enter Description") },
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            )

            Button(
                onClick = {
                    sendJsonToServer(manualName, manualDesc)
                    manualName = "" // Clear the text box after sending
                    manualDesc = ""
                },
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF696969)) // Set button color
            ) {
                Text("Send Manual JSON", color = Color.White) // Set text color to white for contrast
            }

            // Display the response from the server at the bottom in a scrollable box
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Takes up remaining space
                    .background(Color(0xFF333333))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = responseText,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        MyApp()
    }
}
