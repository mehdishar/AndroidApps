package net.honarnama.sell.activity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.typeface.IIcon;
import com.mikepenz.iconics.typeface.ITypeface;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.parse.LogOutCallback;
import com.parse.ParseException;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.activity.HonarnamaBaseActivity;
import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.core.model.CacheData;
import net.honarnama.core.utils.HonarnamaUser;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.core.utils.WindowUtil;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;
import net.honarnama.sell.fragments.EditItemFragment;
import net.honarnama.sell.fragments.ItemsFragment;
import net.honarnama.sell.fragments.NoNetworkFragment;
import net.honarnama.sell.fragments.StoreInfoFragment;
import net.honarnama.sell.fragments.UserAccountFragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import bolts.Continuation;
import bolts.Task;

public class ControlPanelActivity extends HonarnamaBaseActivity implements Drawer.OnDrawerItemClickListener {

    public static final int DRAWER_ITEM_IDENTIFIER_ACCOUNT = 1;
    public static final int DRAWER_ITEM_IDENTIFIER_STORE_INFO = 2;
    public static final int DRAWER_ITEM_IDENTIFIER_ITEMS = 3;
    public static final int DRAWER_ITEM_IDENTIFIER_ADD_ITEM = 4;
    public static final int DRAWER_ITEM_IDENTIFIER_RULES = 5;
    public static final int DRAWER_ITEM_IDENTIFIER_ABOUT = 6;
    public static final int DRAWER_ITEM_IDENTIFIER_SUPPORT = 7;
    public static final int DRAWER_ITEM_IDENTIFIER_SHARE = 8;
    public static final int DRAWER_ITEM_IDENTIFIER_EXIT = 9;


    private Toolbar mToolbar;
    private ActionBarDrawerToggle mDrawerToggle;
    private Fragment mFragment;
    private EditItemFragment mEditItemFragment;
    private ProgressDialog mWaitingProgressDialog;

    Tracker mTracker;

