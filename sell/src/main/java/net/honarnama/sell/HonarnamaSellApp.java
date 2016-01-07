package net.honarnama.sell;


import com.parse.ParseObject;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.sell.model.Item;

import android.util.Log;

public class HonarnamaSellApp extends HonarnamaBaseApp {

    public static String STORE_LOGO_FILE_NAME = "store_logo.jpg";
    public static String STORE_BANNER_FILE_NAME = "store_banner.jpg";
    public static String NATIONAL_CARD_FILE_NAME = "national_card.jpg";

    @Override
    public void onCreate() {
        ParseObject.registerSubclass(Item.class);
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Log.i(PRODUCTION_TAG, "HonarnamaSellApp.onCreate()");
        }
    }
}
