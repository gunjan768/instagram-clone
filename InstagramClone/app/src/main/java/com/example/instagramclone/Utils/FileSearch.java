package com.example.instagramclone.Utils;


import java.io.File;
import java.util.ArrayList;

public class FileSearch
{
    // Search a directory and return a list of all **directories** contained inside.
    public static ArrayList<String> getDirectoryPaths(String directory)
    {
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFiles = file.listFiles();

        try
        {
            assert listFiles != null;

            for(File listFile : listFiles)
            {
                if(listFile.isDirectory())
                {
                    pathArray.add(listFile.getAbsolutePath());
                }
            }
        }
        catch(NullPointerException ignored)
        {

        }

        return pathArray;
    }

    // Search a directory and return a list of all **files** contained inside.
    public static ArrayList<String> getFilePaths(String directory)
    {
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listFiles = file.listFiles();

        try
        {
            assert listFiles != null;

            for(File listFile : listFiles)
            {
                if(listFile.isFile())
                {
                    pathArray.add(listFile.getAbsolutePath());
                }
            }
        }
        catch(Exception ignored)
        {

        }

        return pathArray;
    }
}