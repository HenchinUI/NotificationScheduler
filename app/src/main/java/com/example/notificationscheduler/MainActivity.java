package com.example.notificationscheduler;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private JobScheduler mScheduler;
    private static final int JOB_ID = 0;

    private RadioGroup networkOptions;
    private Switch mDeviceIdleSwitch;
    private Switch mDeviceChargingSwitch;
    private SeekBar mSeekBar;
    private TextView seekBarProgressLabel; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        networkOptions = findViewById(R.id.networkOptions);
        mDeviceIdleSwitch = findViewById(R.id.deviceIdleSwitch);
        mDeviceChargingSwitch = findViewById(R.id.deviceChargingSwitch);
        mSeekBar = findViewById(R.id.seekBar);

        mScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE); 

        findViewById(R.id.scheduleJobButton).setOnClickListener(v -> scheduleJob());
        findViewById(R.id.cancelJobButton).setOnClickListener(v -> cancelJobs());

    }

    private void scheduleJob() {
        int selectedNetworkID = networkOptions.getCheckedRadioButtonId();
        int selectedNetworkOption = JobInfo.NETWORK_TYPE_NONE;

        if (selectedNetworkID == R.id.anyNetwork) {
            selectedNetworkOption = JobInfo.NETWORK_TYPE_ANY;
        } else if (selectedNetworkID == R.id.wifiNetwork) {
            selectedNetworkOption = JobInfo.NETWORK_TYPE_UNMETERED;
        }

        ComponentName serviceName = new ComponentName(getPackageName(), NotificationJobService.class.getName());
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, serviceName)
                .setRequiredNetworkType(selectedNetworkOption)
                .setRequiresDeviceIdle(mDeviceIdleSwitch.isChecked())
                .setRequiresCharging(mDeviceChargingSwitch.isChecked());

        int seekBarValue = mSeekBar.getProgress();
        boolean seekBarSet = seekBarValue > 0;
        if (seekBarSet) {
            builder.setOverrideDeadline(seekBarValue * 1000L);
        }

        boolean constraintSet = (selectedNetworkOption != JobInfo.NETWORK_TYPE_NONE)
                || mDeviceChargingSwitch.isChecked()
                || mDeviceIdleSwitch.isChecked()
                || seekBarSet;

        if (constraintSet) {
            JobInfo myJobInfo = builder.build();
            int resultCode = mScheduler.schedule(myJobInfo);

            if (resultCode == JobScheduler.RESULT_SUCCESS) {
                Toast.makeText(this, "Job scheduled successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Job scheduling failed!", Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(this, "Please set at least one constraint", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelJobs() {
        if (mScheduler != null) {
            mScheduler.cancelAll();
            Toast.makeText(this, "All jobs canceled", Toast.LENGTH_SHORT).show();
        }
    }
}
