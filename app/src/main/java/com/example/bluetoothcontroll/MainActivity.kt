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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bluetoothcontroll.databinding.ActivityMainBinding
import java.io.IOException
import java.util.UUID

class MainActivity : AppCompatActivity() {

    companion object{
        private const val REQUES_ENABLE_BT = 1
        private const val BtTag = "BT_INFO"
    }

    private lateinit var BtDeviceName : ArrayList<String>
    private lateinit var Bt_devices_ADDR : ArrayList<String>
    private lateinit var binding : ActivityMainBinding
    private lateinit var bluetoothAdapter: BluetoothAdapter

    lateinit var adapter : ArrayAdapter<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



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


//        ListView  Adapter creation
        Bt_devices_ADDR = ArrayList();
        BtDeviceName = ArrayList();
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, BtDeviceName)
        binding.lvDevices.adapter = adapter

        binding.btnSearchBtDevices.setOnClickListener {
            Log.d(BtTag, "Search button clicked")
            val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(receiver, discoverDevicesIntent)
            bluetoothAdapter.startDiscovery()

        }



        binding.lvDevices.setOnItemClickListener { parent, view, position, id ->

            Log.i(BtTag, "Device selected: ${BtDeviceName[position]}")
            val MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            val device = bluetoothAdapter.getRemoteDevice(Bt_devices_ADDR[position])
            val socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID)


            try {
                socket.connect()
                val inputStream = socket.inputStream
                val outputStream = socket.outputStream

                // If no exception is thrown, the connection is successful
                Log.d(BtTag, "Connected to device successfully.")
            } catch (e: IOException) {
                Log.e(BtTag, "Failed to connect: ${e.message}")
            }

        }





    }





    private val receiver = object : BroadcastReceiver(){
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {

            Log.d(BtTag, "Broadcast received")
            val action = intent.action

            if (BluetoothDevice.ACTION_FOUND == action){
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                Log.d(BtTag, "Device found: ${device?.name}")
                if (device?.name != null && BtDeviceName.contains(device.name).not()) {
                    Log.d(BtTag, "Device added to list: ${device.name}")
                    Bt_devices_ADDR.add(device?.address.toString())
                    BtDeviceName.add(device?.name.toString())
                    adapter.notifyDataSetChanged()
                }

            }
        }
    }


    override fun onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}