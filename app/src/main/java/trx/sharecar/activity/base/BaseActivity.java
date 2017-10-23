package trx.sharecar.activity.base;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import net.sf.json.JSONObject;

import trx.sharecar.bean.User;
import trx.sharecar.server.ConnectServer;
import trx.sharecar.util.ExecutorPool;
import trx.sharecar.util.ViewHolder;

/**
 * Created by TRX on 2017/4/22.
 */

public abstract class BaseActivity extends AppCompatActivity {
    private final int GAODE_MAP_PERMISSION = 999;
    private final int PERMISSION_PERMIT = 100;
    private final int PERMISSION_DENY = -100;
    private final int UNKNOW_WRONG = -200;

    private User user;
    private ViewHolder viewHolder;

    protected Handler handler;
    protected ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        user = new User();
        viewHolder = new ViewHolder(getLayoutInflater(),null,getLayoutId());
        setContentView(viewHolder.getRootView());
        initDatas();
        initViews(viewHolder,viewHolder.getRootView());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case GAODE_MAP_PERMISSION:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    handler.sendEmptyMessage(PERMISSION_PERMIT);
                else
                    handler.sendEmptyMessage(PERMISSION_DENY);
                break;
            default:
                handler.sendEmptyMessage(UNKNOW_WRONG);
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    protected abstract int getLayoutId();

    protected abstract void initDatas();

    protected abstract void initViews(ViewHolder holder, View root);

    protected  String setData(String flag,int usernameId,int userpwdId){
        user.clear();
        user.setUsername(((EditText) viewHolder.get(usernameId)).getText().toString().trim());
        user.setUserpwd(((EditText) viewHolder.get(userpwdId)).getText().toString().trim());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("flag",flag);
        jsonObject.put("content",user.toString());
        return jsonObject.toString();
    }

    public  void changeActivity(Context context,Class<?> cls){
        Intent intent = new Intent();
        intent.setClass(context,cls);
        startActivity(intent);
    }

    public void toastText(String msg){
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            if(getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null)
                ( (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE) )
                        .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
        }
        return super.onTouchEvent(event);
    }

    public void getPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION))!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, GAODE_MAP_PERMISSION);
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    Toast.makeText(this, "请允许定位权限！否则APP无法正常运行！", Toast.LENGTH_SHORT).show();
                }
            }else
                handler.sendEmptyMessage(PERMISSION_PERMIT);
        }else
            handler.sendEmptyMessage(PERMISSION_PERMIT);
    }

    public void askDataToServer(final String msg,final String ip,String pdMsg,Context context){
        progressDialog = ProgressDialog.show(context,"",pdMsg,true);
        ExecutorPool.executorPool.startThread(new Runnable() {
            @Override
            public void run() {
                String imsg = msg;
                try {
                    if(Looper.myLooper() == null)
                        Looper.prepare();
                    ConnectServer connectServer = new ConnectServer();
                    connectServer.setIp(ip);
                    if (connectServer.connect()) {
                        connectServer.sendMSG(imsg);
                        imsg = connectServer.receiveMSG();
                        connectServer.close();
                        handler.sendEmptyMessage(Integer.valueOf(imsg));
                    }else
                        handler.sendEmptyMessage(0);
                    Looper.loop();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
}
