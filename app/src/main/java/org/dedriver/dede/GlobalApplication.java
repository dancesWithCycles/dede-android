package org.dedriver.dede;

import android.app.Application;
import android.content.Context;

import org.dedriver.dede.rest.ApiManager;

public class GlobalApplication extends Application {

    public static ApiManager apiManager;
    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        apiManager = ApiManager.getInstance();
    }
}
