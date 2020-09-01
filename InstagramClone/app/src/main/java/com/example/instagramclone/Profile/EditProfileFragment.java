package com.example.instagramclone.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.instagramclone.R;
import com.example.instagramclone.Share.ShareActivity;
import com.example.instagramclone.Utils.FirebaseMethods;
import com.example.instagramclone.Utils.UniversalImageLoader;
import com.example.instagramclone.dialogs.ConfirmPasswordDialog;
import com.example.instagramclone.models.User;
import com.example.instagramclone.models.UserAccountSettings;
import com.example.instagramclone.models.UserSettings;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class EditProfileFragment extends Fragment implements
        ConfirmPasswordDialog.OnConfirmPasswordListener{

    private static final String TAG = "EditProfileFragment";

    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;
    private String userID;


    //EditProfile Fragment widgets
    private EditText mDisplayName, mUsername, mWebsite, mDescription, mEmail, mPhoneNumber;
    private TextView mChangeProfilePhoto;
    private CircleImageView mProfilePhoto;


    private UserSettings mUserSettings;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_editprofile, container, false);

        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        mDisplayName = (EditText) view.findViewById(R.id.display_name);
        mUsername = (EditText) view.findViewById(R.id.username);
        mWebsite = (EditText) view.findViewById(R.id.website);
        mDescription = (EditText) view.findViewById(R.id.description);
        mEmail = (EditText) view.findViewById(R.id.email);
        mPhoneNumber = (EditText) view.findViewById(R.id.phoneNumber);
        mChangeProfilePhoto = (TextView) view.findViewById(R.id.changeProfilePhoto);
        mFirebaseMethods = new FirebaseMethods(getActivity());


        // SetProfileImage();
        setupFirebaseAuth();

        //back arrow for navigating back to "ProfileActivity"
        ImageView backArrow = (ImageView) view.findViewById(R.id.backArrow);

        backArrow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Log.d(TAG, "onClick: navigating back to ProfileActivity");

                // Objects.requireNonNull(getActivity()).finish();

                // assert getFragmentManager() != null;
                // getFragmentManager().beginTransaction().detach(EditProfileFragment.this).commitNowAllowingStateLoss();

                AccountSettingsActivity.mRelativeLayout.setVisibility(View.VISIBLE);
                Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction().remove(EditProfileFragment.this)
                        .commitAllowingStateLoss();
            }
        });

        ImageView checkMark = (ImageView) view.findViewById(R.id.saveChanges);

        checkMark.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Log.d(TAG, "onClick: attempting to save changes.");

                saveProfileSettings();
            }
        });

        return view;
    }

    @Override
    public void onConfirmPassword(String password)
    {
        // Log.d(TAG, "onConfirmPassword: got the password: " + password);

        // Get auth credentials from the user for re-authentication. The example below shows email and password credentials but there are multiple possible
        // providers, such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential(Objects.requireNonNull(Objects.requireNonNull(mAuth.getCurrentUser()).getEmail()), password);

        mAuth.getCurrentUser().reauthenticate(credential)
        .addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    // Log.d(TAG, "User re-authenticated.");

                    // Check to see if the email is not already present in the database.
                    mAuth.fetchSignInMethodsForEmail(mEmail.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<SignInMethodQueryResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                try
                                {
                                    if(Objects.requireNonNull(Objects.requireNonNull(task.getResult()).getSignInMethods()).size() == 1)
                                    {
                                        // Log.d(TAG, "onComplete: that email is already in use.");
                                        Toast.makeText(getActivity(), "That email is already in use", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        // Log.d(TAG, "onComplete: That email is available.");

                                        // The email is available so update it.
                                        mAuth.getCurrentUser().updateEmail(mEmail.getText().toString())
                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if (task.isSuccessful())
                                                {
                                                    // Log.d(TAG, "User email address updated.");

                                                    Toast.makeText(getActivity(), "email updated", Toast.LENGTH_SHORT).show();

                                                    mFirebaseMethods.updateEmail(mEmail.getText().toString());
                                                }
                                            }
                                        });
                                    }
                                }
                                catch(NullPointerException e)
                                {
                                    Log.e(TAG, "onComplete: NullPointerException: "  +e.getMessage() );
                                }
                            }
                        }
                    });
                }
                else
                {
                    Log.d(TAG, "onComplete: re-authentication failed.");
                }
            }
        });
    }


    // Retrieves the data contained in the widgets and submits it to the database Before doing so it checks to make sure the username chosen is unique.
    private void saveProfileSettings()
    {
        final String displayName = mDisplayName.getText().toString();
        final String username = mUsername.getText().toString();
        final String website = mWebsite.getText().toString();
        final String description = mDescription.getText().toString();
        final String email = mEmail.getText().toString();
        final long phoneNumber = Long.parseLong(mPhoneNumber.getText().toString());


        // Case1: if the user made a change to their username.
        if(!mUserSettings.getUser().getUsername().equals(username))
        {

            checkIfUsernameExists(username);
        }

        // Case2: if the user made a change to their email.
        if(!mUserSettings.getUser().getEmail().equals(email))
        {
            // step1) Re authenticate -Confirm the password and email.
            ConfirmPasswordDialog dialog = new ConfirmPasswordDialog();

            assert getFragmentManager() != null;
            dialog.show(getFragmentManager(), getString(R.string.confirm_password_dialog));

            dialog.setTargetFragment(EditProfileFragment.this, 1);

            // step2) check if the email already is registered -'fetchProvidersForEmail(String email)'
            // step3) change the email -submit the new email to the database and authentication
        }

        // Change the rest of the settings that do not require uniqueness.

        if(!mUserSettings.getSettings().getDisplay_name().equals(displayName))
        {
            // Update display name.
            mFirebaseMethods.updateUserAccountSettings(displayName, null, null, 0);
        }

        if(!mUserSettings.getSettings().getWebsite().equals(website))
        {
            // Update website
            mFirebaseMethods.updateUserAccountSettings(null, website, null, 0);
        }

        if(!mUserSettings.getSettings().getDescription().equals(description))
        {
            // Update description.
            mFirebaseMethods.updateUserAccountSettings(null, null, description, 0);
        }

        if(mUserSettings.getUser().getPhone_number() != phoneNumber)
        {
            // Update phoneNumber.
            mFirebaseMethods.updateUserAccountSettings(null, null, null, phoneNumber);
        }
    }


    // Check is @param username already exists in teh database.

    private void checkIfUsernameExists(final String username)
    {
        // Log.d(TAG, "checkIfUsernameExists: Checking if  " + username + " already exists.");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference
                .child(getString(R.string.dbname_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(!dataSnapshot.exists())
                {
                    // Add the username to the database.
                    mFirebaseMethods.updateUsername(username);

                    Toast.makeText(getActivity(), "saved username.", Toast.LENGTH_SHORT).show();
                }

                for(DataSnapshot singleSnapshot: dataSnapshot.getChildren())
                {
                    if(singleSnapshot.exists())
                    {
                        // Log.d(TAG, "checkIfUsernameExists: FOUND A MATCH: " + singleSnapshot.getValue(User.class).getUsername());

                        Toast.makeText(getActivity(), "That username already exists.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void setProfileWidgets(UserSettings userSettings)
    {
        // Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database: " + userSettings.toString());
        // Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database: " + userSettings.getUser().getEmail());
        // Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database: " + userSettings.getUser().getPhone_number());

        mUserSettings = userSettings;

        // User user = userSettings.getUser();

        UserAccountSettings settings = userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mWebsite.setText(settings.getWebsite());
        mDescription.setText(settings.getDescription());
        mEmail.setText(userSettings.getUser().getEmail());
        mPhoneNumber.setText(String.valueOf(userSettings.getUser().getPhone_number()));

        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Log.d(TAG, "onClick: changing profile photo");

                Intent intent = new Intent(getActivity(), ShareActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);     // 268435456

                Objects.requireNonNull(getActivity()).startActivity(intent);
                getActivity().finish();
            }
        });
    }

    private void setupFirebaseAuth()
    {
        // Log.d(TAG, "setupFirebaseAuth: setting up firebase auth.");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        userID = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        mAuthListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user != null)
                {
                    // User is signed in.
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                }
                else
                {
                    // User is signed out.
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };

        myRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                // Retrieve user information from the database.
                setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));

                // Retrieve images for the user in question.
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    @Override
    public void onStart()
    {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
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
}