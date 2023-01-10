package com.alive.union.longer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public final class DaiBoox1 extends Service {

    public static DaiBoom3 b;

    public IBinder onBind(Intent intent) {
        try {
            if (b == null) {
                synchronized (DaiBoox1.class) {
                    if (b == null) {
                        Context applicationContext = getApplicationContext();
                        b = new DaiBooy3(applicationContext);
                    }
                }
            }
            DaiBoom3 daiBoom3Var = b;
            if (daiBoom3Var != null) {
                try {
                    return daiBoom3Var.asBinder();
                } catch (Throwable unused) {
                }
            }
        } catch (Throwable th) {
        }
        return null;
    }

    public void onCreate() {
        super.onCreate();
    }
}