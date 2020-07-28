package com.example.instagramclone.Home;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagramclone.Adapter.UserAdapter;
import com.example.instagramclone.R;
import com.example.instagramclone.models.UserAccountSettings;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

public class FollowersActivity extends AppCompatActivity
{
    String userId;
    String title;

    private TreeMap<String,Boolean>idList;

    RecyclerView recyclerView;
    UserAdapter userAdapter;
    List<UserAccountSettings> userList;

    private void setToolBarLayout()
    {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                finish();
            }
        });
    }

    private void setRecyclerView()
    {
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(this, userList, false);
        recyclerView.setAdapter(userAdapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        Intent intent = getIntent();
        userId = intent.getStringExtra("user_id");
        title = intent.getStringExtra("title");

        setToolBarLayout();
        setRecyclerView();

        idList = new TreeMap<>();

        switch(title)
        {
//            case "likes":
//                getLikes();
//
//                break;
//
//            case "following":
//                getFollowing();
//
//                break;
//
//            case "followers":
//                getFollowers();
//
//                break;

            case "views":
                getViews();

                break;
        }
    }

    private void getViews()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(userId).child(Objects.requireNonNull(getIntent().getStringExtra("story_id"))).child("views");

        reference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                idList.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    idList.put(snapshot.getKey(), true);
                }

                showUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

//    private void getFollowers()
//    {
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow").child(userId).child("followers");
//
//        reference.addListenerForSingleValueEvent(new ValueEventListener()
//        {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//            {
//                idList.clear();
//
//                for(DataSnapshot snapshot : dataSnapshot.getChildren())
//                {
//                    idList.add(snapshot.getKey());
//                }
//
//                showUsers();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError)
//            {
//
//            }
//        });
//    }
//
//    private void getFollowing()
//    {
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow").child(userId).child("following");
//
//        reference.addListenerForSingleValueEvent(new ValueEventListener()
//        {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//            {
//                idList.clear();
//
//                for(DataSnapshot snapshot : dataSnapshot.getChildren())
//                {
//                    idList.add(snapshot.getKey());
//                }
//
//                showUsers();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError)
//            {
//
//            }
//        });
//    }
//
//    private void getLikes()
//    {
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Likes").child(userId);
//
//        reference.addListenerForSingleValueEvent(new ValueEventListener()
//        {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//            {
//                idList.clear();
//
//                for(DataSnapshot snapshot : dataSnapshot.getChildren())
//                {
//                    idList.add(snapshot.getKey());
//                }
//
//                showUsers();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError)
//            {
//
//            }
//        });
//    }

    private void showUsers()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(getString(R.string.dbname_user_account_settings));

        reference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                userList.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    UserAccountSettings userAccountSettings = snapshot.getValue(UserAccountSettings.class);

                    assert userAccountSettings != null;
                    if(idList.containsKey(userAccountSettings.getUser_id()))
                    {
                        userList.add(userAccountSettings);
                    }
                }

                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }
}