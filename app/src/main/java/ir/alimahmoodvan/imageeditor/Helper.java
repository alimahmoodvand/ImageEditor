package ir.alimahmoodvan.imageeditor;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import static ir.alimahmoodvan.imageeditor.MainActivity.TAG;

/**
 * Created by acer on 4/23/2019.
 */

public class Helper {
    public static boolean isSubscribed(final Context context){
        String subscribe=Helper.getData(context,"subscribe");
        if (subscribe == "subscribed") {
            return true;
        }
        return false;
    }
    public static void saveData(Context context,String key,String value){
        SharedPreferences settings =   context.getApplicationContext().getSharedPreferences(context.getResources().getString(R.string.shared_preferences), 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }
    public static String getData(Context context,String key){
        SharedPreferences settings = context.getSharedPreferences(context.getResources().getString(R.string.shared_preferences), 0);
        return settings.getString(key, "");
    }
    public static String getOperator(Context context) {
        String operator = "other";
        try {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                SmsManager smsManager = SmsManager.getDefault();
                SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
                List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
                for (SubscriptionInfo subscriptionInfo : subscriptionInfoList) {
                    if (subscriptionInfo.getSubscriptionId() == smsManager.getSubscriptionId()) {
                        String carrierName = subscriptionInfo.getCarrierName().toString().toLowerCase();
                        if (carrierName.contains("ir-") || carrierName.contains("mci") || carrierName.contains("tci")) {
                            operator = "hamrah";
                        } else if (carrierName.contains("irancell") || carrierName.contains("mtn")) {
                            operator = "irancell";
                        }
                        return operator;
                    }

                }
            } else {
                TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                String carrierName = "";
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    carrierName = tManager.getNetworkOperatorName().toLowerCase();
                } else {
                    carrierName = tManager.getNetworkOperatorName().toLowerCase();
                }
                if (carrierName.contains("ir-") || carrierName.contains("mci") || carrierName.contains("tci")) {
                    operator = "hamrah";
                } else if (carrierName.contains("irancell") || carrierName.contains("mtn")) {
                    operator = "irancell";
                }
                return operator;
            }
        } catch (Exception ex) {
            Log.d(TAG, "getOperator: "+ex.getMessage());
        }
        return operator;
    }
    public static String getData(String url) {
        URL mUrl = null;
        String content = "";
        try {
            mUrl = new URL(url + "?_=" + System.currentTimeMillis());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            assert mUrl != null;
            URLConnection connection = mUrl.openConnection();
            connection.setUseCaches(false);
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = "";
            while ((line = br.readLine()) != null) {
                content += line;
            }
            br.close();
        } catch (IOException e) {
            return content;
        }
        return content;
    }
    public static void showToast(Context context,String msg){
        Toast toast= Toast.makeText(context,msg,Toast.LENGTH_SHORT);
        View view = toast.getView();
        TextView text = view.findViewById(android.R.id.message);
        text.setTextColor(context.getResources().getColor(android.R.color.holo_blue_bright));
        toast.show();
    }
    public static String sendHttp(String link,String data) {
        try {
            byte[] postData = data.getBytes();
            int postDataLength = postData.length;

            URL url = new URL(link);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("charset", "utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            conn.setUseCaches(false);
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.write(postData);
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = "";
            String content = "";
            while ((line = br.readLine()) != null) {
                content += line;
            }
            br.close();
            Log.d(TAG, "sendHttp: ");
            return content;
        } catch (Exception e) {
            Log.d(TAG, "sendRequestOTP: " + e.getMessage());
        }
        return "";
    }
}
