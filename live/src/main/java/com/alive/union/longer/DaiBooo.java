package com.alive.union.longer;

import android.app.job.JobParameters;
import android.app.job.JobService;

public  class DaiBooo extends JobService {

    public final String f60a;

    public DaiBooo() {
        this("2g");
    }

    public DaiBooo(String str) {

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
