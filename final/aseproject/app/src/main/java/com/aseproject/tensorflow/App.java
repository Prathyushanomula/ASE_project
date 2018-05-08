package com.aseproject.tensorflow;

import android.app.Application;
import android.content.Context;



public class App extends Application {

    private static Context sApplicationContext;

    @Override
    public void onCreate() {

        super.onCreate();

        sApplicationContext = getApplicationContext();

        // Initialize the SDK before executing any other operations,
        //FacebookSdk.sdkInitialize(sApplicationContext);

    }

    public static Context getContext() {
        return sApplicationContext;
        //return instance.getApplicationContext();
    }

}

