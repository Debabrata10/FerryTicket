package com.amtron.ferryticket.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amtron.ferryticket.BuildConfig;
import com.amtron.ferryticket.ProxyServer;
import com.amtron.ferryticket.R;
import com.amtron.ferryticket.databinding.ActivityProxyBinding;

import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;
import kotlinx.coroutines.DelicateCoroutinesApi;

@DelicateCoroutinesApi public class ProxyActivity extends AppCompatActivity {

    ActivityProxyBinding binding;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProxyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sharedPreferences = this.getSharedPreferences(
                "IWTCounter",
                MODE_PRIVATE
        );
        editor = sharedPreferences.edit();
        binding.getProxy.setOnClickListener(v -> {
            getProxy();
        });
    }

    public void getProxy() {
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
                            binding.proxyLl.setVisibility(View.VISIBLE);
                            String PROXY_IP = data.getStringExtra("PROXY_IP");
                            String PROXY_PORT = data.getStringExtra("PROXY_PORT");
                            String proxy = "PROXY_IP : " + PROXY_IP + "\n" + "PROXY_PORT : " + PROXY_PORT;
                            binding.proxy.setText(proxy);

                            binding.saveProxyBtn.setOnClickListener(v -> {
                                editor.putString("proxy_ip", PROXY_IP);
                                editor.putString("proxy_port", PROXY_PORT);
                                editor.apply();
                                SweetAlertDialog saveProxySettingsAlert = new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE);
                                saveProxySettingsAlert.setTitle("SUCCESS!!");
                                saveProxySettingsAlert.setCancelable(false);
                                saveProxySettingsAlert.setContentText("Proxy settings have been successfully saved");
                                saveProxySettingsAlert.showCancelButton(false);
                                saveProxySettingsAlert.setConfirmText("GO BACK");
                                saveProxySettingsAlert.show();
                                saveProxySettingsAlert.setConfirmClickListener(c -> {
                                    saveProxySettingsAlert.dismiss();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("pos_settings", "yes");
                                    Intent i = new Intent(this, PosActivity.class);
                                    i.putExtras(bundle);
                                    startActivity(i);
                                });
                            });
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