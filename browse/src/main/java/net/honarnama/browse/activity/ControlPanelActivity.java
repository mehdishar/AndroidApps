package net.honarnama.browse.activity;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.adapter.MainFragmentAdapter;
import net.honarnama.browse.fragment.ChildFragment;
import net.honarnama.browse.fragment.EventPageFragment;
import net.honarnama.browse.fragment.ItemPageFragment;
import net.honarnama.browse.fragment.SearchFragment;
import net.honarnama.browse.fragment.ShopPageFragment;
import net.honarnama.browse.widget.MainTabBar;
import net.honarnama.browse.widget.LockableViewPager;
import net.honarnama.core.fragment.AboutFragment;
import net.honarnama.core.fragment.ContactFragment;
import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.core.utils.WindowUtil;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.TextView;


import java.util.List;

import static net.honarnama.browse.widget.MainTabBar.TAB_EVENTS;
import static net.honarnama.browse.widget.MainTabBar.TAB_SEARCH;
import static net.honarnama.browse.widget.MainTabBar.TAB_ITEMS;
import static net.honarnama.browse.widget.MainTabBar.TAB_SHOPS;

public class ControlPanelActivity extends HonarnamaBrowseActivity implements MainTabBar.OnTabItemClickListener {

    public static Button btnRed; // Works as a badge
    //Declared static; so it can be accessed from all other Activities

    MainFragmentAdapter mMainFragmentAdapter;
    LockableViewPager mViewPager;
    private Toolbar mToolbar;
    private int mActiveTab;
    private MainTabBar mMainTabBar;
    private String mShopId;
    private String mEventId;
    private String mItemId;
    public TextView mTitle;
    private DrawerLayout mDrawer;
    public NavigationView mNavigationView;

    public ContactFragment mContactFragment;
    public AboutFragment mAboutFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
//        LayoutInflaterCompat.setFactory(getLayoutInflater(), new IconicsLayoutInflater(getDelegate()));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContactFragment = ContactFragment.getInstance(HonarnamaBaseApp.BROWSE_APP_KEY);
        mAboutFragment = AboutFragment.getInstance(HonarnamaBaseApp.BROWSE_APP_KEY);

        mMainFragmentAdapter =
                new MainFragmentAdapter(
                        getSupportFragmentManager());

