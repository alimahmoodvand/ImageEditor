package ir.alimahmoodvan.imageeditor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import co.ronash.pushe.Pushe;

public class MainActivity extends Activity {
    public static final String TAG = "PhotoerImageActivity";
    int galleryRequest=1;
    int cameraRequest=2;
    private String realPath="";
    private String operator="other";
    private JSONObject subData=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Pushe.initialize(this,true);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        boolean subscribed=Helper.isSubscribed(getApplicationContext());
        if(!subscribed){
            operator=Helper.getOperator(getApplicationContext());
            Log.d(TAG, "onCreate: "+operator);
            String res=Helper.getData("http://199.127.99.12/data.json");
            try{
                JSONObject tmp=new JSONObject(res);
                subData=tmp.getJSONObject(operator);
                Helper.saveData(getApplicationContext(),"subData",subData.toString());
                showSubscribeDialog();
            }catch (Exception e){
                Log.d(TAG, "onCreate: "+e.getMessage());
            }
        }
        ImageButton btn_gallery=findViewById(R.id.btn_gallery);

        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
//                photoPickerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                Log.d(TAG, "onClick: btn_gallery");
                startActivityForResult(photoPickerIntent, galleryRequest);

            }
        });
        ImageButton btn_camera=findViewById(R.id.btn_camera);
        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = timeStamp + ".jpg";
                File storageDir = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES);
                realPath = storageDir.getAbsolutePath() + "/" + imageFileName;
                File file = new File(realPath);
                Uri outputFileUri = Uri.fromFile(file);
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
//                cameraIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                Log.d(TAG, "onClick: btn_camera");
                startActivityForResult(cameraIntent, cameraRequest);
            }
        });
    }
    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
