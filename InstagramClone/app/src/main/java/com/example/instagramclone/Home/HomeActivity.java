package com.example.instagramclone.Home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.ToxicBakery.viewpager.transforms.AccordionTransformer;
import com.ToxicBakery.viewpager.transforms.RotateUpTransformer;
import com.example.instagramclone.Login.LoginActivity;
import com.example.instagramclone.R;
import com.example.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.instagramclone.Utils.MainfeedListAdapter;
import com.example.instagramclone.Utils.SectionsPagerAdapter;
import com.example.instagramclone.Utils.UniversalImageLoader;
import com.example.instagramclone.Utils.ViewCommentsFragment;
import com.example.instagramclone.models.Photo;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity implements MainfeedListAdapter.OnLoadMoreItemsListener
{
    @Override
    public void onLoadMoreItems()
    {
        HomeFragment fragment = (HomeFragment)getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" + mViewPager.getCurrentItem());

        if(fragment != null)
        {
            // Calling displayMorePhotos() defined in HomeFragment.java.
            fragment.displayMorePhotos();
        }
    }

    private static final String TAG = "HomeActivity";
    private static final int ACTIVITY_NUM = 0;
    private static final int HOME_FRAGMENT = 1;

    private Context mContext = HomeActivity.this;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ViewPager mViewPager;
    private FrameLayout mFrameLayout;
    private RelativeLayout mRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mViewPager = findViewById(R.id.viewpager_container);
        mFrameLayout = findViewById(R.id.frame_container);
        mRelativeLayout = findViewById(R.id.relLayoutParent);

        setupFirebaseAuth();

        initImageLoader();
        setupBottomNavigationView();
        setupViewPager();
    }

    public void onCommentThreadSelected(Photo photo, String callingActivity)
    {
        // Log.d(TAG, "onCommentThreadSelected: selected a comment thread");

        ViewCommentsFragment fragment = new ViewCommentsFragment();

        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        args.putString(getString(R.string.home_activity), getString(R.string.home_activity));

        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_container, fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();
    }

    public void hideLayout()
    {
        mRelativeLayout.setVisibility(View.GONE);
        mFrameLayout.setVisibility(View.VISIBLE);
    }

    public void showLayout()
    {
        mRelativeLayout.setVisibility(View.VISIBLE);
        mFrameLayout.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();

        if(mFrameLayout.getVisibility() == View.VISIBLE)
        {
            showLayout();
        }
    }

    private void initImageLoader()
    {
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    // Responsible for adding the 3 tabs: Camera, Home, Messages.
    private void setupViewPager()
    {
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getSupportFragmentManager(), 1);

        adapter.addFragment(new CameraFragment());      //index 0
        adapter.addFragment(new HomeFragment());        //index 1
        adapter.addFragment(new MessagesFragment());    //index 2

        // The ViewPager is the widget that allows the user to swipe left or right to see an entirely new screen. In a sense, it's just a nicer way to
        // show the user multiple tabs. It also has the ability to dynamically add and remove pages (or tabs) at anytime. It is used in tob tabs.
        mViewPager.setAdapter(adapter);

        // We can customize how the pages animate as they are being swiped between using the PageTransformer.
        mViewPager.setPageTransformer(true, new RotateUpTransformer());

        // TabLayout provides a horizontal layout to display tabs. To slide left or right use ViewPager.
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_camera).setText(getString(R.string.cameraTag));
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_action_name);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_arrow);

        // To change the color of the selected tab's text color please see layout_top_tabs.xml file.
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                ColorFilter colorFilter = new PorterDuffColorFilter(Color.BLUE, PorterDuff.Mode.SRC_IN);

                Objects.requireNonNull(tab.getIcon()).setColorFilter(colorFilter);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {
                Objects.requireNonNull(tab.getIcon()).clearColorFilter();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab)
            {

            }
        });
    }

    // BottomNavigationView setup.
    private void setupBottomNavigationView()
    {
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);

        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();

        // Gets the menu item at the given index.
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

    // ************************************************************* Firebase starts ***********************************************************************

    private void checkCurrentUser(FirebaseUser user)
    {
         // Log.d(TAG, "checkCurrentUser: checking if user is logged in" + user);

        if(user == null)
        {
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivity(intent);
        }
        else
        {
            mViewPager.setCurrentItem(HOME_FRAGMENT);
        }
    }

    // Setup the firebase auth object.
    private void setupFirebaseAuth()
    {
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                //check if the user is logged in.
                checkCurrentUser(user);

                if(user != null)
                {
                    // Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                }
                else
                {
                    // Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    public void onStart()
    {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);

        checkCurrentUser(mAuth.getCurrentUser());
    }

    @Override
    public void onStop()
    {
        super.onStop();

        if(mAuthListener != null)
        {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    // ************************************************************* Firebase ends *************************************************************************
}