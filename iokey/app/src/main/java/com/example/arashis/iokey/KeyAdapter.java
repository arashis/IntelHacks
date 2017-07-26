package com.example.arashis.iokey;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;


/**
 * Created by kosa-a on 2017/07/14.
 */

public class KeyAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<String> mStatusArray = new ArrayList<String>() {{add("Open"); add("Lock"); add("Open"); add("Lock"); add("Open"); add("Lock"); }};
    private ArrayList<String> mTimeArray = new ArrayList<String>() {{add("7/25  12:15:12"); add("7/25  12:15:50");add("7/25  21:33:02");add("7/25  21:33:15");add("7/26  08:05:45");add("7/26  08:06:10");}};
    private static class ViewHolder {
        public TextView statusView;
        public TextView  timeView;
    }

    public KeyAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void additem(String s, String t){
        mStatusArray.add(s);
        mTimeArray.add(t);
    }

    public int getCount() {
        return mStatusArray.size();
    }

    public Object getItem(int position) {
        return mStatusArray.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    //ここら辺の詳細は
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.list_item, null);
            holder = new ViewHolder();
            holder.statusView = (TextView)convertView.findViewById(R.id.keystatus);
            holder.timeView = (TextView)convertView.findViewById(R.id.time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        holder.statusView.setText(mStatusArray.get(position));
        holder.timeView.setText(mTimeArray.get(position));

        return convertView;
    }
}
