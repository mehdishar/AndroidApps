package net.honarnama.sell.fragments;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.parse.ImageSelector;

import net.honarnama.GRPCUtils;
import net.honarnama.HonarnamaBaseApp;
import net.honarnama.base.BuildConfig;
import net.honarnama.core.activity.ChooseArtCategoryActivity;
import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.core.model.ArtCategory;
import net.honarnama.core.model.Item;
import net.honarnama.core.model.Store;
import net.honarnama.core.utils.GenericGravityTextWatcher;
import net.honarnama.nano.ArtCategoryId;
import net.honarnama.nano.CreateOrUpdateItemReply;
import net.honarnama.nano.CreateOrUpdateItemRequest;
import net.honarnama.nano.GetItemReply;
import net.honarnama.nano.GetOrDeleteItemRequest;
import net.honarnama.nano.HonarnamaProto;
import net.honarnama.nano.ReplyProperties;
import net.honarnama.nano.RequestProperties;
import net.honarnama.nano.SellServiceGrpc;
import net.honarnama.sell.model.HonarnamaUser;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.core.utils.PriceFormatterTextWatcher;
import net.honarnama.core.utils.TextUtil;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;
import net.honarnama.sell.activity.ControlPanelActivity;
import net.honarnama.sell.utils.AwsUploader;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import bolts.Continuation;
import bolts.Task;
import io.fabric.sdk.android.services.concurrency.AsyncTask;


public class EditItemFragment extends HonarnamaBaseFragment implements View.OnClickListener {

    public static EditItemFragment mEditItemFragment;

    private static final String SAVE_INSTANCE_STATE_KEY_DIRTY = "dirty";
    private static final String SAVE_INSTANCE_STATE_KEY_ITEM_ID = "itemId";
    private static final String SAVE_INSTANCE_STATE_KEY_TITLE = "title";
    private static final String SAVE_INSTANCE_STATE_KEY_DESCRIPTION = "description";
    private static final String SAVE_INSTANCE_STATE_KEY_PRICE = "price";
    private static final String SAVE_INSTANCE_STATE_KEY_CATEGORY_ID = "categoryId";
    private static final String SAVE_INSTANCE_STATE_KEY_CATEGORY_PARENT_ID = "categoryParentId";
    private static final String SAVE_INSTANCE_STATE_KEY_CATEGORY_NAME = "categoryName";

    private EditText mTitleEditText;
    private EditText mDescriptionEditText;
    private EditText mPriceEditText;
    private TextView mImagesTitleTextView;
    private ScrollView mScrollView;

    private ProgressDialog mLoadingDialog;
    private Button mChooseCategoryButton;
    private TextView mCategoryTextView;

    private ImageSelector[] mItemImages;

    private net.honarnama.nano.Item mItem;
    private long mItemId;

    private boolean mDirty = false;
    private boolean mCreateNew = false;

    private int mCategoryId = -1;
    private int mCategoryParentId = -1;
    private String mCategoryName;

    public Store mStore = null;
    private boolean mFragmentHasView = false;

    private Tracker mTracker;

    ProgressDialog mProgressDialog;

    public synchronized static EditItemFragment getInstance() {
        if (mEditItemFragment == null) {
            mEditItemFragment = new EditItemFragment();
        }
        return mEditItemFragment;
    }

    public void reset(Context context, boolean createNew) {

        if (mFragmentHasView) {
            mTitleEditText.setText("");
            mDescriptionEditText.setText("");
            mPriceEditText.setText("");
            mChooseCategoryButton.setText(context.getString(R.string.select));
            for (ImageSelector imageSelector : mItemImages) {
                imageSelector.removeSelectedImage();
            }
            mTitleEditText.setError(null);
            mDescriptionEditText.setError(null);
            mPriceEditText.setError(null);
            mCategoryTextView.setError(null);
            mImagesTitleTextView.setError(null);
        }
        mItem = null;
        mItemId = -1;
        mCategoryId = -1;
        mCategoryParentId = -1;
        mCategoryName = null;
        setDirty(false);
        mCreateNew = createNew;
    }

