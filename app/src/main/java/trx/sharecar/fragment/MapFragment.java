package trx.sharecar.fragment;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import trx.sharecar.activity.MainActivity;
import trx.sharecar.bean.PositionInfo;
import trx.sharecar.R;
import trx.sharecar.service.HeartBeatService;
import trx.sharecar.util.FriendlyTime;

public class MapFragment extends Fragment implements LocationSource,AMapLocationListener{

    private final int ADD_MARKERS = 0;
    private final String TAG ="MapFragment";

    private MapView mapView = null;
    private AMap aMap;
    private View v = null;
    private TextView time,place,latitude,longitude;

    private OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private AMapLocationClientOption mLocationOption;
    private static boolean isfirstAction = false;
    private static boolean isNotifyEmpty = false;

    private ArrayList<MarkerOptions> oldlist = new ArrayList<>();
    private ArrayList<MarkerOptions> newlist = new ArrayList<>();
    private ArrayList<Marker> markers = new ArrayList<>();

    private MyReceiver receiver;
    private IntentFilter filter;

    private Handler mhandler;
    private NotificationCompat.Builder builder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        time = (TextView) view.findViewById(R.id.test_time);
        place = (TextView) view.findViewById(R.id.test_place);
        latitude = (TextView) view.findViewById(R.id.test_latitude);
        longitude = (TextView) view.findViewById(R.id.test_longitude);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
        initView();
    }

    private void initData() {
        mhandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case ADD_MARKERS:
                        addMarker();
                        break;
                    default:
                        break;
                }
            }
        };
        receiver = new MyReceiver();
        filter = new IntentFilter();
        filter.addAction("MYACTION");
    }

    private void initView() {
        if(aMap == null) {
            aMap = mapView.getMap();
        }
        // 设置定位监听
        aMap.setLocationSource(this);
        // 设置默认定位按钮是否显示
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.getUiSettings().setZoomControlsEnabled(false);
        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationEnabled(true);
        //设置小蓝点样式
        aMap.setMyLocationStyle(setupLocationStyle());
        //给maker添加点击事件
        aMap.setOnMarkerClickListener(new OnMarkerClickListeners());
    }

    private MyLocationStyle setupLocationStyle() {
        // 自定义系统定位蓝点
        MyLocationStyle myLocationStyle = new MyLocationStyle();
        // 自定义定位蓝点图标
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.
                fromResource(R.drawable.gps_point));
        // 自定义精度范围的圆形边框颜色
        myLocationStyle.strokeColor(Color.argb(180, 3, 145, 255));
        //自定义精度范围的圆形边框宽度
        myLocationStyle.strokeWidth(5);
        // 设置圆形的填充颜色
        myLocationStyle.radiusFillColor(Color.argb(10, 0, 0, 180));

        return myLocationStyle;

    }

    private void addMarker() {
        if (markers.size() == 0 ) {
            if(newlist.size() != 0) {
                markers = aMap.addMarkers(newlist, false);
                oldlist = newlist;
                notifyNewMarkers(newlist.size(),true);
            }else {
                if(!isNotifyEmpty)
                    notifyNewMarkers(0, false);
            }
        } else
            removeRepeat();
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        // sensorListener = new MySensorListener(getActivity());
        mapView.onResume();
        getActivity().registerReceiver(receiver,filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onStop() {
        super.onStop();
        // sensorListener.disableSensor();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if(mlocationClient != null)
            mlocationClient.onDestroy();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if(mlocationClient == null){
            mlocationClient = new AMapLocationClient(getActivity().getApplicationContext());
            mLocationOption = new AMapLocationClientOption();
            mLocationOption.setInterval(1000);
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
        }

    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation.getErrorCode() == 0) {
                // 显示系统小蓝点
                mListener.onLocationChanged(amapLocation);
                if(amapLocation.getProvince().equals(amapLocation.getCity()))
                    PositionInfo.positionInfo = new PositionInfo(
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date(amapLocation.getTime())),
                            amapLocation.getCity()+amapLocation.getStreet(),
                            amapLocation.getLatitude(),amapLocation.getLongitude());

                else
                    PositionInfo.positionInfo = new PositionInfo(
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(new Date(amapLocation.getTime())),
                            amapLocation.getProvince()+amapLocation.getCity()+amapLocation.getStreet(),
                            amapLocation.getLatitude(),amapLocation.getLongitude());

                PositionInfo.positionInfo.setCity(amapLocation.getCity());
                //可在其中解析amapLocation获取相应内容。
                StringBuffer str = new StringBuffer(256);
//                获取定位类型
//                0
//                定位失败
//                请通过AMapLocation.getErrorCode()方法获取错误码，并参考错误码对照表进行问题排查。
//                1
//                GPS定位结果
//                通过设备GPS定位模块返回的定位结果，精度较高，在10米－100米左右
//                2
//                前次定位结果
//                网络定位请求低于1秒、或两次定位之间设备位置变化非常小时返回，设备位移通过传感器感知。
//                4
//                缓存定位结果
//                        返回一段时间前设备在同样的位置缓存下来的网络定位结果
//                5
//                Wifi定位结果
//                属于网络定位，定位精度相对基站定位会更好，定位精度较高，在5米－200米之间。
//                6
//                基站定位结果
//                纯粹依赖移动、连通、电信等移动网络定位，定位精度在500米-5000米之间。
//                8
//                离线定位结果
                str.append("type:"+amapLocation.getLocationType()+"\n");
                //获取时间
                str.append("time:"+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).
                        format(new Date(amapLocation.getTime()))+"\n");
                //获取纬度
                str.append("latitude:"+amapLocation.getLatitude()+"\n");
                //获取经度
                str.append("longitude:"+amapLocation.getLongitude()+"\n");
                //地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                str.append("address:"+amapLocation.getAddress()+"\n");
                //国家信息
                str.append("country:"+amapLocation.getCountry()+"\n");
                //省信息
                str.append("province:"+amapLocation.getProvince()+"\n");
                //城市信息
                str.append("city:"+amapLocation.getCity()+"\n");
                //城区信息
                str.append("district:"+amapLocation.getDistrict()+"\n");
                //街道信息
                str.append("street:"+amapLocation.getStreet()+"\n");

                //Log.i("IMAGE",str.toString());

                time.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).
                        format(new Date(amapLocation.getTime())));
                place.setText(amapLocation.getAddress());
                latitude.setText(String.valueOf(amapLocation.getLatitude()));
                longitude.setText(String.valueOf(amapLocation.getLongitude()));
                if(!isfirstAction)
                    startServices();
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode()+ ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr",errText);
            }
        }
    }

    private class OnMarkerClickListeners implements AMap.OnMarkerClickListener {
        @Override
        public boolean onMarkerClick(Marker marker) {
            if(!marker.isInfoWindowShown()) {
                marker.showInfoWindow();
            } else if(marker.isInfoWindowShown()){
                marker.hideInfoWindow();
            }
            return true;
        }
    }

    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            newlist = dealBroadCastMsg(bundle.getString("markers"));
            mhandler.sendEmptyMessage(ADD_MARKERS);
        }
    }

    private void startServices(){
        Intent intent = new Intent(getActivity(),HeartBeatService.class);
        getActivity().startService(intent);
    }

    private ArrayList<MarkerOptions> dealBroadCastMsg(String msg){
        JSONObject object;
        JSONArray jsonArray;
        ArrayList<MarkerOptions> list = new ArrayList<>();
        FriendlyTime time = new FriendlyTime();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA);
        try {
            if(JSONObject.fromObject(msg).getString("flag").equals("broadcast")){
                jsonArray = JSONObject.fromObject(msg).getJSONArray("content");
                if(jsonArray.size() !=0) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        object = jsonArray.getJSONObject(i);
                        list.add(new MarkerOptions()
                                .position(new LatLng(object.getDouble("latitude"), object.getDouble("longitude")))
                                .title(object.getString("reason"))
                                .snippet(time.getFriendlyTime(df.parse(object.getString("time")))));
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return list;
    }

    private void removeRepeat() {
        class Holder {
            Set<Integer> same;
            Set<Integer> remove;
            Iterator<MarkerOptions> itOld;
            Iterator<MarkerOptions> itNew;
            int tag = 0;
        }
        Holder holder;
        if(v == null) {
            v = new View(getActivity());
            holder = new Holder();
            holder.same = new HashSet<Integer>();
            holder.remove = new HashSet<Integer>();
            v.setTag(holder);
        }else
            holder = (Holder) v.getTag();

        holder.itOld = oldlist.iterator();
        holder.itNew = newlist.iterator();
        // 1.
        Log.i("removeRepeat", "new: "+newlist.size()+" || old: "+oldlist.size());
        for (int i = 0; i < newlist.size(); i++) {
            for (int j = 0; j < oldlist.size(); j++) {
                if ((oldlist.get(j).getPosition().latitude == newlist.get(i).getPosition().latitude &&
                        oldlist.get(j).getPosition().longitude == newlist.get(i).getPosition().longitude) ||
                        AMapUtils.calculateLineDistance(oldlist.get(j).getPosition(), newlist.get(i).getPosition()) < 500) {
                    holder.same.add(j);
                    holder.remove.add(i);
                }
            }
        }
        //2.
        Log.i("removeRepeat", "same: "+holder.same.size()+" || remove: "+holder.remove.size());
        if(holder.remove.size()>0) {
            int i = -1;
            while(holder.itNew.hasNext()){
                i++;
                holder.itNew.next();
                if(holder.remove.contains(i))
                    holder.itNew.remove();
            }
        }
        // 3.
        Log.i("removeRepeat", "new: "+newlist.size());
        if (holder.same.size() > 0)
            holder.tag = 1;
        else
            holder.tag = 2;

        switch (holder.tag){
            case 1:
                if(newlist.size()>0) {
                    int j = -1;
                    while (holder.itOld.hasNext()) {
                        j++;
                        holder.itOld.next();
                        if (!holder.same.contains(j)) {
                            markers.get(j).remove();
                            markers.remove(j);
                            holder.itOld.remove();
                        }
                    }
                    Log.i("removeRepeat", "增加了");
                    markers.addAll(aMap.addMarkers(newlist, false));
                    oldlist.addAll(newlist);
                    notifyNewMarkers(newlist.size(),true);
                    break;
                }else{
                    if(holder.same.size() == oldlist.size()) {
                        Log.i("removeRepeat", "没有变化");
                        break;
                    }else{
                        int j = -1;
                        while (holder.itOld.hasNext()) {
                            j++;
                            holder.itOld.next();
                            if (!holder.same.contains(j)) {
                                markers.get(j).remove();
                                markers.remove(j);
                                holder.itOld.remove();
                            }
                        }
                        Log.i("removeRepeat", "减少了");
                        break;
                    }
                }
            case 2:{
                for (Marker m:markers) {
                    m.remove();
                }
                markers.clear();
                markers = aMap.addMarkers(newlist,false);
                oldlist = newlist;
                notifyNewMarkers(newlist.size(),true);
                Log.i("removeRepeat", "全部改变");
                break;
            }
            default:
                Log.i("removeRepeat", "未知错误");
                break;
        }

        holder.same.clear();
        holder.remove.clear();
        holder.tag=0;
    }

    private void notifyNewMarkers(int num,boolean empty){
        Intent resultIntent = new Intent();
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        resultIntent.setClass(getActivity(),MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                getActivity(),1,resultIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        if(empty) {
            builder = new NotificationCompat.Builder(getActivity())
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setCategory(Notification.CATEGORY_MESSAGE)
                    .setContentTitle("已发现新的拥堵路段")
                    .setContentText("新发现" + num + "个距离您最近的拥堵路段请及时避让")
                    .setSmallIcon(R.drawable.ic_launcher);
            isNotifyEmpty = false;
        }else{
            builder = new NotificationCompat.Builder(getActivity())
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setCategory(Notification.CATEGORY_MESSAGE)
                    .setContentTitle("未发现新的拥堵路段")
                    .setContentText("附近一路畅通！")
                    .setSmallIcon(R.drawable.ic_launcher);
            isNotifyEmpty = true;
        }
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setFullScreenIntent(resultPendingIntent,true);
        manager.notify(0,builder.build());
    }

}
