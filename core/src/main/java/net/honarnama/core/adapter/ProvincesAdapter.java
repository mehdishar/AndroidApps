package net.honarnama.core.adapter;


import net.honarnama.base.R;
import net.honarnama.core.model.Provinces;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by elnaz on 11/29/15.
 */
public class ProvincesAdapter extends BaseAdapter {
    private final Context mContext;
    public TreeMap<Number, Provinces> mProvinceObjectsTreeMap;
    public List<String> mProvincesNameList = new ArrayList<String>();
    public List<String> mProvincesIdsList = new ArrayList<String>();


    public ProvincesAdapter(Context context, TreeMap<Number, Provinces> provincesTreeMap) {
        super();
        mContext = context;
        mProvinceObjectsTreeMap = provincesTreeMap;

        if (mProvinceObjectsTreeMap != null) {
            for (Provinces province : mProvinceObjectsTreeMap.values()) {
                mProvincesIdsList.add(province.getObjectId());
                mProvincesNameList.add(province.getName());
            }
        }

    }

    @Override
    public int getCount() {
        if (mProvinceObjectsTreeMap != null) {
            return mProvinceObjectsTreeMap.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = layoutInflater.inflate(R.layout.province_row, parent, false);

        TextView provinceNameTextView = (TextView) rowView.findViewById(R.id.province_name_text_view);

        provinceNameTextView.setText(mProvincesNameList.get(position));
        return rowView;
    }
}
