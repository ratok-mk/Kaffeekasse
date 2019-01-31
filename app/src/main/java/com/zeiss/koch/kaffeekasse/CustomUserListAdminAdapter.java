package com.zeiss.koch.kaffeekasse;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Created by ogmkoch on 31.01.2019.
 */

public class CustomUserListAdminAdapter extends BaseAdapter {
    private static List<User> originalData;

    private LayoutInflater mInflater;
    private final SqlDatabaseHelper db;

    public CustomUserListAdminAdapter(Context context, List<User> results) {
        db = new SqlDatabaseHelper(context);
        originalData = results;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return originalData.size();
    }

    public Object getItem(int position) {
        return originalData.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.user_list_item, null);
            holder = new ViewHolder();
            holder.txtName = (TextView) convertView.findViewById(R.id.userListAdminText);
            holder.txtInfo = (TextView) convertView.findViewById(R.id.userListAdminInfo);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        User user = originalData.get(position);
        holder.txtName.setText(String.format("%1$d. %2$s", position + 1, user.getName()));

        List<Payment> userPayments = db.getUserPayments(user);
        boolean userIsActive = true;
        java.util.Date currentDate = new java.util.Date();
        if (userPayments.size() > 0) {
            if (dateDiff(currentDate, userPayments.get(0).getDatetime(), TimeUnit.DAYS) >= 90) {
                userIsActive = false;
            }

        }
        else {
            userIsActive = false;
        }

        if (userIsActive) {
            holder.txtInfo.setText("Active");
            holder.txtInfo.setTextAppearance(android.R.style.TextAppearance_Large);
        }
        else {
            holder.txtInfo.setText("Inactive");
            holder.txtInfo.setTextAppearance(R.style.TextAppearance_Large_Warning_Yellow);
        }

        return convertView;
    }

    /**
     * Get a diff between two dates
     * @param date1 the oldest date
     * @param date2 the newest date
     * @param timeUnit the unit in which you want the diff
     * @return the diff value, in the provided unit
     */
    public static long dateDiff(Date date1, Date date2, TimeUnit timeUnit) {
        long diffInMillies = date2.getTime() - date1.getTime();
        return timeUnit.convert(diffInMillies,TimeUnit.MILLISECONDS);
    }

    static class ViewHolder {
        TextView txtName;
        TextView txtInfo;
    }
}
