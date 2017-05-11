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

import com.atalay.bluetoothhelper.Common.PrinterCommands;
import com.atalay.bluetoothhelper.Model.BluetoothCallback;
import com.atalay.bluetoothhelper.Provider.BluetoothProvider;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, BluetoothCallback {

    private Button   printer_send;
    private Button   printer_sendImg;
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
        printer_sendImg = (Button) findViewById(R.id.printer_sendImg);
        printer_openprinterlist = (Button) findViewById(R.id.printer_openprinterlist);
        printer_text = (EditText) findViewById(R.id.printer_text);
        printer_copycount = (EditText) findViewById(R.id.printer_copycount);
        printer_name = (TextView) findViewById(R.id.printer_name);

        printer_send.setOnClickListener(this);
        printer_sendImg.setOnClickListener(this);
        printer_openprinterlist.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.printer_openprinterlist: openList(); break;
            case R.id.printer_send: sendToPrinter();break;
            case R.id.printer_sendImg: sendImageToPrinter();break;
        }

    }

    private void sendImageToPrinter() {
        initProvider();
        bluetoothProvider
                .connect()
                .setCopyCount(Integer.valueOf(printer_copycount.getText().toString().trim()))
                .printAddText("...")//Any base64Image
                .execute(BluetoothProvider.PrintType.IMAGE);
    }

    private void sendByteToPrinter(){
        initProvider();
        bluetoothProvider
                .connect()
                .setCopyCount(Integer.valueOf(printer_copycount.getText().toString().trim()))
                .isTest(printer_test.isChecked())
                .print(PrinterCommands.FEED_LINE)
                .execute(BluetoothProvider.PrintType.BYTE);
    }

    private void openList() {
        bluetoothProvider.openPrinterListActivity();
    }

    private void sendToPrinter() {
        initProvider();
        bluetoothProvider
                .connect()
                .setCopyCount(Integer.valueOf(printer_copycount.getText().toString().trim()))
                .isTest(printer_test.isChecked())
                .print(printer_text.getText().toString().trim())
                .execute(BluetoothProvider.PrintType.TEXT);
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
