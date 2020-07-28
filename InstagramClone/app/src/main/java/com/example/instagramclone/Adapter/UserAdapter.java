package com.example.instagramclone.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.instagramclone.Profile.ProfileActivity;
import com.example.instagramclone.R;
import com.example.instagramclone.models.UserAccountSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ImageViewHolder>
{
    private Context mContext;
    private List<UserAccountSettings> mUsers;
    private boolean isFragment;

    private FirebaseUser firebaseUser;

    public UserAdapter(Context context, List<UserAccountSettings> users, boolean isFragment)
    {
        mContext = context;
        mUsers = users;
        this.isFragment = isFragment;
    }

    @NonNull
    @Override
    public UserAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);

        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserAdapter.ImageViewHolder holder, final int position)
    {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final UserAccountSettings userAccountSettings = mUsers.get(position);

        holder.btn_follow.setVisibility(View.VISIBLE);

        // See whether you are following the user ( which is one of the viewers who had seen your stories ) or not.
        isFollowing(userAccountSettings.getUser_id(), holder.btn_follow);

        holder.username.setText(userAccountSettings.getUsername());
        holder.fullname.setText(userAccountSettings.getDisplay_name());
        Glide.with(mContext).load(userAccountSettings.getProfile_photo()).into(holder.image_profile);

        if(userAccountSettings.getUser_id().equals(firebaseUser.getUid()))
        {
            holder.btn_follow.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
//                if(isFragment)
//                {
//                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", MODE_PRIVATE).edit();
//                    editor.putString("profileid", userAccountSettings.getUser_id());
//                    editor.apply();
//
//                    ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
//                            new ProfileFragment()).commit();
//                }
//                else
                //{
                    Intent intent = new Intent(mContext, ProfileActivity.class);
                    intent.putExtra("calling_activity", "story_following_user");
                    intent.putExtra("user_following_id", userAccountSettings.getUser_id());

                    mContext.startActivity(intent);
                //}
            }
        });

        holder.btn_follow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(holder.btn_follow.getText().toString().equals("follow"))
                {
                    FirebaseDatabase.getInstance().getReference().child("following")
                            .child(firebaseUser.getUid())
                            .child(userAccountSettings.getUser_id())
                            .child("user_id")
                            .setValue(userAccountSettings.getUser_id());

                    FirebaseDatabase.getInstance().getReference().child("followers")
                            .child(userAccountSettings.getUser_id())
                            .child(firebaseUser.getUid())
                            .child("user_id")
                            .setValue(firebaseUser.getUid());

//                    addNotification(userAccountSettings.getUser_id());
                }
                else
                {
                    FirebaseDatabase.getInstance().getReference().child("following")
                            .child(firebaseUser.getUid())
                            .child(userAccountSettings.getUser_id())
                            .removeValue();

                    FirebaseDatabase.getInstance().getReference().child("followers")
                            .child(userAccountSettings.getUser_id())
                            .child(firebaseUser.getUid())
                            .removeValue();
                }
            }
        });
    }

//    private void addNotification(String userid)
//    {
//        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userid);
//
//        HashMap<String, Object> hashMap = new HashMap<>();
//        hashMap.put("userid", firebaseUser.getUid());
//        hashMap.put("text", "started following you");
//        hashMap.put("postid", "");
//        hashMap.put("ispost", false);
//
//        reference.push().setValue(hashMap);
//    }

    @Override
    public int getItemCount()
    {
        return mUsers.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView username;
        public TextView fullname;
        public CircleImageView image_profile;
        public Button btn_follow;

        public ImageViewHolder(View itemView)
        {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            fullname = itemView.findViewById(R.id.fullname);
            image_profile = itemView.findViewById(R.id.image_profile);
            btn_follow = itemView.findViewById(R.id.btn_follow);
        }
    }

    private void isFollowing(final String userId, final Button button)
    {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        assert firebaseUser != null;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("following")
                .child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.child(userId).exists())
                {
                    button.setText("following");
                }
                else
                {
                    button.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }
}