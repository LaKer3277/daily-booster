package com.alive.union.longer;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

public  class DaiBoojobService1 extends JobService {

    public final String f60a;

    public DaiBoojobService1() {
        this("wifi");
    }

    public DaiBoojobService1(String str) {
        this.f60a = str;
    }


    @Override // android.app.job.JobService
    public boolean onStartJob(JobParameters jobParameters) {
        return true;
    }


    @Override // android.app.job.JobService
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

}
