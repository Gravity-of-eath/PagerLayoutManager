package com.ysp.mqcq02_applist;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ysp.mqcq02_applist.layputer.PagerGridLayoutManager;
import com.ysp.mqcq02_applist.layputer.PagerGridSnapHelper;

import java.util.List;


public class ApplistFragment extends Fragment implements AppFinder.AppFindListener, AppClickListener, PagerLayoutManager.OnPageChangeListener, View.OnClickListener {

    private RecyclerView app_list;
    private ApplistAdapter applistAdapter;
    private PagerSnapHelper helper;
    TextView indicator_text;
    private PagerGridLayoutManager layoutManager;
    private PagerLayoutManager pagerLayoutManager;

    public ApplistFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applistAdapter = new ApplistAdapter(this);
        AppFinder appFinder = new AppFinder(this, getContext());
        appFinder.execute();
        helper = new PagerSnapHelper();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_applist, container, false);
        view.findViewById(R.id.left).setOnClickListener(this);
        view.findViewById(R.id.right).setOnClickListener(this);
        indicator_text = view.findViewById(R.id.indicator_text);
        app_list = view.findViewById(R.id.app_list);
        app_list.setAdapter(applistAdapter);
        pagerLayoutManager = new PagerLayoutManager(5, 4).setPageChangeListener(this);
        app_list.setLayoutManager(pagerLayoutManager);
//        layoutManager = new PagerGridLayoutManager(5, 4, PagerGridLayoutManager.HORIZONTAL);
//        app_list.setLayoutManager(layoutManager);
//        new PagerGridSnapHelper().attachToRecyclerView(app_list);
        return view;
    }

    @Override
    public void onAppFind(ApplicationInformation applicationInformation) {

    }

    @Override
    public void onAppFindFinish(List<ApplicationInformation> applicationInformations) {
        applistAdapter.setInstalledApplications(applicationInformations);
        applistAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAppClick(ApplicationInformation info) {
//        startActivity(info.launchIntent);
    }

    @Override
    public void onPageChange(int page) {
        indicator_text.setText("" + page);
    }

    @Override
    public void onClick(View v) {
        if (R.id.left == v.getId()) {
//            layoutManager.prePage();
            pagerLayoutManager.smoothScrollToPage(3);
        } else if (R.id.right == v.getId()) {
//            layoutManager.nextPage();
            pagerLayoutManager.smoothScrollToPage(0);
        } else {
        }


    }
}