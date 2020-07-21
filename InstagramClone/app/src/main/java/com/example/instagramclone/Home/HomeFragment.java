package com.example.instagramclone.Home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.instagramclone.R;
import com.example.instagramclone.Utils.MainfeedListAdapter;
import com.example.instagramclone.models.Comment;
import com.example.instagramclone.models.Photo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HomeFragment extends Fragment
{
    private static final String TAG = "HomeFragment";

    private ArrayList<Photo> mPhotos;
    private ArrayList<Photo> mPaginatedPhotos;
    private ArrayList<String> mFollowing;

    private ListView mListView;
    private MainfeedListAdapter mAdapter;
    private int mResults, totalComments;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        // "Inflating" a view means taking the layout XML and parsing it to create the view and viewGroup objects from the elements and their attributes
        // specified within, and then adding the hierarchy of those views and viewGroups to the parent ViewGroup. You can also inflate views explicitly by
        // using the LayoutInflater. In that case you have to :
            // 1) Get an instance of the LayoutInflater.
            // 2) Specify the XML to inflate.
            // 3) Use the returned View.
            // 4) Set the content view with returned view (above).

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mListView = view.findViewById(R.id.listView1);
        mFollowing = new ArrayList<>();
        mPhotos = new ArrayList<>();

        getFollowing();

        return view;
    }

    // If you keep changing the fragments but remains in the same activity ( i.e all fragments that you went to belong to the one same activity ) then
    // firebase listener will not run again and again. But if you will go the different activity then the listener will get destroy and again on coming back
    // listener will run.
    private void getFollowing()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(getString(R.string.dbname_following)).child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // Log.d(TAG, "onDataChange: found user here in the top level so see it : " + dataSnapshot);

                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                {
                    // Log.d(TAG, "onDataChange: found user: " + singleSnapshot);

                    mFollowing.add(singleSnapshot.child(getString(R.string.field_user_id)).getValue().toString());
                }

                mFollowing.add(FirebaseAuth.getInstance().getCurrentUser().getUid());

                getPhotos();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private void getPhotos()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        for(int i=0; i<mFollowing.size(); i++)
        {
            final int count = i;

            Query query = reference.child(getString(R.string.dbname_user_photos)).child(mFollowing.get(i)).orderByChild(getString(R.string.field_user_id))
                    .equalTo(mFollowing.get(i));

            query.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                    {
                        Photo photo = new Photo();
                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                        photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                        photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                        photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                        totalComments = (int) singleSnapshot.child(getString(R.string.field_comments)).getChildrenCount();

                        ArrayList<Comment> comments = new ArrayList<>();

                        // You can avoid this collecting of comments part as in home page comments are not visible hence we don't need them. When user
                        // clicked the comment button or click on View all x comments then we need the comments.
                        for(DataSnapshot dSnapshot : singleSnapshot.child(getString(R.string.field_comments)).getChildren())
                        {
                            Comment comment = new Comment();

                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                            comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());

                            comments.add(comment);
                        }

                        photo.setComments(comments);
                        mPhotos.add(photo);
                    }

                    if(count >= mFollowing.size() -1)
                    {
                        displayPhotos();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });
        }
    }

    private void displayPhotos()
    {
        // Log.d(TAG,"Displaying photos of all the users whom which we follow");

        mPaginatedPhotos = new ArrayList<>();

        if(mPhotos != null)
        {
            try
            {
                Collections.sort(mPhotos, new Comparator<Photo>()
                {
                    @Override
                    public int compare(Photo photo1, Photo photo2)
                    {
                        return photo2.getDate_created().compareTo(photo1.getDate_created());
                    }
                });

                int iterations = mPhotos.size();

                if(iterations > 10)
                iterations = 10;

                mResults = 10;

                for(int i=0; i<iterations; i++)
                {
                    mPaginatedPhotos.add(mPhotos.get(i));
                }

                // Log.i("All the images here watch it", String.valueOf(mPaginatedPhotos));

                mAdapter = new MainfeedListAdapter(getActivity(), R.layout.layout_mainfeed_listitem, mPaginatedPhotos);
                mListView.setAdapter(mAdapter);
            }
            catch(NullPointerException e)
            {
                Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage() );
            }
            catch(IndexOutOfBoundsException e)
            {
                Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage() );
            }
        }
    }

    public void displayMorePhotos()
    {
        // Log.d(TAG, "displayMorePhotos: displaying more photos");

        try
        {
            if(mPhotos.size() > mResults && mPhotos.size() > 0)
            {
                int iterations;

                if(mPhotos.size() > (mResults + 10))
                {
                    // Log.d(TAG, "displayMorePhotos: there are greater than 10 more photos");
                    iterations = 10;
                }
                else
                {
                    // Log.d(TAG, "displayMorePhotos: there is less than 10 more photos");
                    iterations = mPhotos.size() - mResults;
                }

                // Add the new photos to the paginated results.
                for(int i = mResults; i < mResults + iterations; i++)
                {
                    mPaginatedPhotos.add(mPhotos.get(i));
                }

                mResults = mResults + iterations;
                mAdapter.notifyDataSetChanged();
            }
        }
        catch (NullPointerException e)
        {
            Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage() );
        }
        catch (IndexOutOfBoundsException e)
        {
            Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage() );
        }
    }
}