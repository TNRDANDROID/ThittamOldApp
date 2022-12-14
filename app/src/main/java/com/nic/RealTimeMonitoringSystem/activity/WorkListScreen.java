package com.nic.RealTimeMonitoringSystem.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SearchView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.VolleyError;
import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.nic.RealTimeMonitoringSystem.R;
import com.nic.RealTimeMonitoringSystem.adapter.CommonAdapter;
import com.nic.RealTimeMonitoringSystem.adapter.WorkListAdapter;
import com.nic.RealTimeMonitoringSystem.api.Api;
import com.nic.RealTimeMonitoringSystem.api.ApiService;
import com.nic.RealTimeMonitoringSystem.api.ServerResponse;
import com.nic.RealTimeMonitoringSystem.constant.AppConstant;
import com.nic.RealTimeMonitoringSystem.dataBase.DBHelper;
import com.nic.RealTimeMonitoringSystem.dataBase.dbData;
import com.nic.RealTimeMonitoringSystem.databinding.ActivityWorkListBinding;
import com.nic.RealTimeMonitoringSystem.model.RealTimeMonitoringSystem;
import com.nic.RealTimeMonitoringSystem.session.PrefManager;
import com.nic.RealTimeMonitoringSystem.support.MyDividerItemDecoration;
import com.nic.RealTimeMonitoringSystem.support.ProgressHUD;
import com.nic.RealTimeMonitoringSystem.utils.UrlGenerator;
import com.nic.RealTimeMonitoringSystem.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WorkListScreen extends AppCompatActivity implements View.OnClickListener,Api.ServerResponseListener {
    private ActivityWorkListBinding activityWorkListBinding;
    private ShimmerRecyclerView recyclerView;
    private WorkListAdapter workListAdapter;
    public dbData dbData = new dbData(this);
    private SearchView searchView;
    String pref_Scheme, pref_finYear;
    private ArrayList<RealTimeMonitoringSystem> WorkList = new ArrayList<>();
    private List<RealTimeMonitoringSystem> Scheme = new ArrayList<>();
    private List<RealTimeMonitoringSystem> FinYearList = new ArrayList<>();
    private PrefManager prefManager;
    private SQLiteDatabase db;
    public static DBHelper dbHelper;
    private ProgressHUD progressHUD;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityWorkListBinding = DataBindingUtil.setContentView(this, R.layout.activity_work_list);
        activityWorkListBinding.setActivity(this);
        try {
            dbHelper = new DBHelper(this);
            db = dbHelper.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        prefManager = new PrefManager(this);
        prefManager.setPvCode(getIntent().getStringExtra(AppConstant.PV_CODE));
        setSupportActionBar(activityWorkListBinding.toolbar);
       initRecyclerView();
        workListAdapter = new WorkListAdapter(WorkListScreen.this, WorkList,dbData);
        recyclerView.setAdapter(workListAdapter);
        loadOfflineFinYearListDBValues();
        activityWorkListBinding.finyearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    pref_finYear = FinYearList.get(position).getFinancialYear();
                    prefManager.setFinancialyearName(pref_finYear);
                    loadOfflineSchemeListDBValues(pref_finYear);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }

        });
        activityWorkListBinding.schemeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    pref_Scheme = Scheme.get(position).getSchemeName();
                    prefManager.setSchemeName(pref_Scheme);
                    WorkList = new ArrayList<>();
                    workListAdapter.notifyDataSetChanged();
                    prefManager.setKeySpinnerSelectedSchemeSeqId(Scheme.get(position).getSchemeSequentialID());
                    if(Utils.isOnline()){
                        getWorkList();
                    }
                    else {
                        // initRecyclerView();
                        new fetchScheduletask().execute();
                    }
