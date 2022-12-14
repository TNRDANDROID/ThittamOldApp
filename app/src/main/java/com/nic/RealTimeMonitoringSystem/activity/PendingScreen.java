package com.nic.RealTimeMonitoringSystem.activity;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.widget.SearchView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.nic.RealTimeMonitoringSystem.R;
import com.nic.RealTimeMonitoringSystem.adapter.PendingScreenAdapter;
import com.nic.RealTimeMonitoringSystem.api.Api;
import com.nic.RealTimeMonitoringSystem.api.ApiService;
import com.nic.RealTimeMonitoringSystem.api.ServerResponse;
import com.nic.RealTimeMonitoringSystem.constant.AppConstant;
import com.nic.RealTimeMonitoringSystem.dataBase.DBHelper;
import com.nic.RealTimeMonitoringSystem.dataBase.dbData;
import com.nic.RealTimeMonitoringSystem.databinding.PendingScreenBinding;
import com.nic.RealTimeMonitoringSystem.model.RealTimeMonitoringSystem;
import com.nic.RealTimeMonitoringSystem.session.PrefManager;
import com.nic.RealTimeMonitoringSystem.support.ProgressHUD;
import com.nic.RealTimeMonitoringSystem.utils.UrlGenerator;
import com.nic.RealTimeMonitoringSystem.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class PendingScreen extends AppCompatActivity implements Api.ServerResponseListener {
    private PendingScreenBinding pendingScreenBinding;
    private ShimmerRecyclerView recyclerView;
    private PrefManager prefManager;
    private SQLiteDatabase db;
    public static DBHelper dbHelper;
    public dbData dbData = new dbData(this);
    ArrayList<RealTimeMonitoringSystem> pendingList = new ArrayList<>();
    private PendingScreenAdapter pendingScreenAdapter;
    private SearchView searchView;
    JSONObject dataset = new JSONObject();
    private ProgressHUD progressHUD;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pendingScreenBinding = DataBindingUtil.setContentView(this, R.layout.pending_screen);
        pendingScreenBinding.setActivity(this);
        setSupportActionBar(pendingScreenBinding.toolbar);
        try {
            dbHelper = new DBHelper(this);
            db = dbHelper.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        prefManager = new PrefManager(this);

        pendingList = new ArrayList<>();
        pendingScreenAdapter = new PendingScreenAdapter(PendingScreen.this,pendingList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView = pendingScreenBinding.pendingListRecycler;
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        new fetchpendingtask().execute();

    }



    public class fetchpendingtask extends AsyncTask<JSONObject, Void,
            ArrayList<RealTimeMonitoringSystem>> {
        @Override
        protected ArrayList<RealTimeMonitoringSystem> doInBackground(JSONObject... params) {
            dbData.open();
            pendingList = new ArrayList<>();
            pendingList = dbData.getSavedWorkImage("","","","","");
            Log.d("PENDING_COUNT", String.valueOf(pendingList.size()));
            return pendingList;
        }

        @Override
        protected void onPostExecute(ArrayList<RealTimeMonitoringSystem> pendingList) {
            super.onPostExecute(pendingList);
            pendingScreenAdapter = new PendingScreenAdapter(PendingScreen.this,
                    pendingList);
            recyclerView.setAdapter(pendingScreenAdapter);
            recyclerView.showShimmerAdapter();
            recyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadCards();
                }
            }, 1000);
        }
    }

    private void loadCards() {

        recyclerView.hideShimmerAdapter();

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
                pendingScreenAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
// filter recycler view when text is changed
                pendingScreenAdapter.getFilter().filter(query);
                return false;
            }
        });
        return true;
    }

    public class toUploadTask extends AsyncTask<RealTimeMonitoringSystem, Void,
            JSONObject> {
        @Override
        protected JSONObject doInBackground(RealTimeMonitoringSystem... realTimeValue) {
            dbData.open();
            JSONArray track_data = new JSONArray();
            ArrayList<RealTimeMonitoringSystem> assets = dbData.getSavedWorkImage("upload",realTimeValue[0].getDistictCode(),realTimeValue[0].getBlockCode(),realTimeValue[0].getPvCode(),String.valueOf(realTimeValue[0].getWorkId()));

            if (assets.size() > 0) {
                for (int i = 0; i < assets.size(); i++) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        String work_id = String.valueOf(assets.get(i).getWorkId());

                        jsonObject.put(AppConstant.WORK_ID,work_id);
                        jsonObject.put(AppConstant.WORK_GROUP_ID,assets.get(i).getWorkGroupID());
                        jsonObject.put(AppConstant.WORK_TYPE_ID,assets.get(i).getWorkTypeID());
                        jsonObject.put(AppConstant.DISTRICT_CODE,assets.get(i).getDistictCode());
                        jsonObject.put(AppConstant.BLOCK_CODE,assets.get(i).getBlockCode());
                        jsonObject.put(AppConstant.PV_CODE,assets.get(i).getPvCode());
                        jsonObject.put(AppConstant.TYPE_OF_WORK,assets.get(i).getTypeOfWork());
                        if(assets.get(i).getTypeOfWork().equalsIgnoreCase(AppConstant.ADDITIONAL_WORK)){
                            String cd_work_no = String.valueOf(assets.get(i).getCdWorkNo());
                            jsonObject.put(AppConstant.CD_WORK_NO,cd_work_no);
                            jsonObject.put(AppConstant.WORK_TYPE_FLAG_LE,assets.get(i).getWorkTypeFlagLe());
                            prefManager.setTypeOfWork(AppConstant.ADDITIONAL_WORK);
                            prefManager.setDeleteCdWorkNo(cd_work_no);
                            prefManager.setDeleteCdWorkTypeFlag(assets.get(i).getWorkTypeFlagLe());
                        }else if(assets.get(i).getTypeOfWork().equalsIgnoreCase(AppConstant.GROUP_WORK)){
                            String work_type_link_id = String.valueOf(assets.get(i).getWork_type_link_id());
                            String group_work_type = String.valueOf(assets.get(i).getGroup_work_type());
                            String is_group_work = String.valueOf(assets.get(i).getIs_group_work());
                            jsonObject.put("work_type_link_id",work_type_link_id);
                            jsonObject.put("group_work_type",group_work_type);
                            jsonObject.put("is_group_work",is_group_work);
                            prefManager.setTypeOfWork(AppConstant.GROUP_WORK);
                            prefManager.setDeleteWork_type_link_id(work_type_link_id);
                            prefManager.setDeletegroup_work_type(group_work_type);
                        }else {
                            prefManager.setTypeOfWork(AppConstant.MAIN_WORK);
                        }
                        jsonObject.put(AppConstant.WORK_STAGE_CODE,assets.get(i).getWorkStageCode());
                        jsonObject.put(AppConstant.KEY_LATITUDE,assets.get(i).getLatitude());
                        jsonObject.put(AppConstant.KEY_LONGITUDE,assets.get(i).getLongitude());
                        jsonObject.put(AppConstant.KEY_CREATED_DATE,assets.get(i).getCreatedDate());

                        Bitmap bitmap = assets.get(i).getImage();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
                        byte[] imageInByte = baos.toByteArray();
                        String image_str = Base64.encodeToString(imageInByte, Base64.DEFAULT);

                        jsonObject.put(AppConstant.KEY_IMAGES,image_str);
                        jsonObject.put(AppConstant.KEY_IMAGE_REMARK,assets.get(i).getImageRemark());

                        track_data.put(jsonObject);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                dataset = new JSONObject();

                try {
                    dataset.put(AppConstant.KEY_SERVICE_ID,"work_phy_stage_save");
                    dataset.put(AppConstant.KEY_TRACK_DATA,track_data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return dataset;
        }

        @Override
        protected void onPostExecute(JSONObject dataset) {
            super.onPostExecute(dataset);
            syncData();
        }
    }

    public void syncData() {
        try {
            new ApiService(this).makeJSONObjectRequest("saveImage", Api.Method.POST, UrlGenerator.getWorkListUrl(), saveListJsonParams(), "not cache", this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSONObject saveListJsonParams() throws JSONException {
        String authKey = Utils.encrypt(prefManager.getUserPassKey(), getResources().getString(R.string.init_vector), dataset.toString());
        JSONObject dataSet = new JSONObject();
        dataSet.put(AppConstant.KEY_USER_NAME, prefManager.getUserName());
        dataSet.put(AppConstant.DATA_CONTENT, authKey);
        Log.d("saveImage", "" + authKey);
        return dataSet;
    }

    @Override
    public void OnMyResponse(ServerResponse serverResponse) {
        try {
            String urlType = serverResponse.getApi();
            JSONObject responseObj = serverResponse.getJsonResponse();

            if ("saveImage".equals(urlType) && responseObj != null) {
                String key = responseObj.getString(AppConstant.ENCODE_DATA);
                String responseDecryptedBlockKey = Utils.decrypt(prefManager.getUserPassKey(), key);
                JSONObject jsonObject = new JSONObject(responseDecryptedBlockKey);
                if (jsonObject.getString("STATUS").equalsIgnoreCase("OK") && jsonObject.getString("RESPONSE").equalsIgnoreCase("OK")) {
                    Utils.showAlert(this, "Your Data is Synchronized to the server!");
                    String type_of_work = prefManager.getTypeOfWork();
                    dbData.open();
                    if(type_of_work.equalsIgnoreCase(AppConstant.MAIN_WORK))
                    {
                        dbData.deleteWorkListTable();
                        db.delete(DBHelper.SAVE_IMAGE, "dcode = ? and bcode = ? and pvcode = ? and work_id = ? and  type_of_work = ?", new String[]{prefManager.getDistrictCode(), prefManager.getBlockCode(), prefManager.getPvCode(), prefManager.getDeleteWorkId(),type_of_work});
                        pendingScreenAdapter.removeSavedItem(prefManager.getDeleteAdapterPosition());
                        pendingScreenAdapter.notifyDataSetChanged();
                    }else if(type_of_work.equalsIgnoreCase(AppConstant.ADDITIONAL_WORK))
                    {
                        dbData.deleteAdditionalListTable();
                        db.delete(DBHelper.SAVE_IMAGE, "dcode = ? and bcode = ? and pvcode = ? and work_id = ? and  type_of_work = ? and cd_work_no = ? and work_type_flag_le = ?", new String[]{prefManager.getDistrictCode(), prefManager.getBlockCode(), prefManager.getPvCode(), prefManager.getDeleteWorkId(),type_of_work,prefManager.getDeleteCdWorkNo(),prefManager.getDeleteCdWorkTypeFlag()});
                        pendingScreenAdapter.removeSavedItem(prefManager.getDeleteAdapterPosition());
                        pendingScreenAdapter.notifyDataSetChanged();
                    }
                    else if(type_of_work.equalsIgnoreCase(AppConstant.GROUP_WORK))
                    {
                        dbData.deleteGroupWorkListTable();
                        db.delete(DBHelper.SAVE_IMAGE, "dcode = ? and bcode = ? and pvcode = ? and work_id = ? and  type_of_work = ? and work_type_link_id = ? and group_work_type = ?", new String[]{prefManager.getDistrictCode(), prefManager.getBlockCode(), prefManager.getPvCode(), prefManager.getDeleteWorkId(),type_of_work,prefManager.getDeleteWork_type_link_id(),prefManager.getDeletegroup_work_type()});
                        pendingScreenAdapter.removeSavedItem(prefManager.getDeleteAdapterPosition());
                        pendingScreenAdapter.notifyDataSetChanged();
                    }
                }
                else if (jsonObject.getString("STATUS").equalsIgnoreCase("OK") && jsonObject.getString("RESPONSE").equalsIgnoreCase("FAIL")) {
                    Utils.showAlert(this, jsonObject.getString("MESSAGE"));
                    String type_of_work = prefManager.getTypeOfWork();
                    /*dbData.open();
                    if(type_of_work.equalsIgnoreCase(AppConstant.MAIN_WORK))
                    {
                        db.delete(DBHelper.SAVE_IMAGE, "dcode = ? and bcode = ? and pvcode = ? and work_id = ? and  type_of_work = ?", new String[]{prefManager.getDistrictCode(), prefManager.getBlockCode(), prefManager.getPvCode(), prefManager.getDeleteWorkId(),type_of_work});
                        pendingScreenAdapter.removeSavedItem(prefManager.getDeleteAdapterPosition());
                        pendingScreenAdapter.notifyDataSetChanged();
                    }else if(type_of_work.equalsIgnoreCase(AppConstant.ADDITIONAL_WORK))
                    {
                        db.delete(DBHelper.SAVE_IMAGE, "dcode = ? and bcode = ? and pvcode = ? and work_id = ? and  type_of_work = ? and cd_work_no = ? and work_type_flag_le = ?", new String[]{prefManager.getDistrictCode(), prefManager.getBlockCode(), prefManager.getPvCode(), prefManager.getDeleteWorkId(),type_of_work,prefManager.getDeleteCdWorkNo(),prefManager.getDeleteCdWorkTypeFlag()});
                        pendingScreenAdapter.removeSavedItem(prefManager.getDeleteAdapterPosition());
                        pendingScreenAdapter.notifyDataSetChanged();
                    }*/
                }
                Log.d("savedImage", "" + responseDecryptedBlockKey);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
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
    public void OnError(VolleyError volleyError) {

    }
}