        mViewPager = (LockableViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(mMainFragmentAdapter);
        mViewPager.setSwipeable(false);
//        mViewPager.setOffscreenPageLimit(4);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mActiveTab = position;
                ChildFragment childFragment = mMainFragmentAdapter.getItem(position);
                if (!childFragment.hasContent()) {
                    switchFragment(mMainFragmentAdapter.getDefaultFragmentForTab(position), false, getResources().getString(R.string.hornama));
                } else {
                    FragmentManager childFragmentManager = mMainFragmentAdapter.getItem(mActiveTab)
                            .getChildFragmentManager();
                    HonarnamaBaseFragment topFragment = (HonarnamaBaseFragment) childFragmentManager.findFragmentById(R.id.child_fragment_root);
                    mTitle.setText(topFragment.getTitle(HonarnamaBrowseApp.getInstance()));
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mMainTabBar = (MainTabBar) findViewById(R.id.tab_bar);
        mMainTabBar.setOnTabItemClickListener(this);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setDefaultTab();
        setSupportActionBar(mToolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//        getSupportActionBar().setHomeButtonEnabled(false);
//        getSupportActionBar().setLogo(new IconicsDrawable(ControlPanelActivity.this)
//                .icon(GoogleMaterial.Icon.gmd_menu)
//                .color(Color.WHITE)
//                .sizeDp(20));

        mTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(new IconicsDrawable(ControlPanelActivity.this)
                    .icon(GoogleMaterial.Icon.gmd_menu)
                    .color(Color.WHITE)
                    .sizeDp(20));
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        mNavigationView = (NavigationView) findViewById(R.id.navView);
        resetMenuIcons();
        setupDrawerContent();

        handleExternalIntent(getIntent());

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


    public void resetMenuIcons() {
        Menu menu = mNavigationView.getMenu();
        IconicsDrawable contactDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .icon(GoogleMaterial.Icon.gmd_email);
        menu.getItem(0).setIcon(contactDrawable);
        menu.getItem(0).setChecked(false);

        IconicsDrawable gavelDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .icon(GoogleMaterial.Icon.gmd_gavel);
        menu.getItem(1).setIcon(gavelDrawable);
        menu.getItem(1).setChecked(false);

        IconicsDrawable aboutDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .icon(GoogleMaterial.Icon.gmd_info_outline);
        menu.getItem(2).setIcon(aboutDrawable);
        menu.getItem(2).setChecked(false);

        IconicsDrawable shareDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .icon(GoogleMaterial.Icon.gmd_share);
        menu.getItem(3).setIcon(shareDrawable);
        menu.getItem(3).setChecked(false);

        IconicsDrawable supportDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .icon(GoogleMaterial.Icon.gmd_stars);
        menu.getItem(4).setIcon(supportDrawable);
        menu.getItem(4).setChecked(false);

        IconicsDrawable swapDrawable =
                new IconicsDrawable(ControlPanelActivity.this)
                        .color(getResources().getColor(R.color.gray_extra_dark))
                        .icon(GoogleMaterial.Icon.gmd_swap_horiz);
        menu.getItem(5).setIcon(swapDrawable);
        menu.getItem(5).setChecked(false);
    }

    public void selectDrawerItem(MenuItem menuItem) {
        mMainTabBar.deselectAllTabs();
        switch (menuItem.getItemId()) {
            case R.id.item_contact_us:
                menuItem.setChecked(true);
                IconicsDrawable contactDrawable =
                        new IconicsDrawable(ControlPanelActivity.this)
                                .color(getResources().getColor(R.color.dark_cyan))
                                .icon(GoogleMaterial.Icon.gmd_email);
                menuItem.setIcon(contactDrawable);
                ContactFragment contactFragment = ContactFragment.getInstance(HonarnamaBaseApp.BROWSE_APP_KEY);
                switchFragment(contactFragment, false, contactFragment.getTitle(ControlPanelActivity.this));
                break;

            case R.id.item_rules:
                String url = "http://www.honarnama.net/terms";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                break;

            case R.id.item_about_us:
                menuItem.setChecked(true);
                IconicsDrawable aboutDrawable =
                        new IconicsDrawable(ControlPanelActivity.this)
                                .color(getResources().getColor(R.color.dark_cyan))
                                .icon(GoogleMaterial.Icon.gmd_info_outline);
                menuItem.setIcon(aboutDrawable);
                AboutFragment aboutFragment = AboutFragment.getInstance(HonarnamaBaseApp.BROWSE_APP_KEY);
                switchFragment(aboutFragment, false, aboutFragment.getTitle(ControlPanelActivity.this));
                break;

            case R.id.item_share_us:
                menuItem.setChecked(true);
                IconicsDrawable shareDrawable =
                        new IconicsDrawable(ControlPanelActivity.this)
                                .color(getResources().getColor(R.color.dark_cyan))
                                .icon(GoogleMaterial.Icon.gmd_share);
                menuItem.setIcon(shareDrawable);
                break;

            case R.id.item_support_us:
                menuItem.setChecked(true);
                IconicsDrawable supportDrawable =
                        new IconicsDrawable(ControlPanelActivity.this)
                                .color(getResources().getColor(R.color.dark_cyan))
                                .icon(GoogleMaterial.Icon.gmd_stars);
                menuItem.setIcon(supportDrawable);
                break;

            case R.id.item_switch_app:
                menuItem.setChecked(true);
                IconicsDrawable swapDrawable =
                        new IconicsDrawable(ControlPanelActivity.this)
                                .color(getResources().getColor(R.color.dark_cyan))
                                .icon(GoogleMaterial.Icon.gmd_swap_horiz);
                menuItem.setIcon(swapDrawable);
                break;

        }
        mDrawer.closeDrawer(Gravity.RIGHT);
    }

    public void removeActiveTabTopNavMenuFragment() {
        FragmentManager childFragmentManager = mMainFragmentAdapter.getItem(mActiveTab)
                .getChildFragmentManager();

        if (childFragmentManager.getBackStackEntryCount() > 0) {
            List<Fragment> fragments = childFragmentManager.getFragments();
            Fragment topFragment = fragments.get(fragments.size() - 1);
//            Fragment topFragment = childFragmentManager.findFragmentById(R.id.child_fragment_root);
            if (topFragment != null) {

                if (topFragment.getClass().getName() == mContactFragment.getClass().getName() ||
                        topFragment.getClass().getName() == mAboutFragment.getClass().getName()) {
                    FragmentTransaction fragmentTransaction = childFragmentManager.beginTransaction();
                    fragmentTransaction.remove(topFragment);
                    fragmentTransaction.commit();
                    childFragmentManager.popBackStackImmediate();
                    childFragmentManager.executePendingTransactions();
                }
            }
        }
    }

    public void switchFragment(Fragment fragment, boolean isExternal, String toolbarTitle) {
        WindowUtil.hideKeyboard(ControlPanelActivity.this);
        try {
            logE("inja mActiveTab in switchFragment " + mActiveTab);
            FragmentManager childFragmentManager = mMainFragmentAdapter.getItem(mActiveTab)
                    .getChildFragmentManager();
            childFragmentManager.executePendingTransactions();
            removeActiveTabTopNavMenuFragment();

            if (fragment.isAdded()) {
                logE("inja fragment.isAdded");
                return;
            }

            FragmentTransaction fragmentTransaction = childFragmentManager.beginTransaction();

            fragmentTransaction.add(R.id.child_fragment_root, fragment);

            fragmentTransaction.addToBackStack(null);

            fragmentTransaction.commitAllowingStateLoss();

            childFragmentManager.executePendingTransactions();

            if (!TextUtils.isEmpty(toolbarTitle)) {
                mTitle.setText(toolbarTitle);
            }
        } catch (Exception e) {
            logE("Exception While Switching Fragments in CPA." + e);
        }
    }

    public void displayShopPage(String shopId, boolean isExternal) {
        setShopId(shopId);
//        mMainTabBar.deselectAllTabs();
        switchFragment(ShopPageFragment.getInstance(shopId), isExternal, getResources().getString(R.string.art_shop));
    }

    public void displayEventPage(String eventId, boolean isExternal) {
        setEventId(eventId);
//        mMainTabBar.deselectAllTabs();
        switchFragment(EventPageFragment.getInstance(eventId), isExternal, getResources().getString(R.string.art_event));
    }

    public void displayItemPage(String itemId, boolean isExternal) {
        setItemId(itemId);
        switchFragment(ItemPageFragment.getInstance(itemId), isExternal, "مشاهده محصول");
        //TODO SET ITEM CATEGORY AS TITLE
//        mTitle.setText();
    }

    public void setShopId(String shopId) {
        mShopId = shopId;
    }

    public void setEventId(String eventId) {
        mEventId = eventId;
    }

    public void setItemId(String itemId) {
        mItemId = itemId;
    }


    @Override
    public void onTabSelect(Object tabTag, boolean userTriggered) {
        logE("inja onTabSelect");
        WindowUtil.hideKeyboard(ControlPanelActivity.this);
        int tag = (Integer) tabTag;
        removeActiveTabTopNavMenuFragment();
        if (mActiveTab == Integer.valueOf(TAB_SEARCH) && tag != Integer.valueOf(TAB_SEARCH)) {
            SearchFragment searchFragment = (SearchFragment) mMainFragmentAdapter.getDefaultFragmentForTab(TAB_SEARCH);
            searchFragment.resetFields();
        }
        resetMenuIcons();
        mActiveTab = tag;
        switch (tag) {
            case TAB_ITEMS:
                mViewPager.setCurrentItem(TAB_ITEMS, false);
                break;
            case TAB_EVENTS:
                mViewPager.setCurrentItem(TAB_EVENTS, false);
                break;
            case TAB_SHOPS:
                mViewPager.setCurrentItem(TAB_SHOPS, false);
                break;
            case TAB_SEARCH:
                mViewPager.setCurrentItem(TAB_SEARCH, false);
                break;
            default:
                mViewPager.setCurrentItem(TAB_ITEMS, false);
                break;
        }
        mMainFragmentAdapter.getItem(tag).onTabClick();
    }

    @Override
    public void onSelectedTabClick(Object tabTag, boolean byUser) {
        logE("inja onSelectedTabClick");
        mMainTabBar.selectTabViewWithTabTag(tabTag);
        WindowUtil.hideKeyboard(ControlPanelActivity.this);
        int tag = (int) tabTag;
        mMainFragmentAdapter.getItem(tag).onSelectedTabClick();
        removeActiveTabTopNavMenuFragment();
    }

    public void setDefaultTab() {
        // Fetch the selected tab index with default
        int selectedTabIndex = getIntent().getIntExtra(SELECTED_TAB_EXTRA_KEY, TAB_ITEMS);
        // Switch to page based on index
        mViewPager.setCurrentItem(selectedTabIndex);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        menu.findItem(R.id.search).setIcon(new IconicsDrawable(ControlPanelActivity.this)
//                .icon(GoogleMaterial.Icon.gmd_search)
//                .color(Color.WHITE)
//                .sizeDp(20));
        return true;
    }


    @Override
    public void onBackPressed() {
        mMainTabBar.selectTabViewWithTabTag(mActiveTab);
        resetMenuIcons();
        if (!mMainFragmentAdapter.getItem(mActiveTab).back()) {
            if (mActiveTab != TAB_ITEMS) {
                mMainTabBar.setSelectedTab(TAB_ITEMS);
            } else {
                finish();
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
        handleExternalIntent(intent);
    }

    private void processIntent(Intent intent) {
        Uri data = intent.getData();
        if (BuildConfig.DEBUG) {
            logD("processIntent :: data= " + data);
        }

        if (data != null) {
            if (intent.getAction().equals(Intent.ACTION_VIEW)) {
                List<String> segments = data.getPathSegments();
                if (segments.size() > 1 && segments.get(0).equals("shop")) {
                    String shopId = segments.get(1).replace("/", "");
                    mMainTabBar.setSelectedTab(TAB_SHOPS);
                    displayShopPage(shopId, true);
                    return;
                }

                if (segments.size() > 1 && segments.get(0).equals("event")) {
                    String eventId = segments.get(1).replace("/", "");
                    mMainTabBar.setSelectedTab(TAB_EVENTS);
                    displayEventPage(eventId, true);
                    return;
                }

                if (segments.size() > 1 && segments.get(0).equals("item")) {
                    String itemId = segments.get(1).replace("/", "");
                    mMainTabBar.setSelectedTab(TAB_ITEMS);
                    displayItemPage(itemId, true);
                    return;
                }
            }
        }
    }

    public void handleExternalIntent(final Intent intent) {
        findViewById(R.id.tab_bar).getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @SuppressLint("NewApi")
                    @SuppressWarnings("deprecation")
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            findViewById(R.id.tab_bar).getViewTreeObserver()
                                    .removeGlobalOnLayoutListener(this);
                        } else {
                            findViewById(R.id.tab_bar).getViewTreeObserver()
                                    .removeOnGlobalLayoutListener(this);
                        }
                        processIntent(intent);
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mDrawer.isDrawerOpen(Gravity.RIGHT)) {
                    mDrawer.closeDrawer(Gravity.RIGHT);
                } else {
                    mDrawer.openDrawer(Gravity.RIGHT);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


}