//                    JSONObject jsonObject = new JSONObject();
////                    try {
////                        jsonObject.put(AppConstant.KEY_SCHEME_SEQUENTIAL_ID,WorkList.get(position).getSchemeSequentialID());
////                        jsonObject.put(AppConstant.FINANCIAL_YEAR,WorkList.get(position).getFinancialYear());
////                    } catch (JSONException e) {
////                        e.printStackTrace();
////                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void initRecyclerView() {
        activityWorkListBinding.workList.setVisibility(View.VISIBLE);
        recyclerView = activityWorkListBinding.workList;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL, 20));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);

       // new fetchScheduletask().execute();
    }

    public class fetchScheduletask extends AsyncTask<Void, Void,
            ArrayList<RealTimeMonitoringSystem>> {
        @Override
        protected ArrayList<RealTimeMonitoringSystem> doInBackground(Void... params) {
            dbData.open();
            WorkList = new ArrayList<>();
            WorkList = dbData.getAllWorkLIst("fetch",pref_finYear,prefManager.getDistrictCode(),prefManager.getBlockCode(),prefManager.getPvCode(),prefManager.getKeySpinnerSelectedSchemeSeqId());
            Log.d("WORKLIST_COUNT", String.valueOf(WorkList.size()));
            return WorkList;
        }

        @Override
        protected void onPostExecute(ArrayList<RealTimeMonitoringSystem> workList) {
            super.onPostExecute(workList);
            if(!Utils.isOnline()) {
                if (workList.size() == 0) {
                    Utils.showAlert(WorkListScreen.this, "No Data Available in Local Database. Please, Turn On mobile data");
                }
            }
            workListAdapter = new WorkListAdapter(WorkListScreen.this, WorkList,dbData);
            recyclerView.setAdapter(workListAdapter);
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

    public void loadOfflineFinYearListDBValues() {
        FinYearList.clear();
        Cursor FinYear = null;
        FinYear = db.rawQuery("select * from " + DBHelper.FINANCIAL_YEAR_TABLE_NAME, null);

        RealTimeMonitoringSystem finYearListValue = new RealTimeMonitoringSystem();
        finYearListValue.setFinancialYear("Select Financial year");
        FinYearList.add(finYearListValue);
        if (FinYear.getCount() > 0) {
            if (FinYear.moveToFirst()) {
                do {
                    RealTimeMonitoringSystem finyearList = new RealTimeMonitoringSystem();
                    String financialYear = FinYear.getString(FinYear.getColumnIndexOrThrow(AppConstant.FINANCIAL_YEAR));
                    finyearList.setFinancialYear(financialYear);
                    FinYearList.add(finyearList);
                } while (FinYear.moveToNext());
            }
        }

        activityWorkListBinding.finyearSpinner.setAdapter(new CommonAdapter(this, FinYearList, "FinYearList"));
    }

    public void loadOfflineSchemeListDBValues(String fin_year) {
        Cursor SchemeList = null;
        String Qury = "SELECT * FROM " + DBHelper.SCHEME_TABLE_NAME + " where fin_year = '" + fin_year + "'";
        Log.d("Schemequery",""+Qury);
        SchemeList = db.rawQuery(Qury, null);

        Scheme.clear();
        RealTimeMonitoringSystem schemeListValue = new RealTimeMonitoringSystem();
        schemeListValue.setSchemeName("Select Scheme");
        Scheme.add(schemeListValue);
        if (SchemeList.getCount() > 0) {
            if (SchemeList.moveToFirst()) {
                do {
                    RealTimeMonitoringSystem schemeList = new RealTimeMonitoringSystem();
                    Integer schemeSequentialID = SchemeList.getInt(SchemeList.getColumnIndexOrThrow(AppConstant.KEY_SCHEME_SEQUENTIAL_ID));
                    String schemeName = SchemeList.getString(SchemeList.getColumnIndexOrThrow(AppConstant.KEY_SCHEME_NAME));
                    String financial_year = SchemeList.getString(SchemeList.getColumnIndexOrThrow(AppConstant.FINANCIAL_YEAR));
                    schemeList.setSchemeSequentialID(schemeSequentialID);
                    schemeList.setSchemeName(schemeName);
                    schemeList.setFinancialYear(financial_year);
                    Scheme.add(schemeList);

                } while (SchemeList.moveToNext());
            }
        }
        activityWorkListBinding.schemeSpinner.setAdapter(new CommonAdapter(this, Scheme, "SchemeList"));
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
                workListAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                workListAdapter.getFilter().filter(query);
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

    public void getWorkList() {
        try {
            new ApiService(this).makeJSONObjectRequest("WorkList", Api.Method.POST, UrlGenerator.getWorkListUrl(), workListJsonParams(), "not cache", this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject workListJsonParams() throws JSONException {
        String authKey = Utils.encrypt(prefManager.getUserPassKey(), getResources().getString(R.string.init_vector), Utils.workListBlockWiseJsonParams(this).toString());
        JSONObject dataSet = new JSONObject();
        dataSet.put(AppConstant.KEY_USER_NAME, prefManager.getUserName());
        dataSet.put(AppConstant.DATA_CONTENT, authKey);
        Log.d("workList", "" + authKey);
        return dataSet;
    }

    public void OnMyResponse(ServerResponse serverResponse) {
        try {
            String urlType = serverResponse.getApi();
            JSONObject responseObj = serverResponse.getJsonResponse();

            if ("WorkList".equals(urlType) && responseObj != null) {
                String key = responseObj.getString(AppConstant.ENCODE_DATA);
                String responseDecryptedSchemeKey = Utils.decrypt(prefManager.getUserPassKey(), key);
                JSONObject jsonObject = new JSONObject(responseDecryptedSchemeKey);
                if (jsonObject.getString("STATUS").equalsIgnoreCase("OK") && jsonObject.getString("RESPONSE").equalsIgnoreCase("OK")) {
                    new InsertWorkListTask().execute(jsonObject.getJSONObject(AppConstant.JSON_DATA));
                    JSONArray jsondata = jsonObject.getJSONObject(AppConstant.JSON_DATA).getJSONArray(AppConstant.ADDITIONAL_WORK);
                    if (jsondata.length() > 0) {
                        new InsertAdditioanlListTask().execute(jsonObject.getJSONObject(AppConstant.JSON_DATA));
                    }
                    new fetchScheduletask().execute();
                } else if(jsonObject.getString("STATUS").equalsIgnoreCase("OK") && jsonObject.getString("RESPONSE").equalsIgnoreCase("NO_RECORD")){
//                    dbData.open();
//                    if(Utils.isOnline()){
//                        dbData.deleteWorkListTable();
//                    }
                    new fetchScheduletask().execute();
                    Utils.showAlert(this,"NO RECORD FOUND!");
                }
                String authKey = responseDecryptedSchemeKey.toString();
                int maxLogSize = 4000;
                for(int i = 0; i <= authKey.length() / maxLogSize; i++) {
                    int start = i * maxLogSize;
                    int end = (i+1) * maxLogSize;
                    end = end > authKey.length() ? authKey.length() : end;
                    Log.v("to_send", authKey.substring(start, end));
                }
                Log.d("WorkListResp", "" + responseDecryptedSchemeKey);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void OnError(VolleyError volleyError) {

    }

    public class InsertWorkListTask extends AsyncTask<JSONObject, Void, Void> {

        private  boolean running = true;

//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            ArrayList<RealTimeMonitoringSystem> workList_count = dbData.getAllWorkLIst("insert",pref_finYear,prefManager.getDistrictCode(),prefManager.getBlockCode(),prefManager.getPvCode());
//            if (workList_count.size() <= 0) {
//                running = true;
//            }else {
//                running = false;
//              //  Utils.showAlert(WorkListScreen.this,"Already Downloaded");
//            }
//        }

        @Override
        protected Void doInBackground(JSONObject... params) {

            dbData.open();

            db.delete(DBHelper.WORK_LIST_TABLE_BASED_ON_FINYEAR_VIlLAGE, "fin_year = ? and  dcode = ? and  bcode = ? and pvcode = ? and scheme_id = ? ", new String[]{pref_finYear,prefManager.getDistrictCode(), prefManager.getBlockCode(), prefManager.getPvCode(), String.valueOf(prefManager.getKeySpinnerSelectedSchemeSeqId())});
            /*In online Delete DB to fetch the Api data*/
//            if(Utils.isOnline()){
//                dbData.deleteWorkListTable();
//            }
//            ArrayList<RealTimeMonitoringSystem> workList_count = dbData.getAllWorkLIst("insert",pref_finYear,prefManager.getDistrictCode(),prefManager.getBlockCode(),prefManager.getPvCode(),0);
//           if (workList_count.size() <= 0) {
                if (params.length > 0) {
                    JSONArray jsonArray = new JSONArray();
                    try {
                        jsonArray = params[0].getJSONArray(AppConstant.MAIN_WORK);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    for (int i = 0; i < jsonArray.length(); i++) {
                        RealTimeMonitoringSystem workList = new RealTimeMonitoringSystem();
                        try {
                            workList.setDistictCode(jsonArray.getJSONObject(i).getString(AppConstant.DISTRICT_CODE));
                            workList.setBlockCode(jsonArray.getJSONObject(i).getString(AppConstant.BLOCK_CODE));
                            workList.setPvCode(jsonArray.getJSONObject(i).getString(AppConstant.PV_CODE));
                            workList.setWorkId(jsonArray.getJSONObject(i).getInt(AppConstant.WORK_ID));
                            workList.setSchemeGroupID(jsonArray.getJSONObject(i).getInt(AppConstant.SCHEME_GROUP_ID));
                            workList.setSchemeID(jsonArray.getJSONObject(i).getInt(AppConstant.SCHEME_ID));
                            workList.setSchemeGroupName(jsonArray.getJSONObject(i).getString(AppConstant.SCHEME_GROUP_NAME));
                            workList.setSchemeName(jsonArray.getJSONObject(i).getString(AppConstant.KEY_SCHEME_NAME));
                            workList.setFinancialYear(jsonArray.getJSONObject(i).getString(AppConstant.FINANCIAL_YEAR));
                            workList.setAgencyName(jsonArray.getJSONObject(i).getString(AppConstant.AGENCY_NAME));
                            workList.setWorkGroupNmae(jsonArray.getJSONObject(i).getString(AppConstant.WORK_GROUP_NAME));
                            workList.setWorkName(jsonArray.getJSONObject(i).getString(AppConstant.WORK_NAME));
                            workList.setWorkGroupID(jsonArray.getJSONObject(i).getString(AppConstant.WORK_GROUP_ID));
                            workList.setWorkTypeID(jsonArray.getJSONObject(i).getString(AppConstant.WORK_TYPE));
                            workList.setCurrentStage(jsonArray.getJSONObject(i).getInt(AppConstant.CURRENT_STAGE_OF_WORK));
                            workList.setIntialAmount(jsonArray.getJSONObject(i).getString(AppConstant.AS_VALUE));
                            workList.setAmountSpendSoFar(jsonArray.getJSONObject(i).getString(AppConstant.AMOUNT_SPENT_SOFAR));
                            workList.setStageName(jsonArray.getJSONObject(i).getString(AppConstant.STAGE_NAME));
                            workList.setBeneficiaryName(jsonArray.getJSONObject(i).getString(AppConstant.BENEFICIARY_NAME));
                            workList.setBeneficiaryFatherName(jsonArray.getJSONObject(i).getString(AppConstant.BENEFICIARY_FATHER_NAME));
                            workList.setWorkTypeName(jsonArray.getJSONObject(i).getString(AppConstant.WORK_TYPE_NAME));
                            workList.setYnCompleted(jsonArray.getJSONObject(i).getString(AppConstant.YN_COMPLETED));
                            workList.setCdProtWorkYn(jsonArray.getJSONObject(i).getString(AppConstant.CD_PROT_WORK_YN));
                            workList.setStateCode(jsonArray.getJSONObject(i).getInt(AppConstant.STATE_CODE));
                            workList.setDistrictName(jsonArray.getJSONObject(i).getString(AppConstant.DISTRICT_NAME));
                            workList.setBlockName(jsonArray.getJSONObject(i).getString(AppConstant.BLOCK_NAME));
                            workList.setPvName(jsonArray.getJSONObject(i).getString(AppConstant.PV_NAME));
                            workList.setCommunity(jsonArray.getJSONObject(i).getString(AppConstant.COMMUNITY_NAME));
                            workList.setGender(jsonArray.getJSONObject(i).getString(AppConstant.GENDER));
                            workList.setLastVisitedDate(jsonArray.getJSONObject(i).getString(AppConstant.LAST_VISITED_DATE));
                            workList.setImageAvailable(jsonArray.getJSONObject(i).getString(AppConstant.KEY_IMAGE_AVAILABLE));

                            dbData.insertWorkList(workList);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
//           }
            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressHUD = ProgressHUD.show(WorkListScreen.this, "Downloading", true, false, null);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(progressHUD!=null){
                progressHUD.cancel();
            }
        }
    }

    public class InsertAdditioanlListTask extends AsyncTask<JSONObject, Void, Void> {

        @Override
        protected Void doInBackground(JSONObject... params) {

            dbData.open();
//            if(Utils.isOnline()){
//                dbData.deleteAdditionalListTable();
//            }
            ArrayList<RealTimeMonitoringSystem> workList_count = dbData.getAllAdditionalWork("",pref_finYear,prefManager.getDistrictCode(),prefManager.getBlockCode(),prefManager.getPvCode(),prefManager.getKeySpinnerSelectedSchemeSeqId());
            if (workList_count.size() <= 0) {
                if (params.length > 0) {
                    JSONArray jsonArray = new JSONArray();
                    try {
                        jsonArray = params[0].getJSONArray(AppConstant.ADDITIONAL_WORK);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    for (int i = 0; i < jsonArray.length(); i++) {
                        RealTimeMonitoringSystem additioanlList = new RealTimeMonitoringSystem();
                        try {

                            additioanlList.setDistictCode(prefManager.getDistrictCode());
                            additioanlList.setBlockCode(prefManager.getBlockCode());
                            additioanlList.setPvCode(prefManager.getPvCode());
                            additioanlList.setSchemeID(jsonArray.getJSONObject(i).getInt(AppConstant.SCHEME_ID));
                            additioanlList.setFinancialYear(jsonArray.getJSONObject(i).getString(AppConstant.FINANCIAL_YEAR));
                            additioanlList.setWorkId(jsonArray.getJSONObject(i).getInt(AppConstant.WORK_ID));
                            additioanlList.setWorkGroupID(jsonArray.getJSONObject(i).getString(AppConstant.WORK_GROUP_ID));
                            additioanlList.setRoadName(jsonArray.getJSONObject(i).getString(AppConstant.ROAD_NAME));
                            additioanlList.setCdWorkNo(jsonArray.getJSONObject(i).getInt(AppConstant.CD_WORK_NO));
                            additioanlList.setCdCode(jsonArray.getJSONObject(i).getInt(AppConstant.CD_CODE));
                            additioanlList.setCdName(jsonArray.getJSONObject(i).getString(AppConstant.CD_NAME));
                            additioanlList.setChainageMeter(jsonArray.getJSONObject(i).getString(AppConstant.CHAINAGE_METER));
                            additioanlList.setCurrentStage(jsonArray.getJSONObject(i).getInt(AppConstant.CURRENT_STAGE_OF_WORK));
                            additioanlList.setCdTypeId(jsonArray.getJSONObject(i).getInt(AppConstant.CD_TYPE_ID));
                            additioanlList.setWorkTypeFlagLe(jsonArray.getJSONObject(i).getString(AppConstant.WORK_TYPE_FLAG_LE));
                            additioanlList.setWorkStageName(jsonArray.getJSONObject(i).getString(AppConstant.WORK_SATGE_NAME));
                            additioanlList.setImageAvailable(jsonArray.getJSONObject(i).getString(AppConstant.KEY_IMAGE_AVAILABLE));


                            dbData.insertAdditionalWorkList(additioanlList);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
           }
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        workListAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(workListAdapter);
    }
}

