package com.amtron.ferryticket.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amtron.ferryticket.BuildConfig;
import com.amtron.ferryticket.ProxyServer;
import com.amtron.ferryticket.R;

import java.util.Objects;

public class ProxyActivity extends AppCompatActivity {

    TextView txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proxy);
        Objects.requireNonNull(getSupportActionBar()).setTitle("PROXY SETTINGS");
        new ProxyServer(this).execute();
        try {
            txt = findViewById(R.id.proxy);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getProxy(View view) {
        txt.setText("");
        String package_name = BuildConfig.APPLICATION_ID;
        String CUSTOM_ACTION = "cn.desert.newpos.payui.master.PROXY_DETAILS";
        Intent i = new Intent();
        i.setAction(CUSTOM_ACTION);
        i.putExtra("package", package_name);
        startActivityForResult(i, 600);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                if (requestCode == 600) {
                    if (data.hasExtra("result_code")) {
                        boolean datas = data.getBooleanExtra("result_code", false);
                        if (data.getBooleanExtra("result_code", false)) {
                            String PROXY_IP = data.getStringExtra("PROXY_IP");
                            String PROXY_PORT = data.getStringExtra("PROXY_PORT");
                            String proxy = "PROXY_IP : " + PROXY_IP + "\n" + "PROXY_PORT : " + PROXY_PORT;
                            txt.setText(proxy);
                        } else {
                            Toast.makeText(getApplicationContext(), data.getStringExtra("message"), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Empty result", Toast.LENGTH_LONG).show();
                    }
                }
            } else {
                Toast.makeText(getApplicationContext(), "Data null", Toast.LENGTH_LONG).show();
            }
        } else
            Toast.makeText(getApplicationContext(), "Result Failed", Toast.LENGTH_LONG).show();
    }


}