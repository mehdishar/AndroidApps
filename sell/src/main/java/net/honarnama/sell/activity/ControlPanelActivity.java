package net.honarnama.sell.activity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.context.IconicsLayoutInflater;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.dialog.CustomAlertDialog;
import net.honarnama.base.fragment.AboutFragment;
import net.honarnama.base.fragment.ContactFragment;
import net.honarnama.base.fragment.HonarnamaBaseFragment;
import net.honarnama.base.utils.CommonUtil;
import net.honarnama.base.utils.FileUtil;
import net.honarnama.base.utils.WindowUtil;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;
import net.honarnama.sell.fragments.CPanelFragment;
import net.honarnama.sell.fragments.EditItemFragment;
import net.honarnama.sell.fragments.EventManagerFragment;
import net.honarnama.sell.fragments.ItemsFragment;
import net.honarnama.sell.fragments.StoreFragment;
import net.honarnama.sell.fragments.UserAccountFragment;
import net.honarnama.sell.model.HonarnamaUser;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;

public class ControlPanelActivity extends HonarnamaSellActivity implements View.OnClickListener {

    public static final int ITEM_IDENTIFIER_ACCOUNT = 0;

    public static final int ITEM_IDENTIFIER_STORE_INFO = 1;
    public static final int ITEM_IDENTIFIER_ITEMS = 2;
    public static final int ITEM_IDENTIFIER_ADD_ITEM = 3;
    public static final int ITEM_IDENTIFIER_EVENT_MANAGER = 4;

    public static final int ITEM_IDENTIFIER_CONTACT = 5;
    public static final int ITEM_IDENTIFIER_RULES = 6;
    public static final int ITEM_IDENTIFIER_ABOUT = 7;
    public static final int ITEM_IDENTIFIER_SHARE = 8;
    public static final int ITEM_IDENTIFIER_SUPPORT = 9;
    public static final int ITEM_IDENTIFIER_SWITCH_APP = 10;

    public static final int ITEM_IDENTIFIER_EXIT = 11;


    private Toolbar mToolbar;
    private HonarnamaBaseFragment mFragment;
    private EditItemFragment mEditItemFragment;
    private ProgressDialog mWaitingProgressDialog;

    public CustomAlertDialog mConfirmationDialog;

    Tracker mTracker;

    private DrawerLayout mDrawer;
    public NavigationView mNavigationView;
    public RelativeLayout mNavFooter;
    private ItemsFragment mItemsFragment;

    public TextView mCustomToolbarTitle;

    @Override
    protected void onResume() {
        super.onResume();

        if (mFragment == null) {
            switchFragment(CPanelFragment.getInstance());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        LayoutInflaterCompat.setFactory(getLayoutInflater(), new IconicsLayoutInflater(getDelegate()));

        super.onCreate(savedInstanceState);

        mEditItemFragment = EditItemFragment.getInstance();
        mItemsFragment = ItemsFragment.getInstance();

        if (!HonarnamaUser.isLoggedIn()) {
            if (BuildConfig.DEBUG) {
                logD("User is not logged in!");
            }
            Intent intent = new Intent(ControlPanelActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        mTracker = HonarnamaSellApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("CPA");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        WindowUtil.hideKeyboard(ControlPanelActivity.this);
        mWaitingProgressDialog = new ProgressDialog(ControlPanelActivity.this);
        setContentView(R.layout.activity_control_panel);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
//        mToolbar.setTitle(getString(R.string.toolbar_title));
        setSupportActionBar(mToolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setHomeAsUpIndicator(new IconicsDrawable(ControlPanelActivity.this)
                        .icon(GoogleMaterial.Icon.gmd_menu)
                        .color(Color.WHITE)
                        .sizeDp(20));
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        } else {
            findViewById(R.id.toolbar_hamburger_icon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDrawer.isDrawerOpen(Gravity.RIGHT)) {
                        mDrawer.closeDrawer(Gravity.RIGHT);
                    } else {
                        mDrawer.openDrawer(Gravity.RIGHT);
                        WindowUtil.hideKeyboard(ControlPanelActivity.this);
                    }
                }
            });

            mCustomToolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        }

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.navView);

        resetMenuIcons();
        setupDrawerContent();
        mNavFooter = (RelativeLayout) findViewById(R.id.footer_container);
        mNavFooter.setOnClickListener(this);

        processIntent(getIntent());

        checkAndUpdateMeta(false, 0);

