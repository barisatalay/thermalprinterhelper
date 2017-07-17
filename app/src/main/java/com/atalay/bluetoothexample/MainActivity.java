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

        String base64Str = "";

        bluetoothProvider
                .connect()
                .setCopyCount(Integer.valueOf(printer_copycount.getText().toString().trim()))
//                .printImageText("...")//Any base64Image
                .printImageText(base64Str)//Any base64Image
                .printByteArray(PrinterCommands.FEED_LINE_2)
                .printByteArray(PrinterCommands.FEED_LINE_2)
                .printByteArray(PrinterCommands.FEED_LINE_2)
                .printByteArray(PrinterCommands.DEFAULT_SETTINGS)
                .printText(bluetoothProvider.prepareTestData())
                .execute();
    }

    private void sendByteToPrinter(){
        initProvider();
        bluetoothProvider
                .connect()
                .setCopyCount(Integer.valueOf(printer_copycount.getText().toString().trim()))
                .printByteArray(PrinterCommands.FEED_LINE_2)
                .execute();
    }

    private void openList() {
        bluetoothProvider.openPrinterListActivity();
    }

    private void sendToPrinter() {
        initProvider();
        bluetoothProvider
                .connect()
                .setCopyCount(Integer.valueOf(printer_copycount.getText().toString().trim()))
                .printByteArray(PrinterCommands.DEFAULT_SETTINGS)
                .printText(printer_text.getText().toString().trim())
//                .print(printer_text.getText().toString().trim())
                .execute();
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
