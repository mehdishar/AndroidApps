package net.honarnama.browse.fragment;

import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.browse.adapter.EventsAdapter;
import net.honarnama.browse.adapter.ItemsAdapter;
import net.honarnama.browse.adapter.ShopsAdapter;
import net.honarnama.base.utils.NetworkManager;
import net.honarnama.base.utils.WindowUtil;
import net.honarnama.nano.Event;
import net.honarnama.nano.Store;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;


/**
 * Created by elnaz on 2/11/16.
 */
public class SearchFragment extends HonarnamaBrowseFragment implements View.OnClickListener, AdapterView.OnItemClickListener {
    public static SearchFragment mSearchFragment;
    private ListView mListView;

    ItemsAdapter mItemsAdapter;
    ShopsAdapter mShopsAdapter;
    EventsAdapter mEventsAdapterr;

    String msearchTerm;
    public EditText mSearchEditText;
    public View mSearchButton;
    public RelativeLayout mEmptyListContainer;
    public LinearLayout mLoadingCircle;


    private ToggleButton mItemsToggleButton;
    private ToggleButton mShopsToggleButton;
    private ToggleButton mEventsToggleButton;

    public RelativeLayout mOnErrorRetry;

    public SearchSegment mSearchSegment;

    public synchronized static SearchFragment getInstance() {
        if (mSearchFragment == null) {
            mSearchFragment = new SearchFragment();
        }
        return mSearchFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        mSearchEditText = (EditText) rootView.findViewById(R.id.serach_term);
        mSearchButton = rootView.findViewById(R.id.search_btn);

        mItemsToggleButton = (ToggleButton) rootView.findViewById(R.id.items_tg_btn);
        mShopsToggleButton = (ToggleButton) rootView.findViewById(R.id.shops_tg_btn);
        mEventsToggleButton = (ToggleButton) rootView.findViewById(R.id.events_tg_btn);
        mItemsToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemsToggleButton.setChecked(true);
                mShopsToggleButton.setChecked(false);
                mEventsToggleButton.setChecked(false);
            }
        });
        mShopsToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShopsToggleButton.setChecked(true);
                mItemsToggleButton.setChecked(false);
                mEventsToggleButton.setChecked(false);
            }
        });

        mEventsToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEventsToggleButton.setChecked(true);
                mItemsToggleButton.setChecked(false);
                mShopsToggleButton.setChecked(false);
            }
        });

        mEmptyListContainer = (RelativeLayout) rootView.findViewById(R.id.empty_list_container);
        mLoadingCircle = (LinearLayout) rootView.findViewById(R.id.loading_circle_container);
        mListView = (ListView) rootView.findViewById(R.id.listView);

        mSearchButton.setOnClickListener(this);

        mItemsAdapter = new ItemsAdapter(HonarnamaBrowseApp.getInstance());
        mShopsAdapter = new ShopsAdapter(HonarnamaBrowseApp.getInstance());
        mEventsAdapterr = new EventsAdapter(HonarnamaBrowseApp.getInstance());

        mOnErrorRetry = (RelativeLayout) rootView.findViewById(R.id.on_error_retry_container);
        mOnErrorRetry.setOnClickListener(this);

        mListView.setOnItemClickListener(this);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSearchSegment != null) {
            if (mSearchSegment == SearchSegment.ITEMS) {
                mItemsToggleButton.setChecked(true);
                mShopsToggleButton.setChecked(false);
                mEventsToggleButton.setChecked(false);
                mListView.setAdapter(mItemsAdapter);
                searchItems();
            } else if (mSearchSegment == SearchSegment.SHOPS) {
                mShopsToggleButton.setChecked(true);
                mItemsToggleButton.setChecked(false);
                mEventsToggleButton.setChecked(false);
                mListView.setAdapter(mShopsAdapter);
                searchShops();
            } else {
                if (mSearchSegment == SearchSegment.EVENTS) {
                    mEventsToggleButton.setChecked(true);
                    mItemsToggleButton.setChecked(false);
                    mShopsToggleButton.setChecked(false);
                    mListView.setAdapter(mEventsAdapterr);
                    searchEvents();
                }
            }

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }


    @Override
    public String getTitle(Context context) {
        return getString(R.string.hornama);
    }

    @Override
    public void onSelectedTabClick() {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
        if (mSearchSegment == SearchSegment.ITEMS) {
            //TODO
//            ParseObject selectedItem = (ParseObject) mItemsAdapter.getItem(position);
//            if (selectedItem != null) {
//                controlPanelActivity.displayItemPage(selectedItem.getObjectId(), false);
//            }
        }

        if (mSearchSegment == SearchSegment.SHOPS) {
            //TODO
//            ParseObject selectedShop = (ParseObject) mShopsAdapter.getItem(position);
//            if (selectedShop != null) {
//                controlPanelActivity.displayShopPage(selectedShop.getObjectId(), false);
//            }
        }

        if (mSearchSegment == SearchSegment.EVENTS) {
            //TODO
//            ParseObject selectedEvent = (ParseObject) mEventsAdapterr.getItem(position);
//            if (selectedEvent != null) {
//                controlPanelActivity.displayEventPage(selectedEvent.getObjectId(), false);
//            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.search_btn) {
            if (isVisible()) {
                WindowUtil.hideKeyboard(getActivity());
            }
            msearchTerm = mSearchEditText.getText().toString().trim();
            if (TextUtils.isEmpty(msearchTerm)) {
                if (isVisible()) {
                    Toast.makeText(getActivity(), "عبارت مورد جستجو را وارد نکردید.", Toast.LENGTH_LONG).show();
                }
                return;
            }

            if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                return;
            }

            mEmptyListContainer.setVisibility(View.GONE);
            mLoadingCircle.setVisibility(View.VISIBLE);
            mListView.setEmptyView(mLoadingCircle);
            mOnErrorRetry.setVisibility(View.GONE);

            if (mItemsToggleButton.isChecked()) {
                mListView.setAdapter(mItemsAdapter);
                mSearchSegment = SearchSegment.ITEMS;
                searchItems();
                return;
            }
            if (mShopsToggleButton.isChecked()) {
                mListView.setAdapter(mShopsAdapter);
                mSearchSegment = SearchSegment.SHOPS;
                searchShops();
                return;
            }
            if (mEventsToggleButton.isChecked()) {
                mListView.setAdapter(mEventsAdapterr);
                mSearchSegment = SearchSegment.EVENTS;
                searchEvents();
                return;
            }
        }

        if (v.getId() == R.id.on_error_retry_container) {
            if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                return;
            }
            ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
            controlPanelActivity.refreshTopFragment();
        }
    }

    public void searchItems() {
        mEmptyListContainer.setVisibility(View.GONE);
        mLoadingCircle.setVisibility(View.VISIBLE);
        mListView.setEmptyView(mLoadingCircle);
        mOnErrorRetry.setVisibility(View.GONE);
        ArrayList emptyList = new ArrayList<>();
        mItemsAdapter.setItems(emptyList);
        mItemsAdapter.notifyDataSetChanged();
//TODO
//        Item.search(msearchTerm).continueWith(new Continuation<List<Item>, Object>() {
//            @Override
//            public Object then(Task<List<Item>> task) throws Exception {
//                mLoadingCircle.setVisibility(View.GONE);
//                mEmptyListContainer.setVisibility(View.VISIBLE);
//                mListView.setEmptyView(mEmptyListContainer);
//
//                if (task.isFaulted() && ((ParseException) task.getError()).getCode() != ParseException.OBJECT_NOT_FOUND) {
//                    logE("Searching items with search term" + msearchTerm + " failed. Error: " + task.getError(), "", task.getError());
//                    if (isVisible()) {
//                        Toast.makeText(getActivity(), HonarnamaBrowseApp.getInstance().getString(R.string.error_getting_items_list) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
//                    }
//                    mOnErrorRetry.setVisibility(View.VISIBLE);
//                } else {
//
//                    mOnErrorRetry.setVisibility(View.GONE);
//                    List<Item> foundItems = task.getResult();
//                    mItemsAdapter.setItems(foundItems);
//                    mItemsAdapter.notifyDataSetChanged();
//                }
//                return null;
//            }
//        });
    }

    public void searchShops() {
        mEmptyListContainer.setVisibility(View.GONE);
        mLoadingCircle.setVisibility(View.VISIBLE);
        mListView.setEmptyView(mLoadingCircle);
        mOnErrorRetry.setVisibility(View.GONE);
        List<Store> emptyList = new ArrayList<>();
        mShopsAdapter.setShops(emptyList);
        mShopsAdapter.notifyDataSetChanged();

//        Shop.search(msearchTerm).continueWith(new Continuation<List<Store>, Object>() {
//            @Override
//            public Object then(Task<List<Store>> task) throws Exception {
//
//                mLoadingCircle.setVisibility(View.GONE);
//                mEmptyListContainer.setVisibility(View.VISIBLE);
//                mListView.setEmptyView(mEmptyListContainer);
//
//                if (task.isFaulted() && ((ParseException) task.getError()).getCode() != ParseException.OBJECT_NOT_FOUND) {
//                    logE("Searching shops with search term" + msearchTerm + " failed. Error: " + task.getError(), task.getError());
//                    if (isVisible()) {
//                        Toast.makeText(getActivity(), HonarnamaBrowseApp.getInstance().getString(R.string.error_getting_shop_list) + getString(R.string.check_net_connection), Toast.LENGTH_LONG).show();
//                    }
//                    mOnErrorRetry.setVisibility(View.VISIBLE);
//                } else {
//                    mOnErrorRetry.setVisibility(View.GONE);
//                    List<Store> foundItems = task.getResult();
//                    mShopsAdapter.setShops(foundItems);
//                    mShopsAdapter.notifyDataSetChanged();
//                }
//                return null;
//            }
//        });
    }

    public void searchEvents() {

        mEmptyListContainer.setVisibility(View.GONE);
        mLoadingCircle.setVisibility(View.VISIBLE);
        mListView.setEmptyView(mLoadingCircle);
        mOnErrorRetry.setVisibility(View.GONE);
        List<Event> emptyList = new ArrayList<>();
        mEventsAdapterr.setEvents(emptyList);
        mEventsAdapterr.notifyDataSetChanged();

        //TODO
//        Event.search(msearchTerm).continueWith(new Continuation<List<Event>, Object>() {
//            @Override
//            public Object then(Task<List<Event>> task) throws Exception {
//                mLoadingCircle.setVisibility(View.GONE);
//                mEmptyListContainer.setVisibility(View.VISIBLE);
//                mListView.setEmptyView(mEmptyListContainer);
//
//                if (task.isFaulted() && ((ParseException) task.getError()).getCode() != ParseException.OBJECT_NOT_FOUND) {
//                    logE("Searching events with search term" + msearchTerm + " failed. Error: " + task.getError(), "", task.getError());
//                    if (isVisible()) {
//                        Toast.makeText(getActivity(), HonarnamaBrowseApp.getInstance().getString(R.string.error_getting_event_list) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
//                    }
//                    mOnErrorRetry.setVisibility(View.VISIBLE);
//                } else {
//                    mOnErrorRetry.setVisibility(View.GONE);
//                    List<Event> foundItems = task.getResult();
//                    mEventsAdapterr.setEvents(foundItems);
//                    mEventsAdapterr.notifyDataSetChanged();
//                }
//                return null;
//            }
//        });
    }

    public void resetFields() {
//        if (mSearchEditText != null) {
//            mSearchEditText.setText("");
//            mItemsToggleButton.setChecked(true);
//            mShopsToggleButton.setChecked(false);
//            mEventsToggleButton.setChecked(false);
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public enum SearchSegment {
        ITEMS("items", 0),
        SHOPS("shops", 1),
        EVENTS("events", 2);

        private String stringValue;
        private int intValue;

        private SearchSegment(String toString, int value) {
            stringValue = toString;
            intValue = value;
        }

    }
}

