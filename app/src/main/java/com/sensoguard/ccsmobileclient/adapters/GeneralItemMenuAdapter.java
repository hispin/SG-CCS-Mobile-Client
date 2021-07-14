package com.sensoguard.ccsmobileclient.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.sensoguard.ccsmobileclient.R;
import com.sensoguard.ccsmobileclient.classes.GeneralItemMenu;
import com.sensoguard.ccsmobileclient.interfaces.CallToParentInterface;

import java.util.List;


public class GeneralItemMenuAdapter extends ParentAdapter<GeneralItemMenu> {


    private ViewHolder viewHolder;
    private CallToParentInterface callToParentInterface;

    public GeneralItemMenuAdapter(Context context, int resId, List<GeneralItemMenu> objects, CallToParentInterface callToParentInterface) {
        super(context, resId, objects, callToParentInterface);
        this.callToParentInterface = callToParentInterface;
    }

    @NonNull
    @Override
    public View getView(int position, View rowView, @NonNull ViewGroup parent) {
        final GeneralItemMenu generalItemMenu = getItem(position);
        Resources rc = context.getResources();
        if (rowView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater != null) {
                rowView = inflater.inflate(resId, parent, false);
            } else {
                this.clear();
                this.notifyDataSetChanged();
                ((ListView) parent).setAdapter(null);
                return rowView;
            }
            viewHolder.tvTitleItemMenu = rowView.findViewById(R.id.tvTitleItemMenu);
            viewHolder.ivIconItemMenu = rowView.findViewById(R.id.ivIconItemMenu);
            viewHolder.constrainItemMenu = rowView.findViewById(R.id.constrainItemMenu);
            rowView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) rowView.getTag();
        }

        if (GeneralItemMenu.selectedItem.equals(generalItemMenu.getValue())) {
            viewHolder.constrainItemMenu.setBackgroundColor(ContextCompat.getColor(context, R.color.gray2));
        } else {
            viewHolder.constrainItemMenu.setBackgroundColor(ContextCompat.getColor(context, R.color.gray1));
        }

        viewHolder.tvTitleItemMenu.setText(generalItemMenu.getTitle());
        viewHolder.ivIconItemMenu.setImageDrawable(ContextCompat.getDrawable(context, generalItemMenu.getIconSmall()));
        viewHolder.constrainItemMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callToParentInterface != null) {
                    GeneralItemMenu.selectedItem = generalItemMenu.getValue();
                    callToParentInterface.selectedItem(generalItemMenu);
                }
                notifyDataSetChanged();
            }
        });

        return rowView;
    }

    public static class ViewHolder {
        TextView tvTitleItemMenu;
        ImageView ivIconItemMenu;
        ConstraintLayout constrainItemMenu;
    }

}
