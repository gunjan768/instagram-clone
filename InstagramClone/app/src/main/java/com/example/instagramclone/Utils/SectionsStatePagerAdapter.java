package com.example.instagramclone.Utils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// FragmentStatePagerAdapter is used to store the fragments and display them dynamically using ViewPager as per request. It is used in AccountSettingsActivity.
public class SectionsStatePagerAdapter extends FragmentStatePagerAdapter
{
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final HashMap<Fragment, Integer> mFragments = new HashMap<>();
    private final HashMap<String, Integer> mFragmentNumbers = new HashMap<>();
    private final HashMap<Integer, String> mFragmentNames = new HashMap<>();

    public SectionsStatePagerAdapter(@NonNull FragmentManager fm, int behavior)
    {
        super(fm, behavior);
    }

    @Override
    public Fragment getItem(int position)
    {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount()
    {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment, String fragmentName)
    {
        mFragmentList.add(fragment);

        mFragments.put(fragment, mFragmentList.size()-1);
        mFragmentNumbers.put(fragmentName, mFragmentList.size()-1);
        mFragmentNames.put(mFragmentList.size()-1, fragmentName);
    }

    /**
     * returns the fragment with the name @param
     * @param fragmentName
     * @return
     */
    public Integer getFragmentNumber(String fragmentName)
    {
        if(mFragmentNumbers.containsKey(fragmentName))
        {
            return mFragmentNumbers.get(fragmentName);
        }
        else
        {
            return null;
        }
    }


    /**
     * returns the fragment with the name @param
     * @param fragment
     * @return
     */
    public Integer getFragmentNumber(Fragment fragment)
    {
        if(mFragmentNumbers.containsKey(fragment))
        {
            return mFragmentNumbers.get(fragment);
        }
        else
        {
            return null;
        }
    }

    /**
     * returns the fragment with the name @param
     * @param fragmentNumber
     * @return
     */
    public String getFragmentName(Integer fragmentNumber)
    {
        if(mFragmentNames.containsKey(fragmentNumber))
        {
            return mFragmentNames.get(fragmentNumber);
        }
        else
        {
            return null;
        }
    }
}