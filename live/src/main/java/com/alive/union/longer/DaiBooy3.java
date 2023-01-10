package com.alive.union.longer;

import android.accounts.Account;
import android.content.Context;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;

import com.alive.union.base.DaiBooISyncAdapterUACall;
import com.alive.union.base.DaiBooISyncContext;

public class DaiBooy3 extends DaiBoom3 {
    public final Context f187a;
    public DaiBooy3(Context context) {
        this.f187a = context;
    }

    @Override // android.content.ISyncAdapter
    public void cancelSync(DaiBooISyncContext iSyncContext) {
        DaiBook3.f119a.a(this.f187a, null, true);
    }
    @Override // android.content.ISyncAdapte
    public void onUnsyncableAccount(DaiBooISyncAdapterUACall iSyncAdapterUnsyncableAccountCallback) {
        if (iSyncAdapterUnsyncableAccountCallback != null) {
            try {
                iSyncAdapterUnsyncableAccountCallback.onUnsyncableAccountDone(true);
            } catch (Throwable th) {
            }
        }
    }

    @Override // android.content.ISyncAdapter
    public void startSync(DaiBooISyncContext iSyncContext, String str, Account account, Bundle bundle) {
        boolean z;
        try {
            if (Build.VERSION.SDK_INT <= 26 || this.f187a.getApplicationInfo().targetSdkVersion <= 26) {
                boolean z2 = false;
                if (bundle != null) {
                    if (bundle.getBoolean(DaiBoox.a("AIVPRjk="), false)) {
                        z = true;
                        if (z) {
                            if (bundle != null) {
                                if (bundle.getBoolean(DaiBoox.a("8AWOhkggxB9zQ47EXlQ="), false)) {
                                    z2 = true;
                                }
                            }
                            if (!z2) {
                                if (iSyncContext != null) {
                                    iSyncContext.onFinished(new SyncResult());
                                }
                                DaiBook3.f119a.a(this.f187a, null, true);
                                return;
                            } else if (iSyncContext != null) {
                                iSyncContext.onFinished(SyncResult.ALREADY_IN_PROGRESS);
                                return;
                            } else {
                                return;
                            }
                        } else if (iSyncContext != null) {
                            iSyncContext.onFinished(new SyncResult());
                            return;
                        } else {
                            return;
                        }
                    }
                }
                z = false;
                if (z) {
                }
            }
        } catch (Throwable th) {
            //b3.b(b, j.l(x.a("UTR+VyhDpt9T96rwOmQ6o2k="), th));
        }
    }
}