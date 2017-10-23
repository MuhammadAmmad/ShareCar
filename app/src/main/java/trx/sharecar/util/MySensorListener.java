package trx.sharecar.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

public class MySensorListener implements SensorEventListener {
    private final String TAG = "MySensorListener";
    private SensorManager mSensorManager;
    private Sensor sensor;
    private Context mContext;

    public MySensorListener(Context context){
        mContext = context;
        enableSensor();
    }

    // 注册传感器服务，在本java和Activity里面都要注册，但是取消注册的时候，只在activity里面取消注册即可。
    public void enableSensor() {
        // 在这里真正注册Service服务
        mSensorManager = (SensorManager) mContext
                .getSystemService(Context.SENSOR_SERVICE);
        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        if (mSensorManager == null) {
            Log.v("sensor..", "Sensors not supported");
        }

        mSensorManager.registerListener(this, sensor,
                SensorManager.SENSOR_DELAY_NORMAL);

    }

    // 取消注册传感器服务
    public void disableSensor() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
            mSensorManager = null;
        }
    }

    public static int getScreenRotationOnPhone(Context context) {
        final Display display = ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        switch (display.getRotation()) {
            case Surface.ROTATION_0:
                return 0;

            case Surface.ROTATION_90:
                return 90;

            case Surface.ROTATION_180:
                return 180;

            case Surface.ROTATION_270:
                return -90;
        }
        return 0;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float a = event.values[0];
        float b = event.values[1];
        float c = event.values[2];
        Log.i(TAG, "onSensorChanged: "+a+"||"+b+"||"+c);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
