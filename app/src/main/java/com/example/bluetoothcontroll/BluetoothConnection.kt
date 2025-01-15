package com.example.bluetoothcontroll

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bluetoothcontroll.Data.BluetoothDevices
import com.example.bluetoothcontroll.databinding.ActivityBluetoothConnectionBinding
import com.example.bluetoothcontroll.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

class BluetoothConnection : AppCompatActivity() {

    companion object{
        private const val REQUES_ENABLE_BT = 1
        private const val BtTag = "BT_INFO"
    }

    private lateinit var BtDevices: ArrayList<BluetoothDevices>
    private lateinit var binding : ActivityBluetoothConnectionBinding
    private lateinit var bluetoothAdapter: BluetoothAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityBluetoothConnectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val BtGreyStatus = ContextCompat.getDrawable(this, R.drawable.bt_disable_status_bg)
        val BtGreenStatus = ContextCompat.getDrawable(this@BluetoothConnection, R.drawable.bt_enable_status_bg)

        // Bluetooth adapter creation
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Log.d(BtTag, "Bluetooth is not available")
            Toast.makeText(this,"Bluetooth is not available", Toast.LENGTH_SHORT).show()
            finish();
        }


        if (!bluetoothAdapter.isEnabled){

            Log.d(BtTag, "Bluetooth is not enabled")
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            startActivityForResult(enableBtIntent, REQUES_ENABLE_BT)
        }



        BtDevices = ArrayList<BluetoothDevices>()
        binding.rvBtDevices.layoutManager = LinearLayoutManager(this);
        binding.rvBtDevices.adapter = BluetoothRvAdapter(BtDevices)

        binding.btnSearchBtDevices.setOnClickListener {
            binding.btnSearchBtDevices.background = BtGreyStatus
            binding.btnSearchBtDevices.isEnabled = false;

            Log.d(BtTag, "Search button clicked")
            val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(receiver, discoverDevicesIntent)
            bluetoothAdapter.startDiscovery()

            lifecycleScope.launch {
                Log.d(BtTag, "Lifecycle scope launched")
                Thread.sleep(10000);
                Log.d(BtTag, "Thread sleep finished")
                StopSearch();
                binding.btnSearchBtDevices.background = BtGreenStatus
                binding.btnSearchBtDevices.isEnabled = true;
            }

        }



//        binding.lvDevices.setOnItemClickListener { parent, view, position, id ->
//
//            Log.i(BtTag, "Device selected: ${BtDeviceName[position]}")
//            val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
//            val device = bluetoothAdapter.getRemoteDevice(Bt_devices_ADDR[position])
//            val socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID)
//
//
//            try {
//                socket.connect()
//                val inputStream = socket.inputStream
//                val outputStream = socket.outputStream
//
//                // If no exception is thrown, the connection is successful
//                Log.d(BtTag, "Connected to device successfully.")
//            } catch (e: IOException) {
//                Log.e(BtTag, "Failed to connect: ${e.message}")
//            }
//
//        }

    }


    private val receiver = object : BroadcastReceiver(){
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {

            Log.d(BtTag, "Broadcast received")
            val action = intent.action

            if (BluetoothDevice.ACTION_FOUND == action){
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                Log.d(BtTag, "Device found: ${device?.name}")
                if (BtDeviceDontContain(device?.name.toString())) {
                    Log.d(BtTag, "Device added to list: ${device?.name}")
                    BtDevices.add(BluetoothDevices(device?.name.toString(), device?.address.toString()))
                    binding.rvBtDevices.adapter?.notifyDataSetChanged()
                }

            }
        }
    }


    override fun onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }




    private fun BtDeviceDontContain(name: String): Boolean{
        for (device in BtDevices){
            if (device.name == name){
                return false;
            }
        }

        return true;
    }


    @SuppressLint("MissingPermission")
    private fun StopSearch(){
        bluetoothAdapter.cancelDiscovery()
        unregisterReceiver(receiver)
    }
}