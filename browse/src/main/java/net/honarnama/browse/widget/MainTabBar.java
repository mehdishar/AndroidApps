package net.honarnama.browse.widget;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import net.honarnama.browse.R;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by elnaz on 2/9/16.
 */
public class MainTabBar extends LinearLayout {

    public static final int TAB_HOME = 0;

    public static final int TAB_CATS = 1;

    public static final int TAB_SHOPS = 2;

    public static final int TAB_FAVS = 3;

    private int mSelectedTabColor;

    private int mNotSelectedTabColor;

    private Object mSelectedTabTag;

    private OnTabItemClickListener mOnTabItemClickListener;

    private TextView mUpdatesCountView;

    private Context mContext;

    public MainTabBar(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public MainTabBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    private void init() {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
        mSelectedTabColor = getContext().getResources().getColor(R.color.amber_launcher_color);
        mNotSelectedTabColor = getContext().getResources().getColor(R.color.gray);

        Tab homeTab = new Tab(Integer.valueOf(TAB_HOME),
                new IconicsDrawable(mContext)
                        .icon(GoogleMaterial.Icon.gmd_home)
                        .color(Color.RED)
                        .sizeDp(20), R.string.main_page);
        Tab catsTab = new Tab(Integer.valueOf(TAB_CATS),
                new IconicsDrawable(mContext)
                        .icon(GoogleMaterial.Icon.gmd_list)
                        .color(Color.RED)
                        .sizeDp(20), R.string.categories);
        Tab shopsTab = new Tab(Integer.valueOf(TAB_SHOPS),
                new IconicsDrawable(mContext)
                        .icon(GoogleMaterial.Icon.gmd_store)
                        .color(Color.RED)
                        .sizeDp(20), R.string.shops);
        Tab favsTab = new Tab(Integer.valueOf(TAB_FAVS),
                new IconicsDrawable(mContext)
                        .icon(GoogleMaterial.Icon.gmd_favorite)
                        .color(Color.RED)
                        .sizeDp(20), R.string.favorites);
        setTabs(new MainTabBar.Tab[]{
                homeTab, catsTab, shopsTab, favsTab
        }, TAB_HOME);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    /**
     * called to populate tab bar's tabs. Tabs will be added by given ordering in tabs array
     */
    public void setTabs(Tab[] tabs, int selectedItemTag) {

        for (final Tab tab : tabs) {
            View tabView = LayoutInflater.from(getContext()).inflate(R.layout.main_tab_layout, null);
            TextView label = (TextView) tabView.findViewById(R.id.label);
            label.setVisibility(VISIBLE);
            label.setText(tab.label);
            ImageView icon = (ImageView) tabView.findViewById(R.id.icon);
            icon.setImageDrawable(tab.icon);
            tabView.setTag(tab.tag);
            if (tab.tag.equals(selectedItemTag)) {
                selectTabView(tabView);
            } else {
                deselectTabView(tabView);
            }
            tabView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    setSelectedTab(tab.tag, true);
                }
            });
            LinearLayout.LayoutParams params;

            params = new LinearLayout.LayoutParams(
                    0,
                    WRAP_CONTENT);
            params.weight = 1;
            addView(tabView, 0, params);
        }
    }

    /**
     * called when selecting tabs programmatically, not by user's input
     *
     * @param tabTag tag for tab desired to be selected
     */
    public void setSelectedTab(@NonNull Object tabTag) {
        setSelectedTab(tabTag, false);
    }

    /**
     * @param tabTag tag for tab desired to be selected
     * @param byUser whether user selected this tab
     */
    private void setSelectedTab(@NonNull Object tabTag, boolean byUser) {
        if (tabTag.equals(mSelectedTabTag)) {
            if (mOnTabItemClickListener != null) {
                mOnTabItemClickListener.onSelectedTabClick(tabTag, byUser);
            }
        } else {
            if (mOnTabItemClickListener != null) {
                mOnTabItemClickListener.onTabSelect(tabTag, byUser);
            }
            selectTabView(findViewWithTag(tabTag));
        }
    }

    /**
     * called to select tab, and change its view to selected mode
     *
     * @param tabView tab view to be selected
     */
    private void selectTabView(@NonNull View tabView) {
        if (mSelectedTabTag != null) {
            deselectTabView(findViewWithTag(mSelectedTabTag));
        }
        TextView label = (TextView) tabView.findViewById(R.id.label);
        label.setTextColor(mSelectedTabColor);
        ImageView icon = (ImageView) tabView.findViewById(R.id.icon);
        icon.setColorFilter(mSelectedTabColor);
        mSelectedTabTag = tabView.getTag();
    }

    /**
     * called to deselect tab, and change its view to deselected mode
     *
     * @param tabView tab view to be deselected
     */
    private void deselectTabView(@NonNull View tabView) {
        TextView label = (TextView) tabView.findViewById(R.id.label);
        label.setTextColor(mNotSelectedTabColor);
        ImageView icon = (ImageView) tabView.findViewById(R.id.icon);
        icon.setColorFilter(mNotSelectedTabColor);
    }

    public void deselectAllTabs() {
        deselectTabView(findViewWithTag(TAB_HOME));
        deselectTabView(findViewWithTag(TAB_CATS));
        deselectTabView(findViewWithTag(TAB_SHOPS));
        deselectTabView(findViewWithTag(TAB_FAVS));
    }

    public void setOnTabItemClickListener(@NonNull OnTabItemClickListener onTabItemClickListener) {
        mOnTabItemClickListener = onTabItemClickListener;
    }

    public static class Tab {

        Object tag;
        IconicsDrawable icon;
        int label;

        public Tab(Object tag, IconicsDrawable icon, int label) {
            this.tag = tag;
            this.icon = icon;
            this.label = label;
        }
    }

    public interface OnTabItemClickListener {

        /**
         * called when another tab was selected until now, and now, this tab is selected
         *
         * @param tabTag tag for selected tab
         * @param byUser whether user selected this tab
         */
        void onTabSelect(Object tabTag, boolean byUser);

        /**
         * called when selecting curretly selected tab
         *
         * @param tabTag tag for selected tab
         * @param byUser whether user selected this tab
         */
        void onSelectedTabClick(Object tabTag, boolean byUser);
    }
}
