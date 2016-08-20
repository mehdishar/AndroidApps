package net.honarnama.browse.fragment;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.parse.ImageSelector;

import net.honarnama.HonarnamaBaseApp;
import net.honarnama.browse.HonarnamaBrowseApp;
import net.honarnama.browse.R;
import net.honarnama.browse.activity.ControlPanelActivity;
import net.honarnama.base.utils.NetworkManager;
import net.honarnama.base.utils.ObservableScrollView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by elnaz on 2/15/16.
 */
public class EventPageFragment extends HonarnamaBrowseFragment implements View.OnClickListener, AdapterView.OnItemClickListener, ObservableScrollView.OnScrollChangedListener {
    public static EventPageFragment mEventPageFragment;
    private Tracker mTracker;
    public ProgressBar mBannerProgressBar;

    public TextView mNameTextView;
    public TextView mDateTextView;
    public TextView mDescTextView;
    public TextView mAddreddTextView;
    public TextView mPlaceTextView;

    private ObservableScrollView mScrollView;
    private View mBannerFrameLayout;
    private ImageSelector mBannerImageView;
    private ProgressBar mEventInfoProgressBar;

    private RelativeLayout mShare;
    public String mEventId;

    public RelativeLayout mShopContainer;
    public RelativeLayout mOnErrorRetry;

    @Override
    public String getTitle(Context context) {
        return getString(R.string.art_event);
    }

    public synchronized static EventPageFragment getInstance(int eventId) {
        EventPageFragment eventPageFragment = new EventPageFragment();
        Bundle args = new Bundle();
        args.putInt("eventId", eventId);
        eventPageFragment.setArguments(args);
//        shopPageFragment.setOwner(owner);
        return eventPageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = HonarnamaBrowseApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("EventPageFragment");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onSelectedTabClick() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_event_page, container, false);
        mEventId = getArguments().getString("eventId");


        mScrollView = (ObservableScrollView) rootView.findViewById(R.id.fragment_scroll_view);
        mScrollView.setOnScrollChangedListener(this);
        mBannerFrameLayout = rootView.findViewById(R.id.event_banner_frame_layout);
        mBannerImageView = (ImageSelector) rootView.findViewById(R.id.event_banner_image_view);
        mBannerProgressBar = (ProgressBar) rootView.findViewById(R.id.banner_progress_bar);

        mEventInfoProgressBar = (ProgressBar) rootView.findViewById(R.id.event_info_progress_bar);

        mNameTextView = (TextView) rootView.findViewById(R.id.event_name_text_view);
        mDateTextView = (TextView) rootView.findViewById(R.id.event_date_text_view);
        mDescTextView = (TextView) rootView.findViewById(R.id.event_desc_text_view);
        mAddreddTextView = (TextView) rootView.findViewById(R.id.address);
        mPlaceTextView = (TextView) rootView.findViewById(R.id.event_place_text_view);
        mShare = (RelativeLayout) rootView.findViewById(R.id.event_share_container);
        mShare.setOnClickListener(this);

        mOnErrorRetry = (RelativeLayout) rootView.findViewById(R.id.on_error_retry_container);
        mOnErrorRetry.setOnClickListener(this);
        final RelativeLayout deletedEventMsg = (RelativeLayout) rootView.findViewById(R.id.deleted_event_msg);

        final FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        final RelativeLayout infoContainer = (RelativeLayout) rootView.findViewById(R.id.event_info_container);
        mEventInfoProgressBar.setVisibility(View.VISIBLE);
        mOnErrorRetry.setVisibility(View.GONE);

