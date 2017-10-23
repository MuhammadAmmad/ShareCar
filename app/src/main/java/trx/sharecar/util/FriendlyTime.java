package trx.sharecar.util;

import java.util.Date;

/**
 * Created by gehuiyu on 17/4/27.
 */

public class FriendlyTime {

    public String getFriendlyTime(Date d){
        try {
            long delta = (new Date().getTime() - d.getTime())/1000;
            if(delta <= 0)
                return "刚刚";
            if(delta/(60*60*24*365) > 0)
                return delta/(60*60*24*365)+"年前";
            if(delta/(60*60*24*30) > 0)
                return delta/(60*60*24*30)+"个月前";
            if(delta/(60*60*24*7) > 0)
                return delta/(60*60*24*7)+"周前";
            if(delta/(60*60*24) > 0)
                return delta/(60*60*24)+"天前";
            if(delta/(60*60) > 0)
                return delta/(60*60)+"小时前";
            if(delta/(60) > 0)
                return delta/(60)+"分钟前";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
