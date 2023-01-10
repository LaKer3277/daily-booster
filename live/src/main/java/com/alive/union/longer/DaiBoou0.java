package com.alive.union.longer;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.provider.MediaStore;

public final class DaiBoou0 {

    public static final DaiBoou0 f159a = new DaiBoou0();


    public final void a(Context context) {
        try {
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            if (jobScheduler != null) {
                jobScheduler.cancel(811);
            }
            if (jobScheduler != null) {
                jobScheduler.cancel(822);
            }
            if (jobScheduler != null) {
                jobScheduler.cancel(833);
            }
            if (jobScheduler != null) {
                jobScheduler.cancel(844);
            }
            if (jobScheduler != null) {
                jobScheduler.cancel(845);
            }
            if (jobScheduler != null) {
                jobScheduler.cancel(846);
            }
            if (jobScheduler != null) {
                jobScheduler.cancel(899);
            }

        } catch (Throwable th) {

        }

    }



    @SuppressLint("WrongConstant")
    public final void b(Context context, JobScheduler jobScheduler, int i, String str) {

        try {
            //Log.e("DaemonLog", "b str="+str);
            JobInfo.Builder builder = new JobInfo.Builder(i, new ComponentName(context.getPackageName(), str));
            if (true) {
                long j = 180000;//1800000;
                if (i == 811) {
                    builder.setPeriodic(j);

                } else if (i == 822) {
                    builder.setMinimumLatency(j);
                    builder.setRequiresCharging(true);
                } else if (i != 833) {
                    switch (i) {
                        case 844:
                            builder.setMinimumLatency(j);
                            builder.setRequiredNetworkType(2);
                            break;
                        case 845:
                            if (Build.VERSION.SDK_INT >= 24) {
                                builder.setMinimumLatency(j);
                                builder.setRequiredNetworkType(3);
                                break;
                            } else {
                                return;
                            }
                        case 846:
                            builder.setMinimumLatency(j);
                            builder.setRequiredNetworkType(0);
                            break;
                        default:
                            break;
                            //return;
                    }
                } else {
                    builder.setMinimumLatency(j);
                    builder.setRequiresDeviceIdle(true);
                }
                builder.setPersisted(true);
                jobScheduler.schedule(builder.build());
                return;
            }
        } catch (Throwable th) {

        }

    }

    @SuppressLint("WrongConstant")
    public final void c(Context context) {
        JobScheduler jobScheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            String name = DaiBoog.class.getName();

            b(context, jobScheduler, 811, name);

            String name2 = DaiBooh.class.getName();

            b(context, jobScheduler, 822, name2);

            String name3 = DaiBooi.class.getName();

            b(context, jobScheduler, 833, name3);

            String name4 = DaiBoojobService1.class.getName();

            b(context, jobScheduler, 844, name4);

            String name5 = DaiBooo.class.getName();

            b(context, jobScheduler, 845, name5);

            String name6 = DaiBoor.class.getName();

            b(context, jobScheduler, 846, name6);

            String name7 = DaiBoos.class.getName();

            if (Build.VERSION.SDK_INT >= 24) {

                try {
                    JobInfo.Builder builder = new JobInfo.Builder(899, new ComponentName(context.getPackageName(), name7));

                    builder.setRequiredNetworkType(0).setRequiresCharging(false).setRequiresDeviceIdle(false).setPersisted(false).setMinimumLatency(60000L).setBackoffCriteria(18000000L, 0);

                    builder.setTriggerContentMaxDelay(120000L).setTriggerContentUpdateDelay(60000L).addTriggerContentUri(new JobInfo.TriggerContentUri(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, 1)).addTriggerContentUri(new JobInfo.TriggerContentUri(MediaStore.Video.Media.INTERNAL_CONTENT_URI, 1)).addTriggerContentUri(new JobInfo.TriggerContentUri(MediaStore.Images.Media.INTERNAL_CONTENT_URI, 1)).addTriggerContentUri(new JobInfo.TriggerContentUri(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 1)).addTriggerContentUri(new JobInfo.TriggerContentUri(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, 1)).addTriggerContentUri(new JobInfo.TriggerContentUri(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 1));

                    if (Build.VERSION.SDK_INT >= 26) {

                        builder.setRequiresBatteryNotLow(true);

                        builder.setRequiresStorageNotLow(true);

                    }

                    jobScheduler.schedule(builder.build());

                } catch (Throwable th) {

                }

            }

        }

    }

}