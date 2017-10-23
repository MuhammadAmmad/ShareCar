package trx.sharecar.activity;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;

import trx.sharecar.R;
import trx.sharecar.activity.base.BaseActivity;
import trx.sharecar.util.ViewHolder;

public class RegisterActivity extends BaseActivity implements View.OnClickListener{

    private EditText username;
    private EditText userpwd;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_register;
    }

    @Override
    protected void initDatas() {
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case -1:
                        toastText("注册失败！");
                        break;
                    case 1:
                        getPermission();
                        break;
                    case 0:
                        toastText("连接失败!");
                        break;
                    case 100:
                        toastText("注册成功！");
                        changeActivity(RegisterActivity.this,MainActivity.class);
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
        username = holder.get(R.id.register_username);
        userpwd = holder.get(R.id.register_userpwd);
        holder.setOnClickListener(this,R.id.btn_register);
    }

    @Override
    public void onClick(View view) {
        String msg = setData("register",R.id.register_username,R.id.register_userpwd)+'\n';
        if(username.getText().toString().trim().equals("") || userpwd.getText().toString().trim().equals(""))
            toastText("请输入用户名/密码！");
        else
            askDataToServer(msg,"","注册中请稍后...",RegisterActivity.this);
    }
}
