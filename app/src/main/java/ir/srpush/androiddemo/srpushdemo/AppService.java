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
    public static String FCM_Token;
    private Date lastPing = Calendar.getInstance().getTime();
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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences prefs = getSharedPreferences("authPrefrence", MODE_PRIVATE);
        session = prefs.getString("session", null);
        FCM_Token = prefs.getString("FCM_Token", null);
        serverAddress = prefs.getString("host", null);

        if (session != null && serverAddress != null) {
            start();
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
        handleMessage("status:connecting");

        URI uri;
        try {
            uri = new URI(serverAddress);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("session", session);
        headers.put("platform", "android");
        headers.put("platformtools", FCM_Token);

        Draft protocol = new Draft_6455();

        mWebSocketClient = new WebSocketClient(uri, protocol, headers, 1000) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                lastPing = Calendar.getInstance().getTime();
                IsOnline = true;
                handleMessage("status:online");
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
                handleMessage("status:offline");
            }

            @Override
            public void onError(Exception e) {
                Log.i("srpushDemo", "Error " + e.getMessage());
                IsOnline = false;
                handleMessage("status:offline");
            }
        };
        mWebSocketClient.connect();
    }

    private void handleMessage(String message) {
        String jsonCsData = message;

        Intent cstateIntent = new Intent("new_message");
        cstateIntent.putExtra("msg", jsonCsData);
        sendBroadcast(cstateIntent);
    }
}
