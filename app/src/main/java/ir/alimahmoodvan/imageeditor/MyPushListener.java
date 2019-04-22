package ir.alimahmoodvan.imageeditor;

import android.util.Log;

import org.json.JSONObject;

import co.ronash.pushe.PusheListenerService;



/**
 * Created by acer on 4/17/2019.
 */

public class MyPushListener extends PusheListenerService {
    String TAG="MyPushListener";
    @Override
    public void onMessageReceived(JSONObject customContent, JSONObject pushMessage){
        Log.d(TAG, "onMessageReceived: "+customContent.toString());
        // Your Code
    }
}