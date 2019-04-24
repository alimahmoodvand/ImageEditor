package ir.alimahmoodvan.imageeditor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import static ir.alimahmoodvan.imageeditor.MainActivity.TAG;

/**
 * Created by acer on 4/23/2019.
 */

public class Subscription {
    private Activity context;
    public Subscription(Activity context){
        this.context=context;
    }
    public boolean isSubscribed(final Context context){
        String subscribe=Helper.getData(context,"subscribe");
        if (subscribe == "subscribed") {
            return true;
        }
        return false;
    }
    public AlertDialog.Builder  showSubscribeDialog(){
        try {
            JSONObject subData=new JSONObject(Helper.getData(context,"subData"));
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            final EditText edittext = new EditText(context);
            edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
            alert.setMessage(subData.getString("content1"));
            alert.setTitle(subData.getString("title1"));
            alert.setView(edittext);
            alert.setCancelable(false);
            alert.setPositiveButton(subData.getString("positive1"),null);

            alert.setNegativeButton(subData.getString("negative1"),null);
            final AlertDialog mAlertDialog = alert.create();
            mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(final DialogInterface dialog) {

                    Button ok = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // TODO Do something
                            String code = edittext.getText().toString();
                            if(code.length()==11) {
                                dialog.dismiss();
                                sendRequestOTP(code);
                            }else{
                                Helper.showToast(context,context.getResources().getString(R.string.number_incorrect));
                            }
                        }
                    });
                    Button cancel = mAlertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // TODO Do something
                            Helper.showToast(context,context.getResources().getString(R.string.btn_neg));
                            dialog.dismiss();
                        }
                    });
                }
            });
            return alert;
        }catch (Exception e){
            Log.d(TAG, "showSubscribeDialog: "+e);
        }
        return null;
    }
    private void sendRequestOTP(String mobile) {
        try {
            JSONObject subData=new JSONObject(Helper.getData(context,"subData"));
            String urlParameters = "mobile="+mobile+"&token="+context.getResources().getString(R.string.token);
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
                    Helper.saveData(context,"transcode",transcode);
                    String otpreference=data.getJSONObject("data").getString("otpreference");
                    Helper.saveData(context,"otpreference",otpreference);
                    Helper.saveData(context,"mobile",mobile);
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
            JSONObject subData=new JSONObject(Helper.getData(context,"subData"));
            AlertDialog.Builder alert = new AlertDialog.Builder(context);
            final EditText edittext = new EditText(context);
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
                    Helper.showToast(context,context.getResources().getString(R.string.btn_neg));
                }
            });
            AlertDialog dialog = alert.show();
            TextView messageView = (TextView) dialog.findViewById(android.R.id.message);
            messageView.setGravity(Gravity.RIGHT);

            TextView titleView = (TextView) dialog.findViewById(context.getResources().getIdentifier("alertTitle", "id", "android"));
            if (titleView != null) {
                titleView.setGravity(Gravity.RIGHT);
            }
        }catch (Exception e){
            Log.d(TAG, "showOTPDialog: "+e.getMessage());
        }
    }
    private void sendRequestCode(String code) {
        try {
            JSONObject subData=new JSONObject(Helper.getData(context,"subData"));
            String mobile=Helper.getData("mobile");
            String transcode=Helper.getData("transcode");
            String otpreference=Helper.getData("otpreference");
            String urlParameters = "mobile="+mobile+"&transcode="+transcode+"&otpreference="+otpreference+"&code="+code+"&token="+context.getResources().getString(R.string.token);
            String request =subData.getString("urlconfirm");
            String res=Helper.sendHttp(request,urlParameters);
            if(!res.isEmpty()){
                JSONObject data= new JSONObject(res);
                if(data.getString("status")=="1"){
                    Toast toast= Toast.makeText(context,context.getResources().getString(R.string.subscribe_message),Toast.LENGTH_SHORT);
                    View view = toast.getView();
                    TextView text = view.findViewById(android.R.id.message);
                    text.setTextColor(context.getResources().getColor(android.R.color.holo_blue_bright));
                    toast.show();
                    Helper.saveData(context,"subscribe","subscribed");
                }else if(data.getString("status")=="-1"){
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "sendRequestOTP: " + e.getMessage());
        }

    }
}