    Drawer mResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!HonarnamaUser.isAuthenticatedUser()) {
            if (BuildConfig.DEBUG) {
                logD("User was not authenticated!");
            }
            return;
        }
        mTracker = HonarnamaBaseApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("ControlPanel");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        WindowUtil.hideKeyboard(ControlPanelActivity.this);
        mWaitingProgressDialog = new ProgressDialog(ControlPanelActivity.this);
        setContentView(R.layout.activity_control_panel);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.toolbar_title);
        setSupportActionBar(mToolbar);

        mResult = new DrawerBuilder().withActivity(this)
                .withDrawerGravity(Gravity.RIGHT)
                .withRootView(R.id.drawer_container)
                .withToolbar(mToolbar)
                .withActionBarDrawerToggleAnimated(true)
                .withSelectedItem(-1)
                .withTranslucentStatusBar(false)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(R.string.nav_title_seller_account).
                                withIcon(GoogleMaterial.Icon.gmd_account_circle).withIdentifier(DRAWER_ITEM_IDENTIFIER_ACCOUNT),
                        new DividerDrawerItem().withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.nav_title_store_info).
                                withIdentifier(DRAWER_ITEM_IDENTIFIER_STORE_INFO).withIcon(GoogleMaterial.Icon.gmd_store),
                        new SecondaryDrawerItem().withName(R.string.nav_title_items).
                                withIdentifier(DRAWER_ITEM_IDENTIFIER_ITEMS).withIcon(GoogleMaterial.Icon.gmd_view_list),
                        new SecondaryDrawerItem().withName(R.string.nav_title_new_item).
                                withIdentifier(DRAWER_ITEM_IDENTIFIER_ADD_ITEM).withIcon(GoogleMaterial.Icon.gmd_edit),
                        new DividerDrawerItem().withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.rules).
                                withIdentifier(DRAWER_ITEM_IDENTIFIER_RULES).withIcon(FontAwesome.Icon.faw_gavel).withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.about_us).
                                withIdentifier(DRAWER_ITEM_IDENTIFIER_ABOUT).withIcon(GoogleMaterial.Icon.gmd_info_outline),
                        new SecondaryDrawerItem().withName(R.string.support_us).
                                withIdentifier(DRAWER_ITEM_IDENTIFIER_SUPPORT).withIcon(GoogleMaterial.Icon.gmd_star_circle).withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.share_us).
                                withIdentifier(DRAWER_ITEM_IDENTIFIER_SHARE).withIcon(GoogleMaterial.Icon.gmd_share),
                        new DividerDrawerItem().withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.nav_title_exit_app).
                                withIdentifier(DRAWER_ITEM_IDENTIFIER_EXIT).withIcon(GoogleMaterial.Icon.gmd_power_off)

                )
                .withOnDrawerItemClickListener(this)
                .build();
        mDrawerToggle = new ActionBarDrawerToggle(this, mResult.getDrawerLayout(), null, R.string.drawer_open, R.string.drawer_close) {
        };


        mResult.getDrawerLayout().post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (mResult.isDrawerOpen()) {
//                    mResult.closeDrawer();
//                } else {
//                    mResult.openDrawer();
//                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(false);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mResult.setActionBarDrawerToggle(mDrawerToggle);

//        mResult.addStickyFooterItem(new SecondaryDrawerItem().withName("StickyFooterItem").with);
        this.mDrawerToggle.syncState();

        mEditItemFragment = EditItemFragment.getInstance();

        processIntent(getIntent());

//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ControlPanelActivity.this);
        final SharedPreferences sharedPref = HonarnamaBaseApp.getInstance().getSharedPreferences(HonarnamaUser.getCurrentUser().getUsername(), Context.MODE_PRIVATE);
//        final SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        if (!sharedPref.getBoolean(HonarnamaSellApp.PREF_LOCAL_DATA_STORE_SYNCED, false)) {
            mResult.openDrawer();
            if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                HonarnamaBaseFragment fragment = NoNetworkFragment.getInstance();
                switchFragment(fragment);
                return;
            }
            new CacheData(ControlPanelActivity.this).startSyncing().continueWith(new Continuation<Void, Object>() {
                @Override
                public Object then(Task<Void> task) throws Exception {
                    if (task.isFaulted()) {
                        HonarnamaBaseFragment fragment = NoNetworkFragment.getInstance();
                        switchFragment(fragment);
                        Toast.makeText(ControlPanelActivity.this, R.string.syncing_data_failed, Toast.LENGTH_LONG).show();
                    }
                    return null;
                }
            });
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        processIntent(intent);
    }

    private void processIntent(Intent intent) {
        Uri data = intent.getData();
        if (BuildConfig.DEBUG) {
            logD("processIntent :: data= " + data);
        }

        if (data != null) {
            final String itemId = data.getQueryParameter("itemId");
            if (itemId != null) {
                if (mEditItemFragment.isDirty()) {
                    switchFragmentFromEdittingItem(new OnAcceptedListener() {
                        @Override
                        public void onAccepted() {
                            switchFragmentToEditItem(itemId);
                        }
                    });
                } else {
                    switchFragmentToEditItem(itemId);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//            getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item_row clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            if (mResult.isDrawerOpen()) {
                mResult.closeDrawer();
            } else {
                mResult.openDrawer();
            }
        }
        if (id == R.id.add_item_action) {
            mEditItemFragment.reset(ControlPanelActivity.this, true);
            mResult.setSelection(DRAWER_ITEM_IDENTIFIER_ADD_ITEM);
            switchFragment(mEditItemFragment);

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Action")
                    .setAction("AddItem")
                    .build());
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (mFragment != null) {
            mFragment.onActivityResult(requestCode, resultCode, intent);
        }
    }

    private interface OnAcceptedListener {
        public void onAccepted();
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        HonarnamaBaseFragment fragment = null;
        switch (drawerItem.getIdentifier()) {
            case DRAWER_ITEM_IDENTIFIER_ACCOUNT:
                fragment = UserAccountFragment.getInstance();
                break;
            case DRAWER_ITEM_IDENTIFIER_STORE_INFO:
                fragment = StoreInfoFragment.getInstance();
                break;
            case DRAWER_ITEM_IDENTIFIER_ITEMS:
                fragment = ItemsFragment.getInstance();
                break;
            case DRAWER_ITEM_IDENTIFIER_ADD_ITEM:
                fragment = EditItemFragment.getInstance();
                break;
            case DRAWER_ITEM_IDENTIFIER_RULES:
                String url = "http://www.honarnama.net/rules";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;

            case DRAWER_ITEM_IDENTIFIER_SUPPORT:
                Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setData(Uri.parse("bazaar://details?id=" + HonarnamaSellApp.getInstance().getPackageName()));
                intent.setPackage("com.farsitel.bazaar");
                startActivity(intent);
                break;
            case DRAWER_ITEM_IDENTIFIER_EXIT:
                //sign user out
                mWaitingProgressDialog.setMessage(getString(R.string.please_wait));
                mWaitingProgressDialog.setCancelable(false);
                mWaitingProgressDialog.show();
                HonarnamaUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            logE("Error logging user out." + " Error Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e, "", e);
                        }
                        if (mWaitingProgressDialog.isShowing()) {
                            mWaitingProgressDialog.dismiss();
                        }

                        Intent intent = new Intent(ControlPanelActivity.this, LoginActivity.class);
                        finish();
                        startActivity(intent);
                    }
                });

                break;
        }
        // Not null && (Another section || Maybe editing but wants to create new item_row)
