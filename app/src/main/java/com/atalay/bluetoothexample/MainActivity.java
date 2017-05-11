package com.atalay.bluetoothexample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.atalay.bluetoothhelper.Model.BluetoothCallback;
import com.atalay.bluetoothhelper.Provider.BluetoothProvider;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, BluetoothCallback {

    private Button   printer_send;
    private Button   printer_openprinterlist;
    private EditText printer_text;
    private EditText printer_copycount;
    private TextView printer_name;
    private CheckBox printer_test;

    BluetoothProvider bluetoothProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initUi();

        initProvider();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        initProvider();
    }

    private void initProvider() {
        bluetoothProvider = new BluetoothProvider(this, this);
        refreshBluetoothName();
//        bluetoothProvider
//                .showPrinterListActivity(false)
//                .setDeviceAddress("98:D3:31:B2:A5:AA");
    }

    private void refreshBluetoothName() {
        printer_name.setText(getString(R.string.printer_active) + bluetoothProvider.getDeviceAddress());
    }

    private void initUi() {
        printer_test = (CheckBox) findViewById(R.id.printer_test);
        printer_send = (Button) findViewById(R.id.printer_send);
        printer_openprinterlist = (Button) findViewById(R.id.printer_openprinterlist);
        printer_text = (EditText) findViewById(R.id.printer_text);
        printer_copycount = (EditText) findViewById(R.id.printer_copycount);
        printer_name = (TextView) findViewById(R.id.printer_name);

        printer_send.setOnClickListener(this);
        printer_openprinterlist.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.printer_openprinterlist: openList(); break;
            case R.id.printer_send: sendToPrinter();break;
        }

    }

    private void openList() {
        bluetoothProvider.openPrinterListActivity();
    }

    private void sendToPrinter() {
        bluetoothProvider
                .connect()
                .setCopyCount(Integer.valueOf(printer_copycount.getText().toString().trim()))
                .isTest(printer_test.isChecked())
                .printText("<br/> www.santsg.com<br/> Tour Op : ATALAY TOUR<br/> Booked : 23.04.2016<br/> Pnr No.: TW - 519<br/> ______________________________<br/> Tour<br/> Finike TOUR<br/> ANTALYA CITY<br/> On : 23.04.2016 Pick Up : 19:30<br/> Passengers<br/> BARIS ATALAY<br/> Adult : 2 Child: 0 Infant: 0<br/> ______________________________<br/> Presa Di Finica<br/> Room No.:  <br/> Region : ANTALYA CITY<br/> 00:00<br/> ______________________________<br/> Guide : MAHMUT TUNCER<br/> Remark : <br/> Price : 100 EUR <br/> Tour Description : <br/><br /><br /><br /><br /><br /><br /><br /><br /><br /><br />")
//                .printText(printer_text.getText().toString().trim())
                .run();
    }

    @Override
    public void onBegin() {
        refreshBluetoothName();
    }

    @Override
    public void onErrorImportant(String errorMessage) {
        Toast.makeText(this.getApplicationContext(), "[Importand] " + errorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onError(String errorMessage) {
        Toast.makeText(this.getApplicationContext(), "[Normal] " + errorMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onSuccessful() {}
}
