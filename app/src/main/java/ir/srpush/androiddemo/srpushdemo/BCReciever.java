package ir.srpush.androiddemo.srpushdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

public class BCReciever extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final android.net.NetworkInfo netInfo = connMgr.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected()) {
            AppService.haveNetwork = true;
            Intent myIntent = new Intent(context, AppService.class);
            context.startService(myIntent);
        } else {
            AppService.haveNetwork = false;
        }
    }

}
