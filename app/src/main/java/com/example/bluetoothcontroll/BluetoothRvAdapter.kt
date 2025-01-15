package com.example.bluetoothcontroll

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bluetoothcontroll.Data.BluetoothDevices

class BluetoothRvAdapter(private val itemList: List<BluetoothDevices>) : RecyclerView.Adapter<BluetoothRvAdapter.BtViewAdapter>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BtViewAdapter {
       val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_bt_item, parent, false)
        return BtViewAdapter(view)
    }

    override fun onBindViewHolder(holder: BtViewAdapter, position: Int) {
       val item = itemList[position]

        holder.tvName.text = item.name;
        holder.tvAddr.text = item.address;
    }

    override fun getItemCount(): Int = itemList.size

    class BtViewAdapter(view: View) : RecyclerView.ViewHolder(view) {
        val tvName = view.findViewById<TextView>(R.id.Item_name)
        val tvAddr = view.findViewById<TextView>(R.id.Item_addr)
    }
}