    public void setItemId(Context context, int itemId) {
        reset(context, false);
        mItemId = itemId;
    }

    private void setDirty(boolean dirty) {
        mDirty = dirty;
    }

    public boolean isDirty() {
        return mDirty;
    }

    @Override
    public String getTitle(Context context) {
        if (mItemId > 0) {
            return context.getString(R.string.nav_title_edit_item);
        } else {
            return context.getString(R.string.register_new_item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        TextWatcher textWatcherToMarkDirty = new TextWatcher() {
            String mValue;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mValue = charSequence.toString();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mValue != editable + "") {
                    mDirty = true;
                }
            }
        };
        mTitleEditText.addTextChangedListener(textWatcherToMarkDirty);
        mDescriptionEditText.addTextChangedListener(textWatcherToMarkDirty);
        mPriceEditText.addTextChangedListener(textWatcherToMarkDirty);
        mPriceEditText.addTextChangedListener(new GenericGravityTextWatcher(mPriceEditText));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
            Intent intent = new Intent(getActivity(), ControlPanelActivity.class);
            getActivity().finish();
            startActivity(intent);
        }

        mFragmentHasView = true;
        final View rootView = inflater.inflate(R.layout.fragment_edit_item, container, false);

        mTitleEditText = (EditText) rootView.findViewById(R.id.editProductTitle);

        mDescriptionEditText = (EditText) rootView.findViewById(R.id.editProductDescription);

        mPriceEditText = (EditText) rootView.findViewById(R.id.editItemPrice);
        mPriceEditText.addTextChangedListener(new PriceFormatterTextWatcher(mPriceEditText));

        mImagesTitleTextView = (TextView) rootView.findViewById(R.id.edit_item_images_title_text_view);
        mScrollView = (ScrollView) rootView.findViewById(R.id.edit_item_scroll_view);

        mCategoryTextView = (TextView) rootView.findViewById(R.id.edit_item_category_text_view);
        mChooseCategoryButton = (Button) rootView.findViewById(R.id.edit_item_category_semi_button);

        mChooseCategoryButton.setOnClickListener(this);

        ImageSelector.OnImageSelectedListener onImageSelectedListener =
                new ImageSelector.OnImageSelectedListener() {
                    @Override
                    public boolean onImageSelected(Uri selectedImage, boolean cropped) {
                        mImagesTitleTextView.setError(null);
                        mDirty = true;
                        return true;
                    }

                    @Override
                    public void onImageRemoved() {
                        mDirty = true;
                    }

                    @Override
                    public void onImageSelectionFailed() {
                    }
                };

        mItemImages = new ImageSelector[]{
                (ImageSelector) rootView.findViewById(R.id.itemImage1),
                (ImageSelector) rootView.findViewById(R.id.itemImage2),
                (ImageSelector) rootView.findViewById(R.id.itemImage3),
                (ImageSelector) rootView.findViewById(R.id.itemImage4)
        };
        for (ImageSelector imageSelector : mItemImages) {
            imageSelector.setActivity(getActivity());
            imageSelector.setOnImageSelectedListener(onImageSelectedListener);
        }

        // Mind fuck starts from here
        // The fragment is created
        // * What if the wants to create a new item_row?
        //    mCreateNew = true
        // * What if we were in the middle of editing an item_row, and the user clicked on create new?
        //    mCreateNew = true
        // * What if we were in the middle of creating new item_row, and phone called, and fragment was killed?
        //    mCreateNew = false, savedDirty = true, savedItemId = null
        // * What if we were in the middle of editing an item_row, and phone called, and fragment was killed?
        //    mCreateNew = false, savedDirty = true, savedItemId = THE_ID
        // * What if the wants to edit an item_row?
        //    mCreateNew = false, savedDirty = false, mItemId = THE_ID

        if (BuildConfig.DEBUG) {
            logD("onCreateView :: mCreateNew= " + mCreateNew);
        }
        if (mCreateNew) {
            reset(getActivity(), true);
            mTracker = HonarnamaSellApp.getInstance().getDefaultTracker();
            mTracker.setScreenName("AddItem");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        } else {
            mTracker = HonarnamaSellApp.getInstance().getDefaultTracker();
            mTracker.setScreenName("EditItem");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());

