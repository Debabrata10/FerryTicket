package com.amtron.ferryticket.ui;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.amtron.ferryticket.R;
import com.amtron.ferryticket.helper.ResponseHelper;
import com.amtron.ferryticket.model.LastTransaction;
import com.amtron.ferryticket.model.Ticket;
import com.amtron.ferryticket.model.User;
import com.amtron.ferryticket.network.Client;
import com.amtron.ferryticket.network.RetrofitHelper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;
import kotlinx.coroutines.DelicateCoroutinesApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@DelicateCoroutinesApi
public class LastTxnStatusActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private SharedPreferences.Editor editor;
    private User user;
    private Ticket ticket;
    private LastTransaction lastTransaction;
    CursorLoader cursorLoader;
    TextView txt_amount, txt_date, txt_invoice, txt_rrn, txt_authcode, txt_cardtype, txt_cardno, txt_transaction, txt_ticketNo;
    String amount, invoice, rrn, auth_code, date_time, card_no, transaction_id, card, txnIdString;
    Boolean checkForTransaction = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_approved);

        SharedPreferences sharedPreference = this.getSharedPreferences("IWTCounter", MODE_PRIVATE);
        editor = sharedPreference.edit();

        String userString = sharedPreference.getString("user", "");
        user = new Gson().fromJson(userString, User.class);

        String ticketString = sharedPreference.getString("ticket", "");
        ticket = new Gson().fromJson(ticketString, Ticket.class);

        try {
            String lastTransactionString = sharedPreference.getString("last_transaction", "");
            ticket = new Gson().fromJson(ticketString, Ticket.class);

            if (!lastTransactionString.isEmpty()) {
                lastTransaction = new Gson().fromJson(lastTransactionString, LastTransaction.class);
            }
        } catch (Exception e) {
            Log.d("warning", "No last transaction found");
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            txnIdString = bundle.getString("last_txn");
            if (txnIdString != null) {
                checkForTransaction = true;
            }
        }

        txt_amount = findViewById(R.id.txt_amount);
        txt_date = findViewById(R.id.txt_date);
        txt_invoice = findViewById(R.id.txt_invoice);
        txt_rrn = findViewById(R.id.txt_rrn);
        txt_cardno = findViewById(R.id.txt_cardno);
        txt_cardtype = findViewById(R.id.txt_cardtype);
        txt_authcode = findViewById(R.id.txt_authcode);
        txt_transaction = findViewById(R.id.transaction_id);
        txt_ticketNo = findViewById(R.id.ticket_no);
        getSupportLoaderManager().initLoader(1, null, this);
    }

    public void onClickBack(View view) {
        finish();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        cursorLoader = new CursorLoader(this, Uri.parse("content://com.example.menusample.provider.InAppTxnProvider/cte"), null, null, null, null);
        return cursorLoader;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
        try {
            if (cursor.getCount() == 0) {
                Toast.makeText(LastTxnStatusActivity.this, "No Record found", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                cursor.moveToFirst();
                StringBuilder res = new StringBuilder();

                while (!cursor.isAfterLast()) {
                    amount = cursor.getString(cursor.getColumnIndexOrThrow("amount"));
                    invoice = cursor.getString(cursor.getColumnIndexOrThrow("invoice"));
                    rrn = cursor.getString(cursor.getColumnIndexOrThrow("rrn"));
                    auth_code = cursor.getString(cursor.getColumnIndexOrThrow("auth_code"));
                    date_time = cursor.getString(cursor.getColumnIndexOrThrow("date_time"));
                    card_no = cursor.getString(cursor.getColumnIndexOrThrow("card_no"));
                    transaction_id = cursor.getString(cursor.getColumnIndexOrThrow("transaction_id"));
                    card = cursor.getString(cursor.getColumnIndexOrThrow("card"));
//                    ticketNo = cursor.getString(cursor.getColumnIndexOrThrow("additional_attribute1"));
                    cursor.moveToNext();
                }

                int start_no = card_no.length() - 4;
                int end = card_no.length();
                card_no = card_no.substring(start_no, end);
                card_no = "**** **** **** " + card_no;
                txt_amount.setText("INR " + amount);
                txt_date.setText(date_time);
                txt_invoice.setText(invoice);
                txt_rrn.setText(rrn);
                txt_cardno.setText(card_no);
                txt_cardtype.setText(card);
                txt_authcode.setText(auth_code);
                txt_transaction.setText(transaction_id);

                if (checkForTransaction) {
                    char[] tempTransactionArray = transaction_id.toCharArray();
                    StringBuilder sb = new StringBuilder();
                    for (char c : tempTransactionArray) {
                        if (Character.isDigit(c)) {
                            sb.append(c);
                        }
                    }
                    String trans_id = sb.toString();
                    if (txnIdString.equals(trans_id)) {
                        System.out.println(txnIdString);
                        System.out.println(trans_id);
                        if (txnIdString.equals(sb.toString())) {
                            String[] arrString = date_time.split(" ");
                            String date = arrString[0];
                            String time = arrString[1];

                            callServerSideForTicketConfirmation(
                                    Double.parseDouble(amount),
                                    date,
                                    time,
                                    invoice,
                                    rrn,
                                    card_no,
                                    card,
                                    auth_code,
                                    ticket.getId(),
                                    trans_id
                            );
                        }
                    } else {
                        Toast.makeText(this, "Previous payment failed. Please book again", Toast.LENGTH_SHORT).show();
                        System.out.println(txnIdString);
                        System.out.println(trans_id);
                        startActivity(new Intent(this, TicketListActivity.class));
                    }
                }
            }
        } catch (NullPointerException ne) {
            Log.d("LastTxnStatus", "is null");
        }
    }

    public void callServerSideForTicketConfirmation(Double amount, String in_app_date, String in_app_time,
                                                    String invoice, String rrn, String cardToBeSend, String card_type,
                                                    String auth_code, Integer ticketId, String tid) {
        SweetAlertDialog alert = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        alert.setTitle("VERIFYING");
        alert.setCancelable(false);
        alert.show();

        Client client = Objects.requireNonNull(RetrofitHelper.ForJava.Companion.getInstance(this)).create(Client.class);
        Call<JsonObject> call = client.sendPosDataToServer(
                "Bearer " + user.getToken(),
                amount,
                in_app_date,
                in_app_time,
                invoice,
                rrn,
                cardToBeSend,
                card_type,
                auth_code,
                ticketId,
                tid
        );
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    ResponseHelper helper = new ResponseHelper();
                    helper.responseHelper(response.body());
                    if (helper.isStatusSuccessful()) {
                        try {
                            JSONObject obj = new JSONObject(helper.getDataAsString());
                            ticket.setMode_of_payment(obj.get("mode_of_payment").toString());
                            ticket.setOrder_status(obj.get("order_status").toString());
                            ticket.setService_amt(Double.parseDouble(obj.get("service_amt").toString()));
                            ticket.setTotal_amt(Double.parseDouble(obj.get("total_amt").toString()));
                            editor.putString("ticket", new Gson().toJson(ticket));
                            editor.apply();

                            startActivity(new Intent(LastTxnStatusActivity.this, InAppApprovedActivity.class));
                        } catch (JSONException err) {
                            Log.d("Error", err.toString());
                        }
                    } else {
                        alert.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                        alert.setCancelText("RETRY");
                        alert.hideConfirmButton();
                        alert.setCancelable(false);
                        alert.setCancelClickListener(v ->
                                {
                                    alert.dismissWithAnimation();
                                    callServerSideForTicketConfirmation(amount, in_app_date, in_app_time, invoice, rrn, cardToBeSend, card_type, auth_code, ticket.getId(), tid);
                                }
                        );
                        Log.d("ERROR!!", helper.getErrorMsg());
                    }
                } else {
                    alert.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    alert.setCancelText("RETRY");
                    alert.hideConfirmButton();
                    alert.setCancelable(false);
                    alert.setCancelClickListener(v ->
                            {
                                alert.dismissWithAnimation();
                                callServerSideForTicketConfirmation(amount, in_app_date, in_app_time, invoice, rrn, cardToBeSend, card_type, auth_code, ticket.getId(), tid);
                            }
                    );
                    Log.d("ERROR!!", "Response Error Code " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                alert.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                alert.setCancelText("RETRY");
                alert.hideConfirmButton();
                alert.setCancelable(false);
                alert.setCancelClickListener(v ->
                        {
                            alert.dismissWithAnimation();
                            callServerSideForTicketConfirmation(amount, in_app_date, in_app_time, invoice, rrn, cardToBeSend, card_type, auth_code, ticket.getId(), tid);
                        }
                );
                Log.d("ERROR!!", "Server Error. Please try again.");
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
//        Intent i = new Intent(LastTxnStatusActivity.this, MainActivity.class);
//        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(i);
//        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);


    }
}

