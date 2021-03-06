package com.quarterlife.detectuserinactivitytest1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.dueeeke.videoplayer.player.VideoView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.quarterlife.detectuserinactivitytest1.Fragment.HomeFragment;
import com.quarterlife.detectuserinactivitytest1.Fragment.SettingFragment;
import com.quarterlife.detectuserinactivitytest1.Fragment.UserFragment;
import com.quarterlife.detectuserinactivitytest1.ViewPager.CustomViewPager;
import com.quarterlife.detectuserinactivitytest1.ViewPager.ViewPagerAdapter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final long DISCONNECT_TIMEOUT = 1 * 60 * 1000; // set inactivity time : 1 minute
    private boolean isFirstOnResume = true; // detect if the application is onResume() for the first time or not
    private static boolean isVideo = true; // connect API and check if AD is video or not ( true -> video / false -> image )
    private static ImageView ad_img;
    private static FrameLayout ad_video;
    private static VideoView ad_videoView;
    public static boolean previousPage = false; // default: no previous page
    public static String PACKAGE_NAME;
    private BottomNavigationView bottomNavigationView;
    private CustomViewPager viewPager;

    //========= onCreate START =========//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get package name
        PACKAGE_NAME = getPackageName();

        // initial View
        ad_img = findViewById(R.id.imageView);
        ad_video = findViewById(R.id.videoLayout);
        ad_videoView = findViewById(R.id.videoView);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        viewPager = findViewById(R.id.fragment_container);
        viewPager.setPagingEnabled(false); // 禁止左右滑動換頁

        // connect API and check AD type
        showAD(); // show AD

        // initial ViewPager
        initViewPager();
    }
    //========= onCreate END =========//

    //========= initial ViewPager START =========//
    private void initViewPager() {
        // create ViewPagerAdapter
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        // set viewPagerAdapter
        viewPager.setAdapter(viewPagerAdapter);
        // create ArrayList<Fragment>
        List<Fragment> list = new ArrayList<>();
        // add Fragment in list
        list.add(new HomeFragment());
        list.add(new UserFragment());
        list.add(new SettingFragment());
        // set list to viewPagerAdapter
        viewPagerAdapter.setList(list);
        // setOnNavigationItemSelectedListener
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
    }
    //========= initial ViewPager END =========//

    //========= set BottomNavigationView START =========//
    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    // set selected fragment
                    switch (menuItem.getItemId()){
                        case R.id.nav_home:
                            viewPager.setCurrentItem(0);
                            break;
                        case R.id.nav_user:
                            viewPager.setCurrentItem(1);
                            break;
                        case R.id.nav_setting:
                            viewPager.setCurrentItem(2);
                            break;
                    }

                    return true;
                }
            };
    //========= set BottomNavigationView END =========//

    //========= create handler START =========//
    private Handler disconnectHandler = new Handler(){
        public void handleMessage(Message msg) {
        }
    };
    //========= create handler END =========//

    //========= create callback START =========//
    private Runnable disconnectCallback = new Runnable() {
        @Override
        public void run() {
            showAD(); // show AD
        }
    };
    //========= create callback END =========//

    //========= show AD START =========//
    public static void showAD() {
        // if HomeFragment's videoView is playing, pause the video
        if(HomeFragment.home_videoView != null && HomeFragment.home_videoView.isPlaying()) HomeFragment.home_videoView.pause(); // pause video

        // set visibility
        if(!isVideo){ // image
            if(ad_img.getVisibility() != View.VISIBLE) ad_img.setVisibility(View.VISIBLE); // show ad_img
            if(ad_video.getVisibility() != View.GONE) ad_video.setVisibility(View.GONE); // hide ad_video
        } else { // video
            if(ad_video.getVisibility() != View.VISIBLE) ad_video.setVisibility(View.VISIBLE); // show ad_video
            if(ad_img.getVisibility() != View.GONE) ad_img.setVisibility(View.GONE); // hide ad_img
        }

        // set content
        if(!isVideo){ // image
            ad_img.setImageResource(R.drawable.ad_image); // set image resource

        } else { // video
//            ad_videoView.setUrl("https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4"); // set video url
            ad_videoView.setUrl("android.resource://" + PACKAGE_NAME + "/" + R.raw.vertical_video); // set local video path
            ad_videoView.start(); // start video
        }
    }
    //========= show AD END =========//

    //========= reset timer START =========//
    public void resetDisconnectTimer(){
        isFirstOnResume = false; // set isFirstLaunch = false

        disconnectHandler.removeCallbacks(disconnectCallback); // remove the old one
        disconnectHandler.postDelayed(disconnectCallback, DISCONNECT_TIMEOUT); // post the new one
    }
    //========= reset timer END =========//

    //========= stop timer START =========//
    public void stopDisconnectTimer(){
        disconnectHandler.removeCallbacks(disconnectCallback); // remove the old one
    }
    //========= stop timer END =========//

    //========= onUserInteraction START =========//
    @Override
    public void onUserInteraction(){ // onUserInteraction: any interaction, including touch, slide, click, etc.
        resetDisconnectTimer(); // if user have any interaction, reset the timer
    }
    //========= onUserInteraction END =========//

    //========= onResume START =========//
    @Override
    public void onResume() {
        super.onResume();

        if(!isFirstOnResume){

            /*  do not reset the timer when the application is onResume() for the first time  */
            if(!previousPage){ // no previous page
                resetDisconnectTimer(); // reset timer
            } else { // from another page
                previousPage = false; // reset previousPage
            }

            /*  do not start video when the application is onResume() for the first time  */
            if(ad_video.getVisibility() == View.VISIBLE){
                ad_videoView.resume(); // resume video
            }
        }
    }
    //========= onResume END =========//

    //========= onPause START =========//
    @Override
    protected void onPause() {
        super.onPause();
        if(ad_videoView != null && ad_video.getVisibility() == View.VISIBLE) ad_videoView.pause(); // pause video
    }
    //========= onPause END =========//

    //========= onDestroy START =========//
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(ad_videoView != null) ad_videoView.release(); // release video
    }
    //========= onDestroy END =========//

    //========= onStop START =========//
    @Override
    public void onStop() {
        super.onStop();
        stopDisconnectTimer(); // stop timer
    }
    //========= onStop END =========//

    //========= onBackPressed START =========//
    @Override
    public void onBackPressed() {
        if (!ad_videoView.onBackPressed()) {
            super.onBackPressed();
        }
    }
    //========= onBackPressed END =========//

    //========= imgGone : hide ad_img START =========//
    public void imgGone(View view) {
        if(ad_img.getVisibility() != View.GONE) ad_img.setVisibility(View.GONE); // hide ad_img
    }
    //========= imgGone : hide ad_img END =========//

    //========= videoGone : hide ad_video START =========//
    public void videoGone(View view) {
        if(ad_videoView != null) ad_videoView.release(); // release video
        if(ad_video.getVisibility() != View.GONE) ad_video.setVisibility(View.GONE); // hide ad_video
        viewPager.setCurrentItem(0); // switch viewPager to HomeFragment

        // set bottomNavigationView selected item : HomeFragment
        bottomNavigationView.setSelectedItemId(R.id.nav_home); // use this
//        bottomNavigationView.getMenu().getItem(0).setChecked(true); // or this, both can work

        HomeFragment.startHomeVideo(); // start HomeFragment video
    }
    //========= videoGone : hide ad_video END =========//
}