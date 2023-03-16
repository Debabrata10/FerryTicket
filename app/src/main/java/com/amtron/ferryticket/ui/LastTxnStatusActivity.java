package com.amtron.ferryticket.ui;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.amtron.ferryticket.R;

public class LastTxnStatusActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    CursorLoader cursorLoader;
    TextView txt_amount, txt_date, txt_invoice, txt_rrn, txt_authcode, txt_cardtype, txt_cardno;
    String amount, invoice, rrn, auth_code, date_time, card_no, transaction_id, card;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_approved);
        txt_amount = findViewById(R.id.txt_amount);
        txt_date = findViewById(R.id.txt_date);
        txt_invoice = findViewById(R.id.txt_invoice);
        txt_rrn = findViewById(R.id.txt_rrn);
        txt_cardno = findViewById(R.id.txt_cardno);
        txt_cardtype = findViewById(R.id.txt_cardtype);
        txt_authcode = findViewById(R.id.txt_authcode);

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
            }
        } catch (NullPointerException ne) {

        }
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

