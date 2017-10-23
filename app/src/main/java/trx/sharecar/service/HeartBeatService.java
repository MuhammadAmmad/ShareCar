package trx.sharecar.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import trx.sharecar.bean.PositionInfo;
import trx.sharecar.util.ExecutorPool;
import trx.sharecar.server.ConnectServer;

public class HeartBeatService extends Service {
    private final String TAG = "HeartBeatService ";
    private final MyBinder binder = new MyBinder();

    @Override
    public void onCreate() {
        Log.i(TAG,"onCreate");
        binder.serverConnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class MyBinder extends Binder {
        private JSONArray getArray = new JSONArray();
        private JSONObject sendObject = new JSONObject();

        public void serverConnect(){
            ExecutorPool.executorPool.startThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String msg = setData();
                        if(Looper.myLooper() == null)
                            Looper.prepare();
                        ConnectServer connectServer = new ConnectServer();
                        if(connectServer.connect()){
                            connectServer.sendMSG(msg);
                            while((msg = connectServer.receiveMSG()) != null) {
                                if(JSONObject.fromObject(msg).getString("flag").equals("this is idle...")){
                                    msg = setData();
                                    connectServer.sendMSG(msg);
                                }
                                else if(JSONObject.fromObject(msg).getString("flag").equals("markers")){
                                    dealMSG(JSONObject.fromObject(msg));
                                }
                            }
                        }
                        Looper.loop();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        }

        private void dealMSG(JSONObject jsonObject) {
            JSONArray jsonArray = jsonObject.getJSONArray("content");
            JSONObject send = new JSONObject();
            if(jsonArray.size() != 0) {
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject oldobject = jsonArray.getJSONObject(i);
                    JSONObject newObject = new JSONObject();
                    newObject.put("time", oldobject.getString("time"));
                    newObject.put("latitude", oldobject.getDouble("latitude"));
                    newObject.put("longitude", oldobject.getDouble("longitude"));
                    newObject.put("reason", oldobject.getString("reason"));
                    getArray.add(newObject);
                }
            }
            send.put("flag", "broadcast");
            send.put("content", getArray);
            send(send.toString());
            getArray.clear();
        }

        private String setData() {
            sendObject.put("flag","myposition");
            sendObject.put("content", PositionInfo.positionInfo.getCity());
            sendObject.put("latitude",PositionInfo.positionInfo.getLatitude());
            sendObject.put("longitude",PositionInfo.positionInfo.getLongitude());
            return sendObject.toString();
        }
    }

    public void send(String msg){
        Intent intent = new Intent("MYACTION");
        Bundle bundle = new Bundle();
        bundle.putString("markers",msg);
        intent.putExtras(bundle);
        sendBroadcast(intent);
    }

}
