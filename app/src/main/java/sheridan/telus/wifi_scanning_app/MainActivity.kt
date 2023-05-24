package sheridan.telus.wifi_scanning_app

import android.Manifest
import android.R
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import sheridan.telus.wifi_scanning_app.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    // View binding declaration
    private lateinit var binding: ActivityMainBinding

    // Database binding declaration
    private lateinit var mDb:ScanResultsDatabase

    // Wifi manager binding declaration
    private lateinit var wifiManager: WifiManager
    private lateinit var wifiScanReceiver: BroadcastReceiver

    // Building & Floor text binding declaration
    private lateinit var txtBuilding : EditText
    private lateinit var txtFloor : EditText

    // Buttons binding declaration
    private lateinit var btnScan: Button
    private lateinit var btnClear: Button
    private lateinit var btnSave: Button
    private lateinit var btnDelete :  Button

    // Result list & adapter binding declaration
    private lateinit var apList: ListView
    private lateinit var scanResultsAdapter: ArrayAdapter<String>

    // Progress bar binding declaration
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize binding
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        // WifiManager API
        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        // Initialize the room database
        mDb = ScanResultsDatabase.getInstance(applicationContext)

        // Update the view
        txtBuilding = binding.txtBuilding
        txtFloor = binding.txtFloor
        btnScan = binding.btnScan
        btnClear = binding.btnClear
        btnSave = binding.btnSave
        btnDelete = binding.btnDelete
        apList = binding.apList
        progressBar = binding.progressBar

        // Create an ArrayAdapter for the AP ListView
        scanResultsAdapter = ArrayAdapter(this, R.layout.simple_list_item_1)
        apList.adapter = scanResultsAdapter

        // Set click listener for the scan button
        btnScan.setOnClickListener {
            val permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )

            val hasPermissions = permissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }

            if (!hasPermissions) {
                ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    REQUEST_CODE
                )
            } else {

                if(txtBuilding.text.toString().isEmpty() || txtFloor.text.toString().isEmpty()) {
                    // Display a toast message indicating that the values are required
                    Toast.makeText(this, "Building name and floor number are required.", Toast.LENGTH_SHORT).show()
                } else{
                    startWifiScan()
                }
            }
        }

        // Set click listener for the save button
        btnSave.setOnClickListener {
            saveScanResults()
        }

        // Set click listener for the clear button
        btnClear.setOnClickListener {
            // Clear the scan results adapter
            scanResultsAdapter.clear()

            // Clear building & floor edit text
            txtBuilding.text.clear()
            txtFloor.text.clear()

            // Stop the Wi-Fi scan if it is in progress
            if (::wifiScanReceiver.isInitialized) {
                unregisterReceiver(wifiScanReceiver)
                progressBar.visibility = View.INVISIBLE
            }
        }

        // Set click listener for the delete button
        btnDelete.setOnClickListener {
            // Retrieve the DAO instance from the database
            val scanResultsDao = mDb.scanResultsDao()

            // Call the deleteAll() method on the DAO using a background thread
            Thread {
                scanResultsDao.deleteAll()
            }.start()

            Toast.makeText(this, "All scan results deleted!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startWifiScan() {
        wifiScanReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                try {
                    val success =
                        intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                    if (success) {
                        // Show scan success toast
                        scanSuccess()
                        handleScanResults(wifiManager.scanResults)
                    } else {
                        // Scan failure handling
                        scanFailure()
                    }
                } catch (e: SecurityException) {
                    // Handle SecurityException
                    e.printStackTrace()
                    scanFailure()
                } finally {
                    // Hide the progress bar
                    progressBar.visibility = View.INVISIBLE
                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        registerReceiver(wifiScanReceiver, intentFilter)

        // The deprecation of the WifiManager API does not directly affect the accuracy or functionality of Wi-Fi scanning.
        // The deprecation typically means that the API is no longer actively maintained and might not receive updates or improvements in future Android versions.
        // However, it should still work as intended for the supported Android versions.
        try {

            // Show the progress bar to indicate that scanning is in progress
            progressBar.visibility = View.VISIBLE
            wifiManager.startScan()
        } catch (e: SecurityException) {
            // Handle SecurityException
            e.printStackTrace()
            scanFailure()
        }
    }

    private fun handleScanResults(results: List<ScanResult>) {
        // Clear the previous scan results
        scanResultsAdapter.clear()

        // Add the scanned Wi-Fi access points to the adapter
        for (result in results) {
            val ssid = result.SSID ?: ""
            val bssid = result.BSSID ?: ""
            val signalStrength = result.level
            val capabilities = result.capabilities ?: ""

            val scanResultDetails = "SSID: $ssid\nCapabilities: $capabilities\nBSSID: $bssid\nSignal Strength: $signalStrength\n"
            scanResultsAdapter.add(scanResultDetails)
        }

        // Notify the adapter that the data has changed
        scanResultsAdapter.notifyDataSetChanged()
    }

    private fun scanSuccess() {
        Toast.makeText(this, "Wi-Fi scan succeed!", Toast.LENGTH_SHORT).show()
    }

    private fun scanFailure() {
        Toast.makeText(this, "Wi-Fi scan failed", Toast.LENGTH_SHORT).show()
    }

    private fun saveScanResults() {
        val scanResults = ArrayList<ScanResultsEntity>()

        for (i in 0 until scanResultsAdapter.count) {
            val scanResult = scanResultsAdapter.getItem(i)

            // Extract the necessary information from the scan result details
//            val ssid = scanResult?.substringAfter("SSID: ")?.substringBefore(" \n")
//            val capabilities = scanResult?.substringAfter("capabilities")?.substringBefore("\n")
//            val bssid = scanResult?.substringAfter("BSSID: ")?.substringBefore("\n")
//            val signalStrength = scanResult?.substringAfter("Signal Strength: ")?.substringBefore("\n")?.toInt()
//
//            // Create a ScanResultsEntity object and add it to the list
//            val scanResultEntity = ScanResultsEntity(0, ssid, capabilities, bssid, signalStrength)
//            scanResults.add(scanResultEntity)

            // Extract the necessary information from the scan result details
            val ssidPattern = """SSID: ([^\n]+)""".toRegex()
            val ssidMatchResult = ssidPattern.find(scanResult!!)
            val ssid = ssidMatchResult?.groupValues?.get(1)?.trim()

            val capabilitiesPattern = """Capabilities: ([^\n]+)""".toRegex()
            val capabilitiesMatchResult = capabilitiesPattern.find(scanResult)
            val capabilities = capabilitiesMatchResult?.groupValues?.get(1)?.trim()

            val bssidPattern = """BSSID: ([^\n]+)""".toRegex()
            val bssidMatchResult = bssidPattern.find(scanResult)
            val bssid = bssidMatchResult?.groupValues?.get(1)?.trim()

            val signalStrengthPattern = """Signal Strength: ([^\n]+)""".toRegex()
            val signalStrengthMatchResult = signalStrengthPattern.find(scanResult)
            val signalStrength = signalStrengthMatchResult?.groupValues?.get(1)?.trim()?.toInt()

            if(txtBuilding.text.toString().isEmpty() || txtFloor.text.toString().isEmpty()) {
                // Display a toast message indicating that the values are required
                Toast.makeText(this, "Building name and floor number are required.", Toast.LENGTH_SHORT).show()
            } else {
                // Create a ScanResultsEntity object and add it to the list
                val scanResultEntity = ScanResultsEntity(0, txtBuilding.text.toString(), txtFloor.text.toString(), ssid, capabilities, bssid, signalStrength)
                scanResults.add(scanResultEntity)
            }

        }

        // Insert the scan results into the database using a background thread
        Thread {
            mDb.scanResultsDao().insertAll(scanResults)
        }.start()

        Toast.makeText(this, "Scan results saved!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiScanReceiver)
    }

    companion object {
        private const val REQUEST_CODE = 1
    }

}