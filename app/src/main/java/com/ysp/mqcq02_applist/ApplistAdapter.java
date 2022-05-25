package com.ysp.mqcq02_applist;

import android.content.pm.ApplicationInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ApplistAdapter extends RecyclerView.Adapter<AppViewHolder> {
    private static final String TAG = "ApplistAdapter";
    private List<ApplicationInformation> installedApplications;
    private AppClickListener listener;

    public ApplistAdapter(AppClickListener listener) {
        this.listener = listener;
    }

    public void setListener(AppClickListener listener) {
        this.listener = listener;
    }

    public List<ApplicationInformation> getInstalledApplications() {
        return installedApplications;
    }

    public void setInstalledApplications(List<ApplicationInformation> installedApplications) {
        this.installedApplications = installedApplications;
    }

    @Override
    public int getItemViewType(int position) {
        Log.d(TAG, "getItemViewType: =" );
        return 0;
    }

    @Override
    public long getItemId(int position) {
        Log.d(TAG, "onCreateViewHolder: =" );
        return 0;
    }

    int i = 0;

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_item, parent, false);
        AppViewHolder viewHolder = new AppViewHolder(view);
        viewHolder.setListener(listener);
        Log.d(TAG, "onCreateViewHolder: index=" + i++);
        return viewHolder;
    }

    int j = 0;

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: index=" + j++);
        holder.setInfo(installedApplications.get(position));
    }

    @Override
    public int getItemCount() {
        return installedApplications == null || installedApplications.isEmpty() ? 0 : installedApplications.size();
    }
}
