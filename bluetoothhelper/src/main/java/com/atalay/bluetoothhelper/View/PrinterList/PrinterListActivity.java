package com.atalay.bluetoothhelper.View.PrinterList;

import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.atalay.bluetoothhelper.Adapter.BluetoothDeviceAdapter;
import com.atalay.bluetoothhelper.Common.UtilsDialog;
import com.atalay.bluetoothhelper.R;

/**
 * Created by baris on 9.03.2017.
 */

public class PrinterListActivity extends AppCompatActivity implements PrinterListView, View.OnClickListener, AdapterView.OnItemClickListener {

    private ListView printerlist_list;
    private PrinterListPresenter presenter;
    private Button toolbar_refresh;
    private ImageButton toolbar_left_button;

    private TextView     printerlist_active;
    private ProgressBar  printerlist_progress;
    private LinearLayout printerlist_content;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printerlist);

        initUi();
        initPresenter();
    }

    private void initPresenter() {
        presenter = new PrinterListPresenter(this);
        presenter.attachView(this);

        printerlist_active.setText("Active Printer: " + (presenter.getActivePrinter().isEmpty()?"NONE":presenter.getActivePrinter()));
    }

    private void initUi() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        printerlist_list = (ListView) findViewById(R.id.printerlist_list);
        toolbar_refresh = (Button) findViewById(R.id.toolbar_refresh);
        toolbar_left_button = (ImageButton) findViewById(R.id.toolbar_left_button);

        printerlist_active = (TextView) findViewById(R.id.printerlist_active);
        printerlist_progress = (ProgressBar) findViewById(R.id.printerlist_progress);
        printerlist_content = (LinearLayout) findViewById(R.id.printerlist_content);

        printerlist_progress.setVisibility(View.GONE);

        toolbar_left_button.setOnClickListener(this);
        toolbar_refresh.setOnClickListener(this);
        printerlist_list.setOnItemClickListener(this);
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this.getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume(){
        super.onResume();
        presenter.onResume();
    }

    @Override
    protected void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void enabledRefreshButton() {
        toolbar_refresh.setEnabled(true);
    }

    @Override
    public void refreshAdapter() {
        ((BluetoothDeviceAdapter)printerlist_list.getAdapter()).refreshDevicAddress();
        ((BluetoothDeviceAdapter)printerlist_list.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public void loadDevices(BluetoothDeviceAdapter bluetoothDeviceAdapter) {

        printerlist_list.setAdapter(bluetoothDeviceAdapter);
    }

    @Override
    public void bluetoothSearchStarted() {
        toolbar_refresh.setEnabled(false);
        printerlist_content.setVisibility(View.VISIBLE);
        printerlist_active.setVisibility(View.GONE);
        printerlist_progress.setVisibility(View.VISIBLE);
    }

    @Override
    public void bluetoothSearchEnded() {
        printerlist_content.setVisibility(View.GONE);
        toolbar_refresh.setEnabled(true);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.toolbar_refresh) {
            presenter.refreshBluetooth();
        } else if (i == R.id.toolbar_left_button) {
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BluetoothDeviceAdapter deviceAdapter = ((BluetoothDeviceAdapter) parent.getAdapter());
        final BluetoothDevice device =  deviceAdapter.getItem(position);

        if (device.getBondState() == BluetoothDevice.BOND_BONDED && !presenter.getActivePrinter().isEmpty()) {
            final String[] titles = getResources().getStringArray(R.array.printlist_unpaireds);
            UtilsDialog.createAlertDialog(this)
                    .setTitle(R.string.choice_make)
                    .setSingleChoiceItems(titles, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case 0: presenter.unpairDialog(device); break;
                                case 1: presenter.printTest(); break;
                            }
                            dialog.dismiss();
                        }
                    })
                    .setCancelable(false)
                    .show();
        }else {
            presenter.pairDevice(device);
        }
    }
}
