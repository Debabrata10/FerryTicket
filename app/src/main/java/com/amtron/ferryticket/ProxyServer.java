package com.amtron.ferryticket;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProxyServer extends AsyncTask<String, Void, String> {

    final String username = "ventureinfotek\\amtron";// replace username in "xxxxxxxx"
    final String password = "U$er@12345"; //replace password
    final String url = "https://serviceurl";

    int proxyPort = 8080;
    String proxyHost = "192.168.153.200"; // Airtel
    //    String proxyHost = "192.168.99.7"; //Vodafone
    Authenticator proxyAuthenticator = (route, response) -> {
        String credential = Credentials.basic(username, password);
        return response.request().newBuilder()
                .header("Proxy-Authorization", credential)
                .build();
    };
    private ProgressDialog pDialogs;

    public ProxyServer(Activity activity) {
        pDialogs = new ProgressDialog(activity);
    }

    @Override
    protected String doInBackground(String... strings) {
        String res = "";
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .proxy(new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)))
                    .proxyAuthenticator(proxyAuthenticator)
                    .protocols(Arrays.asList(Protocol.HTTP_1_1))
                    .build();

            Log.e("URL: ", url);

            RequestBody formBody = new FormBody.Builder()
                    .build();

            Request request = new Request.Builder().url(url).post(formBody).build();
            Response response = client.newCall(request).execute();
            res = Objects.requireNonNull(response.body()).string();
            int rres = response.code();
            int rsres = response.code();

        } catch (Exception e) {
            res = e.toString();
            e.printStackTrace();
        }
        return res;
    }

    @Override
    protected void onPreExecute() {
//        pDialogs = new ProgressDialog(null);
//        pDialogs.show();
//        pDialogs.setMessage("Connecting to Proxy...");
//        pDialogs.setCancelable(false);
//        pDialogs.setCanceledOnTouchOutside(false);
        pDialogs.setMessage("Connecting to Proxy...");
        pDialogs.show();
        Log.d("msg", "Connecting to Proxy...");
    }

    @Override
    protected void onPostExecute(String result) {
//        Log.e("Response is: ", result);
        try {
            if (pDialogs != null || pDialogs.isShowing()) {
                pDialogs.dismiss();
            }
        } catch (Exception e) {
            Log.d("tag", String.valueOf(e));
        }
        Log.d("msg", result);
    }
}