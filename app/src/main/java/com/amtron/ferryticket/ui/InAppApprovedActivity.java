package com.amtron.ferryticket.ui;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
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

import com.amtron.ferryticket.R;
import com.amtron.ferryticket.databinding.ActivityInAppApprovedBinding;
import com.amtron.ferryticket.helper.DateAndTimeHelper;
import com.amtron.ferryticket.helper.ResponseHelper;
import com.amtron.ferryticket.model.Others;
import com.amtron.ferryticket.model.PassengerDetails;
import com.amtron.ferryticket.model.Ticket;
import com.amtron.ferryticket.model.User;
import com.amtron.ferryticket.model.Vehicle;
import com.amtron.ferryticket.network.Client;
import com.amtron.ferryticket.network.RetrofitHelper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.pos.device.printer.PrintCanvas;
import com.pos.device.printer.PrintTask;
import com.pos.device.printer.Printer;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import cn.pedant.SweetAlert.SweetAlertDialog;
import kotlinx.coroutines.DelicateCoroutinesApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@DelicateCoroutinesApi
public class InAppApprovedActivity extends AppCompatActivity {

    //    TextView txt_invoice,  txt_authcode, txt_cardtype, txt_cardno;
    String amount, in_app_date, in_app_time, invoice, rrn, orderNo, card_no, card_type, auth_code, ferryDepartureTime, ferryArrivalTime, ticketNo, ticketDate, source, destination, serviceName, paymentMode, tid, cardToBeSend;
    JSONArray posJSONArray;
    ActivityInAppApprovedBinding binding;
    //need below two values to horizontally center align bitmap
    static float bitmapWidth;
    float canvasWidth;
    private Printer printer = null;
    private PrintTask printTask = null;
    private SharedPreferences sharedPreference;
    private SharedPreferences.Editor editor;
    private Ticket ticket;
    //    private CardDetails cardDetails;
    private Bitmap qrBitmap;
    private ArrayList<PassengerDetails> passengerDetailsList;
    private ArrayList<Vehicle> vehiclesList;
    private ArrayList<Others> othersList;
    private User user;

    public static byte[] draw2PxPoint(Bitmap bitmap) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        bitmapWidth = bitmap.getWidth();
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
        binding = ActivityInAppApprovedBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreference = this.getSharedPreferences("IWTCounter", MODE_PRIVATE);
        SharedPreferences tidSharedPreference = this.getSharedPreferences("IWT_TID", MODE_PRIVATE);
        editor = sharedPreference.edit();

        tid = tidSharedPreference.getString("tid", "");
        String userString = sharedPreference.getString("user", "");
        user = new Gson().fromJson(userString, User.class);

        try {
            String ticketString = sharedPreference.getString("ticket", "");
            ticket = new Gson().fromJson(ticketString, Ticket.class);
            ticketNo = ticket.getTicket_no();
            orderNo = ticket.getRrn();
            passengerDetailsList = (ArrayList<PassengerDetails>) ticket.getPassenger();
            vehiclesList = (ArrayList<Vehicle>) ticket.getVehicle();
            othersList = (ArrayList<Others>) ticket.getOther();
            source = ticket.getSource().getGhat_name();
            destination = ticket.getDestination().getGhat_name();
            serviceName = ticket.getFerry().getFerry_name();
            paymentMode = ticket.getMode_of_payment();
            ticketDate = new DateAndTimeHelper().changeDateFormat("dd MMM, yyyy", ticket.getFerry_date());
            ferryDepartureTime = new DateAndTimeHelper().changeTimeFormat(ticket.getFs_departure_time());
            ferryArrivalTime = new DateAndTimeHelper().changeTimeFormat(ticket.getFs_reached_time());
            String time = ferryDepartureTime + " - " + ferryArrivalTime;

            binding.ticketNo.setText(ticketNo);
            binding.txtDate.setText(ticketDate);
            binding.time.setText("");
            binding.srcGhat.setText(source);
            binding.destGhat.setText(destination);
            binding.paymentMode.setText(paymentMode);
            binding.serviceName.setText(serviceName);
            if (orderNo.isEmpty()) {
                binding.rrnLl.setVisibility(View.GONE);
            } else {
                binding.txtRrn.setText(orderNo);
            }
            binding.serviceTime.setText(time);
            binding.netAmount.setText("₹" + ticket.getNet_amt());
            if (ticket.getWallet_service_charge() == 1) {
                binding.serviceCharge.setText("₹" + ticket.getService_amt());
            } else {
                binding.custServCharge.setVisibility(View.GONE);
            }
            binding.txtAmount.setText("₹" + ticket.getTotal_amt());

            //generate qr
            try {
                QRCodeWriter writer = new QRCodeWriter();
                BitMatrix bitMatrix = writer.encode(ticket.getQr_string(), BarcodeFormat.QR_CODE, 390, 390); //qr size
                int width = bitMatrix.getWidth();
                int height = bitMatrix.getHeight();
                qrBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        qrBitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                    }
                }
            } catch (WriterException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            Log.d("ticket details", "not found");
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, TicketListActivity.class));
        }
        /*try {
            String cardDetailsString = sharedPreference.getString("card_details", "");
            cardDetails = new Gson().fromJson(cardDetailsString, CardDetails.class);
            Log.d("card details", cardDetails.toString());

            card_no = cardDetails.getCard_no();
            binding.cardNumber.setText(card_no);
        } catch (Exception e) {
            Log.d("Scanned card details", "not found");
        }*/

        printer = Printer.getInstance();
        printTask = new PrintTask();
        printTask.setGray(130);

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
                cardToBeSend = getIntent().getStringExtra("card_no");
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
                binding.cardNumber.setText(card_no);
