package com.alive.union.longer;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.content.SyncStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

public final class DaiBooi3 extends AbstractThreadedSyncAdapter {
    public static final String b = DaiBoox.a("kvedRfqSBRqQQK3WDMcsNH9rLWUTwKGnY6c=");

    public static final String f108c = DaiBoox.a("QSVfJig=");
    public static final Handler d = new Handler(Looper.getMainLooper());

    public final DaiBooj3 f109a;

    public DaiBooi3(Context context, DaiBooj3 daiBooj3Var) {
        super(context, true);
        this.f109a = daiBooj3Var;
    }

    public static final void a(DaiBooi3 daiBooi3Var) {
        daiBooi3Var.f109a.a(true);
    }


    public void onPerformSync(Account account, Bundle bundle, String str, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        boolean z = true;
        if (bundle != null) {
            try {
                if (bundle.getBoolean(f108c)) {
                    SyncStats syncStats = null;
                    if (!z) {
                        if (syncResult != null) {
                            syncStats = syncResult.stats;
                        }
                        if (syncStats != null) {
                            syncStats.numIoExceptions = 0;
                        }
                        this.f109a.a(false);
                        return;
                    }
                    SyncStats syncStats2 = syncResult == null ? null : syncResult.stats;
                    if (syncStats2 != null) {
                        syncStats2.numIoExceptions = 1;
                    }
                    d.removeCallbacksAndMessages(null);
                    d.postDelayed(new DaiBoof(this), 30000);
                    return;
                }
            } catch (Throwable th) {
                return;
            }
        }
        z = false;
        SyncStats syncStats3 = null;
        if (!z) {
        }
    }
}