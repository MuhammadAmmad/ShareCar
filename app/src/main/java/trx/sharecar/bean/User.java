package trx.sharecar.bean;

import net.sf.json.JSONObject;

/**
 * Created by TRX on 2017/3/10.
 */

public class User {
    private static String username;
    private static String userpwd;
    private JSONObject jsonObject = new JSONObject();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserpwd() {
        return userpwd;
    }

    public void setUserpwd(String userpwd) {
        this.userpwd = userpwd;
    }

    public void clear(){
        username = "";
        userpwd = "";
    }

    @Override
    public String toString() {
        jsonObject.clear();
        jsonObject.put("username",username);
        jsonObject.put("userpwd",userpwd);
        return jsonObject.toString();
    }
}
