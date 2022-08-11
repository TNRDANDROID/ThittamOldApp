package com.nic.RealTimeMonitoringSystem.dataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "RealTimeMonitoringSystem";
    private static final int DATABASE_VERSION = 1;

    public static final String DISTRICT_TABLE_NAME = "DistrictTable";
    public static final String BLOCK_TABLE_NAME = " BlockTable";
    public static final String VILLAGE_TABLE_NAME = " villageTable";
    public static final String SCHEME_TABLE_NAME = "SchemeList";
    public static final String FINANCIAL_YEAR_TABLE_NAME = "FinancialYear";
    public static final String WORK_STAGE_TABLE = "work_type_stage_link";
    public static final String ADDITIONAL_WORK_STAGE_TABLE = "addditional_work_stages";
    public static final String WORK_LIST_TABLE_BASED_ON_FINYEAR_VIlLAGE = "WorkList_based_on_finYear_village";
    public static final String ADDITIONAL_WORK_LIST = "additional_work_list";
    public static final String GROUP_WORK_LIST = "group_work_list";
    public static final String SAVE_IMAGE = "save_image";

    private Context context;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;

    }

    //creating tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DISTRICT_TABLE_NAME + " ("
                + "dcode INTEGER," +
                "dname TEXT)");

        db.execSQL("CREATE TABLE " + BLOCK_TABLE_NAME + " ("
                + "dcode INTEGER," +
                "bcode INTEGER," +
                "bname TEXT)");

        db.execSQL("CREATE TABLE " + VILLAGE_TABLE_NAME + " ("
                + "dcode INTEGER," +
                "bcode INTEGER," +
                "pvcode INTEGER," +
                "pvname TEXT)");


        db.execSQL("CREATE TABLE " + SCHEME_TABLE_NAME + " ("
                + "scheme_name TEXT," +
                "fin_year  TEXT," +
                "scheme_seq_id INTEGER)");

        db.execSQL("CREATE TABLE " + WORK_STAGE_TABLE + " ("
                + "work_group_id  INTEGER," +
                "work_type_id  INTEGER," +
                "work_stage_order  INTEGER," +
                "work_stage_code  INTEGER," +
                "work_stage_name TEXT)");

        db.execSQL("CREATE TABLE " + ADDITIONAL_WORK_STAGE_TABLE + " ("
                + "work_type_code  INTEGER," +
                "work_stage_order  INTEGER," +
                "work_stage_code  INTEGER," +
                "work_stage_name TEXT," +
                "cd_type_flag TEXT)");

        db.execSQL("CREATE TABLE " + FINANCIAL_YEAR_TABLE_NAME + " ("
                + "fin_year TEXT)");

        db.execSQL("CREATE TABLE " + WORK_LIST_TABLE_BASED_ON_FINYEAR_VIlLAGE + " ("
                + "dcode INTEGER," +
                "bcode INTEGER," +
                "pvcode INTEGER," +
                "work_id INTEGER," +
                "scheme_group_id INTEGER," +
                "scheme_id INTEGER," +
                "schemegrp_name  TEXT," +
                "scheme_name  TEXT," +
                "fin_year  TEXT," +
                "agency_name  TEXT," +
                "wrkgrpname  TEXT," +
                "work_name  TEXT," +
                "work_group_id  INTEGER," +
                "work_type  INTEGER," +
                "mworkid  INTEGER," +
                "current_stage_of_work INTEGER," +
                "as_value INTEGER," +
                "amount_spent_sofar INTEGER," +
                "stage_name TEXT," +
                "hai_beneficiary_name TEXT," +
                "hai_beneficiary_fhname TEXT," +
                "worktypname TEXT," +
                "yn_completed TEXT," +
                "cd_prot_work_yn TEXT," +
                "state_code INTEGER," +
                "dname TEXT," +
                "bname TEXT," +
                "pvname TEXT," +
                "community_name TEXT," +
                "gender_text TEXT," +
                "upd_date TEXT,"+
                "is_group_work TEXT,"+
                "group_works_completed TEXT,"+
                "group_work_type TEXT,"+
                "image_available TEXT)");

        db.execSQL("CREATE TABLE " + ADDITIONAL_WORK_LIST + " ("
                + "dcode INTEGER," +
                "bcode INTEGER," +
                "pvcode INTEGER," +
                "scheme_id  INTEGER," +
                "fin_year  TEXT," +
                "work_id  INTEGER," +
                "work_group_id  INTEGER," +
                "roadname  TEXT," +
                "cd_work_no  INTEGER," +
                "current_stage_of_work INTEGER," +
                "cd_type_id INTEGER," +
                "work_type_flag_le  TEXT," +
                "cd_code  INTEGER," +
                "cd_name  TEXT," +
                "chainage_meter  TEXT," +
                "work_stage_name TEXT,"+
                "image_available TEXT)");

        db.execSQL("CREATE TABLE " + GROUP_WORK_LIST + " ("
                + "dcode INTEGER," +
                "bcode INTEGER," +
                "pvcode INTEGER," +
                "scheme_id  INTEGER," +
                "fin_year  TEXT," +
                "work_id  INTEGER," +
                "work_group_id  INTEGER," +
                "work_name  TEXT," +
                "group_works_completed  TEXT," +
                "is_group_work  TEXT," +
                "work_type_link_id  TEXT," +
                "current_stage_of_work INTEGER," +
                "group_work_type TEXT," +
                "work_type_name TEXT," +
                "work_stage_name  TEXT," +
                "image_available TEXT)");

        db.execSQL("CREATE TABLE " + SAVE_IMAGE + " ("
                + "work_id INTEGER," +
                "type_of_work TEXT," +
                "work_group_id  INTEGER," +
                "work_type_id  INTEGER," +
                "cd_work_no  INTEGER," +
                "work_type_flag_le  TEXT," +
                "work_type_link_id  TEXT," +
                "group_work_type  TEXT," +
                "is_group_work  TEXT," +
                "dcode TEXT," +
                "bcode TEXT," +
                "pvcode TEXT," +
                "work_stage_code TEXT," +
                "work_stage_name TEXT," +
                "latitude TEXT," +
                "longitude TEXT," +
                "images blob," +
                "remark TEXT," +
                "server_flag  INTEGER DEFAULT 0," +
                "created_date TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion >= newVersion) {
            //drop table if already exists
            db.execSQL("DROP TABLE IF EXISTS " + DISTRICT_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + BLOCK_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + VILLAGE_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + SCHEME_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + WORK_STAGE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + FINANCIAL_YEAR_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + WORK_LIST_TABLE_BASED_ON_FINYEAR_VIlLAGE);
            db.execSQL("DROP TABLE IF EXISTS " + ADDITIONAL_WORK_LIST);
            db.execSQL("DROP TABLE IF EXISTS " + GROUP_WORK_LIST);
            db.execSQL("DROP TABLE IF EXISTS " + ADDITIONAL_WORK_STAGE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + SAVE_IMAGE);

            onCreate(db);
        }
    }


}