//        super.onActivityResult(reqCode, resultCode, data);
//        ProgressDialog progress = new ProgressDialog(this);
//        progress.setTitle("Loading");
//        progress.setMessage("Wait while loading...");
//        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
//        progress.show();
// To dismiss the dialog
        try {
            if (resultCode == RESULT_OK) {
                if (galleryRequest == reqCode) {
                    try {
                        realPath = ImageFilePath.getPath(MainActivity.this, data.getData());
                        Log.d(TAG, "onActivityResult: " + realPath);
                        Intent intent = new Intent(getBaseContext(), EffectsFilterActivity.class);
                        intent.putExtra("realPath", realPath);
                        //     progress.dismiss();
                        startActivity(intent);
                    } catch (Exception e) {
                        ///    progress.dismiss();
                        Log.d(TAG, "onActivityResult: " + e.getMessage());
                    }
                }
                if (cameraRequest == reqCode) {
                    try {
                        File imgFile = new File(realPath);
                        Log.d(TAG, "onActivityResult: " + realPath);
                        if (imgFile.exists()) {
                            Intent intent = new Intent(getBaseContext(), EffectsFilterActivity.class);
                            intent.putExtra("realPath", realPath);
                            //     progress.dismiss();
                            startActivity(intent);
                        }
                    } catch (Exception e) {
                        // progress.dismiss();
                        Log.d(TAG, "onActivityResult: " + e.getMessage());
                    }
                }
            } else {
                //progress.dismiss();
            }
        }catch (Exception ex){

        }
    }
    private void showSubscribeDialog(){
        try {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            final EditText edittext = new EditText(this);
            edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
            alert.setMessage(subData.getString("content1"));
            alert.setTitle(subData.getString("title1"));
            alert.setView(edittext);
            alert.setCancelable(false);
            alert.setPositiveButton(subData.getString("positive1"), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String code = edittext.getText().toString();
                    Log.d(TAG, "onClick: "+code);
                    if(code.length()==11) {
                        sendRequestOTP(code);
                    }
                }
            });

            alert.setNegativeButton(subData.getString("negative1"), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Helper.showToast(getApplicationContext(),getApplicationContext().getResources().getString(R.string.btn_neg));
                }
                });
            AlertDialog dialog = alert.show();
            TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
            messageView.setGravity(Gravity.RIGHT);
            TextView titleView = (TextView) dialog.findViewById(getResources().getIdentifier("alertTitle", "id", "android"));
            if (titleView != null) {
                titleView.setGravity(Gravity.RIGHT);
            }
        }catch (Exception e){
            Log.d(TAG, "showSubscribeDialog: "+e.getMessage());
        }
    }
    private void sendRequestOTP(String mobile) {
        try {
            String urlParameters = "mobile="+mobile+"&token="+getResources().getString(R.string.token);
            String request = subData.getString("urlotp");
            String res=Helper.sendHttp(request,urlParameters);
            Log.d(TAG, "sendRequestOTP: "+urlParameters);
            Log.d(TAG, "sendRequestOTP: "+res);
            if(!res.isEmpty()){
                JSONObject data= new JSONObject(res);
                Log.d(TAG, "sendRequestOTP: ");
                if(data.getString("status").equals("1")){
                    //{"status":"1","message":"otp transaction request was successfully","data":{"transcode":"15388931515381","otpreference":"153889315171656"}}
                    String transcode=data.getJSONObject("data").getString("transcode");
                    Helper.saveData(getApplicationContext(),"transcode",transcode);
                    String otpreference=data.getJSONObject("data").getString("otpreference");
                    Helper.saveData(getApplicationContext(),"otpreference",otpreference);
                    Helper.saveData(getApplicationContext(),"mobile",mobile);
                    showOTPDialog();
                }else if(data.getString("status")=="-1"){

                }
            }
        } catch (Exception e) {
            Log.d(TAG, "sendRequestOTP: " + e.getMessage());
        }

    }
    private void showOTPDialog(){
        try {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            final EditText edittext = new EditText(this);
            edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
            alert.setMessage(subData.getString("content2"));
            alert.setTitle(subData.getString("title2"));
            alert.setView(edittext);
            alert.setCancelable(false);
            alert.setPositiveButton(subData.getString("positive2"), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String code = edittext.getText().toString();
                    sendRequestCode(code);
                }
            });

            alert.setNegativeButton(subData.getString("negative2"), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Helper.showToast(getApplicationContext(),getResources().getString(R.string.btn_neg));
                }
            });
            AlertDialog dialog = alert.show();
            TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
            messageView.setGravity(Gravity.RIGHT);

            TextView titleView = (TextView) dialog.findViewById(getResources().getIdentifier("alertTitle", "id", "android"));
            if (titleView != null) {
                titleView.setGravity(Gravity.RIGHT);
            }
        }catch (Exception e){
            Log.d(TAG, "showOTPDialog: "+e.getMessage());
        }
    }
    private void sendRequestCode(String code) {
        try {
            String mobile=Helper.getData("mobile");
            String transcode=Helper.getData("transcode");
            String otpreference=Helper.getData("otpreference");
            String urlParameters = "mobile="+mobile+"&transcode="+transcode+"&otpreference="+otpreference+"&code="+code+"&token="+getResources().getString(R.string.token);
            String request =subData.getString("urlconfirm");
            String res=Helper.sendHttp(request,urlParameters);
            if(!res.isEmpty()){
                JSONObject data= new JSONObject(res);
                if(data.getString("status")=="1"){
                    Toast toast= Toast.makeText(this,getResources().getString(R.string.subscribe_message),Toast.LENGTH_SHORT);
                    View view = toast.getView();
                    TextView text = view.findViewById(android.R.id.message);
                    text.setTextColor(getResources().getColor(android.R.color.holo_blue_bright));
                    toast.show();
                    Helper.saveData(getApplicationContext(),"subscribe","subscribed");
                }else if(data.getString("status")=="-1"){
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "sendRequestOTP: " + e.getMessage());
        }
    }

}
