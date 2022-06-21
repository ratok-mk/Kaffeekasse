package com.zeiss.koch.kaffeekasse;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.zeiss.koch.kaffeekasse.R.style.TextAppearance_Medium_Warning;
import static com.zeiss.koch.kaffeekasse.R.style.TextAppearance_Medium_Warning_Yellow;


/**
 * Created by ogmkoch on 31.01.2019.
 */

public class CustomUserListAdminAdapter extends BaseAdapter implements Filterable {
    private static List<User> originalData;
    private List<User> filteredData;
    private ItemFilter mFilter = new ItemFilter();

    private LayoutInflater mInflater;
    private final SqlDatabaseHelper db;

    public CustomUserListAdminAdapter(Context context, List<User> results) {
        db = new SqlDatabaseHelper(context);
        originalData = results;
        filteredData = results;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return filteredData.size();
    }

    public User getItem(int position) {
        return filteredData.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.activity_settings_user_list_item, null);
            holder = new ViewHolder();
            holder.txtName = (TextView) convertView.findViewById(R.id.userListAdminText);
            holder.txtInfo = (TextView) convertView.findViewById(R.id.userListAdminInfo);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        User user = filteredData.get(position);
        holder.txtName.setText(String.format("%1$d. %2$s", position + 1, user.getName()));

        boolean userIsActive = db.getUserIsActive(user);;

        if (userIsActive) {
            holder.txtInfo.setText("Active");
            holder.txtInfo.setTextAppearance(android.R.style.TextAppearance_Medium);
        }
        else {
            holder.txtInfo.setText("Inactive");
            holder.txtInfo.setTextAppearance(TextAppearance_Medium_Warning_Yellow);
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

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    static class ViewHolder {
        TextView txtName;
        TextView txtInfo;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            final List<User> list = originalData;
            int count = list.size();
            final ArrayList<User> nlist = new ArrayList<User>(count);

            User filterableUser ;
            for (int i = 0; i < count; i++) {
                filterableUser = list.get(i);
                if (filterableUser.getName().toLowerCase().contains(filterString)) {
                    nlist.add(filterableUser);
                }
            }

            results.values = nlist;
            results.count = nlist.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredData = (ArrayList<User>) results.values;
            notifyDataSetChanged();
        }

    }
}