        //TODO
//        Event.getEventById(mEventId).continueWith(new Continuation<ParseObject, Object>() {
//            @Override
//            public Object then(Task<ParseObject> task) throws Exception {
//                mEventInfoProgressBar.setVisibility(View.GONE);
//                if (task.isFaulted()) {
//                    if (((ParseException) task.getError()).getCode() == ParseException.OBJECT_NOT_FOUND) {
//                        if (isVisible()) {
//                            Toast.makeText(getActivity(), getActivity().getString(R.string.error_event_no_longer_exists), Toast.LENGTH_SHORT).show();
//                        }
//                        deletedEventMsg.setVisibility(View.VISIBLE);
//                    } else {
//                        logE("Getting event with id " + mEventId + " for event page failed. Error: " + task.getError(), "", task.getError());
//                        if (isVisible()) {
//                            Toast.makeText(getActivity(), getActivity().getString(R.string.error_displaying_event) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
//                        }
//                        mOnErrorRetry.setVisibility(View.VISIBLE);
//                    }
//                    return null;
//                } else {
//                    fab.setVisibility(View.VISIBLE);
//                    infoContainer.setVisibility(View.VISIBLE);
//                    mOnErrorRetry.setVisibility(View.GONE);
//                    mShare.setVisibility(View.VISIBLE);
//                    final Event event = (Event) task.getResult();
//
//                    ParseFile eventBanner = event.getParseFile(Event.BANNER);
//                    if (eventBanner != null) {
//                        mBannerProgressBar.setVisibility(View.VISIBLE);
//                        mBannerImageView.loadInBackground(eventBanner, new GetDataCallback() {
//                            @Override
//                            public void done(byte[] data, ParseException e) {
//                                mBannerProgressBar.setVisibility(View.GONE);
//                                if (e != null) {
//                                    logE("Getting  banner image for event " + mEventId + " failed. Code: " + e.getCode() + " // Msg: " + e.getMessage() + " // Error: " + e, "", e);
//                                    if (isVisible()) {
//                                        Toast.makeText(getActivity(), getString(R.string.error_displaying_store_banner) + getString(R.string.please_check_internet_connection), Toast.LENGTH_LONG).show();
//                                    }
//                                }
//                            }
//                        });
//                    }
//
//                    mNameTextView.setText(TextUtil.convertEnNumberToFa(event.getName()));
//
//                    Locale locale = new Locale("fa", "IR");
//
//                    Date startDate = event.getStartDate();
//                    String jalaliStartDate = JalaliCalendar.getJalaliDate(startDate);
//
//                    Date endDate = event.getEndDate();
//                    String jalaliEndDate = JalaliCalendar.getJalaliDate(endDate);
//
//                    mDateTextView.setText("تاریخ برگزاری رویداد از " +
//                                    TextUtil.convertEnNumberToFa(jalaliStartDate) +
//                                    " تا " +
//                                    TextUtil.convertEnNumberToFa(jalaliEndDate) +
//                                    " است. "
//
//                    );
//
//                    mDescTextView.setText(TextUtil.convertEnNumberToFa(event.getDescription()));
//                    mAddreddTextView.append(" " + event.getAddress());
//                    mPlaceTextView.setText(event.getProvince().getString(Province.NAME) + "، " + event.getCity().getString(City.NAME));
//
//                    fab.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            ContactDialog contactDialog = new ContactDialog();
//                            contactDialog.showDialog(getActivity(), event.getPhoneNumber(), event.getCellNumber(),
//                                    getResources().getString(R.string.event_contact_dialog_warning_msg));
//
//                        }
//                    });
//
//                }
//                return null;
//            }
//        });

        return rootView;

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.event_share_container) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, mNameTextView.getText().toString());
            sendIntent.putExtra(Intent.EXTRA_TEXT, "سلام،" + "\n"
                    + "رویداد "
                    + mNameTextView.getText().toString() + "\n" +
                    "تو برنامه هنرنما ثبت شده. "
                    +
                    "جزئیاتشو اینجا میتونی ببینی:"
                    + "\n" + HonarnamaBaseApp.WEB_ADDRESS + "/event/" + mEventId);
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

    }

    @Override
    public void onScrollChanged(int deltaX, int deltaY) {
        int scrollY = mScrollView.getScrollY();
        // Add parallax effect
        mBannerFrameLayout.setTranslationY(scrollY * 0.5f);

    }

}


