package com.general.files;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.utils.Logger;


public class ConfigDriverTripStatusJobService extends JobService {

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.e("ConfigDriverTripStatus", "::JobService::CREATE::");
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Logger.e("ConfigDriverTripStatus", "::JobService::START::");
        try {
            if (MyApp.getInstance().getCurrentAct() != null) {
                ConfigDriverTripStatus.getInstance().executeTaskRun(() -> ConfigDriverTripStatusJobService.this.jobFinished(params, true));
            } else {
                ConfigDriverTripStatusJobService.this.jobFinished(params, true);
            }
        } catch (Exception e) {
            ConfigDriverTripStatusJobService.this.jobFinished(params, true);
        }
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Logger.e("ConfigDriverTripStatus", "::JobService::STOP::");
        return true;
    }
}
