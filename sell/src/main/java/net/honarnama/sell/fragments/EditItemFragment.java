package net.honarnama.sell.fragments;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ImageSelector;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;

import net.honarnama.core.activity.ChooseCategoryActivity;
import net.honarnama.core.fragment.HonarnamaBaseFragment;
import net.honarnama.core.utils.NetworkManager;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;
import net.honarnama.sell.activity.ControlPanelActivity;
import net.honarnama.sell.model.Item;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import bolts.Continuation;
import bolts.Task;


public class EditItemFragment extends HonarnamaBaseFragment implements View.OnClickListener {

    public static EditItemFragment mEditItemFragment;

    private static final String SAVE_INSTANCE_STATE_KEY_DIRTY = "dirty";
    private static final String SAVE_INSTANCE_STATE_KEY_ITEM_ID = "itemId";
    private static final String SAVE_INSTANCE_STATE_KEY_TITLE = "title";
    private static final String SAVE_INSTANCE_STATE_KEY_DESCRIPTION = "description";
    private static final String SAVE_INSTANCE_STATE_KEY_PRICE = "price";
    private static final String SAVE_INSTANCE_STATE_KEY_CATEGORY_ID = "categoryId";


    private EditText mTitleEditText;
    private EditText mDescriptionEditText;
    private TextView mImagesHintTextView;
    private EditText mPriceEditText;
    private TextView mImagesTitleTextView;
    private ScrollView mScrollView;

    private ProgressDialog mLoadingDialog;
    private Button mChooseCategoryButton;
    private TextView mCategoryTextView;

    private ImageSelector[] mItemImages;

    private Item mItem;
    private String mItemId;

    private boolean mDirty = false;
    private boolean mCreateNew = false;

    private String mCategoryId;
    private String mCategoryName;

    public synchronized static EditItemFragment getInstance() {
        if (mEditItemFragment == null) {
            mEditItemFragment = new EditItemFragment();
        }
        return mEditItemFragment;
    }

    public void reset(boolean createNew) {
        mItem = null;
        mItemId = null;

        mDirty = false;
        mCreateNew = createNew;
    }

    public void setItemId(String itemId) {
        reset(false);
        mItemId = itemId;
    }

    public boolean isDirty() {
        return mDirty;
    }

