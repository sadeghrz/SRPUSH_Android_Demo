package ir.srpush.androiddemo.srpushdemo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AppService extends Service {

    private String serverAddress;
    private boolean mActive = false;
    private Thread mThread;
    public static String session;
    private Date lastPing = Calendar.getInstance().getTime();
    public static Boolean IsOnline = true;
    private GsonBuilder builder = new GsonBuilder();
    private Gson gson = null;
    private WebSocketClient mWebSocketClient;
    public static boolean haveNetwork = true;

    public AppService() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        gson = builder.create();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences prefs = getSharedPreferences("authPrefrence", MODE_PRIVATE);
        session = prefs.getString("session", null);
        serverAddress = prefs.getString("host", null);

        if (session != null && serverAddress != null) {
            start();
        }
        return AppService.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("srpushDemo", "onDestroy()");

        SharedPreferences prefs = getSharedPreferences("authPrefrence", MODE_PRIVATE);
        session = prefs.getString("session", null);
        if (session != null) {
            Intent intent = new Intent("com.android.techtrainner");
            sendBroadcast(intent);
            return;
        }

        try {
            mWebSocketClient.closeBlocking();
        } catch (Exception ignore) {}

        mActive = false;
    }

    public void start() {
        if (!mActive) {
            //Log.e("srpushDemo", "start");
            mActive = true;
            if (mThread == null || !mThread.isAlive()) {
                mThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Looper.prepare();
                        connectWS();
                        Looper.loop();
                    }
                });
                mThread.start();

                // online checker
                Thread checkerThread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        while (mActive) {
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            long diff = Calendar.getInstance().getTime().getTime() -
                                    lastPing.getTime();
                            diff = TimeUnit.MILLISECONDS.toSeconds(diff);

                            if (diff > 8) {
                                if (IsOnline) {
                                    Log.d("srpushDemo", "client is now offline");
                                }
                                IsOnline = false;
                                if (haveNetwork) {
                                    connectWS();
                                }
                            }
                        }
                    }
                });
                checkerThread.start();
            }
        }
    }

    private void connectWS() {
        handleMessage("status:connecting ...");

        URI uri;
        try {
            uri = new URI(serverAddress);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("session", session);

        Draft protocol = new Draft_6455();

        mWebSocketClient = new WebSocketClient(uri, protocol, headers, 1000) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                lastPing = Calendar.getInstance().getTime();
                IsOnline = true;
                handleMessage("status:connected");
                Log.d("srpushDemo", "client is now online");
            }

            @Override
            public void onWebsocketPing(WebSocket conn, Framedata f) {
                Log.i("srpushDemo", "ping");
                super.onWebsocketPing(conn, f);
            }

            @Override
            public void onMessage(String s) {
                if (s.equals("pi")) {
                    mWebSocketClient.send("po");
                    lastPing = Calendar.getInstance().getTime();
                    return;
                }
                handleMessage(s);
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("srpushDemo", "Closed " + s);
                IsOnline = false;
                handleMessage("status:closed");
            }

            @Override
            public void onError(Exception e) {
                Log.i("srpushDemo", "Error " + e.getMessage());
                IsOnline = false;
                handleMessage("status:error");
            }
        };
        mWebSocketClient.connect();
    }

    private void handleMessage(String message) {
        String jsonCsData = message;

        if (!message.startsWith("status:")) {
            WebSocketMessages msg = gson.fromJson(message, WebSocketMessages.class);
            if (mWebSocketClient != null && mWebSocketClient.isOpen()) {
                // msgType = 1 for delivery
                mWebSocketClient.send(
                        "{ " +
                                "\"MsgDT\": " +
                                    "{ \"MsgType\": 1, " +
                                    "\"Data\": { \"_id\": \"" + msg.get_id() + "\" } }, " +
                                "\"SeId\":\"" + session + "\", " +
                                "\"ReId\":\"0\"" +
                        "}"
                );
            }

            Log.d("srpushDemo", "new Message");
            jsonCsData = gson.toJsonTree(msg.getMsgDT()).toString();
        }

        Intent cstateIntent = new Intent("new_message");
        cstateIntent.putExtra("msg", jsonCsData);
        sendBroadcast(cstateIntent);
    }
}
