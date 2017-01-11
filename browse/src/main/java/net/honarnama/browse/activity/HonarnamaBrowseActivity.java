package net.honarnama.browse.activity;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.activity.HonarnamaBaseActivity;
import net.honarnama.base.helper.MetaUpdater;
import net.honarnama.base.interfaces.MetaUpdateListener;
import net.honarnama.base.model.City;
import net.honarnama.base.model.Province;
import net.honarnama.nano.ReplyProperties;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.Date;

/**
 * Created by elnaz on 2/11/16.
 */
public class HonarnamaBrowseActivity extends HonarnamaBaseActivity {
    public final static String SELECTED_TAB_EXTRA_KEY = "selectedTabIndex";

    SharedPreferences mSharedPreferences;

    MetaUpdateListener mMetaUpdateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences = getSharedPreferences(HonarnamaBaseApp.PREF_NAME_BROWSE_APP, Context.MODE_PRIVATE);

        mMetaUpdateListener = new MetaUpdateListener() {
            @Override
            public void onMetaUpdateDone(int replyCode) {

                //getting the current time in milliseconds, and creating a Date object from it:
                Date date = new Date(System.currentTimeMillis()); //or simply new Date();

                //converting it back to a milliseconds representation:
                long millis = date.getTime();

                SharedPreferences.Editor editor = HonarnamaBaseApp.getAppSharedPref().edit();
                editor.putLong(HonarnamaBaseApp.PREF_KEY_META_CHECKED_TIME, millis);
                editor.commit();

                if (net.honarnama.base.BuildConfig.DEBUG) {
                    logD("Browse Meta Update replyCode: " + replyCode);
                }
                switch (replyCode) {
                    case ReplyProperties.UPGRADE_REQUIRED:
                        displayUpgradeRequiredDialog();
                        break;
                }
            }
        };
    }

    public int getUserLocationProvinceId() {
        return mSharedPreferences.getInt(HonarnamaBaseApp.PREF_KEY_DEFAULT_LOCATION_PROVINCE_ID, Province.ALL_PROVINCE_ID);
    }

    public int getUserLocationCityId() {
        return mSharedPreferences.getInt(HonarnamaBaseApp.PREF_KEY_DEFAULT_LOCATION_CITY_ID, City.ALL_CITY_ID);
    }


    public String getUserLocationProvinceName() {
        return mSharedPreferences.getString(HonarnamaBaseApp.PREF_KEY_DEFAULT_LOCATION_PROVINCE_NAME, "");
    }

    public String getUserLocationCityName() {
        return mSharedPreferences.getString(HonarnamaBaseApp.PREF_KEY_DEFAULT_LOCATION_CITY_NAME, "");
    }

    public void checkAndUpdateMeta(boolean forceUpdate) {

        long metaVersion = getSharedPreferences(HonarnamaBaseApp.PREF_NAME_BROWSE_APP, Context.MODE_PRIVATE)
                .getLong(HonarnamaBaseApp.PREF_KEY_META_VERSION, 0);

        if (forceUpdate || metaVersion == 0) {
            MetaUpdater metaUpdater = new MetaUpdater(mMetaUpdateListener, metaVersion);
            metaUpdater.execute();
        }
    }

    public void runScheduledMetaUpdate() {
        if (BuildConfig.DEBUG) {
            logD("runScheduledMetaUpdate in background (onPause)");
        }
        long lastMetaCheckTime = getSharedPreferences(HonarnamaBaseApp.PREF_NAME_BROWSE_APP, Context.MODE_PRIVATE)
                .getLong(HonarnamaBaseApp.PREF_KEY_META_CHECKED_TIME, 0);

        // Check time elapsed
        if (System.currentTimeMillis() > lastMetaCheckTime + 24 * 60 * 60 * 1000) {
            checkAndUpdateMeta(true);
        }
    }

    public void exitApp() {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            finishAndRemoveTask();
            System.exit(0);
        } else {
            finish();
            System.exit(0);
        }
    }

}
