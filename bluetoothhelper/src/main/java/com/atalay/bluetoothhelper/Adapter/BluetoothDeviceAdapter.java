package com.atalay.bluetoothhelper.Adapter;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.atalay.bluetoothhelper.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by baris on 10.03.2017.
 */

public class BluetoothDeviceAdapter extends ArrayAdapter<BluetoothDevice> {

    private LayoutInflater mInflater;
    private Activity context;
    private static List<BluetoothDevice> allData;
    private List<Integer> selectedList;
    ViewHolder holder = null;
    private SharedPreferences preferences;
    private String deviceAddress = "";

    public BluetoothDeviceAdapter(Activity context, List<BluetoothDevice> list) {
        super(context, R.layout.item_bluetoothdevice , list);
        this.context = context;
        this.allData = new ArrayList<BluetoothDevice>();
        this.selectedList = new ArrayList<Integer>();
        if(list != null)
            allData.addAll(list);

        preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        mInflater = context.getLayoutInflater();

        refreshDevicAddress();
    }

    public void refreshDevicAddress() {
        String prefKey = context.getString(R.string.pref_general_printer_address);
        deviceAddress = preferences.getString(prefKey,"");
    }

    @Override
    public int getCount() {
        return allData.size();
    }

    @Override
    public BluetoothDevice getItem(int position) {
        return allData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public BluetoothDevice getSelected(){
        for(BluetoothDevice aItem : allData)
            return aItem;

        return null;
    }

    class ViewHolder {
        ImageView    item_bluetoothdevice_image;
        LinearLayout item_bluetoothdevice_bg;
        TextView     item_bluetoothdevice_text;

        public ViewHolder(View view) {
            item_bluetoothdevice_image = (ImageView) view.findViewById(R.id.item_bluetoothdevice_image);
            item_bluetoothdevice_bg    = (LinearLayout) view.findViewById(R.id.item_bluetoothdevice_bg);
            item_bluetoothdevice_text  = (TextView) view.findViewById(R.id.item_bluetoothdevice_text);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.item_bluetoothdevice, parent, false);
        holder = null;

        BluetoothDevice item = allData.get(position);

        if (convertView == null) {
            holder = new ViewHolder(view);
            view.setTag(holder);
        } else {
            view = convertView;
            holder = ((ViewHolder) view.getTag());
        }

        holder.item_bluetoothdevice_bg.setId(position);
        holder.item_bluetoothdevice_bg.setBackgroundResource(R.color.generalWhite);

//        if (item.getBondState() != BluetoothDevice.BOND_BONDED || deviceAddress.isEmpty()) {
        if (deviceAddress.isEmpty() && deviceAddress == item.getAddress()) {
            holder.item_bluetoothdevice_image.setColorFilter(ContextCompat.getColor(context,R.color.generalGreen));
        }else{
            holder.item_bluetoothdevice_image.setColorFilter(null);
        }

        if(selectedList.contains(position))
            holder.item_bluetoothdevice_bg.setBackgroundResource(R.color.listview_item_selected);

        holder.item_bluetoothdevice_text.setText(item.getName() == null?"No Name":item.getName() + "\n" + item.getAddress());

        return view;
    }
}
