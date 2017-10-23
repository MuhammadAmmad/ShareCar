package trx.sharecar.fragment.base;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import trx.sharecar.server.ConnectServer;
import trx.sharecar.util.ExecutorPool;
import trx.sharecar.util.ViewHolder;

public abstract class BaseFragment extends Fragment {
    private ViewHolder viewHolder;

    protected Handler handler;
    protected ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewHolder = new ViewHolder(inflater,container,getLayoutId());
        return viewHolder.getRootView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initDatas();
        initViews(viewHolder,viewHolder.getRootView());
    }

    protected abstract int getLayoutId();

    protected abstract void initDatas();

    protected abstract void initViews(ViewHolder holder, View root);

    public void toastText(String msg){
        Toast.makeText(getActivity().getApplicationContext(),msg,Toast.LENGTH_SHORT).show();
    }

    public void askDataToServer(final String msg,String pdMsg,Context context){
        progressDialog = ProgressDialog.show(context,"",pdMsg,true);
        ExecutorPool.executorPool.startThread(new Runnable() {
            @Override
            public void run() {
                String imsg = msg;
                try {
                    if(Looper.myLooper() == null)
                        Looper.prepare();
                    ConnectServer connectServer = new ConnectServer();
                    Message message = Message.obtain();
                    if (connectServer.connect()) {
                        connectServer.sendMSG(imsg);
                        imsg = connectServer.receiveMSG();
                        connectServer.close();
                        message.obj = imsg;
                        handler.sendMessage(message);
                    }else {
                        message.obj = "0";
                        handler.sendMessage(message);
                    }
                    Looper.loop();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }
}
