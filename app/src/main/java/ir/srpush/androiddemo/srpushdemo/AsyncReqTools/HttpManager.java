package ir.srpush.androiddemo.srpushdemo.AsyncReqTools;

import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public class HttpManager {

    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public static String getData(String uri, JSONObject Jobj, final String username, final String pass) {
        try {
            //String credential = "ss";
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .build();

            String credential = Credentials.basic(username, pass);
            RequestBody body = RequestBody.create(JSON, Jobj.toString());
            Request req = new Request.Builder()
                    .url(uri)
                    .post(body)
                    .header("Authorization", credential)
                    .build();
            Response res = client.newCall(req).execute();
            return res.body().string();
        }
        catch (Exception ex) {
            return "Error Find:" + ex.getMessage();
        }
    }
}