    @Override
    public String getTitle(Context context) {
        if (mItemId != null) {
            return context.getString(R.string.nav_title_edit_item);
        } else {
            return context.getString(R.string.nav_title_new_item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_item, container, false);

        mTitleEditText = (EditText) rootView.findViewById(R.id.editProductTitle);
        mDescriptionEditText = (EditText) rootView.findViewById(R.id.editProductDescription);
        mImagesHintTextView = (TextView) rootView.findViewById(R.id.itemImageHint);
        mPriceEditText = (EditText) rootView.findViewById(R.id.editItemPrice);
        mImagesTitleTextView = (TextView) rootView.findViewById(R.id.edit_item_images_title_text_view);
        mScrollView = (ScrollView) rootView.findViewById(R.id.edit_item_scroll_view);

        mCategoryTextView = (TextView) rootView.findViewById(R.id.edit_item_category_text_view);
        mChooseCategoryButton = (Button) rootView.findViewById(R.id.edit_item_category_semi_button);

        mChooseCategoryButton.setOnClickListener(this);

        ImageSelector.OnImageSelectedListener onImageSelectedListener =
                new ImageSelector.OnImageSelectedListener() {
                    @Override
                    public boolean onImageSelected(Uri selectedImage, boolean cropped) {
                        mImagesHintTextView.setError(null);
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
            imageSelector.setActivity(this.getActivity());
            imageSelector.setOnImageSelectedListener(onImageSelectedListener);
        }

        // Mind fuck starts from here
        // The fragment is created
        // * What if the wants to create a new item?
        //    mCreateNew = true
        // * What if we were in the middle of editing an item, and the user clicked on create new?
        //    mCreateNew = true
        // * What if we were in the middle of creating new item, and phone called, and fragment was killed?
        //    mCreateNew = false, savedDirty = true, savedItemId = null
        // * What if we were in the middle of editing an item, and phone called, and fragment was killed?
        //    mCreateNew = false, savedDirty = true, savedItemId = THE_ID
        // * What if the wants to edit an item?
        //    mCreateNew = false, savedDirty = false, mItemId = THE_ID

        logD(null, "onCreateView :: mCreateNew= " + mCreateNew);

        if (mCreateNew) {
            mTitleEditText.setText("");
            mDescriptionEditText.setText("");
            for (ImageSelector imageSelector : mItemImages) {
                imageSelector.removeSelectedImage();
            }
        } else {
            boolean savedDirty = false;
            String savedItemId = null;
            if (savedInstanceState != null) {
                savedDirty = savedInstanceState.getBoolean(SAVE_INSTANCE_STATE_KEY_DIRTY);
                savedItemId = savedInstanceState.getString(SAVE_INSTANCE_STATE_KEY_ITEM_ID);
            }

            logD(null, "onCreateView :: savedDirty= " + savedDirty + ", savedItemId= " + savedItemId);

            if (savedDirty) {
                mDirty = true;
                for (ImageSelector imageSelector : mItemImages) {
                    imageSelector.restore(savedInstanceState);
                }
                mItemId = savedItemId;
                mTitleEditText.setText(savedInstanceState.getString("title"));
                mDescriptionEditText.setText(savedInstanceState.getString("description"));
            } else {
                if (mItemId != null) {
                    showLoadingDialog();
                    ParseQuery<Item> query = ParseQuery.getQuery(Item.class);
                    query.getInBackground(mItemId, new GetCallback<Item>() {
                        @Override
                        public void done(Item item, ParseException e) {
                            if (e != null) {
                                logE("Exception while loading item", "mItemId= " + mItemId, e);
                                Toast.makeText(getActivity(), getActivity().getString(R.string.error_loading_item), Toast.LENGTH_LONG).show();
                            } else {
                                // TODO: check if still we are need this
                                mItem = item;
                                mTitleEditText.setText(mItem.getTitle());
                                mDescriptionEditText.setText(mItem.getDescription());
                                ParseFile[] images = mItem.getImages();
                                for (int i = 0; i < Item.NUMBER_OF_IMAGES; i++) {
                                    if (images[i] != null) {
                                        mItemImages[i].loadInBackground(images[i], new GetDataCallback() {
                                            @Override
                                            public void done(byte[] data, ParseException e) {
                                                if (e == null) {
                                                    logD(null, "Fetched! Data length: " + data.length);
                                                } else {
                                                    logE("Exception while loading image", "", e);
                                                }
                                            }
                                        });
                                    }
                                }
                                mDirty = false;
                            }
                            hideLoadingDialog();
                        }
                    });
                } else {
                    logE("Unexpected state!");
                }
            }
        }

        TextWatcher textWatcherToMarkDirty = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mDirty = true;
            }
        };
        mTitleEditText.addTextChangedListener(textWatcherToMarkDirty);
        mDescriptionEditText.addTextChangedListener(textWatcherToMarkDirty);

        rootView.findViewById(R.id.saveItemButton).setOnClickListener(this);

        return rootView;
    }

