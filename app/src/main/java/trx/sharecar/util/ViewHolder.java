package trx.sharecar.util;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ViewHolder {
    private final String TAG = "ViewHodler";

    private SparseArray<View> mViews;
    private View mRootView;

    public ViewHolder(LayoutInflater inflater, ViewGroup parent, int layoutId) {
        this.mViews = new SparseArray<View>();
        mRootView = inflater.inflate(layoutId, parent, false);
    }

    public <T extends View> T get(int resId) {
        View view = mViews.get(resId);
        //如果该View没有缓存过，则查找View并缓存
        if (view == null) {
            view = mRootView.findViewById(resId);
            mViews.put(resId, view);
        }
        if (view == null){
            Log.e(TAG, "View == null");
        }
        return (T) view;
    }

    public View getRootView() {
        return mRootView;
    }

    public boolean setText(CharSequence text, @NonNull int res_id) {
        try {
            TextView textView = get(res_id);
            textView.setText(text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean setText(@NonNull int res_id, CharSequence text) {
        return setText(text, res_id);
    }

    public void setOnClickListener(View.OnClickListener l, int... ids) {
        if (ids == null) {
            return;
        }
        for (int id : ids) {
            get(id).setOnClickListener(l);
        }
    }


}
