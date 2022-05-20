package com.ysp.mqcq02_applist;

import android.content.pm.ApplicationInfo;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AppViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private AppClickListener listener;
    private ApplicationInformation info;
    private ImageView icon;
    private TextView label;

    public ApplicationInformation getInfo() {
        return info;
    }

    public void setInfo(ApplicationInformation info) {
        this.info = info;
        setData();
    }

    public void setListener(AppClickListener listener) {
        this.listener = listener;
    }

    public AppViewHolder(@NonNull View itemView, ApplicationInformation info) {
        super(itemView);
        icon = itemView.findViewById(R.id.icon);
        label = itemView.findViewById(R.id.label);
        itemView.setOnClickListener(this);
        this.info = info;
        setData();
    }

    public AppViewHolder(@NonNull View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        icon = itemView.findViewById(R.id.icon);
        label = itemView.findViewById(R.id.label);
    }

    private void setData() {
        icon.setImageDrawable(info.icon);
        label.setText(info.index + "");
    }

    @Override
    public void onClick(View v) {
        if (this.listener != null) {
            this.listener.onAppClick(info);
        }
    }
}
