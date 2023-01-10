package com.alive.union.longer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public final class DaiBook1 extends Service {

    public IBinder onBind(Intent intent) {

        try {
            Context applicationContext = getApplicationContext();
            try {
                return new DaiBooSyncBeanC3DaiBoo(applicationContext).asBinder();
            } catch (Throwable unused) {
                return null;
            }
        } catch (Throwable th) {
            return null;
        }
    }

    public void onCreate() {
        super.onCreate();
    }
}