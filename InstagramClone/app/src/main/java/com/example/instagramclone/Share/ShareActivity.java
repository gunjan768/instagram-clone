package com.example.instagramclone.Share;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.example.instagramclone.R;
import com.example.instagramclone.Utils.BottomNavigationViewHelper;
import com.example.instagramclone.Utils.Permissions;
import com.example.instagramclone.Utils.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class ShareActivity extends AppCompatActivity
{
    private static final String TAG = "ShareActivity";

    private static final int ACTIVITY_NUM = 2;
    private static final int VERIFY_PERMISSIONS_REQUEST = 1;
    public static String ROOT_DIRECTORY = "";

    public static ArrayList<String>DIRECTORIES_INSIDE_DCIM;
    private ViewPager mViewPager;
    private Context mContext = ShareActivity.this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        if(!checkPermissionsArray(Permissions.PERMISSIONS))
        {
            verifyPermissions(Permissions.PERMISSIONS);
        }

        DIRECTORIES_INSIDE_DCIM = new ArrayList<>();

        getRootDirectoryPathAndFile();
        setupViewPager();
    }

    private void getRootDirectoryPathAndFile()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        // context.getExternalFilesDirs() will always returns application-specific directory. But the good thing is that application-specific directories
        // are always 4 level deep from the root folder of the storage device. So calling getParentFile() four times on the File f instead of
        // f.getAbsolutePath() will get you the root path of your phone's removable storage. Use getExternalFilesDir(), getExternalCacheDir(), or
        // getExternalMediaDirs() (methods on Context) instead of Environment.getExternalStorageDirectory() as Environment.getExternalStorageDirectory() is
        // deprecated in API level 29.

        // Log.d(TAG, "onCreate: " + Arrays.toString(getExternalFilesDirs(Environment.DIRECTORY_PICTURES)));
        // Log.d(TAG, "onCreate: " + Environment.getExternalStorageDirectory().getAbsolutePath());
        // Log.d(TAG, "onCreate: " + Environment.getExternalStorageState());

        // String[] folders = fileList();
        // Log.d(TAG, "getRootDirectoryPath: " + Arrays.toString(folders));

        for(File file : getExternalFilesDirs(Environment.DIRECTORY_PICTURES))
        {
            // Log.d(TAG, file.getParentFile().getParentFile().getParentFile().getParentFile().getParent());

            ROOT_DIRECTORY = Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(Objects.requireNonNull(file.getParentFile())
                    .getParentFile()).getParentFile()).getParentFile()).getParent();

            // We will break the process and will run it for once only as we only want the picture directory of Internal Storage and not of the external
            // SD card ( memory card ). If you want let the process to be continued further.
            break;
        }

        // In Android Q If you giving permission and its granted or not working so go to Manifest and add  android:requestLegacyExternalStorage="true" in
        // application tag. If you are listing files of the given directory then you have to add the above mentioned line.
        assert ROOT_DIRECTORY != null;
        File directory = new File(ROOT_DIRECTORY + "/DCIM");
        File[] files = directory.listFiles();

        // getFilesDir() will return the absolute path to the directory on the filesystem where files created with openFileOutput(String, int) are stored. The
        // returned path may change over time if the calling app is moved to an adopted storage device, so only relative paths should be persisted. No additional
        // permissions are required for the calling app to read or write files under the returned path
        // File[] listOfFiles = getFilesDir().listFiles();
        // Log.d("Files", "Size: "+ Arrays.toString(listOfFiles));

        assert files != null;
        for(File file : files)
        {
            // Log.d("Files", "FileName of each and every individual file in the DCIM directory: " + file.getAbsolutePath());

            DIRECTORIES_INSIDE_DCIM.add(file.getAbsolutePath());
        }
    }

    public int getCurrentTabNumber()
    {
        return mViewPager.getCurrentItem();
    }

    // Set the viewPage for managing the tabs.
    private void setupViewPager()
    {
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), 1);

        sectionsPagerAdapter.addFragment(new GalleryFragment());
        sectionsPagerAdapter.addFragment(new PhotoFragment());

        mViewPager = findViewById(R.id.viewpager_container);
        mViewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabsBottom);
        tabLayout.setupWithViewPager(mViewPager);

        Objects.requireNonNull(tabLayout.getTabAt(0)).setText(getString(R.string.gallery));
        Objects.requireNonNull(tabLayout.getTabAt(1)).setText(getString(R.string.photo));
    }

    public int getTask()
    {
        // Log.d(TAG, "getTask: TASK: " + getIntent().getFlags());
        return getIntent().getFlags();
    }

    // Verify all the permissions passed to the array.
    public void verifyPermissions(String[] permissions)
    {
        // Log.d(TAG, "verifyPermissions: verifying permissions.");

        ActivityCompat.requestPermissions(ShareActivity.this, permissions, VERIFY_PERMISSIONS_REQUEST);
    }

    // Check an array of permissions.
    public boolean checkPermissionsArray(String[] permissions)
    {
        // Log.d(TAG, "checkPermissionsArray: checking permissions array.");

        for(String check : permissions)
        {
            if(!checkPermissions(check))
            {
                return false;
            }
        }

        return true;
    }

    // Check a single permission has been verified.
    public boolean checkPermissions(String permission)
    {
        int permissionRequest = ActivityCompat.checkSelfPermission(ShareActivity.this, permission);

        // Log.d(TAG, "checkPermissions: \n Permission was granted for: " + permission);
        return permissionRequest == PackageManager.PERMISSION_GRANTED;
    }

    // BottomNavigationView setup.
    private void setupBottomNavigationView()
    {
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavViewBar);

        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationViewEx);

        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}