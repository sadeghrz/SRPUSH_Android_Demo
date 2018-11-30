package ir.srpush.androiddemo.srpushdemo;

import com.google.gson.internal.LinkedTreeMap;

public class WebSocketMessages {

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getMId() {
        return MId;
    }

    public void setMId(String MId) {
        this.MId = MId;
    }

    public String getSeId() {
        return SeId;
    }

    public void setSeId(String seId) {
        SeId = seId;
    }

    public String getReId() {
        return ReId;
    }

    public void setReId(String reId) {
        ReId = reId;
    }

    public int getMsgType() {
        return MsgType;
    }

    public void setMsgType(int msgType) {
        MsgType = msgType;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public LinkedTreeMap getMsgDT() {
        return MsgDT;
    }

    public void setMsgDT(LinkedTreeMap msgDT) {
        MsgDT = msgDT;
    }

    private String _id;
    private String MId;
    private String SeId;
    private String ReId;
    private int MsgType;
    private LinkedTreeMap MsgDT;
    private String Date;
}
