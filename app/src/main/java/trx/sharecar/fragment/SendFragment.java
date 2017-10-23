package trx.sharecar.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.sf.json.JSONObject;

import java.util.Vector;

import trx.sharecar.bean.PositionInfo;
import trx.sharecar.fragment.base.BaseFragment;
import trx.sharecar.util.ExecutorPool;
import trx.sharecar.R;
import trx.sharecar.server.ConnectServer;
import trx.sharecar.util.ViewHolder;

public class SendFragment extends BaseFragment implements View.OnClickListener {

    private GridView gridView;
    private MyAdapter myAdapter;
    private TextView plcae,time;
    private EditText detail;
    private Vector<String> vector = new Vector<>();

    private int position;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_send;
    }

    @Override
    protected void initDatas() {
        vector.add("道路施工");
        vector.add("交通事故");
        vector.add("车辆过多");
        vector.add("天气原因");
        myAdapter = new MyAdapter(vector,getActivity());
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                progressDialog.dismiss();
                switch (msg.obj.toString()){
                    case "1":
                        toastText("上传成功！");
                        reset();
                        break;
                    case "0":
                        toastText("连接失败！");
                        break;
                    case "-1":
                        toastText("上传失败！");
                        break;
                    default:
                        toastText("未知错误!");
                        break;
                }
            }
        };
    }

    @Override
    protected void initViews(ViewHolder holder, View root) {
        time = holder.get(R.id.send_time);
        plcae = holder.get(R.id.send_place);
        detail = holder.get(R.id.send_edit_detail);
        gridView = holder.get(R.id.send_gridview);

        gridView.setNumColumns(4);
        gridView.setAdapter(myAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                myAdapter.changeState(i);
                position = i;
            }
        });
        holder.setOnClickListener(this,R.id.send_btn_upload);
    }

    @Override
    public void onClick(View view) {
        String msg = setData("upload")+'\n';
        if(!myAdapter.hasOneSelected())
            toastText("请选择TAG标签！");
        else
            askDataToServer(msg,"上传中请稍后...",getActivity());

    }

    public String setData(String flag){
        PositionInfo.positionInfo.setReason(myAdapter.getItem(position).toString());
        PositionInfo.positionInfo.setDetail(detail.getText().toString());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("flag",flag);
        jsonObject.put("content",PositionInfo.positionInfo.toString());
        return jsonObject.toString();
    }

    public void reset(){
        detail.setText("");
        changeTimeAndPosition();
    }

    public void changeTimeAndPosition(){
        plcae.setText(PositionInfo.positionInfo.getPosition());
        time.setText(PositionInfo.positionInfo.getTime());
    }

    private class MyAdapter extends BaseAdapter{

        private Vector<String> vector;
        private Vector<Boolean> state;
        private LayoutInflater inflater;
        private int lastPosition = -1;

        public MyAdapter(Vector<String> vector, Context context){
            this.vector = vector;
            this.state = new Vector<>();
            this.inflater = LayoutInflater.from(context);
            for (int i = 0; i < vector.size(); i++) {
                state.add(false);
            }
        }

        @Override
        public int getCount() {
            return vector.size();
        }

        @Override
        public Object getItem(int i) {
            return vector.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            class ViewHolder{
                public TextView textView;
                public RelativeLayout layout;
            }
            ViewHolder viewHolder;
            if(view == null){
                view = this.inflater.inflate(R.layout.gridview_item,viewGroup,false);
                viewHolder = new ViewHolder();
                viewHolder.textView = (TextView) view.findViewById(R.id.gridview_item_text);
                viewHolder.layout = (RelativeLayout) view.findViewById(R.id.gridview_item_background);
                view.setTag(viewHolder);
            }else
                viewHolder = (ViewHolder) view.getTag();


            if(state.get(i)) {
                viewHolder.textView.setTextColor(Color.parseColor("#FFFFFF"));
                viewHolder.layout.setBackgroundColor(Color.parseColor("#3F51B5"));
            }else{
                viewHolder.textView.setText(vector.get(i));
                viewHolder.textView.setTextColor(Color.parseColor("#000000"));
                viewHolder.layout.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
            return view;
        }

        public void changeState(int position){
            if(lastPosition != -1)
                state.setElementAt(!state.get(lastPosition),lastPosition);
            state.setElementAt(!state.get(position),position);
            lastPosition = position;
            notifyDataSetChanged();
        }

        public boolean hasOneSelected(){
            for (boolean b : state) {
                if(b)
                    return true;
            }
            return false;
        }

    }

}
