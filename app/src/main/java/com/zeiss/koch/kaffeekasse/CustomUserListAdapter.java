package com.zeiss.koch.kaffeekasse;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by ogmkoch on 07.11.2016.
 */

public class CustomUserListAdapter extends BaseAdapter {
    private static List<User> searchArrayList;

    private LayoutInflater mInflater;

    public CustomUserListAdapter(Context context, List<User> results) {
        searchArrayList = results;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return searchArrayList.size();
    }

    public Object getItem(int position) {
        return searchArrayList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.textview_userlistitem, null);
            holder = new ViewHolder();
            holder.txtName = (TextView) convertView.findViewById(R.id.userListName);
            holder.txtInfo = (TextView) convertView.findViewById(R.id.userListInfo);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txtName.setText(searchArrayList.get(position).getName());
        holder.txtInfo.setText(searchArrayList.get(position).getRole());

        return convertView;
    }

    static class ViewHolder {
        TextView txtName;
        TextView txtInfo;
    }
}
