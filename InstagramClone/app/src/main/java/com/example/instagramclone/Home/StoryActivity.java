package com.example.instagramclone.Home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.instagramclone.R;
import com.example.instagramclone.models.Story;
import com.example.instagramclone.models.UserAccountSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener
{
    int counter = 0;
    long pressTime = 0L;
    long limit = 500L;

    StoriesProgressView storiesProgressView;
    ImageView image, story_photo;
    TextView story_username;

    LinearLayout r_seen;
    TextView seen_number;
    ImageView story_delete;

    List<String> images;
    List<String> storyIds;
    String userId;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener()
    {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            switch(event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();

                    return false;

                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();

                    return limit < now - pressTime;
            }

            return false;
        }
    };

    private void setInitialConfiguration()
    {
        storiesProgressView = findViewById(R.id.stories);
        image = findViewById(R.id.image);
        story_photo = findViewById(R.id.story_photo);
        story_username = findViewById(R.id.story_username);

        r_seen = findViewById(R.id.r_seen);
        seen_number = findViewById(R.id.seen_number);
        story_delete = findViewById(R.id.story_delete);

        r_seen.setVisibility(View.GONE);
        story_delete.setVisibility(View.GONE);

        userId = getIntent().getStringExtra("userid");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        setInitialConfiguration();

        // If we are seeing our own story then we can see the number of viewers and can also delete the story.
        assert userId != null;
        if(userId.equals(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()))
        {
            r_seen.setVisibility(View.VISIBLE);
            story_delete.setVisibility(View.VISIBLE);
        }

        getStories(userId);
        userInfo(userId);

        View reverse = findViewById(R.id.reverse);

        reverse.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                storiesProgressView.reverse();
            }
        });

        reverse.setOnTouchListener(onTouchListener);

        View skip = findViewById(R.id.skip);

        skip.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                storiesProgressView.skip();
            }
        });

        skip.setOnTouchListener(onTouchListener);

        r_seen.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(StoryActivity.this, FollowersActivity.class);

                intent.putExtra("user_id", userId);
                intent.putExtra("story_id", storyIds.get(counter));
                intent.putExtra("title", "views");

                startActivity(intent);
            }
        });

        story_delete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(userId).child(storyIds.get(counter));

                reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(StoryActivity.this, "Deleted!", Toast.LENGTH_SHORT).show();

                            finish();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onNext()
    {
        Glide.with(getApplicationContext()).load(images.get(++counter)).into(image);

        addView(storyIds.get(counter));
        seenNumber(storyIds.get(counter));
    }

    @Override
    public void onPrev()
    {
        if(counter < 1)
        return;

        Glide.with(getApplicationContext()).load(images.get(--counter)).into(image);

        seenNumber(storyIds.get(counter));
    }

    @Override
    public void onComplete()
    {
        finish();
    }

    @Override
    protected void onDestroy()
    {
        storiesProgressView.destroy();

        super.onDestroy();
    }

    @Override
    protected void onPause()
    {
        storiesProgressView.pause();

        super.onPause();
    }

    @Override
    protected void onResume()
    {
        storiesProgressView.resume();

        super.onResume();
    }

    private void getStories(String userId)
    {
        images = new ArrayList<>();
        storyIds = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story")
                .child(userId);

        reference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                images.clear();
                storyIds.clear();

                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Story story = snapshot.getValue(Story.class);
                    long timeCurrent = System.currentTimeMillis();

                    assert story != null;
                    if(timeCurrent >= story.getTimestart() && timeCurrent <= story.getTimeend())
                    {
                        images.add(story.getImageurl());
                        storyIds.add(story.getStoryid());
                    }
                }

                storiesProgressView.setStoriesCount(images.size());
                storiesProgressView.setStoryDuration(5000L);
                storiesProgressView.setStoriesListener(StoryActivity.this);
                storiesProgressView.startStories(counter);

                Glide.with(getApplicationContext()).load(images.get(counter)).into(image);

                addView(storyIds.get(counter));
                seenNumber(storyIds.get(counter));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void userInfo(String userId)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(getString(R.string.dbname_user_account_settings)).child(userId);

        reference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                UserAccountSettings userAccountSettings = dataSnapshot.getValue(UserAccountSettings.class);

                assert userAccountSettings != null;
                Glide.with(getApplicationContext()).load(userAccountSettings.getProfile_photo()).into(story_photo);
                story_username.setText(userAccountSettings.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    // As you viewed the current story of the user whose user-id is userId hence we need to add the name of the current user to the views list.
    private void addView(String storyId)
    {
        // userId is the id of the user whose stories the current user is seeing.
        FirebaseDatabase.getInstance().getReference().child("Story").child(userId)
                .child(storyId).child("views").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);
    }

    private void seenNumber(String storyId)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(userId).child(storyId).child("views");

        reference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                seen_number.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }
}