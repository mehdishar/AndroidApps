package net.honarnama.browse.fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.bumptech.glide.Glide;
import com.parse.ImageSelector;

import net.honarnama.GRPCUtils;
import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.base.model.City;
import net.honarnama.base.model.Province;
import net.honarnama.base.utils.NetworkManager;
import net.honarnama.base.utils.ObservableScrollView;
import net.honarnama.base.utils.TextUtil;
import net.honarnama.base.utils.WindowUtil;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.browse.adapter.ItemsAdapter;
import net.honarnama.browse.dialog.ContactDialog;
import net.honarnama.nano.BrowseItemsReply;
import net.honarnama.nano.BrowseItemsRequest;
import net.honarnama.nano.BrowseServiceGrpc;
import net.honarnama.nano.BrowseStoreReply;
import net.honarnama.nano.BrowseStoreRequest;
import net.honarnama.nano.ReplyProperties;
import net.honarnama.nano.RequestProperties;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import io.fabric.sdk.android.services.concurrency.AsyncTask;

/**
 * Created by elnaz on 2/15/16.
 */
public class ShopPageFragment extends HonarnamaBrowseFragment implements View.OnClickListener, AdapterView.OnItemClickListener, ObservableScrollView.OnScrollChangedListener {
    public static ShopPageFragment mShopPageFragment;
    private ItemsAdapter mItemsAdapter;
    private ImageSelector mLogoImageView;
    private ImageSelector mBannerImageView;
    public ProgressBar mBannerProgressBar;
    public ProgressBar mLogoProgressBar;

    public TextView mShopName;
    public TextView mShopDesc;
    public TextView mShopPlace;
    public ListView mListView;

    private ObservableScrollView mScrollView;
    private View mBannerFrameLayout;
    private RelativeLayout mShare;
    public long mShopId;

    public LinearLayout mItemsloadingCircle;

    RelativeLayout mEmptyListContainer;

    public RelativeLayout mOnErrorRetry;

    public FloatingActionButton mFab;

    public RelativeLayout mInfoContainer;

    public RelativeLayout mDeletedShopMsg;

    private Tracker mTracker;

    @Override
    public String getTitle() {
        return getString(R.string.art_shop);
    }

    public synchronized static ShopPageFragment getInstance(long shopId) {
        ShopPageFragment shopPageFragment = new ShopPageFragment();
        Bundle args = new Bundle();
        args.putLong("shopId", shopId);
        shopPageFragment.setArguments(args);
//        shopPageFragment.setOwner(owner);
        return shopPageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = HonarnamaBrowseApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("ShopPageFragment");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onSelectedTabClick() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_shop_page, container, false);
        mShopId = getArguments().getLong("shopId");

        mListView = (ListView) rootView.findViewById(R.id.shop_items_listView);

        mEmptyListContainer = (RelativeLayout) rootView.findViewById(R.id.empty_list_container);

        mScrollView = (ObservableScrollView) rootView.findViewById(R.id.store_fragment_scroll_view);
        mScrollView.setOnScrollChangedListener(this);
        mBannerFrameLayout = rootView.findViewById(R.id.store_banner_frame_layout);

        mLogoImageView = (ImageSelector) rootView.findViewById(R.id.store_logo_image_view);
        mBannerImageView = (ImageSelector) rootView.findViewById(R.id.store_banner_image_view);

        mBannerProgressBar = (ProgressBar) rootView.findViewById(R.id.banner_progress_bar);
        mLogoProgressBar = (ProgressBar) rootView.findViewById(R.id.logo_progress_bar);

        mShopName = (TextView) rootView.findViewById(R.id.store_name_text_view);
        mShopDesc = (TextView) rootView.findViewById(R.id.store_desc_text_view);
        mShopPlace = (TextView) rootView.findViewById(R.id.shop_place_text_view);
        mShare = (RelativeLayout) rootView.findViewById(R.id.shop_share_container);
        mShare.setOnClickListener(this);

        mOnErrorRetry = (RelativeLayout) rootView.findViewById(R.id.on_error_retry_container);
        mOnErrorRetry.setOnClickListener(this);

        mDeletedShopMsg = (RelativeLayout) rootView.findViewById(R.id.deleted_shop_msg);
        mInfoContainer = (RelativeLayout) rootView.findViewById(R.id.store_info_container);

        mItemsloadingCircle = (LinearLayout) rootView.findViewById(R.id.shop_items_loading_circle);
        setVisibilityInFragment(mEmptyListContainer, View.GONE);
        setVisibilityInFragment(mItemsloadingCircle, View.VISIBLE);

