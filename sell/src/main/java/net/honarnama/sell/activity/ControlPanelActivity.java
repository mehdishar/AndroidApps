package net.honarnama.sell.activity;

import com.parse.ParseObject;

import net.honarnama.HonarNamaBaseActivity;
import net.honarnama.sell.R;
import net.honarnama.sell.fragments.EditItemFragment;
import net.honarnama.sell.fragments.FragmentDrawer;
import net.honarnama.sell.fragments.ItemsFragment;
import net.honarnama.sell.fragments.ProfileFragment;
import net.honarnama.sell.fragments.StoreInfoFragment;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class ControlPanelActivity extends HonarNamaBaseActivity implements FragmentDrawer.FragmentDrawerListener {

    public static final int DRAWER_INDEX_PROFILE = 0;
    public static final int DRAWER_INDEX_STORE_INFO = 1;
    public static final int DRAWER_INDEX_ITEMS = 2;
    public static final int DRAWER_INDEX_ITEM_EDIT = 3;

    private Toolbar mToolbar;
    private FragmentDrawer mDrawerFragment;
//    private TextView mToolbarTitleTextView;
    private Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_panel);

        mToolbar = (Toolbar) findViewById(R.id.control_panel_toolbar);
//        mToolbarTitleTextView = (TextView) findViewById(R.id.toolbar_title);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.hamburger_icon);
        mDrawerFragment = (FragmentDrawer)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        mDrawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
        mDrawerFragment.setDrawerListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            mDrawerFragment.handleDrawerState();
        }

//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDrawerItemSelected(View view, int position) {
        displayView(position, null);
    }

    public void displayView(int position, ParseObject item) {

        String title = getString(R.string.app_name);
        switch (position) {
            case DRAWER_INDEX_PROFILE:
                mFragment = ProfileFragment.getInstance();
                title = getString(R.string.seller_profile);
                break;
            case DRAWER_INDEX_STORE_INFO:
                mFragment = StoreInfoFragment.getInstance();
                title = getString(R.string.nav_title_store_info);
                break;
            case DRAWER_INDEX_ITEMS:
                mFragment = ItemsFragment.newInstance();
                title = getString(R.string.nav_title_products);
                break;
            case DRAWER_INDEX_ITEM_EDIT:
                EditItemFragment editItemFragment = EditItemFragment.getInstance();
                if (item != null) {
                    title = getString(R.string.nav_title_edit_product);
                    editItemFragment.setItem(item);
                } else {
                    title = getString(R.string.nav_title_new_product);
                    editItemFragment.setItem(null);
                }
                mFragment = editItemFragment;
                break;
            default:
                break;
        }

        if (mFragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.container_body, mFragment);
            fragmentTransaction.commit();

            getSupportActionBar().setTitle(title);
            // set the toolbar title
//            mToolbarTitleTextView.setText(title);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mFragment.onActivityResult(requestCode, resultCode, intent);
    }
}
