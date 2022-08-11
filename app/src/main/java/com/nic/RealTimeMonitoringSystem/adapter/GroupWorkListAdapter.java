package com.nic.RealTimeMonitoringSystem.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.databinding.DataBindingUtil;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.nic.RealTimeMonitoringSystem.R;
import com.nic.RealTimeMonitoringSystem.activity.CameraScreen;
import com.nic.RealTimeMonitoringSystem.activity.FullImageActivity;
import com.nic.RealTimeMonitoringSystem.constant.AppConstant;
import com.nic.RealTimeMonitoringSystem.dataBase.DBHelper;
import com.nic.RealTimeMonitoringSystem.dataBase.dbData;
import com.nic.RealTimeMonitoringSystem.databinding.AdapterGroupWorkListBinding;
import com.nic.RealTimeMonitoringSystem.model.RealTimeMonitoringSystem;
import com.nic.RealTimeMonitoringSystem.session.PrefManager;
import com.nic.RealTimeMonitoringSystem.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class GroupWorkListAdapter extends PagedListAdapter<RealTimeMonitoringSystem, GroupWorkListAdapter.MyViewHolder> implements Filterable {
    private List<RealTimeMonitoringSystem> groupWorkList;
    private List<RealTimeMonitoringSystem> groupWorkListFiltered;
    private String letter;
    private Context context;
    private ColorGenerator generator = ColorGenerator.MATERIAL;
    private final com.nic.RealTimeMonitoringSystem.dataBase.dbData dbData;
    PrefManager prefManager;
    public final String dcode,bcode,pvcode;
    public static DBHelper dbHelper;
    public static SQLiteDatabase db;    private LayoutInflater layoutInflater;
    private static DiffUtil.ItemCallback<RealTimeMonitoringSystem> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<RealTimeMonitoringSystem>() {
                @Override
                public boolean areItemsTheSame(RealTimeMonitoringSystem oldItem, RealTimeMonitoringSystem newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @SuppressLint("DiffUtilEquals")
                @Override
                public boolean areContentsTheSame(RealTimeMonitoringSystem oldItem, RealTimeMonitoringSystem newItem) {
                    return oldItem.equals(newItem);
                }
            };
    public GroupWorkListAdapter(Context context, List<RealTimeMonitoringSystem> WorkListValues,dbData dbData) {
        super(DIFF_CALLBACK);
        this.context = context;
        prefManager = new PrefManager(context);
        this.groupWorkList = WorkListValues;
        this.groupWorkListFiltered = WorkListValues;
        this.dbData = dbData;
        dcode = prefManager.getDistrictCode();
        bcode = prefManager.getBlockCode();
        pvcode = prefManager.getPvCode();
        try {
            dbHelper = new DBHelper(context);
            db = dbHelper.getWritableDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private AdapterGroupWorkListBinding groupWorkListBinding;

        public MyViewHolder(AdapterGroupWorkListBinding Binding) {
            super(Binding.getRoot());
            groupWorkListBinding = Binding;
        }
    }

    @Override
    public GroupWorkListAdapter.MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(viewGroup.getContext());
        }
        AdapterGroupWorkListBinding groupWorkListBinding =
                DataBindingUtil.inflate(layoutInflater, R.layout.adapter_group_work_list, viewGroup, false);
        return new MyViewHolder(groupWorkListBinding);
    }

    @Override
    public void onBindViewHolder(GroupWorkListAdapter.MyViewHolder holder, final int position) {
        dbData.open();
        // holder.adapterAdditionalListBinding.workid.setText(String.valueOf(AdditionalListValuesFiltered.get(position).getWorkId()));
        if(groupWorkListFiltered.get(position).getWorkTypeName().isEmpty()){
            holder.groupWorkListBinding.tvCdWorkNameLayout.setVisibility(View.GONE);
        }else {
            holder.groupWorkListBinding.tvCdWorkNameLayout.setVisibility(View.VISIBLE);
            holder.groupWorkListBinding.workName.setText(groupWorkListFiltered.get(position).getWorkTypeName());
        }


        if(groupWorkListFiltered.get(position).getWorkStageName().isEmpty()){
            holder.groupWorkListBinding.tvWorkstageLayout.setVisibility(View.GONE);
        }else {
            holder.groupWorkListBinding.tvWorkstageLayout.setVisibility(View.VISIBLE);
            holder.groupWorkListBinding.tvWorkstage.setText(groupWorkListFiltered.get(position).getWorkStageName());
        }

        String workGroupId = String.valueOf(groupWorkListFiltered.get(position).getWorkGroupID());
        String work_type_link_id = String.valueOf(groupWorkListFiltered.get(position).getWork_type_link_id());
        String group_work_type = String.valueOf(groupWorkListFiltered.get(position).getGroup_work_type());
        String currentStageCode = String.valueOf(groupWorkListFiltered.get(position).getCurrentStage());

        String sql = "select * from "+ DBHelper.WORK_STAGE_TABLE+" where work_stage_order >(select work_stage_order from "+DBHelper.WORK_STAGE_TABLE+" where work_stage_code='"+currentStageCode+"' and work_group_id=" + workGroupId + "  and work_type_id=" + work_type_link_id + ")  and work_group_id=" + workGroupId + "  and work_type_id=" + work_type_link_id + " and work_stage_code != 11 order by work_stage_order";
        //String sql = "select * from "+ DBHelper.WORK_STAGE_TABLE+" where work_stage_order >(select work_stage_order from "+DBHelper.WORK_STAGE_TABLE+" where work_stage_code='"+currentStageCode+"' and work_group_id=" + workGroupId + "  and work_type_id=" + work_type_link_id + ")  and work_group_id=" + workGroupId + "  and work_type_id=" + work_type_link_id + " order by work_stage_order";
        Log.d("que",sql);
        Cursor Stage = db.rawQuery(sql, null);

        if(Stage.getCount() > 0 ){
            holder.groupWorkListBinding.takePhoto.setVisibility(View.VISIBLE);
            holder.groupWorkListBinding.viewOfflineImage.setVisibility(View.VISIBLE);
        }
        else {
            holder.groupWorkListBinding.takePhoto.setVisibility(View.GONE);
            holder.groupWorkListBinding.viewOfflineImage.setVisibility(View.GONE);
        }


        holder.groupWorkListBinding.takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCameraScreen(position);
            }
        });

        final String work_id = String.valueOf(groupWorkListFiltered.get(position).getWorkId());
        final String cd_work_no = String.valueOf(groupWorkListFiltered.get(position).getCdWorkNo());
        final String work_type_flag_le = String.valueOf(groupWorkListFiltered.get(position).getWorkTypeFlagLe());

        ArrayList<RealTimeMonitoringSystem> imageOffline = dbData.selectImage(dcode,bcode,pvcode,work_id, AppConstant.GROUP_WORK,cd_work_no,work_type_flag_le,work_type_link_id,group_work_type);

        if(imageOffline.size() > 0) {
            holder.groupWorkListBinding.viewOfflineImage.setVisibility(View.VISIBLE);
        }
        else {
            holder.groupWorkListBinding.viewOfflineImage.setVisibility(View.GONE);
        }


        if (groupWorkListFiltered.get(position).getImageAvailable().equalsIgnoreCase("Y")) {
            holder.groupWorkListBinding.viewOnlineImage.setVisibility(View.VISIBLE);
        }
        else if(groupWorkListFiltered.get(position).getImageAvailable().equalsIgnoreCase("N")){
            holder.groupWorkListBinding.viewOnlineImage.setVisibility(View.GONE);
        }
        holder.groupWorkListBinding.viewOnlineImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewOfflineImages(work_id,group_work_type,work_type_link_id,AppConstant.GROUP_WORK,"Online",dcode,bcode,pvcode);
            }
        });
        holder.groupWorkListBinding.viewOfflineImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewOfflineImages(work_id,group_work_type,work_type_link_id,AppConstant.GROUP_WORK,"Offline",dcode,bcode,pvcode);
            }
        });
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    groupWorkListFiltered = groupWorkList;
                } else {
                    List<RealTimeMonitoringSystem> filteredList = new ArrayList<>();
                    for (RealTimeMonitoringSystem row : groupWorkList) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getWorkId().toString().toLowerCase().contains(charString.toLowerCase()) || row.getBeneficiaryName().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    groupWorkListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = groupWorkListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                groupWorkListFiltered = (ArrayList<RealTimeMonitoringSystem>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    public void openCameraScreen(int pos) {
        Activity activity = (Activity) context;
        Intent intent = new Intent(activity, CameraScreen.class);
        intent.putExtra(AppConstant.TYPE_OF_WORK,AppConstant.GROUP_WORK);
        intent.putExtra(AppConstant.WORK_GROUP_ID,String.valueOf(groupWorkListFiltered.get(pos).getWorkGroupID()));
        intent.putExtra(AppConstant.WORK_ID,String.valueOf(groupWorkListFiltered.get(pos).getWorkId()));
        intent.putExtra("work_type_link_id",String.valueOf(groupWorkListFiltered.get(pos).getWork_type_link_id()));
        intent.putExtra(AppConstant.CURRENT_STAGE_OF_WORK,String.valueOf(groupWorkListFiltered.get(pos).getCurrentStage()));
        intent.putExtra("group_work_type",String.valueOf(groupWorkListFiltered.get(pos).getGroup_work_type()));
        intent.putExtra("is_group_work",String.valueOf(groupWorkListFiltered.get(pos).getIs_group_work()));
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }

    @Override
    public int getItemCount() {
        return groupWorkListFiltered == null ? 0 : groupWorkListFiltered.size();
    }

    public void viewOfflineImages(String work_id,String group_work_type,String work_type_link_id,String type_of_work,String OnOffType,String dcode,String bcode,String pvcode) {
        Activity activity = (Activity) context;
        Intent intent = new Intent(context, FullImageActivity.class);
        intent.putExtra(AppConstant.WORK_ID,work_id);
        intent.putExtra("group_work_type",group_work_type);
        intent.putExtra("work_type_link_id",work_type_link_id);
        intent.putExtra("OnOffType",OnOffType);

        if(OnOffType.equalsIgnoreCase("Offline")){
            intent.putExtra(AppConstant.TYPE_OF_WORK,type_of_work);
            activity.startActivity(intent);
        }
        else if(OnOffType.equalsIgnoreCase("Online")) {
            if(Utils.isOnline()){
                intent.putExtra(AppConstant.DISTRICT_CODE,dcode);
                intent.putExtra(AppConstant.BLOCK_CODE,bcode);
                intent.putExtra(AppConstant.PV_CODE,pvcode);
                intent.putExtra(AppConstant.TYPE_OF_WORK,type_of_work);
                activity.startActivity(intent);
            }else {
                Utils.showAlert(activity,"Your Internet seems to be Offline.Images can be viewed only in Online mode.");
            }
        }


        activity.overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
    }
}

