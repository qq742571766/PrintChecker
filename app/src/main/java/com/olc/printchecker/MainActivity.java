package com.olc.printchecker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;

import hardware.print.printer;
import viewpagerindicator.TabPageIndicator;

public class MainActivity extends FragmentActivity implements View.OnClickListener {

    private String[] mTabArrays;

    private ViewPager mViewPager;
    private ArrayList<Fragment> mFragmentList;
    private MyFragmentPagerAdapter mMyFrageStatePagerAdapter;

    private static boolean isOutPaper = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTabArrays = new String[]{getResources().getString(R.string.check_title),
                getResources().getString(R.string.scan_title),
                getResources().getString(R.string.web_title)
        };

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("PrintChecker");
        toolbar.setTitleTextColor(Color.WHITE);
        ImageButton helpButton = (ImageButton) findViewById(R.id.btn_help);
        helpButton.setOnClickListener(this);
        TabPageIndicator tabPageIndicator = (TabPageIndicator) findViewById(R.id.indicator);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        mFragmentList = new ArrayList<Fragment>();
        mFragmentList.add(new PrintCheckFragment());
        mFragmentList.add(new ScanPrintFragment());
        mFragmentList.add(new WebPrintFragment());
        mMyFrageStatePagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), mFragmentList);
        mViewPager.setAdapter(mMyFrageStatePagerAdapter);
        tabPageIndicator.setViewPager(mViewPager);
        mViewPager.setCurrentItem(1);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_help:
                Intent helpIntent = new Intent(MainActivity.this, HelpActivity.class);
                startActivity(helpIntent);
                break;
        }
    }

    class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        ArrayList<Fragment> list;

        public MyFragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment> list) {
            super(fm);
            this.list = list;
        }


        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Fragment getItem(int arg0) {
            return list.get(arg0);
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return mTabArrays[position];
        }

        @Override
        public float getPageWidth(int position) {
            return super.getPageWidth(position);
        }
    }
}
