package com.ysp.mqcq02_applist;

import android.content.pm.ApplicationInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ApplistAdapter extends RecyclerView.Adapter<AppViewHolder> {

    private List<ApplicationInformation> installedApplications;

    public List<ApplicationInformation> getInstalledApplications() {
        return installedApplications;
    }

    public void setInstalledApplications(List<ApplicationInformation> installedApplications) {
        this.installedApplications = installedApplications;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_item, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        holder.setInfo(installedApplications.get(position));
    }

    @Override
    public int getItemCount() {
        return installedApplications == null || installedApplications.isEmpty() ? 0 : installedApplications.size();
    }
}
