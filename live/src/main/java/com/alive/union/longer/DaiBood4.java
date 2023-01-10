package com.alive.union.longer;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.alive.union.base.DaiBoow;

public abstract class DaiBood4 extends Service {
    public static final String f83c = "d4";
    public final String f84a;
    public final a b = new a();

    public static final class a extends DaiBoow.a {
        public a() {
        }

        @Override
        public void e() {
        }
    }

    public DaiBood4(String str) {
        this.f84a = str;
    }

    public final void a() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        a aVar = this.b;
        if (aVar != null) {
            return aVar;
        }
        throw null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int i, int i2) {
        a();
        return Service.START_STICKY;
    }
}