package com.amtron.ferryticket.ui;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amtron.ferryticket.R;
import com.amtron.ferryticket.databinding.ActivityInAppApprovedBinding;
import com.amtron.ferryticket.model.Ticket;
import com.google.gson.Gson;
import com.pos.device.printer.PrintCanvas;
import com.pos.device.printer.PrintTask;
import com.pos.device.printer.Printer;

import org.json.JSONArray;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import kotlinx.coroutines.DelicateCoroutinesApi;

@DelicateCoroutinesApi
public class InAppApprovedActivity extends AppCompatActivity {

    //    TextView txt_invoice,  txt_authcode, txt_cardtype, txt_cardno;
    Button printBtn, goBackBtn;
    String amount, in_app_date, in_app_time, invoice, rrn, card_no, card_type, auth_code;
    JSONArray posJSONArray;
    private Printer printer = null;
    private PrintTask printTask = null;
    private SharedPreferences.Editor editor;
    private Ticket ticket;

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
        ActivityInAppApprovedBinding binding = ActivityInAppApprovedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences sharedPreference = this.getSharedPreferences("IWTCounter", MODE_PRIVATE);
        editor = sharedPreference.edit();
        String ticketString = sharedPreference.getString("ticket", "");
        ticket = new Gson().fromJson(ticketString, Ticket.class);

        printer = Printer.getInstance();
        printTask = new PrintTask();
        printTask.setGray(130);

        binding.txtDate.setText(ticket.getFerry_date());
        binding.ticketNo.setText(ticket.getTicket_no());
        binding.srcGhat.setText("NIMATI");
        binding.destGhat.setText("KAMALABARI");
        binding.netAmount.setText("₹" + ticket.getNet_amt());
        binding.serviceCharge.setText("₹" + ticket.getService_amt());

        printBtn.setOnClickListener(v -> {
            PrintCanvas canvas = new PrintCanvas();
            Paint paint = new Paint();
            Bitmap Icon_smc = BitmapFactory.decodeResource(getResources(), R.drawable.awt);

            canvas.drawBitmap(Icon_smc, paint);
            setFontStyle(paint, 2, false);

            canvas.drawText(" ", paint);
            setFontStyle(paint, 1, true);
            canvas.drawText("\t Directorate of Inland Water Transport \n \t \t \t Ulubari, Guwahati 781007\n \t \t \t \t \t \t \t \t \t \t\tAssam", paint);
            canvas.drawText(" ", paint);
            canvas.drawText(" ", paint);
            canvas.drawText("**************************", paint);

            canvas.drawText("Date: " + ticket.getFerry_date(), paint);
            canvas.drawText("Ticket No: " + ticket.getTicket_no(), paint);
            canvas.drawText("RRN No: " + rrn, paint);
            canvas.drawText("Boarding: " + "NIMATI", paint);
            canvas.drawText("Dropping: " + "KAMALABARI", paint);
            canvas.drawText("-------------------------------------", paint);
            canvas.drawText("Net Amout               :\t \t \t₹" + Double.parseDouble(String.valueOf(ticket.getNet_amt())), paint);
            canvas.drawText("Servive Amount          :\t \t \t₹" + Double.parseDouble(String.valueOf(ticket.getService_amt())), paint);
            setFontStyle(paint, 3, false);
            canvas.drawText(" ", paint);
            canvas.drawText("TOTAL - INR " + ticket.getTotal_amt(), paint);
            setFontStyle(paint, 2, false);
            canvas.drawText(" ", paint);
            canvas.drawText("Please keep this ticket safe.This is a one time copy only", paint);
            canvas.drawText("*****************************", paint);
            canvas.drawText("** \t \tThanks.. Visit Again \t \t**", paint);
            canvas.drawText("*****************************", paint);
            setFontStyle(paint, 1, false);
            canvas.drawText("Designed and developed by AMTRON", paint);
            canvas.drawText("Powered by Worldline", paint);
            printData(canvas);
        });

        goBackBtn.setOnClickListener(v -> {
            editor.remove("ticket");
            Intent intent = new Intent(this, BookActivity.class);
            startActivity(intent);
        });

        if (getIntent().hasExtra("message")) {
            String message = getIntent().getStringExtra("message");
            Toast.makeText(getApplicationContext(), getIntent().getStringExtra("message"), Toast.LENGTH_LONG).show();
            if (message.equalsIgnoreCase("Success")) {
                Log.d("success", "yes");
                amount = getIntent().getStringExtra("amount");
                in_app_date = getIntent().getStringExtra("in_app_date");
                in_app_time = getIntent().getStringExtra("in_app_time");
                invoice = getIntent().getStringExtra("invoice");
                rrn = getIntent().getStringExtra("rrn");
                card_no = getIntent().getStringExtra("card_no");
                card_type = getIntent().getStringExtra("card_type");
                auth_code = getIntent().getStringExtra("auth_code");
                if (card_no.isEmpty()) {
                    card_no = "";
                } else {
                    int start_no = card_no.length() - 4;
                    int end = card_no.length();
                    card_no = card_no.substring(start_no, end);
                    card_no = "**** **** **** " + card_no;
                }
                String amt = "INR " + amount;
                binding.txtAmount.setText(amt);
                binding.txtDate.setText(in_app_date);
                binding.txtRrn.setText(rrn);
                binding.time.setText(in_app_time);
//                txt_invoice.setText(invoice);
//                txt_cardno.setText(card_no);
//                txt_cardtype.setText(card_type);
//                txt_authcode.setText(auth_code);
                String[] posData = {amount, in_app_date, in_app_time, invoice, rrn, card_no, card_type, auth_code};
                posJSONArray = new JSONArray(Arrays.asList(posData));
            } else {
                finish();
            }
        }

        callServerSideForTicketConfirmation();
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

    private void printData(PrintCanvas pCanvas) {
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
        }
        return super.onOptionsItemSelected(item);
    }

    private void callServerSideForTicketConfirmation() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, BookActivity.class);
        startActivity(intent);
    }
}