            boolean savedDirty = false;
            long savedItemId = -1;
            if (savedInstanceState != null) {
                savedDirty = savedInstanceState.getBoolean(SAVE_INSTANCE_STATE_KEY_DIRTY);
                savedItemId = savedInstanceState.getLong(SAVE_INSTANCE_STATE_KEY_ITEM_ID);
            }

            if (BuildConfig.DEBUG) {
                logD("onCreateView :: savedDirty= " + savedDirty + ", savedItemId= " + savedItemId);
            }

            if (savedDirty) {
                mDirty = true;
                for (ImageSelector imageSelector : mItemImages) {
                    imageSelector.restore(savedInstanceState);
                }
                mItemId = savedItemId;
                mTitleEditText.setText(savedInstanceState.getString("title"));
                mDescriptionEditText.setText(savedInstanceState.getString("description"));
                mChooseCategoryButton.setText(savedInstanceState.getString("categoryName"));
                mCategoryId = savedInstanceState.getInt("categoryId");
                mCategoryParentId = savedInstanceState.getInt("categoryParentId");
                mPriceEditText.setText(savedInstanceState.getString("price"));
            } else {
                if (mItemId >= 0) {
                    new getItemAsync().execute();
                }

//                if (mItemId >= 0) {
//                    showLoadingDialog();
//
//                    if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
//                        return null;
//                    }
//
//                    query.getInBackground(mItemId, new GetCallback<Item>() {
//                        @Override
//                        public void done(Item item, ParseException e) {
//                            hideLoadingDialog();
//                            if (e != null) {
//                                logE("Exception while loading item_row. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e, "mItemId= " + mItemId, e);
//                                if (isVisible()) {
//                                    Toast.makeText(getActivity(), getString(R.string.error_loading_item) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
//                                }
//                            } else {
//                                // TODO: check if still we need this
//                                mItem = item;
//                                mTitleEditText.setText(mItem.getName());
//                                mDescriptionEditText.setText(mItem.getDescription());
//                                mPriceEditText.setText(mItem.getPrice() + "");
//                                mCategoryId = mItem.getCategory().getId();
//                                mChooseCategoryButton.setText(getString(R.string.getting_information));
//                                new ArtCategory().getCategoryNameById(mCategoryId).continueWith(new Continuation<String, Object>() {
//                                    @Override
//                                    public Object then(Task<String> task) throws Exception {
//                                        if (task.isFaulted()) {
//                                            if (isVisible()) {
//                                                Toast.makeText(getActivity(), getString(R.string.error_finding_category_name) + getString(R.string.please_check_internet_connection), Toast.LENGTH_SHORT).show();
//                                            }
//                                        } else {
//                                            mChooseCategoryButton.setText(task.getResult());
//                                        }
//                                        return null;
//                                    }
//                                });
//
//                                ParseFile[] images = mItem.getImages();
//
//                                int counter = -1;
//                                for (int i = 0; i < Item.NUMBER_OF_IMAGES; i++) {
//                                    if (images[i] != null) {
//                                        counter++;
//                                        switch (counter) {
//                                            case 0:
//                                                rootView.findViewById(R.id.loadingPanel_1).setVisibility(View.VISIBLE);
//                                                rootView.findViewById(R.id.itemImage1).setVisibility(View.GONE);
//                                                break;
//                                            case 1:
//                                                rootView.findViewById(R.id.loadingPanel_2).setVisibility(View.VISIBLE);
//                                                rootView.findViewById(R.id.itemImage2).setVisibility(View.GONE);
//                                                break;
//                                            case 2:
//                                                rootView.findViewById(R.id.loadingPanel_3).setVisibility(View.VISIBLE);
//                                                rootView.findViewById(R.id.itemImage3).setVisibility(View.GONE);
//                                                break;
//                                            case 3:
//                                                rootView.findViewById(R.id.loadingPanel_4).setVisibility(View.VISIBLE);
//                                                rootView.findViewById(R.id.itemImage4).setVisibility(View.GONE);
//                                                break;
//                                        }
//                                        final int finalCounter = counter;
//                                        mItemImages[counter].loadInBackground(images[i], new GetDataCallback() {
//                                            @Override
//                                            public void done(byte[] data, ParseException e) {
//                                                switch (finalCounter) {
//                                                    case 0:
//                                                        rootView.findViewById(R.id.loadingPanel_1).setVisibility(View.GONE);
//                                                        rootView.findViewById(R.id.itemImage1).setVisibility(View.VISIBLE);
//                                                        break;
//                                                    case 1:
//                                                        rootView.findViewById(R.id.loadingPanel_2).setVisibility(View.GONE);
//                                                        rootView.findViewById(R.id.itemImage2).setVisibility(View.VISIBLE);
//                                                        break;
//                                                    case 2:
//                                                        rootView.findViewById(R.id.loadingPanel_3).setVisibility(View.GONE);
//                                                        rootView.findViewById(R.id.itemImage3).setVisibility(View.VISIBLE);
//                                                        break;
//                                                    case 3:
//                                                        rootView.findViewById(R.id.loadingPanel_4).setVisibility(View.GONE);
//                                                        rootView.findViewById(R.id.itemImage4).setVisibility(View.VISIBLE);
//                                                        break;
//                                                }
//                                                if (e == null) {
//                                                    if (data != null) {
//                                                        if (BuildConfig.DEBUG) {
//                                                            logD("Fetched! Data length: " + data.length);
//                                                        }
//                                                    }
//                                                } else {
//                                                    if (isVisible()) {
//                                                        Toast.makeText(getActivity(), getString(R.string.error_displaying_image) + getString(R.string.please_check_internet_connection), Toast.LENGTH_SHORT).show();
//                                                    }
//
//                                                    logE("Exception while loading image. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e, "", e);
//                                                }
//                                            }
//                                        });
//                                    }
//                                }
//                                mDirty = false;
//                            }
//                        }
//                    });
//                } else {
////                    logE("Unexpected state!");
//                }
            }
        }

        rootView.findViewById(R.id.saveItemButton).setOnClickListener(this);

        return rootView;
    }

    private void showLoadingDialog() {
        if (mLoadingDialog == null || !mLoadingDialog.isShowing()) {
            mLoadingDialog = ProgressDialog.show(getActivity(), "", getString(R.string.please_wait), false);
            mLoadingDialog.setCancelable(false);
        }
    }

    private void hideLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.hide();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.saveItemButton:
                if (isFormInputsValid()) {
//                    saveItem();
                    if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
                        return;
                    }
                    new CreateOrUpdateItemAsync().execute();
                }
                break;
            case R.id.edit_item_category_semi_button:
                Intent intent = new Intent(getActivity(), ChooseArtCategoryActivity.class);
                intent.putExtra(HonarnamaBaseApp.EXTRA_KEY_INTENT_CALLER, HonarnamaBaseApp.PREF_NAME_SELL_APP);
                startActivityForResult(intent, HonarnamaSellApp.INTENT_CHOOSE_CATEGORY_CODE);
                break;
            default:
                break;
        }
    }

    private boolean isFormInputsValid() {

        final String title = mTitleEditText.getText().toString();

        final String price = TextUtil.normalizePrice(mPriceEditText.getText().toString());

        final String description = mDescriptionEditText.getText().toString();


        if (!NetworkManager.getInstance().isNetworkEnabled(true)) {
            return false;
        }

        boolean noImage = true;
        for (ImageSelector imageSelector : mItemImages) {
            if ((imageSelector.getFinalImageUri() != null) || (imageSelector.isFileSet() && !imageSelector.isDeleted())) {
                noImage = false;
                break;
            }
        }
        if (noImage) {
            mImagesTitleTextView.requestFocus();
            mImagesTitleTextView.setError(getString(R.string.error_edit_item_no_image));
            mScrollView.fullScroll(ScrollView.FOCUS_UP);
            return false;
        }

        if (title.trim().length() == 0) {
            mTitleEditText.requestFocus();
            mTitleEditText.setError(getString(R.string.error_edit_item_title_is_empty));
            return false;
        }

        if (price.trim().length() == 0) {
            mPriceEditText.requestFocus();
            mPriceEditText.setError(getString(R.string.error_item_price_not_set));
            return false;
        }

        if (Integer.valueOf(price.trim()) < 100) {
            mPriceEditText.requestFocus();
            mPriceEditText.setError(getString(R.string.error_item_price_is_low));
            return false;
        }


        if (mCategoryId < 0) {
            mCategoryTextView.requestFocus();
            mCategoryTextView.setError(getString(R.string.error_category_is_not_selected));
            return false;
        }

        if (description.trim().length() == 0) {
            mDescriptionEditText.requestFocus();
            mDescriptionEditText.setError(getString(R.string.error_edit_item_description_is_empty));
            return false;
        }

        if (!mDirty) {
            if (isVisible()) {
                Toast.makeText(getActivity(), getString(R.string.item_not_changed), Toast.LENGTH_LONG).show();
            }
            return false;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case HonarnamaSellApp.INTENT_CHOOSE_CATEGORY_CODE:

                if (resultCode == getActivity().RESULT_OK) {
                    mCategoryTextView.setError(null);
                    mCategoryName = data.getStringExtra(HonarnamaBaseApp.EXTRA_KEY_CATEGORY_NAME);
                    mCategoryId = data.getIntExtra(HonarnamaBaseApp.EXTRA_KEY_CATEGORY_ID, 0);
                    mCategoryParentId = data.getIntExtra(HonarnamaBaseApp.EXTRA_KEY_CATEGORY_PARENT_ID, 0);
                    setDirty(true);
                    mChooseCategoryButton.setText(mCategoryName);
                }
                break;
            default:
                for (ImageSelector imageSelector : mItemImages) {
                    if (imageSelector.onActivityResult(requestCode, resultCode, data)) {
                        return;
                    }
                }
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mDirty) {
            for (ImageSelector imageSelector : mItemImages) {
                imageSelector.onSaveInstanceState(outState);
            }
            outState.putBoolean(SAVE_INSTANCE_STATE_KEY_DIRTY, true);
            outState.putLong(SAVE_INSTANCE_STATE_KEY_ITEM_ID, mItemId);
            outState.putString(SAVE_INSTANCE_STATE_KEY_TITLE, mTitleEditText.getText().toString().trim());
            outState.putString(SAVE_INSTANCE_STATE_KEY_DESCRIPTION, mDescriptionEditText.getText().toString().trim());
            outState.putString(SAVE_INSTANCE_STATE_KEY_PRICE, mPriceEditText.getText().toString().trim());
            outState.putInt(SAVE_INSTANCE_STATE_KEY_CATEGORY_ID, mCategoryId);
            outState.putInt(SAVE_INSTANCE_STATE_KEY_CATEGORY_PARENT_ID, mCategoryId);
            outState.putString(SAVE_INSTANCE_STATE_KEY_CATEGORY_NAME, mChooseCategoryButton.getText().toString());
        }
    }

    public class getItemAsync extends AsyncTask<Void, Void, GetItemReply> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setCancelable(false);
                mProgressDialog.setMessage(getString(R.string.please_wait));
            }
            if (getActivity() != null && isVisible()) {
                //TODO check if this checking prevents exception or not
                //TODO  if this checking prevents add to others dialog too
                mProgressDialog.show();
            }
        }

        @Override
        protected GetItemReply doInBackground(Void... voids) {
            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();

            GetOrDeleteItemRequest getOrDeleteItemRequest = new GetOrDeleteItemRequest();
            getOrDeleteItemRequest.id = mItemId;
            getOrDeleteItemRequest.requestProperties = rp;
            GetItemReply getItemReply;

            try {
                SellServiceGrpc.SellServiceBlockingStub stub = GRPCUtils.getInstance().getSellServiceGrpc();
                getItemReply = stub.getItem(getOrDeleteItemRequest);
                return getItemReply;
            } catch (InterruptedException e) {
                logE("Error getting user info. Error: " + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(GetItemReply getItemReply) {
            super.onPostExecute(getItemReply);
            if (!getActivity().isFinishing()) { // or call isFinishing() if min sdk version < 17
                dismissProgressDialog();
            }
            if (getItemReply != null) {
                switch (getItemReply.replyProperties.statusCode) {
                    case ReplyProperties.UPGRADE_REQUIRED:
                        ControlPanelActivity controlPanelActivity = ((ControlPanelActivity) getActivity());
                        if (controlPanelActivity != null) {
                            controlPanelActivity.displayUpgradeRequiredDialog();
                        }
                        break;
                    case ReplyProperties.CLIENT_ERROR:
                        switch (getItemReply.errorCode) {
                            case GetItemReply.ITEM_NOT_FOUND:
                                logE("inja Store not found");
                                break;

                            case GetItemReply.NO_CLIENT_ERROR:
                                //TODO bug report
                                break;
                        }
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        //TODO
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        //TODO toast
                        HonarnamaUser.logout(getActivity());
                        break;

                    case ReplyProperties.OK:
                        setItemInfo(getItemReply.item);
                        break;
                }

            } else {
                //TODO toast
            }
        }
    }

    private void dismissProgressDialog() {
        if (!getActivity().isFinishing()) {
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
        }
    }

    public void setItemInfo(net.honarnama.nano.Item item) {
        mItem = item;
        mItemId = item.id;

        mTitleEditText.setText(item.name);
        mDescriptionEditText.setText(item.description);
        mPriceEditText.setText(item.price + "");

        if (item.artCategoryId.level2Id > 0) {
            mCategoryParentId = item.artCategoryId.level1Id;
            mCategoryId = item.artCategoryId.level2Id;
        } else {
            mCategoryParentId = 0;
            mCategoryId = item.artCategoryId.level1Id;
        }
        mChooseCategoryButton.setText(getString(R.string.getting_information));
        new ArtCategory().getCategoryNameById(mCategoryId).continueWith(new Continuation<String, Object>() {
            @Override
            public Object then(Task<String> task) throws Exception {
                if (task.isFaulted()) {
                    if (isVisible()) {
                        Toast.makeText(getActivity(), getString(R.string.error_finding_category_name) + getString(R.string.please_check_internet_connection), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mChooseCategoryButton.setText(task.getResult());
                }
                return null;
            }
        });
    }

    public class CreateOrUpdateItemAsync extends AsyncTask<Void, Void, CreateOrUpdateItemReply> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mProgressDialog == null) {
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setCancelable(false);
                mProgressDialog.setMessage(getString(R.string.please_wait));
            }
            mProgressDialog.show();
        }


        @Override
        protected CreateOrUpdateItemReply doInBackground(Void... voids) {

            final String title = mTitleEditText.getText().toString().trim();
            final String description = mDescriptionEditText.getText().toString().trim();
            final long price = Integer.valueOf(TextUtil.normalizePrice(TextUtil.convertFaNumberToEn(mPriceEditText.getText().toString().trim())));

            RequestProperties rp = GRPCUtils.newRPWithDeviceInfo();

            CreateOrUpdateItemRequest createOrUpdateItemRequest = new CreateOrUpdateItemRequest();
            createOrUpdateItemRequest.item = new net.honarnama.nano.Item();
            createOrUpdateItemRequest.item.name = title;
            createOrUpdateItemRequest.item.description = description;
            createOrUpdateItemRequest.item.price = price;
            createOrUpdateItemRequest.item.artCategoryId = new ArtCategoryId();
            if (mCategoryParentId == 0) {
                createOrUpdateItemRequest.item.artCategoryId.level1Id = mCategoryId;
            } else {
                createOrUpdateItemRequest.item.artCategoryId.level1Id = mCategoryParentId;
                createOrUpdateItemRequest.item.artCategoryId.level2Id = mCategoryId;
            }

            createOrUpdateItemRequest.changingImage = new int[4];
            for (int i = 0; i < mItemImages.length; i++) {
                if (mItemImages[i].isDeleted()) {
                    logE("inja Delete item image " + i);
                    createOrUpdateItemRequest.changingImage[i] = HonarnamaProto.DELETE;
                } else if (mItemImages[i].isChanged() && mItemImages[i].getFinalImageUri() != null) {
                    createOrUpdateItemRequest.changingImage[i] = HonarnamaProto.PUT;
                } else {
                    createOrUpdateItemRequest.changingImage[i] = HonarnamaProto.NOOP;
                }
            }

            createOrUpdateItemRequest.requestProperties = rp;

            CreateOrUpdateItemReply createOrUpdateItemReply;
            try {
                SellServiceGrpc.SellServiceBlockingStub stub = GRPCUtils.getInstance().getSellServiceGrpc();
                if (mItemId > 0) {
                    createOrUpdateItemReply = stub.updateItem(createOrUpdateItemRequest);
                } else {
                    createOrUpdateItemReply = stub.createItem(createOrUpdateItemRequest);
                }

                return createOrUpdateItemReply;
            } catch (InterruptedException e) {
                logE("Error getting user info. Error: " + e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(final CreateOrUpdateItemReply createOrUpdateItemReply) {
            super.onPostExecute(createOrUpdateItemReply);
            logE("inja", "createOrUpdateItemReply is " + createOrUpdateItemReply);
            if (createOrUpdateItemReply != null) {
                switch (createOrUpdateItemReply.replyProperties.statusCode) {
                    case ReplyProperties.UPGRADE_REQUIRED:

                        dismissProgressDialog();
                        ControlPanelActivity controlPanelActivity = ((ControlPanelActivity) getActivity());
                        if (controlPanelActivity != null) {
                            controlPanelActivity.displayUpgradeRequiredDialog();
                        }
                        break;
                    case ReplyProperties.CLIENT_ERROR:
                        mDirty = true;
                        dismissProgressDialog();
                        switch (createOrUpdateItemReply.errorCode) {
                            case CreateOrUpdateItemReply.NO_CLIENT_ERROR:
                                //TODO bug report
                                break;
                            case CreateOrUpdateItemReply.ITEM_NOT_FOUND:
                                //TODO
                                break;
                            case CreateOrUpdateItemReply.STORE_NOT_CREATED:
                                //TODO
                                break;
                            case CreateOrUpdateItemReply.EMPTY_Item:
                                //TODO
                                break;
                        }
                        break;

                    case ReplyProperties.SERVER_ERROR:
                        mDirty = true;
                        dismissProgressDialog();
                        //TODO
                        break;

                    case ReplyProperties.NOT_AUTHORIZED:
                        dismissProgressDialog();
                        //TODO toast
                        HonarnamaUser.logout(getActivity());
                        break;

                    case ReplyProperties.OK:
                        setItemInfo(createOrUpdateItemReply.uptodateItem);

                        ArrayList<Task<Void>> tasks = new ArrayList<>();
                        for (int i = 0; i < mItemImages.length; i++) {
                            if (!TextUtils.isEmpty(createOrUpdateItemReply.imageModificationUrl[i]) && mItemImages[i].getFinalImageUri() != null) {
                                logE("inja Adding item " + i + "to upload task.");
                                final File storeBannerImageFile = new File(mItemImages[i].getFinalImageUri().getPath());
                                tasks.add(new AwsUploader(storeBannerImageFile, createOrUpdateItemReply.imageModificationUrl[i]).upload());
                            }
                        }

                        Task.whenAll(tasks).continueWith(new Continuation<Void, Object>() {
                            @Override
                            public Object then(Task<Void> task) throws Exception {
                                dismissProgressDialog();
                                if (task.isFaulted()) {
                                    mDirty = true;
                                    if (isVisible()) {
                                        Toast.makeText(getActivity(), getString(R.string.error_sending_images) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    mDirty = false;
                                    if (isVisible()) {
                                        Toast.makeText(getActivity(), getString(R.string.item_saved_successfully), Toast.LENGTH_LONG).show();
                                    }

                                }
                                return null;
                            }
                        });


                        break;
                }

            } else {
                //TODO toast
            }
        }
    }

}