//                txt_cardtype.setText(card_type);
//                txt_authcode.setText(auth_code);
                String[] posData = {user.getToken(), amount, in_app_date, in_app_time, invoice, rrn, cardToBeSend, card_type, auth_code, String.valueOf(ticket.getId()), tid};
                posJSONArray = new JSONArray(Arrays.asList(posData));
                Log.d("data sent", String.valueOf(posJSONArray));

                callServerSideForTicketConfirmation(amount, in_app_date, in_app_time, invoice, rrn, cardToBeSend, card_type, auth_code, ticket.getId(), tid);
            } else {
                finish();
            }
        }

        binding.print.setOnClickListener(v -> {
            PrintCanvas canvas = new PrintCanvas();
            canvasWidth = canvas.getWidth();
            Paint paint = new Paint();
            Bitmap Icon_smc = BitmapFactory.decodeResource(getResources(), R.drawable.rect_awt);

            canvas.drawBitmap(Icon_smc, paint);
            setFontStyle(paint, 1, false);

            canvas.drawText(" ", paint);
            setFontStyle(paint, 1, true);
            canvas.drawText("\t \t \t Inland Water Transport, Assam", paint);
            canvas.drawText("**************************************", paint);

            setFontStyle(paint, 2, false);
            canvas.drawText("T/No: " + ticket.getTicket_no(), paint);
            setFontStyle(paint, 1, false);
            canvas.drawText("Date: " + ticketDate, paint);
            canvas.drawText("Timing: " + ferryDepartureTime + "-" + ferryArrivalTime, paint);
            canvas.drawText("Ferry Name: " + serviceName, paint);
            canvas.drawText("Boarding: " + source, paint);
            canvas.drawText("Dropping: " + destination, paint);
            canvas.drawText("Payment Mode: " + paymentMode, paint);
            if (passengerDetailsList.size() > 0) {
                canvas.drawText(" ", paint);
                canvas.drawText("Passengers:", paint);
                for (int i = 0; i < passengerDetailsList.size(); i++) {
                    canvas.drawText((i + 1) + ". " + passengerDetailsList.get(i).getPassenger_name(), paint);
                }
            }
            if (vehiclesList.size() > 0) {
                canvas.drawText(" ", paint);
                canvas.drawText("Vehicles:", paint);
                for (int i = 0; i < vehiclesList.size(); i++) {
                    canvas.drawText((i + 1) + ". " + vehiclesList.get(i).getVehicle_type().getP_name() + " (" + vehiclesList.get(i).getReg_no() + ")", paint);
                }
            }
            if (othersList.size() > 0) {
                canvas.drawText(" ", paint);
                canvas.drawText("Others:", paint);
                for (int i = 0; i < othersList.size(); i++) {
                    canvas.drawText((i + 1) + ". " + othersList.get(i).getOther_name() + " (" + othersList.get(i).getQuantity() + ")", paint);
                }
            }
            canvas.drawText(" ", paint);
            /*canvas.drawText("Card No: " + card_no, paint);
            if (rrn != null) {
                canvas.drawText("RRN/Order No: " + rrn, paint);
            }
            if (!orderNo.isEmpty()) {
                canvas.drawText("RRN/Order No: " + rrn, paint);
            }*/
            canvas.drawText("-------------------------------------", paint);
            canvas.drawText("Net Amount               :\t \t \t₹" + Double.parseDouble(String.valueOf(ticket.getNet_amt())), paint);
            if (ticket.getWallet_service_charge() == 1) {
                canvas.drawText("Service Amount          :\t \t \t₹" + Double.parseDouble(String.valueOf(ticket.getService_amt())), paint);
            }
            canvas.drawText(" ", paint);
            if (ticket.getWallet_service_charge() == 1) {
                canvas.drawText("TOTAL                    :\t \t \t₹" + ticket.getNet_amt() + ticket.getService_amt(), paint);
            } else {
                canvas.drawText("TOTAL                    :\t \t \t₹" + ticket.getTotal_amt(), paint);
            }
            canvas.drawBitmap(qrBitmap, paint);
            canvas.drawText(" ", paint);
            canvas.drawText("**************************************", paint);
            canvas.drawText("\t \t \t \t** \t \tThanks.. Visit Again \t \t**", paint);
            canvas.drawText("\t \t \tDesigned and developed by AMTRON", paint);
            canvas.drawText("\t \t \t \t \t \tPOS powered by Worldline", paint);
            canvas.drawText("**************************************", paint);
            printData(canvas);
        });

        binding.goBack.setOnClickListener(v -> {
            if (sharedPreference.getString("activity_from", "").equals("ticketListActivity")) {
                Intent intent = new Intent(this, TicketListActivity.class);
                startActivity(intent);
            } else {
                editor.remove("ticket");
                editor.remove("passenger_card_details");
                editor.apply();
                Intent intent = new Intent(this, BookActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setFontStyle(Paint paint, int size, boolean isBold) {
        if (isBold) {
            Typeface MONOSPACE_BOLD = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD);
            paint.setTypeface(MONOSPACE_BOLD);

        } else {
            paint.setTypeface(Typeface.MONOSPACE);

        }
        switch (size) {
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

    @SuppressLint("SetTextI18n")
    private void callServerSideForTicketConfirmation(
            String amount,
            String in_app_date,
            String in_app_time,
            String invoice,
            String rrn,
            String card_no,
            String card_type,
            String auth_code,
            Integer id,
            String tid
    ) {
        SweetAlertDialog alert = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        alert.setTitle("VERIFYING");
        alert.setCancelable(false);
        alert.show();
        Client client = Objects.requireNonNull(RetrofitHelper.ForJava.Companion.getInstance(this)).create(Client.class);
        Call<JsonObject> call = client.sendPosDataToServer(
                "Bearer " + user.getToken(),
                Double.parseDouble(amount),
                in_app_date,
                in_app_time,
                invoice,
                rrn,
                card_no,
                card_type,
                auth_code,
                id,
                tid
        );
        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    ResponseHelper helper = new ResponseHelper();
                    helper.responseHelper(response.body());
                    if (helper.isStatusSuccessful()) {
                        alert.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                        alert.dismissWithAnimation();
                        binding.print.setText("PRINT");
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
                        binding.print.setText("VERIFY");
                        binding.print.setOnClickListener(v ->
                                callServerSideForTicketConfirmation(amount, in_app_date, in_app_time, invoice, rrn, cardToBeSend, card_type, auth_code, ticket.getId(), tid)
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
                    binding.print.setText("VERIFY");
                    binding.print.setOnClickListener(v -> callServerSideForTicketConfirmation(amount, in_app_date, in_app_time, invoice, rrn, cardToBeSend, card_type, auth_code, ticket.getId(), tid));
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
                binding.print.setText("VERIFY");
                binding.print.setOnClickListener(v -> callServerSideForTicketConfirmation(amount, in_app_date, in_app_time, invoice, rrn, cardToBeSend, card_type, auth_code, ticket.getId(), tid));
                Log.d("ERROR!!", "Server Error. Please try again.");
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (sharedPreference.getString("activity_from", "").equals("ticketListActivity")) {
            Intent intent = new Intent(this, TicketListActivity.class);
            startActivity(intent);
        } else {
            editor.remove("ticket");
            editor.remove("passenger_card_details");
            editor.apply();
            Intent intent = new Intent(this, BookActivity.class);
            startActivity(intent);
        }
    }
}