        checkAndAskStoragePermission(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            mFragment = (HonarnamaBaseFragment) getSupportFragmentManager().findFragmentById(R.id.frame_container);
        }
    }


    private void setupDrawerContent() {
        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        resetMenuIcons();
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
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
            final int itemId = Integer.valueOf(data.getQueryParameter("itemId"));
            if (itemId > 0) {
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
            if (mDrawer.isDrawerOpen(Gravity.RIGHT)) {
                mDrawer.closeDrawer(Gravity.RIGHT);
            } else {
                mDrawer.openDrawer(Gravity.RIGHT);
                WindowUtil.hideKeyboard(ControlPanelActivity.this);
            }
        }
        if (id == R.id.add_item_action) {
            switchFragmentToNewItem();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (mFragment != null) {
            mFragment.onActivityResult(requestCode, resultCode, intent);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.footer_container:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(HonarnamaBaseApp.WEB_ADDRESS)));
                break;
        }
    }

    private interface OnAcceptedListener {
        public void onAccepted();
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

        checkAndUpdateMeta(false, 0);
        WindowUtil.hideKeyboard(ControlPanelActivity.this);

        mFragment = fragment;

        if (mFragment == CPanelFragment.getInstance()) {
            resetMenuIcons();
        }

        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_container, fragment);
        fragmentTransaction.commit();

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("SwitchFragment")
                .build());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mToolbar.setTitle(fragment.getTitle());
        } else {
            mCustomToolbarTitle.setText(fragment.getTitle());
        }
    }


    public void switchFragmentToNewItem() {
        if (mDrawer.isDrawerOpen(Gravity.RIGHT)) {
            mDrawer.closeDrawer(Gravity.RIGHT);
        }
        mEditItemFragment.reset(true);
        resetMenuIcons();
        selectDrawerItem(mNavigationView.getMenu().getItem(ITEM_IDENTIFIER_ADD_ITEM));
        switchFragment(mEditItemFragment);

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("AddItem")
                .build());
    }


    public void switchFragmentToEditItem(long itemId) {
        mEditItemFragment.setItemId(itemId);
        switchFragment(mEditItemFragment);
    }


    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(Gravity.RIGHT)) {
            mDrawer.closeDrawer(Gravity.RIGHT);
        } else if (mFragment == mEditItemFragment) {
            if (mEditItemFragment.isDirty()) {
                switchFragmentFromEdittingItem(new OnAcceptedListener() {
                    @Override
                    public void onAccepted() {
                        mEditItemFragment.reset(true);
                        switchFragment(ItemsFragment.getInstance());
                        resetMenuIcons();
                        selectDrawerItem(mNavigationView.getMenu().getItem(ITEM_IDENTIFIER_ITEMS));
                    }
                });
            } else {
                mEditItemFragment.reset(true);
                switchFragment(ItemsFragment.getInstance());
                resetMenuIcons();
                selectDrawerItem(mNavigationView.getMenu().getItem(ITEM_IDENTIFIER_ITEMS));
            }
        } else if (mFragment == CPanelFragment.getInstance()) {

            mConfirmationDialog = new CustomAlertDialog(ControlPanelActivity.this,
                    getString(R.string.exit_app_title),
                    getString(R.string.exit_app_confirmation),
                    getString(R.string.yes),
                    getString(R.string.will_stay)
            );
            mConfirmationDialog.showDialog(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!HonarnamaBaseApp.getAppSharedPref().getBoolean(HonarnamaBaseApp.PREF_KEY_SELL_APP_RATED, false)) {
                        askToRate();
                    } else {
                        exitApp();
                    }
                    mConfirmationDialog.dismiss();
                }
            });

        } else {
            switchFragment(CPanelFragment.getInstance());
        }

    }

    public void resetMenuIcons() {
        Menu menu = mNavigationView.getMenu();

        IconicsDrawable accountDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .sizeDp(20)
                        .icon(GoogleMaterial.Icon.gmd_account_circle);
        menu.getItem(ITEM_IDENTIFIER_ACCOUNT).setIcon(accountDrawable);
        menu.getItem(ITEM_IDENTIFIER_ACCOUNT).setChecked(false);

        IconicsDrawable storeDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .sizeDp(20)
                        .icon(GoogleMaterial.Icon.gmd_store);
        menu.getItem(ITEM_IDENTIFIER_STORE_INFO).setIcon(storeDrawable);
        menu.getItem(ITEM_IDENTIFIER_STORE_INFO).setChecked(false);

        IconicsDrawable itemsDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .sizeDp(20)
                        .icon(GoogleMaterial.Icon.gmd_toc);
        menu.getItem(ITEM_IDENTIFIER_ITEMS).setIcon(itemsDrawable);
        menu.getItem(ITEM_IDENTIFIER_ITEMS).setChecked(false);

        IconicsDrawable newItemDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .sizeDp(20)
                        .icon(GoogleMaterial.Icon.gmd_edit);
        menu.getItem(ITEM_IDENTIFIER_ADD_ITEM).setIcon(newItemDrawable);
        menu.getItem(ITEM_IDENTIFIER_ADD_ITEM).setChecked(false);

        IconicsDrawable eventDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .sizeDp(20)
                        .icon(GoogleMaterial.Icon.gmd_event);
        menu.getItem(ITEM_IDENTIFIER_EVENT_MANAGER).setIcon(eventDrawable);
        menu.getItem(ITEM_IDENTIFIER_EVENT_MANAGER).setChecked(false);

        IconicsDrawable contactDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .sizeDp(20)
                        .icon(GoogleMaterial.Icon.gmd_email);
        menu.getItem(ITEM_IDENTIFIER_CONTACT).setIcon(contactDrawable);
        menu.getItem(ITEM_IDENTIFIER_CONTACT).setChecked(false);

        IconicsDrawable rulesDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .sizeDp(20)
                        .icon(GoogleMaterial.Icon.gmd_gavel);
        menu.getItem(ITEM_IDENTIFIER_RULES).setIcon(rulesDrawable);
        menu.getItem(ITEM_IDENTIFIER_RULES).setChecked(false);

        IconicsDrawable aboutDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .sizeDp(20)
                        .icon(GoogleMaterial.Icon.gmd_info_outline);
        menu.getItem(ITEM_IDENTIFIER_ABOUT).setIcon(aboutDrawable);
        menu.getItem(ITEM_IDENTIFIER_ABOUT).setChecked(false);

        IconicsDrawable shareDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .sizeDp(20)
                        .icon(GoogleMaterial.Icon.gmd_share);
        menu.getItem(ITEM_IDENTIFIER_SHARE).setIcon(shareDrawable);
        menu.getItem(ITEM_IDENTIFIER_SHARE).setChecked(false);

        IconicsDrawable supportDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .sizeDp(20)
                        .icon(GoogleMaterial.Icon.gmd_stars);
        menu.getItem(ITEM_IDENTIFIER_SUPPORT).setIcon(supportDrawable);
        menu.getItem(ITEM_IDENTIFIER_SUPPORT).setChecked(false);

        IconicsDrawable swapDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .sizeDp(20)
                        .icon(GoogleMaterial.Icon.gmd_swap_horiz);
        menu.getItem(ITEM_IDENTIFIER_SWITCH_APP).setIcon(swapDrawable);
        menu.getItem(ITEM_IDENTIFIER_SWITCH_APP).setChecked(false);

        IconicsDrawable exitDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .sizeDp(20)
                        .icon(GoogleMaterial.Icon.gmd_exit_to_app);
        menu.getItem(ITEM_IDENTIFIER_EXIT).setIcon(exitDrawable);
        menu.getItem(ITEM_IDENTIFIER_EXIT).setChecked(false);

    }

    public void selectDrawerItem(MenuItem menuItem) {

        HonarnamaBaseFragment fragment = null;

        switch (menuItem.getItemId()) {
            case R.id.item_account:
                menuItem.setChecked(true);
                IconicsDrawable accountDrawable =
                        new IconicsDrawable(ControlPanelActivity.this)
                                .color(getResources().getColor(R.color.amber_primary_dark))
                                .icon(GoogleMaterial.Icon.gmd_account_circle);
                menuItem.setIcon(accountDrawable);
                fragment = UserAccountFragment.getInstance();
                break;


            case R.id.item_store_info:
                menuItem.setChecked(true);
                IconicsDrawable storeDrawable =
                        new IconicsDrawable(ControlPanelActivity.this)
                                .color(getResources().getColor(R.color.amber_primary_dark))
                                .icon(GoogleMaterial.Icon.gmd_store);
                menuItem.setIcon(storeDrawable);
                fragment = StoreFragment.getInstance();
                break;

            case R.id.item_items:
                menuItem.setChecked(true);
                IconicsDrawable itemsDrawable =
                        new IconicsDrawable(ControlPanelActivity.this)
                                .color(getResources().getColor(R.color.amber_primary_dark))
                                .icon(GoogleMaterial.Icon.gmd_view_list);
                menuItem.setIcon(itemsDrawable);
                fragment = ItemsFragment.getInstance();
                break;

            case R.id.item_new_item:
                menuItem.setChecked(true);
                IconicsDrawable newItemDrawable =
                        new IconicsDrawable(ControlPanelActivity.this)
                                .color(getResources().getColor(R.color.amber_primary_dark))
                                .icon(GoogleMaterial.Icon.gmd_edit);
                menuItem.setIcon(newItemDrawable);
                fragment = EditItemFragment.getInstance();
                mEditItemFragment.reset(true);
                break;

            case R.id.item_art_event:
                menuItem.setChecked(true);
                IconicsDrawable eventDrawable =
                        new IconicsDrawable(ControlPanelActivity.this)
                                .color(getResources().getColor(R.color.amber_primary_dark))
                                .icon(GoogleMaterial.Icon.gmd_event);
                menuItem.setIcon(eventDrawable);
                fragment = EventManagerFragment.getInstance();
                break;

            case R.id.item_about_us:
                menuItem.setChecked(true);
                IconicsDrawable aboutDrawable =
                        new IconicsDrawable(ControlPanelActivity.this)
                                .color(getResources().getColor(R.color.amber_primary_dark))
                                .icon(GoogleMaterial.Icon.gmd_info_outline);
                menuItem.setIcon(aboutDrawable);
                fragment = AboutFragment.getInstance();
                break;

            case R.id.item_contact_us:
                menuItem.setChecked(true);
                IconicsDrawable contactDrawable =
                        new IconicsDrawable(ControlPanelActivity.this)
                                .color(getResources().getColor(R.color.amber_primary_dark))
                                .icon(GoogleMaterial.Icon.gmd_email);
                menuItem.setIcon(contactDrawable);
                fragment = ContactFragment.getInstance();
                break;

            case R.id.item_rules:
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(HonarnamaBaseApp.TERMS_ADDRESS));
                startActivity(i);
                break;


            case R.id.item_share_us:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "سلام،" + "\n" + "برنامه‌ٔ هنرنما برای فروشندگان رو از کافه بازار دانلود کن. اینم لینکش:" +
                        "\n" + "http://cafebazaar.ir/app/" + HonarnamaSellApp.getInstance().getPackageName() + "/");
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;

            case R.id.item_support_us:
                callBazaarRatingIntent();
                break;

            case R.id.item_switch_app:
                try {
                    if (CommonUtil.isPackageInstalled(HonarnamaBaseApp.BROWSE_PACKAGE_NAME)) {
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(HonarnamaBaseApp.BROWSE_PACKAGE_NAME);
                        startActivity(launchIntent);
                    } else {
                        callBazaarViewAppPageIntent(HonarnamaBaseApp.BROWSE_PACKAGE_NAME);
                    }
                } catch (Exception e) {
                    logE("Error switching from sell app to browse. Error: " + e, e);
                }
                break;

            case R.id.item_nav_title_exit_app:
                HonarnamaUser.logout(ControlPanelActivity.this);
                break;

        }

        if ((fragment != null)) {
            if (mFragment == mEditItemFragment) {
                if (mEditItemFragment.isDirty()) {
                    final HonarnamaBaseFragment finalFragment = fragment;
                    switchFragmentFromEdittingItem(new OnAcceptedListener() {
                        @Override
                        public void onAccepted() {
                            mEditItemFragment.reset(true);
                            switchFragment(finalFragment);
                        }
                    });
                } else {
                    mEditItemFragment.reset(true);
                    switchFragment(fragment);
                }
            } else {
                mEditItemFragment.reset(true);
                switchFragment(fragment);
            }
        }
        mDrawer.closeDrawer(Gravity.RIGHT);
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
    protected void onDestroy() {
        super.onDestroy();
//        releaseUpdateCheckService();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public void exitApp() {
//        removeTempFiles();
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            finishAndRemoveTask();
            System.exit(0);
        } else {
            finish();
            System.exit(0);
        }
    }


    private void removeTempFiles() {
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Honarnama/honarnama_temporary_files");
        if (storageDir.canWrite()) {
//            storageDir.delete();

            if (storageDir.isDirectory())
                for (File child : storageDir.listFiles())
                    FileUtil.deleteRecursive(child);

            storageDir.delete();

            if (BuildConfig.DEBUG) {
                logD("remove temp files on exit");
            }
        }
    }


    @Override
    public void onPause() {
        super.onPause();
    }

}
