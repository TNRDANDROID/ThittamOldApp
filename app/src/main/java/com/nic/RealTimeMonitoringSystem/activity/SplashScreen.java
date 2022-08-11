package com.nic.RealTimeMonitoringSystem.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.nic.RealTimeMonitoringSystem.BuildConfig;
import com.nic.RealTimeMonitoringSystem.R;
import com.nic.RealTimeMonitoringSystem.databinding.SplashScreenBinding;
import com.nic.RealTimeMonitoringSystem.helper.AppVersionHelper;
import com.nic.RealTimeMonitoringSystem.session.PrefManager;
import com.nic.RealTimeMonitoringSystem.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class SplashScreen extends AppCompatActivity implements
        AppVersionHelper.myAppVersionInterface {
    private TextView textView;
    private Button button;
    private static int SPLASH_TIME_OUT = 2000;
    private PrefManager prefManager;
    public SplashScreenBinding splashScreenBinding;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        splashScreenBinding = DataBindingUtil.setContentView(this,R.layout.splash_screen);
        splashScreenBinding.setActivity(this);
        prefManager = new PrefManager(this);
        if (BuildConfig.BUILD_TYPE.equalsIgnoreCase("production")) {
            if (Utils.isOnline()) {
                checkAppVersion();
            } else {
                showSignInScreen();

            }
        } else {
            arrayList();
            showSignInScreen();
        }
    }


    private void showSignInScreen() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent i = new Intent(SplashScreen.this, LoginScreen.class);

                startActivity(i);
                finish();
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
            }
        }, SPLASH_TIME_OUT);
    }

    private void checkAppVersion() {
        new AppVersionHelper(this, SplashScreen.this).callAppVersionCheckApi();
    }

    @Override
    public void onAppVersionCallback(String value) {
        if (value.length() > 0 && "Update".equalsIgnoreCase(value)) {
            startActivity(new Intent(this, AppUpdateDialog.class));
            finish();
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        } else {
            showSignInScreen();
        }

    }
    private void arrayList(){
        try {
            ArrayList<Integer> arrayList = new ArrayList<>();
            ArrayList<Integer> arrayList_count= new ArrayList<>();
            HashMap<String, Integer> hash_map = new HashMap<String, Integer>();
            arrayList.add(1);
            arrayList.add(3);
            arrayList.add(3);
            arrayList.add(3);
            arrayList.add(5);
            arrayList.add(3);
            arrayList.add(5);
            arrayList.add(5);
            arrayList.add(5);
            arrayList.add(5);
            arrayList.add(5);
            int size = arrayList.size();
            int check_value = 0;
            while (size!=0) {
                int count=0;
                String value = String.valueOf(arrayList.get(check_value));
                for (int i = 0; i < arrayList.size(); i++) {
                    String that_number = String.valueOf(arrayList.get(i));
                    if (value.equals(String.valueOf(arrayList.get(i)))) {
                        count=count+1;
                    }
                }
                arrayList_count.add(count);
                hash_map.put(value,count);
                size--;
                check_value++;
            }

            //int highestNumber = Collections.max(arrayList_count);



            int maxValueInMap=(Collections.max(hash_map.values()));  // This will return max value in the Hashmap
            for (Map.Entry<String, Integer> entry : hash_map.entrySet()) {  // Itrate through hashmap
                if (entry.getValue()==maxValueInMap) {
                    System.out.println("The Number  >>"+entry.getKey()+ " is repeat "+maxValueInMap+" times");     // Print the key with max value
                }
            }
        }
        catch (Exception e){

        }

    }
}
