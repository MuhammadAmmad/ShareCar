package trx.sharecar.bean;

import net.sf.json.JSONObject;

public class PositionInfo {
    private String time;
    private String position;
    private Double latitude;
    private Double longitude;
    private String reason;
    private String detail;
    private String city;
    private JSONObject jsonObject = new JSONObject();
    public static PositionInfo positionInfo;

    public PositionInfo(String time,String position,Double latitude,Double longitude){
        this.time = time;
        this.position = position;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getTime() {
        return time;
    }

    public String getPosition() {
        return position;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @Override
    public String toString() {
        jsonObject.clear();
        jsonObject.put("time",time);
        jsonObject.put("position",position);
        jsonObject.put("city",city);
        jsonObject.put("latitude",latitude);
        jsonObject.put("longitude",longitude);
        jsonObject.put("reason",reason);
        jsonObject.put("detail",detail);
        return jsonObject.toString();

    }
}
