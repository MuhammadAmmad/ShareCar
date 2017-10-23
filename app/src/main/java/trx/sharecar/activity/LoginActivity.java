package trx.sharecar.activity;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import net.sf.json.JSONObject;

import trx.sharecar.activity.base.BaseActivity;
import trx.sharecar.util.ViewHolder;
import trx.sharecar.bean.User;
import trx.sharecar.R;

public class LoginActivity extends BaseActivity implements View.OnClickListener{
    private EditText username,userpwd,ip;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void initDatas() {
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                progressDialog.dismiss();
                switch (msg.what){
                    case -1:
                        toastText("用户名/密码错误！");
                        break;
                    case 1:
                        getPermission();
                        break;
                    case 0:
                        toastText("连接失败!");
                        break;
                    case 100:
                        toastText("登陆成功！");
                        changeActivity(LoginActivity.this,MainActivity.class);
                        finish();
                        break;
                    case -100:
                        toastText("请前往设置中开启GPS权限！");
                        break;
                    case -200:
                        toastText("未知权限错误！");
                        break;
                    default:
                        toastText("未知错误！");
                        break;

                }
            }
        };
    }

    @Override
    protected void initViews(ViewHolder holder, View root) {
        username = holder.get(R.id.login_username);
        userpwd = holder.get(R.id.login_userpwd);
        ip = holder.get(R.id.login_ip);
        holder.setOnClickListener(this, R.id.btn_login, R.id.btn_register);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_login:
                String msg = setData("login",R.id.login_username,R.id.login_userpwd)+'\n';
                if(username.getText().toString().trim().equals("") || userpwd.getText().toString().trim().equals("")){
                    toastText("请输入用户名/密码！");
                    break;
                } else{
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                    askDataToServer(msg, ip.getText().toString().trim(),"登陆中请稍后...",LoginActivity.this);
                }
                break;
            case R.id.btn_register:
                changeActivity(LoginActivity.this,RegisterActivity.class);
                break;
            default:
                break;
        }

    }
}
