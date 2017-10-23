package trx.sharecar.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import trx.sharecar.fragment.base.BaseFragment;
import trx.sharecar.util.ExecutorPool;
import trx.sharecar.R;
import trx.sharecar.server.ConnectServer;
import trx.sharecar.util.FriendlyTime;
import trx.sharecar.util.ViewHolder;

public class SearchFragment extends BaseFragment implements View.OnClickListener{

    private ListView listView;
    private EditText editText;
    private BaseAdapter adapter;

    private List<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search;
    }

    @Override
    protected void initDatas() {
        adapter = new ListViewAdapter(getActivity());
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                progressDialog.dismiss();
                switch (msg.obj.toString()){
                    case "0":
                        toastText("连接失败！");
                        break;
                    case "-1":
                        toastText("查询数据库失败！");
                        break;
                    default:
                        JSONObject object = JSONObject.fromObject(msg.obj);
                        if(object.getInt("is") == 1)
                            if(showList(object))
                                adapter.notifyDataSetChanged();
                            else
                                toastText("没有相关拥堵路段！");
                        else
                            toastText("没有相关拥堵路段！");
                        break;
                }
            }
        };
    }

    @Override
    protected void initViews(ViewHolder holder, View root) {
        listView = holder.get(R.id.search_listview);
        editText = holder.get(R.id.search_edittext);
        holder.setOnClickListener(this,R.id.search_btn);

        listView.setAdapter(adapter);
    }

    private boolean showList(JSONObject object) {
        list.clear();
        JSONArray jsonArray = object.getJSONArray("content");
        if(jsonArray.size() == 0)
            return false;
        for(int i = 0;i<jsonArray.size();i++){
            HashMap<String,Object> map = new HashMap<>();
            map.put("time",jsonArray.getJSONObject(i).getString("time"));
            map.put("position",jsonArray.getJSONObject(i).getString("position"));
            map.put("reason",jsonArray.getJSONObject(i).getString("reason"));
            map.put("detail",jsonArray.getJSONObject(i).getString("detail"));
            list.add(map);
        }
        return true;


    }

    private String setData(String flag) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("flag",flag);
        jsonObject.put("content",editText.getText().toString().trim());
        return jsonObject.toString();
    }

    @Override
    public void onClick(View view) {
        String msg = setData("getListInfo")+'\n';
        if(editText.getText().toString().trim().equals(""))
            toastText("请输入查询关键字！");
        else
            askDataToServer(msg,"查询中请稍后...",getActivity());
    }

    private class ListViewAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private FriendlyTime time;
        private DateFormat df;

        public ListViewAdapter(Context context){
            this.inflater = LayoutInflater.from(context);
            time = new FriendlyTime();
            df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            class ViewHolder{
                public TextView time;
                public TextView position;
                public TextView reason;
                public TextView detail;
            }

            ViewHolder holder;
            if(view == null){
                view = this.inflater.inflate(R.layout.fragment_search_item,viewGroup,false);
                holder = new ViewHolder();
                holder.position = (TextView) view.findViewById(R.id.item_place);
                holder.detail = (TextView) view.findViewById(R.id.item_describe);
                holder.time = (TextView) view.findViewById(R.id.item_time);
                holder.reason = (TextView) view.findViewById(R.id.item_reason);
                view.setTag(holder);
            }else
                holder = (ViewHolder) view.getTag();

            try {
                holder.time.setText(time.getFriendlyTime(df.parse(list.get(i).get("time").toString())));
                holder.position.setText(list.get(i).get("position").toString());
                holder.reason.setText(list.get(i).get("reason").toString());
                holder.detail.setText(list.get(i).get("detail").toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }


            return view;
        }
    }


}
