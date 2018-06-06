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
import java.util.List;


/**
 * Created by ogmkoch on 07.11.2016.
 */

public class CustomUserListAdapter extends BaseAdapter implements Filterable {
    private static List<User> originalData;
    private List<User> filteredData;
    private ItemFilter mFilter = new ItemFilter();

    private LayoutInflater mInflater;
    private final SqlDatabaseHelper db;

    public CustomUserListAdapter(Context context, List<User> results) {
        db = new SqlDatabaseHelper(context);
        originalData = results;
        filteredData = results;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return filteredData.size();
    }

    public Object getItem(int position) {
        return filteredData.get(position);
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

        User user = filteredData.get(position);
        Double balance = db.getBalance(user);
        String balanceText = Formater.valueToCurrencyString(balance);
        holder.txtName.setText(String.format("%1$d. %2$s", position + 1, user.getName()));
        holder.txtInfo.setText(balanceText);

        if (balance < 1.0 && balance >= 0.0)
        {
            holder.txtInfo.setTextAppearance(R.style.TextAppearance_Large_Warning_Yellow);
        }
        else if (balance < 0.0)
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

    public Filter getFilter() {
        return mFilter;
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