//        if ((fragment != null) && ((fragment != mFragment) || (fragment == mEditItemFragment))) {
        if ((fragment != null)) {
            if (mFragment == mEditItemFragment) {
                if (mEditItemFragment.isDirty()) {
                    final HonarnamaBaseFragment finalFragment = fragment;
                    switchFragmentFromEdittingItem(new OnAcceptedListener() {
                        @Override
                        public void onAccepted() {
                            mEditItemFragment.reset(ControlPanelActivity.this, true);
                            switchFragment(finalFragment);
                        }
                    });
                } else {
                    mEditItemFragment.reset(ControlPanelActivity.this, true);
                    switchFragment(fragment);
                }
            } else {
                mEditItemFragment.reset(ControlPanelActivity.this, true);
                switchFragment(fragment);
            }
        }

        return false;
    }

    private void switchFragmentFromEdittingItem(final OnAcceptedListener onAcceptedListener) {
        final AlertDialog.Builder exitEditingDialog = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogCustom));
        exitEditingDialog.setTitle(getString(R.string.exit_from_editing_dialog_title));
        exitEditingDialog.setItems(new String[]{getString(R.string.exit_from_editing_option_dont_exit), getString(R.string.exit_from_editing_option_exit)},
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 1) {
                            onAcceptedListener.onAccepted();
                        }
                        dialog.dismiss();
                    }
                });
        exitEditingDialog.show();
    }

    public void switchFragment(final HonarnamaBaseFragment fragment) {

        WindowUtil.hideKeyboard(ControlPanelActivity.this);
        mFragment = fragment;

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_container, fragment);
        fragmentTransaction.commit();

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("SwitchFragment")
                .build());

        getSupportActionBar().setTitle(fragment.getTitle(this));

    }

    public void switchFragmentToEditItem(String itemId) {
        mEditItemFragment.setItemId(ControlPanelActivity.this, itemId);
        switchFragment(mEditItemFragment);
    }

    @Override
    protected void onStop() {
        if (mWaitingProgressDialog != null) {
            if (mWaitingProgressDialog.isShowing()) {
                mWaitingProgressDialog.dismiss();
            }
        }
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        if (mResult.isDrawerOpen()) {
            mResult.closeDrawer();
        } else if (mFragment == mEditItemFragment) {
            if (mEditItemFragment.isDirty()) {
                switchFragmentFromEdittingItem(new OnAcceptedListener() {
                    @Override
                    public void onAccepted() {
                        mEditItemFragment.reset(ControlPanelActivity.this, true);
                        switchFragment(ItemsFragment.getInstance());
                        mResult.setSelection(DRAWER_ITEM_IDENTIFIER_ITEMS);
                    }
                });
            } else {
                switchFragment(ItemsFragment.getInstance());
                mResult.setSelection(DRAWER_ITEM_IDENTIFIER_ITEMS);
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

}
