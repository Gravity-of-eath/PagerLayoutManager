package com.ysp.mqcq02_applist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

public class AppFinder extends AsyncTask<Void, ApplicationInformation, List<ApplicationInformation>> {
    private AppFindListener listener;

    private Context context;

    public AppFinder(AppFindListener listener, Context context) {
        this.listener = listener;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(ApplicationInformation... values) {
        super.onProgressUpdate(values);
        if (listener != null) {
            listener.onAppFind(values[0]);
        }
    }

    @Override
    protected List<ApplicationInformation> doInBackground(Void... voids) {
        PackageManager packageManager = context.getPackageManager();
        ArrayList<ApplicationInformation> applicationInformations = new ArrayList<>();
        @SuppressLint("WrongConstant") List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(PackageManager.MATCH_ALL);
        for (ApplicationInfo info : installedApplications) {
            ApplicationInformation information = new ApplicationInformation();
            information.applicationInfo = info;
            try {
                information.icon = packageManager.getApplicationIcon(info.packageName);
                information.label = packageManager.getApplicationLabel(info).toString();
                information.launchIntent = packageManager.getLaunchIntentForPackage(info.packageName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            information.index = installedApplications.indexOf(info);
            applicationInformations.add(information);
            publishProgress(information);
        }
        return applicationInformations;
    }

    @Override
    protected void onPostExecute(List<ApplicationInformation> applicationInformation) {
        if (listener != null) {
            listener.onAppFindFinish(applicationInformation);
        }
    }

    interface AppFindListener {
        void onAppFind(ApplicationInformation applicationInformation);

        void onAppFindFinish(List<ApplicationInformation> applicationInformations);
    }
}
