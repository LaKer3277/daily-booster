package com.alive.union.longer;

import android.accounts.Account;
import android.content.Context;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;

import com.alive.union.base.DaiBooISyncAdapterUACall;
import com.alive.union.base.DaiBooISyncContext;

public final class DaiBooc4 extends DaiBooy3 {
    public final Context context;

    public DaiBooc4(Context context) {
        super(context);
        this.context = context;
    }

    @Override // c.o.a.e.y3, android.content.ISyncAdapter
    public void cancelSync(DaiBooISyncContext iSyncContext) {
    }

    @Override // c.o.a.e.y3, android.content.ISyncAdapter
    public void onUnsyncableAccount(DaiBooISyncAdapterUACall iSyncAdapterUnsyncableAccountCallback) {
        if (iSyncAdapterUnsyncableAccountCallback != null) {
            try {
                iSyncAdapterUnsyncableAccountCallback.onUnsyncableAccountDone(false);
            } catch (Throwable th) {
            }
        }
    }

    @Override // c.o.a.e.y3, android.content.ISyncAdapter
    public void startSync(DaiBooISyncContext iSyncContext, String str, Account account, Bundle bundle) {
        try {
            if (Build.VERSION.SDK_INT > 26 && this.context.getApplicationInfo().targetSdkVersion > 26) {
                return;
            }
            if (iSyncContext != null) {
                iSyncContext.onFinished(new SyncResult());
            }
        } catch (Throwable unused) {
        }
    }
}