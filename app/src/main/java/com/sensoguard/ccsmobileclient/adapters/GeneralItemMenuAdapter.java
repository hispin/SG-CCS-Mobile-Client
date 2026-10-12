package com.sensoguard.ccsmobileclient.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.sensoguard.ccsmobileclient.R;
import com.sensoguard.ccsmobileclient.classes.GeneralItemMenu;
import com.sensoguard.ccsmobileclient.interfaces.CallToParentInterface;

import java.util.List;


public class GeneralItemMenuAdapter extends ArrayAdapter<GeneralItemMenu> {

    private final int resId;
    private final CallToParentInterface callToParentInterface;

    public GeneralItemMenuAdapter(Context context, int resId, List<GeneralItemMenu> objects, CallToParentInterface callToParentInterface) {
        super(context, resId, objects);
        this.resId = resId;
        this.callToParentInterface = callToParentInterface;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final GeneralItemMenu generalItemMenu = getItem(position);
        final Context context = getContext();

        View rowView = convertView;
        ViewHolder viewHolder;
        if (rowView == null) {
            rowView = LayoutInflater.from(context).inflate(resId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tvTitleItemMenu = rowView.findViewById(R.id.tvTitleItemMenu);
            viewHolder.ivIconItemMenu = rowView.findViewById(R.id.ivIconItemMenu);
            viewHolder.constrainItemMenu = rowView.findViewById(R.id.constrainItemMenu);
            rowView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) rowView.getTag();
        }

        if (generalItemMenu == null) {
            return rowView;
        }

        if (GeneralItemMenu.selectedItem.equals(generalItemMenu.getValue())) {
            viewHolder.constrainItemMenu.setBackgroundColor(ContextCompat.getColor(context, R.color.gray2));
        } else {
            viewHolder.constrainItemMenu.setBackgroundColor(ContextCompat.getColor(context, R.color.gray1));
        }

        viewHolder.tvTitleItemMenu.setText(generalItemMenu.getTitle());
        viewHolder.ivIconItemMenu.setImageDrawable(ContextCompat.getDrawable(context, generalItemMenu.getIconSmall()));
        viewHolder.constrainItemMenu.setOnClickListener(view -> {
            if (callToParentInterface != null) {
                GeneralItemMenu.selectedItem = generalItemMenu.getValue();
                callToParentInterface.selectedItem(generalItemMenu);
            }
            notifyDataSetChanged();
        });

        return rowView;
    }

    private static class ViewHolder {
        TextView tvTitleItemMenu;
        ImageView ivIconItemMenu;
        ConstraintLayout constrainItemMenu;
    }

}