        mFab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        mItemsAdapter = new ItemsAdapter(getActivity(), this);
        setVisibilityInFragment(mOnErrorRetry, View.GONE);

        mListView.setAdapter(mItemsAdapter);

        mListView.setOnItemClickListener(this);

        new getStoreAsync().execute();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.shop_share_container) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT,
                    getString(R.string.art_shop) + " " + mShopName.getText());
            sendIntent.putExtra(Intent.EXTRA_TEXT, "سلام،" + "\n" + "غرفه هنری " + mShopName.getText() + " تو برنامه هنرما رو ببین: " +
                    "\n" + HonarnamaBaseApp.WEB_ADDRESS + "/store/" + mShopId);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }

        if (v.getId() == R.id.on_error_retry_container) {
            if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                return;
            }
            ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
            controlPanelActivity.refreshTopFragment();
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        net.honarnama.nano.Item selectedItem = mItemsAdapter.getItem(position);
        ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
        controlPanelActivity.displayItemPage(selectedItem.id, false);
    }

    @Override
    public void onScrollChanged(int deltaX, int deltaY) {
        int scrollY = mScrollView.getScrollY();
        // Add parallax effect
        mBannerFrameLayout.setTranslationY(scrollY * 0.5f);
    }

    public class getStoreAsync extends AsyncTask<Void, Void, BrowseStoreReply> {
        BrowseStoreRequest browseStoreRequest;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setVisibilityInFragment(mOnErrorRetry, View.GONE);
            setVisibilityInFragment(mDeletedShopMsg, View.GONE);
        }

        @Override
        protected BrowseStoreReply doInBackground(Void... voids) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            browseStoreRequest = new BrowseStoreRequest();
            browseStoreRequest.requestProperties = rp;

            browseStoreRequest.id = mShopId;

            BrowseStoreReply getStoreReply;
            if (BuildConfig.DEBUG) {
                logD("Request for getting single store is: " + browseStoreRequest);
            }
            try {
                BrowseServiceGrpc.BrowseServiceBlockingStub stub = GRPCUtils.getInstance().getBrowseServiceGrpc();
                getStoreReply = stub.getStore(browseStoreRequest);
                return getStoreReply;
            } catch (Exception e) {
                logE("Error running getStore request. request: " + browseStoreRequest + ". Error: " + e, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(BrowseStoreReply browseStoreReply) {
            super.onPostExecute(browseStoreReply);

            setVisibilityInFragment(mItemsloadingCircle, View.GONE);

            Activity activity = getActivity();

            if (browseStoreReply != null) {
                switch (browseStoreReply.replyProperties.statusCode) {
                    case ReplyProperties.UPGRADE_REQUIRED:
                        if (activity != null) {
                            ControlPanelActivity controlPanelActivity = ((ControlPanelActivity) activity);
                            controlPanelActivity.displayUpgradeRequiredDialog();
                        } else {
                            displayLongToast(getStringInFragment(R.string.upgrade_to_new_version));
                        }
                        break;
                    case ReplyProperties.CLIENT_ERROR:
                        if (browseStoreReply.errorCode == BrowseStoreReply.STORE_NOT_FOUND) {
                            displayLongToast(getStringInFragment(R.string.error_shop_no_longer_exists));
                            setVisibilityInFragment(mDeletedShopMsg, View.VISIBLE);
                        } else {
                            logE("Uncaught error code for getting shop. browse request: " + browseStoreRequest);
                        }
                        break;
                    case ReplyProperties.SERVER_ERROR:
                        setVisibilityInFragment(mOnErrorRetry, View.VISIBLE);
                        displayLongToast(getStringInFragment(R.string.server_error_try_again));
                        logE("Server error running getstore request. request: " + browseStoreRequest);
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        break;

                    case ReplyProperties.OK:
                        setVisibilityInFragment(mOnErrorRetry, View.GONE);
                        if (isAdded()) {
                            net.honarnama.nano.Store store = browseStoreReply.store;
                            loadStoreInfo(store);
                        }
                        break;
                }

            } else {
                setVisibilityInFragment(mOnErrorRetry, View.VISIBLE);
                displayLongToast(getStringInFragment(R.string.check_net_connection));
            }
        }

    }

    private void loadStoreInfo(final net.honarnama.nano.Store shop) {

        setVisibilityInFragment(mFab, View.VISIBLE);
        setVisibilityInFragment(mInfoContainer, View.VISIBLE);
        setVisibilityInFragment(mOnErrorRetry, View.GONE);
        setVisibilityInFragment(mShare, View.VISIBLE);

        setTextInFragment(mShopName, TextUtil.convertEnNumberToFa(shop.name));
        setTextInFragment(mShopDesc, TextUtil.convertEnNumberToFa(shop.description));

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ContactDialog contactDialog = new ContactDialog();
                contactDialog.showDialog(getActivity(), shop.publicPhoneNumber, shop.publicCellNumber,
                        getStringInFragment(R.string.item_contact_dialog_warning_msg));

            }
        });

        String province = Province.getProvinceById(shop.locationCriteria.provinceId).getName();
        String city = City.getCityById(shop.locationCriteria.cityId).getName();

        setTextInFragment(mShopPlace, province + "، " + city);

        if (shop.logo.trim().length() > 0) {
            setVisibilityInFragment(mLogoProgressBar, View.VISIBLE);
            mLogoImageView.setSource(shop.logo, mLogoProgressBar, R.drawable.default_logo_hand);
        }

        if (shop.banner.trim().length() > 0) {
            setVisibilityInFragment(mBannerProgressBar, View.VISIBLE);
            mBannerImageView.setSource(shop.banner, mBannerProgressBar, R.drawable.party_flags);
        } else {
            mBannerImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.party_flags));
        }

        new getShopItemsAsync().execute();

    }

    public class getShopItemsAsync extends AsyncTask<Void, Void, BrowseItemsReply> {
        BrowseItemsRequest browseItemsRequest;

        TextView mEmptyListText;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            setVisibilityInFragment(mEmptyListContainer, View.VISIBLE);
            mListView.setEmptyView(mEmptyListContainer);
            mEmptyListText = (TextView) mEmptyListContainer.findViewById(R.id.empty_items_text_view);
            mEmptyListText.setText(getStringInFragment(R.string.getting_information));
        }

        @Override
        protected BrowseItemsReply doInBackground(Void... voids) {

            if (!NetworkManager.getInstance().isNetworkEnabled(false)) {
                return null;
            }

            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();
            browseItemsRequest = new BrowseItemsRequest();
            browseItemsRequest.requestProperties = rp;
            browseItemsRequest.storeId = mShopId;

            BrowseItemsReply getItemsReply;
            if (BuildConfig.DEBUG) {
                logD("Request for getting shop items is: " + browseItemsRequest);
            }
            try {
                BrowseServiceGrpc.BrowseServiceBlockingStub stub = GRPCUtils.getInstance().getBrowseServiceGrpc();
                getItemsReply = stub.getItems(browseItemsRequest);
                return getItemsReply;
            } catch (Exception e) {
                logE("Error getting shop items. request: " + browseItemsRequest + ". Error: " + e, e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(BrowseItemsReply browseItemsReply) {
            super.onPostExecute(browseItemsReply);

            if (BuildConfig.DEBUG) {
                logD("getting shop items reply: " + browseItemsReply);
            }

            Activity activity = getActivity();

            if (browseItemsReply != null) {
                switch (browseItemsReply.replyProperties.statusCode) {
                    case ReplyProperties.UPGRADE_REQUIRED:
                        if (activity != null) {
                            ControlPanelActivity controlPanelActivity = ((ControlPanelActivity) activity);
                            controlPanelActivity.displayUpgradeRequiredDialog();
                        } else {
                            displayLongToast(getStringInFragment(R.string.upgrade_to_new_version));
                        }
                        break;
                    case ReplyProperties.CLIENT_ERROR:
                        // TODO
                        break;
                    case ReplyProperties.SERVER_ERROR:
                        displayLongToast(getStringInFragment(R.string.server_error_try_again));
                        logE("Server error getting shop items. request: " + browseItemsRequest);
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        break;

                    case ReplyProperties.OK:
                        if (isAdded()) {
                            net.honarnama.nano.Item[] items = browseItemsReply.items;

                            ArrayList itemsList = new ArrayList();

                            if (items.length > 0) {
                                for (net.honarnama.nano.Item item : items) {
                                    itemsList.add(0, item);
                                }

                                setVisibilityInFragment(mEmptyListContainer, View.GONE);
                            } else {
                                mEmptyListText.setText(getStringInFragment(R.string.shop_has_no_item));
                            }

                            mItemsAdapter.setItems(itemsList);
                            mItemsAdapter.notifyDataSetChanged();

                            WindowUtil.setListViewHeightBasedOnChildren(mListView);
                        }
                        break;
                }

            } else {
                displayShortToast(getStringInFragment(R.string.check_net_connection));
            }
        }
    }

}
