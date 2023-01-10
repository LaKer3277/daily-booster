package com.alive.union.longer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public final class DaiBooq1 extends Service {

    public static DaiBooi3 b;

    public IBinder onBind(Intent intent) {

        try {
            if (b == null) {
                synchronized (DaiBooq1.class) {
                    if (b == null) {
                        Context applicationContext = getApplicationContext();
                        b = new DaiBooi3(applicationContext, DaiBoog3.f101a.a(applicationContext));
                    }
                }
            }
            DaiBooi3 daiBooi3Var = b;
            if (daiBooi3Var == null) {
                return null;
            }
            return daiBooi3Var.getSyncAdapterBinder();
        } catch (Throwable th) {
            return null;
        }
    }

    public void onCreate() {
        super.onCreate();
    }
}