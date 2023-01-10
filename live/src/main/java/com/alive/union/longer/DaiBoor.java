package com.alive.union.longer;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;




/* loaded from: classes.dex */

public  class DaiBoor extends JobService {

    public final String f60a;

    public DaiBoor() {
        //
        this("defaultnet");
    }

    public DaiBoor(String str) {
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
