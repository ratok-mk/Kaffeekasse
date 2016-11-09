package com.zeiss.koch.kaffeekasse;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;


/**
 * Created by ogmkoch on 07.11.2016.
 */

public class CustomUserListAdapter extends BaseAdapter {
    private static List<User> searchArrayList;

    private LayoutInflater mInflater;
    private final SqlDatabaseHelper db;

    public CustomUserListAdapter(Context context, List<User> results) {
        db = new SqlDatabaseHelper(context);
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

        User user = searchArrayList.get(position);
        Double balance = db.getBalance(user);
        String balanceText = Formater.valueToCurrencyString(balance);
        holder.txtName.setText(user.getName());
        holder.txtInfo.setText(balanceText);

        if (balance < 0.0)
        {
            holder.txtInfo.setTextAppearance(R.style.TextAppearance_Large_Warning);
        }
        else
        {
            holder.txtInfo.setTextAppearance(android.R.style.TextAppearance_Large);
        }

        return convertView;
    }

    static class ViewHolder {
        TextView txtName;
        TextView txtInfo;
    }
}
