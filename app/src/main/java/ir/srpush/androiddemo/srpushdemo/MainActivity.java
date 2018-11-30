package ir.srpush.androiddemo.srpushdemo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import ir.srpush.androiddemo.srpushdemo.AsyncReqTools.AsyncRequestParams;
import ir.srpush.androiddemo.srpushdemo.AsyncReqTools.AsyncTaskManager;
import ir.srpush.androiddemo.srpushdemo.AsyncReqTools.IRequestReciever;

public class MainActivity extends AppCompatActivity implements IRequestReciever {

    private TextView txtStatus = null;
    private TextView txtMessages = null;
    private TextView txtSession = null;

    private String Username = "5ba4b8939f63c33b916cfe2f";
    private String Pass = "ad016775cc42cdc93a9347482d3375c480e37b39";
    private String apiHost = "http://192.168.1.100:9779/api/createSession";
    private String wsHost = "ws://192.168.1.100:9780";
    private String clientUserID = "DRIVER_145";
    private int sessionTimeout = 500; // expire session after this seconds
    private String session = "";

    private BroadcastReceiver uiThreadMessageReceiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtStatus = findViewById(R.id.txt_status);
        txtMessages = findViewById(R.id.editTxt_messages);
        txtSession = findViewById(R.id.txt_session);

        if (AppService.session != null && AppService.session.length() > 0) {
            session = AppService.session;
            txtSession.setText(AppService.session);
            if (AppService.IsOnline) {
                txtStatus.setText("online");
            }
        } else {
            txtStatus.setText("offline");
        }

        setupBroadcastReceiver();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Intent intent = new Intent();
                String packageName = getPackageName();
                PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
                if (pm != null && !pm.isIgnoringBatteryOptimizations(packageName)) {
                    intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + packageName));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            } catch (Exception e) {
                Log.e("srpushDemo", e.getMessage());
            }
        }
    }

    private void setupBroadcastReceiver() {
        uiThreadMessageReceiver = new BroadcastReceiver() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onReceive(Context context, Intent intent) {
                String msgString = intent.getStringExtra("msg");

                if (msgString.startsWith("status:")) {
                    msgString = msgString.substring(msgString.indexOf(":")+1, msgString.length());
                    txtStatus.setText(msgString);
                    return;
                }

                txtMessages.setText(txtMessages.getText() + "\n" + msgString);
            }
        };
        IntentFilter filter = new IntentFilter("new_message");
        this.registerReceiver(uiThreadMessageReceiver, filter);
    }

    @SuppressLint("SetTextI18n")
    public void getSessionClick(View view) {
        AsyncRequestParams params = new AsyncRequestParams();
        JSONObject json = new JSONObject();
        try {
            json.put("uid", clientUserID);
            json.put("ExTime", sessionTimeout);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        params.set_context(this);
        params.set_uri(apiHost);
        params.set_Jobj(json);
        params.set_username(Username);
        params.set_pass(Pass);
        AsyncTaskManager.Start(params);
    }

    @SuppressLint("SetTextI18n")
    public void connectClick(View view) {
        if (session.length() <= 0 || wsHost.length() <= 0) {
            txtStatus.setText("session not set");
            return;
        }
        disConnectClick(null);
        setupBroadcastReceiver();
        txtStatus.setText("Connecting ...");

        SharedPreferences.Editor editor = this
                .getSharedPreferences("authPrefrence", MODE_PRIVATE).edit();
        editor.putString("session", session);
        editor.putString("host", wsHost);
        editor.apply();

        Intent appService = new Intent(this, AppService.class);
        startService(appService);
    }

    @SuppressLint("SetTextI18n")
    public void disConnectClick(View view) {
        Intent appService = new Intent(this, AppService.class);
        SharedPreferences.Editor editor = this
                .getSharedPreferences("authPrefrence", MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        this.stopService(appService);

        try {
            unregisterReceiver(uiThreadMessageReceiver);
        } catch (Exception ignore) {}

        txtStatus.setText("Disconnected");
    }

    @Override
    public void ReqReciever(String result) {
        try {
            if (result.contains("error") || !result.contains("wss-")) {
                Toast.makeText(this, "خطا در برقراری ارتباط با سرور", Toast.LENGTH_SHORT).show();
                return;
            }
            session = result;
            txtSession.setText(session);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
