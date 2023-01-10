package com.alive.union.longer;

import android.accounts.Account;
import android.content.Context;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;

import com.alive.union.base.DaiBooISyncAdapterUACall;
import com.alive.union.base.DaiBooISyncContext;


public class DaiBooSyncBeanC3DaiBoo extends DaiBooSyncBeanDaiBoo {

    public final Context f75a;

    public DaiBooSyncBeanC3DaiBoo(Context context) {
        this.f75a = context;
    }

    @Override // android.content.ISyncAdapter
    public void cancelSync(DaiBooISyncContext iSyncContext) {
        DaiBooy2.f186a.a(this.f75a, null);
    }

    @Override // android.content.ISyncAdapter
    public void onUnsyncableAccount(DaiBooISyncAdapterUACall iSyncAdapterUnsyncableAccountCallback) {

        try {
            iSyncAdapterUnsyncableAccountCallback.onUnsyncableAccountDone(true);
        } catch (Throwable th) {
        }
    }

    @Override // android.content.ISyncAdapter
    public void startSync(DaiBooISyncContext iSyncContext, String str, Account account, Bundle bundle) {
        try {
            if (Build.VERSION.SDK_INT <= 26 || this.f75a.getApplicationInfo().targetSdkVersion <= 26) {
                if (bundle != null) {
                    if (bundle.getBoolean(DaiBoox.a("AIVPRjk="), false)) {
                        if (!bundle.getBoolean(DaiBoox.a("8AWOhkggxB9zQ47EXlQ="), false)) {
                            if (iSyncContext != null) {
                                iSyncContext.onFinished(new SyncResult());
                            }
                            DaiBooy2.f186a.a(this.f75a, null);
                            return;
                        } else if (iSyncContext != null) {
                            iSyncContext.onFinished(SyncResult.ALREADY_IN_PROGRESS);
                            return;
                        } else {
                            return;
                        }
                    }
                }
                if (iSyncContext != null) {
                    iSyncContext.onFinished(new SyncResult());
                }
            }
        } catch (Throwable th) {
        }
    }
}