package com.nic.RealTimeMonitoringSystem.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.SearchView;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.nic.RealTimeMonitoringSystem.R;
import com.nic.RealTimeMonitoringSystem.adapter.AdditionalListAdapter;
import com.nic.RealTimeMonitoringSystem.adapter.GroupWorkListAdapter;
import com.nic.RealTimeMonitoringSystem.constant.AppConstant;
import com.nic.RealTimeMonitoringSystem.dataBase.DBHelper;
import com.nic.RealTimeMonitoringSystem.dataBase.dbData;
import com.nic.RealTimeMonitoringSystem.databinding.ActivityGroupWorkBinding;
import com.nic.RealTimeMonitoringSystem.model.RealTimeMonitoringSystem;
import com.nic.RealTimeMonitoringSystem.session.PrefManager;
import com.nic.RealTimeMonitoringSystem.support.MyDividerItemDecoration;

import java.util.ArrayList;

public class GroupWork extends AppCompatActivity implements View.OnClickListener{
    private ActivityGroupWorkBinding groupWorkBinding;
    private ShimmerRecyclerView recyclerView;
    private GroupWorkListAdapter groupWorkListAdapter;
    public com.nic.RealTimeMonitoringSystem.dataBase.dbData dbData = new dbData(this);
    private SearchView searchView;
    private PrefManager prefManager;
    private SQLiteDatabase db;
    public static DBHelper dbHelper;
    private String work_id;
    ArrayList<RealTimeMonitoringSystem> groupWorkList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groupWorkBinding = DataBindingUtil.setContentView(this, R.layout.activity_group_work);
        groupWorkBinding.setActivity(this);
        prefManager = new PrefManager(this);
        setSupportActionBar(groupWorkBinding.toolbar);
        initRecyclerView();
    }

    private void initRecyclerView() {
        work_id = getIntent().getStringExtra(AppConstant.WORK_ID);
        groupWorkBinding.workList.setVisibility(View.VISIBLE);
        recyclerView = groupWorkBinding.workList;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 20));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        groupWorkListAdapter = new GroupWorkListAdapter(GroupWork.this, groupWorkList,dbData);
        recyclerView.setAdapter(groupWorkListAdapter);

        new fetchGroupWorkTask().execute();

    }

    public class fetchGroupWorkTask extends AsyncTask<Void, Void,
            ArrayList<RealTimeMonitoringSystem>> {
        @Override
        protected ArrayList<RealTimeMonitoringSystem> doInBackground(Void... params) {
            dbData.open();
            groupWorkList = new ArrayList<>();
            groupWorkList = dbData.getAllGroupWork(work_id,prefManager.getFinancialyearName(),prefManager.getDistrictCode(),prefManager.getBlockCode(),prefManager.getPvCode(),prefManager.getKeySpinnerSelectedSchemeSeqId());
            Log.d("Group_work_count", String.valueOf(groupWorkList.size()));
            return groupWorkList;
        }

        @Override
        protected void onPostExecute(ArrayList<RealTimeMonitoringSystem> additionalList) {
            super.onPostExecute(additionalList);
            groupWorkListAdapter = new GroupWorkListAdapter(GroupWork.this, additionalList,dbData);
            recyclerView.setAdapter(groupWorkListAdapter);
            recyclerView.showShimmerAdapter();
            recyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadCards();
                }
            }, 2000);
        }

        private void loadCards() {

            recyclerView.hideShimmerAdapter();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                groupWorkListAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                groupWorkListAdapter.getFilter().filter(query);
                return false;
            }
        });
        return true;
    }

    @Override
    public void onBackPressed() {
        if (!searchView.isIconified()) {
            searchView.setIconified(true);
            return;
        }
        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED);
        overridePendingTransition(R.anim.slide_enter, R.anim.slide_exit);
    }


    @Override
    public void onClick(View view) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        groupWorkListAdapter.notifyDataSetChanged();
    }
}

