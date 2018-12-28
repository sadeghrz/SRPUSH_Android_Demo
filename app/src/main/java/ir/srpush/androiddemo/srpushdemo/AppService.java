package ir.srpush.androiddemo.srpushdemo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;

public class AppService extends FirebaseMessagingService {

    public static String session;
    public static Boolean IsOnline = true;
    public static boolean haveNetwork = true;

    public AppService() {

    }

    @Override
    public void onNewToken(String token) {
        Log.d("srpushDemo", "Refreshed token: " + token);
        handleMessage("status:newFCMToken:" + token);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            Log.d("srpushDemo", "registered token: " +
                    FirebaseInstanceId.getInstance().getToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("srpushDemo", "onDestroy()");

        /*Intent intent = new Intent("com.android.techtrainner");
        sendBroadcast(intent);
        return;*/
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("srpushDemo", "new Message: " + remoteMessage.toString());
    }

    private void handleMessage(String message) {
        String jsonCsData = message;

        Intent cstateIntent = new Intent("new_message");
        cstateIntent.putExtra("msg", jsonCsData);
        sendBroadcast(cstateIntent);
    }
}
