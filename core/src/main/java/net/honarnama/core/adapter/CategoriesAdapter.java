package net.honarnama.core.adapter;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;


import net.honarnama.base.R;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by elnaz on 11/29/15.
 */
public class CategoriesAdapter extends BaseAdapter {
    private final Context mContext;
    private HashMap<Number, String> mArtCategoriesName;
    private HashMap<Number, String> mArtCategoriesObjectIds;

    private int mSelectedPosition;
    public ArrayList<String> mNodeCategories = new ArrayList<String>();

    public CategoriesAdapter(Context context, HashMap<Number, String> artCategoriesObjectIds, HashMap<Number, String> artCategoriesName, ArrayList<String> nodeCategories) {
        super();
        mContext = context;
        mArtCategoriesObjectIds = artCategoriesObjectIds;
        mArtCategoriesName = artCategoriesName;
        mNodeCategories = nodeCategories;
    }

    @Override
    public int getCount() {
        return mArtCategoriesName.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void refreshArtCategories(HashMap<Number, String> artCategoriesObjectIds, HashMap<Number, String> artCategoriesName) {


        mArtCategoriesObjectIds = artCategoriesObjectIds;
        mArtCategoriesName = artCategoriesName;

        notifyDataSetChanged();
    }

    public void setSelectedPosition(int selectedPosition) {
        mSelectedPosition = selectedPosition;
//        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = layoutInflater.inflate(R.layout.category_row, parent, false);

        TextView categoryNameTextView = (TextView) rowView.findViewById(R.id.category_name_text_view);
        ImageView categoryDrillDownArrowImageView = (ImageView) rowView.findViewById(R.id.category_drill_down_arrow_image_view);
        if (!mNodeCategories.contains(mArtCategoriesObjectIds.get(position))) {
            categoryDrillDownArrowImageView.setVisibility(View.VISIBLE);
            categoryDrillDownArrowImageView.setImageDrawable(new IconicsDrawable(mContext)
                    .icon(GoogleMaterial.Icon.gmd_arrow_in)
                    .color(mContext.getResources().getColor(R.color.nokhodi_botte_jeghe))
                    .sizeDp(15));
        }

        categoryNameTextView.setText(mArtCategoriesName.get(position));
        return rowView;
    }
}