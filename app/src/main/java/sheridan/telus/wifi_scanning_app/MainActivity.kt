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

    // Wifi manager binding
    private lateinit var wifiManager: WifiManager
    private lateinit var wifiScanReceiver: BroadcastReceiver

    // Front end scan button & clear button & result text binding
    private lateinit var btnScan: Button
    private lateinit var btnClear: Button
    private lateinit var apList: ListView
    private lateinit var scanResultsAdapter: ArrayAdapter<String>

    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize binding
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        // Update the view
        btnScan = binding.btnScan
        btnClear = binding.btnClear
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
                startWifiScan()
            }
        }

        // Set click listener for the clear button
        btnClear.setOnClickListener {
            // Clear the scan results adapter
            scanResultsAdapter.clear()
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
            val ssid = result.SSID
            val bssid = result.BSSID
            val signalStrength = result.level
            val capabilities = result.capabilities

            val scanResultDetails = "SSID: $ssid-$capabilities\nBSSID: $bssid\nSignal Strength: $signalStrength\n"
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

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(wifiScanReceiver)
    }

    companion object {
        private const val REQUEST_CODE = 1
    }

}