    private void showLoadingDialog() {
        if (mLoadingDialog == null || !mLoadingDialog.isShowing()) {
            mLoadingDialog = ProgressDialog.show(getActivity(), "", getString(R.string.login_dialog_text), false);
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
                mTitleEditText.setError(null);
                if (isFormInputsValid()) {
                    saveItem();
                } else {
                    Toast.makeText(getActivity(), "لطفا خطاهای مشخص شده را اصلاح و دوباره تلاش کنید.", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.edit_item_category_semi_button:
                Intent intent = new Intent(getActivity(), ChooseCategoryActivity.class);
                startActivityForResult(intent, HonarnamaSellApp.INTENT_CHOOSE_CATEGORY_CODE);
                break;
            default:
                break;
        }
    }

    private boolean isFormInputsValid() {

        final String title = mTitleEditText.getText().toString();
        final String price = mPriceEditText.getText().toString();
        final String description = mDescriptionEditText.getText().toString();

        boolean noImage = true;
        for (ImageSelector imageSelector : mItemImages) {
            if ((imageSelector.getFinalImageUri() != null) || (imageSelector.getParseFile() != null)) {
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

        if(price.trim().length() == 0){
            mPriceEditText.requestFocus();
            mPriceEditText.setError("لطفا قیمت محصول را تعیین کنید.");
            return false;
        }

        if(Integer.valueOf(price.trim()) <100){
            mPriceEditText.requestFocus();
            mPriceEditText.setError("حداقل قیمت محصول ۱۰۰ تومان است.");
            return false;
        }


        if (mCategoryId == null) {
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
            Toast.makeText(getActivity(), R.string.item_not_changed, Toast.LENGTH_LONG).show();
            return false;
        }

        if (!NetworkManager.getInstance().isNetworkEnabled(getActivity(), true)) {
            Toast.makeText(getActivity(), R.string.error_network_is_not_enabled, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void saveItem() {
        String title = mTitleEditText.getText().toString().trim();
        String description = mDescriptionEditText.getText().toString().trim();
        Number price = Integer.valueOf(mPriceEditText.getText().toString().trim());

        final ProgressDialog sendingDataProgressDialog = new ProgressDialog(getActivity());
        sendingDataProgressDialog.setCancelable(false);
        sendingDataProgressDialog.setMessage(getString(R.string.sending_data));
        sendingDataProgressDialog.show();

        try {
            Item.saveWithImages(mItem, title, description, mCategoryId, price, mItemImages).continueWith(new Continuation<Item, Void>() {
                @Override
                public Void then(Task<Item> task) throws Exception {
                    logD(null, "saveItem, Back to then");
                    if (task.isCompleted()) {
                        logD(null, "saveItem, task.isCompleted()");
                        Toast.makeText(getActivity(), R.string.edit_item_save, Toast.LENGTH_LONG).show();
                        mDirty = false;
                        mItem = task.getResult();
                        mItemId = mItem.getObjectId();
                        logD(null, "saveItem, mItem= " + mItem + ", mItemId= " + mItemId);
                    } else {
                        if (task.isFaulted()) {
                            logE("Fault while saveItem", "", task.getError());
                        } else {
                            logD("Canceled while saveItem", "");
                        }
                        Toast.makeText(getActivity(), R.string.error_saving_item, Toast.LENGTH_LONG).show();
                    }
                    sendingDataProgressDialog.dismiss();
                    return null;
                }
            }, Task.UI_THREAD_EXECUTOR);
        } catch (IOException ioe) {
            logE("Exception while saveItem", "", ioe);
            Toast.makeText(getActivity(), R.string.error_saving_item, Toast.LENGTH_LONG).show();
            sendingDataProgressDialog.dismiss();
        }

        ControlPanelActivity activity = (ControlPanelActivity) getActivity();
        activity.switchFragment(ItemsFragment.getInstance());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (ImageSelector imageSelector : mItemImages) {
            if (imageSelector.onActivityResult(requestCode, resultCode, data)) {
                return;
            }
        }
        switch (requestCode) {
            case HonarnamaSellApp.INTENT_CHOOSE_CATEGORY_CODE:

                if (resultCode == getActivity().RESULT_OK) {
                    mCategoryTextView.setError(null);
                    mCategoryName = data.getStringExtra("selectedCategoryName");
                    mCategoryId = data.getStringExtra("selectedCategoryObjectId");
                    mChooseCategoryButton.setText(mCategoryName);
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
            outState.putString(SAVE_INSTANCE_STATE_KEY_ITEM_ID, mItemId);
            outState.putString(SAVE_INSTANCE_STATE_KEY_TITLE, mTitleEditText.getText().toString().trim());
            outState.putString(SAVE_INSTANCE_STATE_KEY_DESCRIPTION, mDescriptionEditText.getText().toString().trim());
        }
    }
}
