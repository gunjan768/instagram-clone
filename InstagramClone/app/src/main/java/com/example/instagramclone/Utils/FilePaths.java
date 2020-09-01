package com.example.instagramclone.Utils;

import android.os.Environment;

import com.example.instagramclone.Share.ShareActivity;

import java.util.ArrayList;

public class FilePaths
{
    // "storage/emulated/0"
    // public String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();

    public String ROOT_DIR = ShareActivity.ROOT_DIRECTORY;
    public String PICTURES = ROOT_DIR + "/Pictures";
    public ArrayList<String>DIRECTORIES_INSIDE_DCIM = ShareActivity.DIRECTORIES_INSIDE_DCIM;

    // public String CAMERA = ROOT_DIR + "/DCIM/Camera";

    public String FIREBASE_IMAGE_STORAGE = "photos/users/";
}