package com.ppgsalomao.findorcreatesample.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ppgsalomao.findorcreatesample.persistence.User;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

public class UserAdapter extends RealmBaseAdapter<User> {
    public UserAdapter(Context context, RealmResults<User> realmResults, boolean automaticUpdate) {
        super(context, realmResults, automaticUpdate);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User user = this.getItem(position);

        TextView view = null;
        if(convertView != null && convertView instanceof TextView) {
            view = (TextView) convertView;
        }

        if(view == null)
            view = new TextView(this.context);

        view.setText(String.format("[%d] %s", user.getId(), user.getName()));

        return view;
    }
}
