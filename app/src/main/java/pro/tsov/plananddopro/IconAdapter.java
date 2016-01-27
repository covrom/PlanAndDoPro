package pro.tsov.plananddopro;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

class IconAdapter extends BaseAdapter {

    private ViewHolder mHolder;
    private String[] mAllIcons;
    private LayoutInflater mInflater;

    public IconAdapter(Context ctx){
        super();
        mInflater = (LayoutInflater) ctx.getSystemService(ctx.LAYOUT_INFLATER_SERVICE);
        mAllIcons = ctx.getResources().getStringArray(R.array.all_icons);
    }

    @Override
    public int getCount() {
        return mAllIcons.length;
    }

    @Override
    public Object getItem(int position) {
        return mAllIcons[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflateViews();
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }

        mHolder.mIcon.setIcon(mAllIcons[position]);

        return convertView;
    }

    private View inflateViews() {
        final View convertView = mInflater.inflate(R.layout.icon_item, null);
        mHolder = new ViewHolder();
        mHolder.mIcon = (IconView) convertView.findViewById(R.id.icon);

        convertView.setTag(mHolder);
        return convertView;
    }

    public class ViewHolder {
        public IconView mIcon;
    }
}