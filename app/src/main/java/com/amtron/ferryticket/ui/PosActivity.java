package com.amtron.ferryticket.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.amtron.ferryticket.BuildConfig;
import com.amtron.ferryticket.R;
import com.amtron.ferryticket.databinding.ActivityPosBinding;
import com.amtron.ferryticket.model.Ticket;
import com.amtron.ferryticket.model.User;
import com.google.gson.Gson;
import com.pos.device.printer.PrintCanvas;
import com.pos.device.printer.PrintTask;
import com.pos.device.printer.Printer;

import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;

import kotlinx.coroutines.DelicateCoroutinesApi;

@DelicateCoroutinesApi
public class PosActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static String param_status = "false";
    public static String sim_status = "false";
    CursorLoader cursorLoader;
    double amount;
    private User user;
    private Ticket ticket;
    private String amountString, package_name;
    private Printer printer = null;
    private PrintTask printTask = null;
    private SharedPreferences.Editor editor;

    private Bundle bundleString;

    public static byte[] draw2PxPoint(Bitmap bitmap) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int square = height * width;

        int[] pixels = new int[square];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        byte[] data = new byte[square >> 3];

        int B = 0, b = 0;
        byte[] bits = {(byte) 0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01};
        for (int i = 0; i < square; i++) {
            if (pixels[i] < -7829368) {//- 0x888888
                data[B] |= bits[b];
            }

            if (b == 7) {
                b = 0;
                B++;
            } else {
                b++;
            }
        }
        return data;
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPosBinding binding = ActivityPosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences sharedPreference = this.getSharedPreferences("IWTCounter", MODE_PRIVATE);
        editor = sharedPreference.edit();
        Gson gson = new Gson();

        getSupportLoaderManager().initLoader(1, null, this);

        binding.goBack.setOnClickListener(v -> {
            startActivity(new Intent(this, TicketActivity.class));
        });

        try {
            bundleString = getIntent().getExtras();
        } catch (Exception e) {
            Log.d("exception error", "Bundle not found");
            startActivity(new Intent(this, TicketActivity.class));
        }

        try {
            String posSettings = bundleString.getString("pos_settings");
            if (posSettings.equals(("yes"))) {
                binding.posPaymentLl.setVisibility(View.GONE);
                binding.posSettingsLl.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.d("exception error", "Pos settings not found");
            binding.posPaymentLl.setVisibility(View.VISIBLE);
            binding.posSettingsLl.setVisibility(View.GONE);
        }

        try {
            amountString = bundleString.getString("price");
            if (amountString.isEmpty()) {
                amount = 0.00;
            } else {
                amount = Double.parseDouble(amountString);
            }
            binding.amt.setText("₹" + amount);
        } catch (Exception e) {
            Log.d("error", "Activity started without price");
//            startActivity(new Intent(this, TicketListActivity.class));
        }

        try {
            String userString = sharedPreference.getString("user", "");
            user = gson.fromJson(userString, User.class);
        } catch (Exception e) {
            Log.d("exception error", "User not found");
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ProfileActivity.class));
        }
        printer = com.pos.device.printer.Printer.getInstance();
        printTask = new PrintTask();
        printTask.setGray(130);

    }

    /*public void functionNavigation(String txn_type) {
        package_name = BuildConfig.APPLICATION_ID;
        String CUSTOM_ACTION = "com.example.menusample.YOUR_ACTION_MAIN";
        Intent i = new Intent();
        i.setAction(CUSTOM_ACTION);
        i.putExtra("txn_type", txn_type);
        i.putExtra("action", "inApp");
        String invoice = amountString;
        i.putExtra("invoice", invoice);
        i.putExtra("receipt_print", "NO");
        i.putExtra("package", package_name);
        startActivityForResult(i,601);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }*/

    // Sale Transaction api
    public void functionPaybyCard(String amt, String txn_type) {
        amt = getConvertDoubleval(amt);
        String transaction_id = getTransactionId();
        package_name = BuildConfig.APPLICATION_ID;
        String receipt = "YES";
        String CUSTOM_ACTION = "cn.desert.newpos.payui.master.YOUR_ACTION";
        Intent i = new Intent();
        i.setAction(CUSTOM_ACTION);
        i.putExtra("amount", amt);// Transaction Amount
        i.putExtra("result_mode", true);// Always true
        i.putExtra("action", "inApp");// InApp indication
        i.putExtra("txn_type", txn_type);// Mode of transaction
        i.putExtra("transaction_id", transaction_id); // Reference Id
        i.putExtra("package", package_name);
        i.putExtra("receipt", receipt);
        startActivityForResult(i, 600);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    public void functionNavigation(String txn_type) {
        package_name = BuildConfig.APPLICATION_ID;
        String CUSTOM_ACTION = "com.example.menusample.YOUR_ACTION_MAIN";
        Intent i = new Intent();
        i.setAction(CUSTOM_ACTION);
        i.putExtra("txn_type", txn_type);
        i.putExtra("action", "inApp");
        i.putExtra("package", package_name);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    // BQR payment api
    public void functionPaybyBqrorUpi(String amt, String txn_type) {
//        finish();
        amt = getConvertDoubleval(amt);
        String receipt = "YES";
        String CUSTOM_ACTION = "com.example.menusample.YOUR_ACTION_BQR";
        package_name = BuildConfig.APPLICATION_ID;
        Intent i = new Intent();
        i.setAction(CUSTOM_ACTION);
        i.putExtra("amount", amt);
        i.putExtra("txn_type", txn_type);
        i.putExtra("action", "inApp");
        i.putExtra("package", package_name);
        i.putExtra("result_mode", true);
        i.putExtra("receipt", receipt);
//        startActivity(i);
        startActivityForResult(i, 601);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    public void functionConfigure(String tid) {

        package_name = BuildConfig.APPLICATION_ID;
        String CUSTOM_ACTION = "com.example.menusample.YOUR_ACTION_CONFIG";
        Intent i = new Intent();
        i.setAction(CUSTOM_ACTION);
        i.putExtra("tid", tid);
        i.putExtra("action", "inApp");
        i.putExtra("package", package_name);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                if (requestCode == 600) {
                    if (data.hasExtra("result_code")) {
                        if (data.getBooleanExtra("result_code", false)) {
                            data.setClass(this, InAppApprovedActivity.class);
                            startActivity(data);
                        } else {
                            Toast.makeText(getApplicationContext(), data.getStringExtra("message"), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Empty result", Toast.LENGTH_LONG).show();
                    }
                } else if (requestCode == 601) {
                    if (data.hasExtra("result_code")) {
                        if (data.getBooleanExtra("result_code", false)) {
                            data.setClass(this, InAppApprovedActivity.class);
                            startActivity(data);
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

    public String getTransactionId() {
        long number = (long) Math.floor(Math.random() * 900000L) + 100000L;
        return addTxnNo();
    }

    public void onClickPayCard(View view) {
        if (sim_status.equalsIgnoreCase("true")) {
            if (param_status.equalsIgnoreCase("true")) {
                String txn_type = "SALE";
                functionPaybyCard(amountString, txn_type);
            } else {
                Toast.makeText(PosActivity.this, "Please do terminal activation", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(PosActivity.this, "Please configure Wifi or GPRS", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickQrStatus(View view) {
        if (sim_status.equalsIgnoreCase("true")) {
            if (param_status.equalsIgnoreCase("true")) {
                String txn_type = "QR Status";
                functionNavigation(txn_type);
            } else {
                Toast.makeText(PosActivity.this, "Please do terminal activation", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(PosActivity.this, "Please configure Wifi or GPRS", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickSettlement(View view) {
        if (sim_status.equalsIgnoreCase("true")) {
            if (param_status.equalsIgnoreCase("true")) {
                String txn_type = "SETTLEMENT";
                functionNavigation(txn_type);
            } else {
                Toast.makeText(PosActivity.this, "Please do terminal activation", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(PosActivity.this, "Please configure Wifi or GPRS", Toast.LENGTH_SHORT).show();
        }

    }

    public void onClickVoid(View view) {
        if (sim_status.equalsIgnoreCase("true")) {
            if (param_status.equalsIgnoreCase("true")) {
                String txn_type = "VOID";
                functionNavigation(txn_type);
            } else {
                Toast.makeText(PosActivity.this, "Please do terminal activation", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(PosActivity.this, "Please configure Wifi or GPRS", Toast.LENGTH_SHORT).show();
        }

    }

    public void onClickReprint(View view) {
        if (sim_status.equalsIgnoreCase("true")) {
            if (param_status.equalsIgnoreCase("true")) {
                String txn_type = "REPRINT";
                functionNavigation(txn_type);
            } else {
                Toast.makeText(PosActivity.this, "Please do terminal activation", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(PosActivity.this, "Please configure Wifi or GPRS", Toast.LENGTH_SHORT).show();
        }


    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
        cursorLoader = new CursorLoader(this, Uri.parse("content://com.example.menusample.provider.InAppProvider/cte"), null, null, null, null);
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> arg0, Cursor cursor) {
        try {
            cursor.moveToFirst();
            StringBuilder res = new StringBuilder();
            while (!cursor.isAfterLast()) {
                sim_status = cursor.getString(cursor.getColumnIndexOrThrow("sim_status"));
                param_status = cursor.getString(cursor.getColumnIndexOrThrow("param_status"));
                cursor.moveToNext();
            }
        } catch (NullPointerException ne) {
            Log.d("msg", "onLoadFinished() error");
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> arg0) {

    }

    public void onClickPayByBqrorUpi(View view) {
        if (sim_status.equalsIgnoreCase("true")) {
            if (param_status.equalsIgnoreCase("true")) {
                String txn_type = "SALE";
                functionPaybyBqrorUpi(amountString, txn_type);
            } else {
                Toast.makeText(PosActivity.this, "Please do terminal activation", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(PosActivity.this, "Please configure Wifi or GPRS", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickTerminalActivation(View view) {

        if (sim_status.equalsIgnoreCase("false")) {
            Toast.makeText(PosActivity.this, "Please configure Wifi or GPRS", Toast.LENGTH_SHORT).show();
        } else {
            package_name = BuildConfig.APPLICATION_ID;
            String tid = "0003200M";
            if (param_status.equalsIgnoreCase("false")) {
                Intent i = getPackageManager().getLaunchIntentForPackage("com.example.menusample");
                i.putExtra("tid", tid);
                i.putExtra("action", "inApp");
                i.putExtra("package", package_name);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            } else {
                String CUSTOM_ACTION = "com.example.menusample.ACTION_PARAM";
                Intent i = new Intent();
                i.setAction(CUSTOM_ACTION);
                i.putExtra("tid", tid);
                i.putExtra("action", "inApp");
                i.putExtra("package", package_name);
                startActivity(i);
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        }
    }

    @SuppressLint("DefaultLocale")
    public String addTxnNo() {
        int currenNo = 1;
        int nextNo = (currenNo + 1) % 1000000;
        return String.format("%06d", nextNo);
    }

    public void onClickAdmin(View view) {
        String tid = "0003200M";
        functionConfigure(tid);
    }

    public void functionAdmin(String tid) {
        String CUSTOM_ACTION = "com.example.menusample";
        Intent i = new Intent();
        i.setAction(CUSTOM_ACTION);
        i.putExtra("tid", "0003200M");
        i.putExtra("action", "paramDownload");
        startActivity(i);
    }

    public void onClickLastTrasaction(View view) {

        Intent i = new Intent(PosActivity.this, LastTxnStatusActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);

    }

    public void onClickProvider(View view) {
        Intent i = new Intent(PosActivity.this, ProxyActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    public void onClickProxy(View view) {
        Intent i = new Intent(PosActivity.this, ProxyActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    public void onClickAddName(View view) {

        getContentResolver().delete(com.amtron.ferryticket.ui.MyProvider.CONTENT_URI, null, null);
        ContentValues values = new ContentValues();
        values.put(com.amtron.ferryticket.ui.MyProvider.amount, amountString);
        values.put(com.amtron.ferryticket.ui.MyProvider.txn_type, "SALE");

        Uri uri = getContentResolver().insert(MyProvider.CONTENT_URI, values);
        Toast.makeText(getBaseContext(), "New record inserted", Toast.LENGTH_LONG)
                .show();
        String CUSTOM_ACTION = "com.example.menusample.nexgo.YOUR_ACTION";
        Intent i = new Intent();
        i.setAction(CUSTOM_ACTION);
        i.putExtra("amount", amountString);
        startActivity(i);
    }

    public String getConvertDoubleval(String amount) {
        Log.e("tag", "print amount" + amount);
        if (amount.length() == 0) {
            amount = "0";
        }
        DecimalFormat formatter = new DecimalFormat("#0.00");
        double d = Double.parseDouble(amount);
        amount = formatter.format(d);
        Log.e("tag", "print new amount" + amount);
        return amount;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_print) {
            onPrintClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void setFontStyle(Paint paint, int size, boolean isBold) {
        if (isBold) {
            Typeface MONOSPACE_BOLD = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
            paint.setTypeface(MONOSPACE_BOLD);

        } else {
            paint.setTypeface(Typeface.MONOSPACE);

        }
        switch (size) {
            case 0:
                break;
            case 1:
                paint.setTextSize(16F);
                break;
            case 2:
                paint.setTextSize(22F);
                break;
            case 3:
                paint.setTextSize(30F);
                break;
            case 4:
                paint.setTextSize(14F);
                break;
            case 5:
                paint.setTextSize(18F);
                break;
            case 6:
                paint.setTextSize(19F);
                break;
            default:
                break;
        }
    }

    public void onPrintClick() {
        PrintCanvas canvas = new PrintCanvas();
        Paint paint = new Paint();
        setFontStyle(paint, 2, false);
        Bitmap Icon_smc = BitmapFactory.decodeResource(getResources(), R.drawable.awt);
        canvas.drawBitmap(Icon_smc, paint);
        setFontStyle(paint, 1, true);
        canvas.drawText("Transaction Amount : " + "Rs.100", paint);
        canvas.drawText("Transaction Description : " + "Success", paint);
        printData(canvas);

    }

    private int printData(PrintCanvas pCanvas) {
        final CountDownLatch latch = new CountDownLatch(1);
        int ret = printer.getStatus();

        if (ret == Printer.PRINTER_OK) {
            byte[] result = draw2PxPoint(pCanvas.getBitmap());

            printTask.setPrintBuffer(result);
            printer.startPrint(printTask, (i, printTask) -> {

                Log.v("ResultControl", "onResult 3 : " + i);
                if (i == -3) {
                    showDialoguePaper();
                }
            });

        } else {
            Toast.makeText(getApplicationContext(), "Please insert paper roll", Toast.LENGTH_LONG).show();

        }
        return ret;
    }

    @SuppressLint("SetTextI18n")
    public void showDialoguePaper() {
        try {
            runOnUiThread(() -> {
                final Dialog dialog = new Dialog(getApplicationContext());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.layout_customerdialog);
                TextView text = dialog.findViewById(R.id.textDialog);

                text.setText("Please insert paper roll");
                // dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_PANEL);
                dialog.show();
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                TextView declineButton = dialog.findViewById(R.id.declineButton);
                TextView textview_no = dialog.findViewById(R.id.textview_no);
                textview_no.setVisibility(View.GONE);
                declineButton.setText("OK");
                textview_no.setOnClickListener(view -> dialog.dismiss());
                declineButton.setOnClickListener(v -> dialog.dismiss());
            });
        } catch (Exception ne) {
            ne.printStackTrace();
        }
    }
}
