package com.aseproject.tensorflow.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mywebsite.arthree.MyLauncher;
import com.mywebsite.arthree.R;
import com.mywebsite.arthree.fragments.LoginFragment;

import java.util.Arrays;
import java.util.List;

public class SocialLoginMainActivity extends BaseActivity implements LoginFragment.OnFragmentInteractionListener,GoogleApiClient.OnConnectionFailedListener  {

    private final static String TAG = SocialLoginMainActivity.class.getSimpleName();

    private static final int RC_SIGN_IN = 123;


    //initialize the FirebaseAuth instance
    public static FirebaseAuth mAuth;
    public static FirebaseAuth.AuthStateListener mAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "SocialLoginMainActivity onCreate");


        // Inflate layout (must be done after Twitter is configured)
        setContentView(R.layout.socialloginmainactivity);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mAuth = FirebaseAuth.getInstance();

        //initialize the AuthStateListener method
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    Log.d(TAG, "onAuthStateChanged:signed_in_getDisplayName:" + user.getDisplayName());
                    Log.d(TAG, "onAuthStateChanged:signed_in_getEmail():" + user.getEmail());
                    Log.d(TAG, "onAuthStateChanged:signed_in_getPhotoUrl():" + user.getPhotoUrl());
                    Log.d(TAG, "onAuthStateChanged:signed_in_emailVerified?:" + user.isEmailVerified());
                    Intent mIntent = new Intent(SocialLoginMainActivity.this, MyLauncher.class);
                    startActivity(mIntent);
                    finish();

                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    initiateLogin();
                }
                // ...
            }
        };


    }//End of on create

    private void initiateLogin() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.Builder(
                        AuthUI.EMAIL_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(
                        AuthUI.GOOGLE_PROVIDER).build(),
                //new AuthUI.IdpConfig.Builder(AuthUI.TWITTER_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(
                        AuthUI.FACEBOOK_PROVIDER).build()

        );
        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        //.setLogo(R.drawable.messenger_bubble_small_white)      // Set logo drawable
                        .setTheme(R.style.AppTheme_AppBarOverlay)      // Set theme
                        .build(),
                RC_SIGN_IN);
    }

    // [END on_create]
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        //showMessage(getString(R.string.google_play_services_error));
        Toast.makeText(SocialLoginMainActivity.this, getString(R.string.google_play_services_error),
                Toast.LENGTH_LONG).show();
        // Sending failed or it was canceled
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        Log.d(TAG, "SocialLoginMainActivity onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "SocialLoginMainActivity onStop");
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onPause () {
        super.onPause ();
        Log.d(TAG, "SocialLoginMainActivity onPause");
        if (mProgress != null){
            mProgress.dismiss();
            Log.d(TAG, "hide mProgress onPause SocialLoginMainActivity");
        }
    }

    @Override
    public void onFragmentInteraction(String FragmentName) {// listens to login fragments buttons
        Log.d(TAG, "FragmentName = "+ FragmentName);

        switch (FragmentName){
            case "RegisterClicked":
                break;
            default:
                break;
        }
    }


    //Activity result after user selects a provider he wants to use
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                // ...
            } else {
                // Sign in failed, check response for error code
                // ...
            }
        }
    }


}
