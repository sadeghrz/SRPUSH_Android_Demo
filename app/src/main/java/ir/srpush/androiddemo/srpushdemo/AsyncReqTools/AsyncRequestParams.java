package ir.srpush.androiddemo.srpushdemo.AsyncReqTools;

import android.content.Context;

import org.json.JSONObject;

public class AsyncRequestParams {

    private JSONObject _Jobj;
    private String _uri;
    private String _Result;
    private String _username;
    private String _pass;
    private Context _context;

    public String get_Result() {
        return _Result;
    }

    public void set_Result(String _Result) {
        this._Result = _Result;
    }

    public Context get_context() {
        return _context;
    }

    public void set_context(Context _context) {
        this._context = _context;
    }

    public String get_uri() {
        return _uri;
    }

    public void set_uri(String _uri) {
        this._uri = _uri;
    }

    public JSONObject get_Jobj() {
        return _Jobj;
    }

    public void set_Jobj(JSONObject _Jobj) {
        this._Jobj = _Jobj;
    }

    public String get_username() {
        return _username;
    }

    public void set_username(String _username) {
        this._username = _username;
    }

    public String get_pass() {
        return _pass;
    }

    public void set_pass(String _pass) {
        this._pass = _pass;
    }

}
