package trx.sharecar.activity;


import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import trx.sharecar.activity.base.BaseActivity;
import trx.sharecar.util.ViewHolder;
import trx.sharecar.custom.BanScrollViewPager;
import trx.sharecar.fragment.MapFragment;
import trx.sharecar.fragment.SearchFragment;
import trx.sharecar.fragment.SendFragment;
import trx.sharecar.R;


public class MainActivity extends BaseActivity {

    private TabLayout tabLayout;

    private List<Fragment> fragments = null;
    private BanScrollViewPager viewPager;
    private FragmentPagerAdapter adapter;
    private MapFragment mapFragment = new MapFragment();
    private SendFragment sendFragment = new SendFragment();
    private SearchFragment searchFragment = new SearchFragment();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initDatas() {
        fragments = new ArrayList<>();
        fragments.add(mapFragment);
        fragments.add(sendFragment);
        fragments.add(searchFragment);
        adapter = new PagerAdapter(getSupportFragmentManager(),fragments);
    }

    @Override
    protected void initViews(ViewHolder holder, View root) {
        tabLayout = holder.get(R.id.tab_tabs);
        viewPager = holder.get(R.id.viewpager);

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
        viewPager.setOffscreenPageLimit(2);

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                if(position == 1)
                    sendFragment.changeTimeAndPosition();
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    private class PagerAdapter extends FragmentPagerAdapter{
        private List<Fragment> fragments = null;
        private String[] tabtext = {"首页","上传","搜索"};

        public PagerAdapter(FragmentManager fm,List<Fragment> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            SpannableString sb = new SpannableString(tabtext[position]);
            return sb;
        }
    }

}
