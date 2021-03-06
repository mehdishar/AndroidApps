package net.honarnama.sell.fragments;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import net.honarnama.base.fragment.HonarnamaBaseFragment;
import net.honarnama.base.utils.NetworkManager;
import net.honarnama.sell.HonarnamaSellApp;
import net.honarnama.sell.R;
import net.honarnama.sell.activity.ControlPanelActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class NoNetworkFragment extends HonarnamaBaseFragment implements View.OnClickListener {

    public static NoNetworkFragment mNoNetworkFragment;
    public ImageView mRetryIcon;
    private Tracker mTracker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTracker = HonarnamaSellApp.getInstance().getDefaultTracker();
        mTracker.setScreenName("NoNetworkFragment");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public synchronized static NoNetworkFragment getInstance() {
        if (mNoNetworkFragment == null) {
            mNoNetworkFragment = new NoNetworkFragment();
        }
        return mNoNetworkFragment;
    }

    @Override
    public String getTitle() {
        return getStringInFragment(R.string.default_app_title);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_no_network, container, false);
        mRetryIcon = (ImageView) rootView.findViewById(R.id.no_network_fragment_retry_icon);
        mRetryIcon.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded()) {
            ControlPanelActivity activity = (ControlPanelActivity) getActivity();
            if (activity != null) {
                activity.setTitle(getTitle());
            }
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.no_network_fragment_retry_icon:
                if (NetworkManager.getInstance().isNetworkEnabled(true)) {

                    ControlPanelActivity controlPanelActivity = (ControlPanelActivity) getActivity();
                    controlPanelActivity.checkAndUpdateMeta(false, 0);

                    Intent intent = new Intent(getActivity(), ControlPanelActivity.class);
                    getActivity().finish();
                    startActivity(intent);
                }
                break;
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
