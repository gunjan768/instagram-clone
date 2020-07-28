package com.example.instagramclone.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.Home.AddStoryActivity;
import com.example.instagramclone.Home.StoryActivity;
import com.example.instagramclone.R;
import com.example.instagramclone.models.Story;
import com.example.instagramclone.models.UserAccountSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder>
{
    private Context mContext;
    private List<Story> mStory;

    public StoryAdapter(Context mContext, List<Story> mStory)
    {
        this.mContext = mContext;
        this.mStory = mStory;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
    {
        // It is possible to create RecyclerView with multiple view types. Just implement getItemViewType(), and take care of the viewType parameter in
        // onCreateViewHolder().
        if(viewType==0)
        {
            View view = LayoutInflater.from(mContext).inflate(R.layout.add_story_item, viewGroup, false);

            return new ViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(mContext).inflate(R.layout.story_item, viewGroup, false);

            return new ViewHolder(view);
        }
    }

    // viewType is the internal parameter associated to the RecyclerView adapter.
    @Override
    public int getItemViewType(int position)
    {
        if(position == 0)
        {
            return 0;
        }

        return 1;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int position)
    {
        final Story story = mStory.get(position);

        userInfo(viewHolder, story.getUserid(), position);

        if(viewHolder.getAdapterPosition() != 0)
        {
            seenStory(viewHolder, story.getUserid());
        }
        else
        {
            myStory(viewHolder.addStoryText, viewHolder.story_plus, false);
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(viewHolder.getAdapterPosition() == 0)
                {
                    myStory(viewHolder.addStoryText, viewHolder.story_photo, true);
                }
                else
                {
                    Intent intent = new Intent(mContext, StoryActivity.class);
                    intent.putExtra("userid", story.getUserid());

                    mContext.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return mStory.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public ImageView story_photo, story_plus, story_photo_seen;
        public TextView story_username, addStoryText;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);

            story_photo = itemView.findViewById(R.id.story_photo);
            story_username = itemView.findViewById(R.id.story_username);
            story_plus = itemView.findViewById(R.id.story_plus);
            addStoryText = itemView.findViewById(R.id.addstory_text);
            story_photo_seen = itemView.findViewById(R.id.story_photo_seen);
        }
    }

    private void userInfo(final ViewHolder viewHolder, String userId, final int position)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("user_account_settings").child(userId);

        reference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                UserAccountSettings userAccountSettings = dataSnapshot.getValue(UserAccountSettings.class);

                assert userAccountSettings != null;
                Glide.with(mContext).load(userAccountSettings.getProfile_photo()).into(viewHolder.story_photo);

                if(position != 0)
                {
                    Glide.with(mContext).load(userAccountSettings.getProfile_photo()).into(viewHolder.story_photo_seen);

                    viewHolder.story_username.setText(userAccountSettings.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void myStory(final TextView textView, final ImageView imageView, final boolean click)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        reference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                int count = 0;
                long timeCurrent = System.currentTimeMillis();

                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Story story = snapshot.getValue(Story.class);

                    assert story != null;
                    if(timeCurrent > story.getTimestart() && timeCurrent < story.getTimeend())
                    {
                        count++;
                    }
                }

                if(click)
                {
                    if(count > 0)
                    {
                        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();

                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "View Story", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                Intent intent = new Intent(mContext, StoryActivity.class);
                                intent.putExtra("userid", FirebaseAuth.getInstance().getCurrentUser().getUid());

                                mContext.startActivity(intent);
                                dialog.dismiss();
                            }
                        });

                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Story", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                Intent intent = new Intent(mContext, AddStoryActivity.class);
                                mContext.startActivity(intent);

                                dialog.dismiss();
                            }
                        });

                        alertDialog.show();
                    }
                    else
                    {
                        Intent intent = new Intent(mContext, AddStoryActivity.class);

                        mContext.startActivity(intent);
                    }
                }
                else
                {
                    if(count > 0)
                    {
                        textView.setText("My story");
                        imageView.setVisibility(View.GONE);
                    }
                    else
                    {
                        textView.setText("Add story");
                        imageView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void seenStory(final ViewHolder viewHolder, String userId)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(userId);

        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                int i = 0;

                for(DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    if(!snapshot.child("views").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).exists() &&
                            System.currentTimeMillis() < snapshot.getValue(Story.class).getTimeend()
                    ) {
                        i++;
                    }
                }

                if(i>0)
                {
                    viewHolder.story_photo.setVisibility(View.VISIBLE);
                    viewHolder.story_photo_seen.setVisibility(View.GONE);
                }
                else
                {
                    viewHolder.story_photo.setVisibility(View.GONE);
                    viewHolder.story_photo_seen.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }
}