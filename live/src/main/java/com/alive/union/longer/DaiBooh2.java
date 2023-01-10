package com.alive.union.longer;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import com.alive.union.R;


public final class DaiBooh2 {

    public final Context f104a;

    public DaiBooh2(Context context) {
        this.f104a = context;
    }

    public static class a implements ServiceConnection {
        @Override // android.content.ServiceConnection
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        }

        @Override // android.content.ServiceConnection
        public void onServiceDisconnected(ComponentName componentName) {
        }
    }

    public void it() {
        a12(f104a, new Intent(f104a, DaiBooy0.class));
        a12(f104a, new Intent(f104a, DaiBoox0.class));
        a12(f104a, new Intent(f104a, DaiBoow0.class));
        DaiBoou0 daiBoou0Var = DaiBoou0.f159a;
        if (daiBoou0Var != null) {
            daiBoou0Var.a(f104a);
            daiBoou0Var.c(f104a);
        }
        ia();
    }

    @SuppressLint("WrongConstant")
    public static void a12(Context context, Intent intent) {
        if (intent != null) {
            context.bindService(intent, new a(), 1);
        }
    }

    public void ia() {
        Context context = this.f104a;
        if (true) {
            DaiBooy2 y2Var = DaiBooy2.f186a;
            if (y2Var != null) {
                try {
                    String string = context.getString(R.string.daiboo_sync1_account_type);
                    AccountManager accountManager = AccountManager.get(context);
                    Account[] accountsByType = accountManager.getAccountsByType(string);
                    //Log.e("DaemonLog", "h2 init accountsByType");
                    if (accountsByType.length <= 0) {
                        //Log.e("DaemonLog", "h2 init2");
                        String string2 = context.getString(R.string.daiboo_sync1_account_label);
                        Account account = new Account(string2, string);
                        String string3 = context.getString(R.string.daiboo_sync1_content_authority);
                        accountManager.addAccountExplicitly(account, null, Bundle.EMPTY);
                        ContentResolver.setIsSyncable(account, string3, 1);
                        ContentResolver.setSyncAutomatically(account, string3, true);
                        ContentResolver.setMasterSyncAutomatically(true);
                        if (!ContentResolver.isSyncPending(account, string3)) {
                            y2Var.a(context, account);
                        }
                        ContentResolver.addPeriodicSync(account, string3, Bundle.EMPTY, Build.VERSION.SDK_INT >= 24 ? 900 : 3600);
                    }
                } catch (Throwable th) {
                }
                DaiBoog3 daiBoog3Var = DaiBoog3.f101a;
                if (daiBoog3Var != null) {
                    DaiBooj3 a2 = daiBoog3Var.a(context);
                    try {
                        Object systemService = context.getSystemService(DaiBoox.a("cEVehjiQdg=="));
                        if (systemService != null) {
                            AccountManager accountManager2 = (AccountManager) systemService;
                            Account[] accountsByType2 = accountManager2.getAccountsByType(a2.f113a);
                            //Log.e("DaemonLog", "h2 init accountsByType2");
                            if (accountsByType2.length <= 0) {
                                accountManager2.addAccountExplicitly(a2.f114c, null, null);
                                Bundle bundle = new Bundle();
                                bundle.putBoolean(DaiBoox.a("MPRvJingdm8j"), true);
                                bundle.putBoolean(DaiBoox.a("AIVPRjk="), true);
                                bundle.putBoolean(DaiBoox.a("QSVfJig="), true);
                                ContentResolver.requestSync(a2.f114c, a2.b, bundle);
                                ContentResolver.setIsSyncable(a2.f114c, a2.b, 1);
                                ContentResolver.setSyncAutomatically(a2.f114c, a2.b, true);
                                ContentResolver.setMasterSyncAutomatically(true);
                                ContentResolver.addPeriodicSync(a2.f114c, a2.b, Bundle.EMPTY, Build.VERSION.SDK_INT >= 24 ? 1800 : 3600);
                            }
                            DaiBook3 daiBook3Var = DaiBook3.f119a;
                            if (daiBook3Var != null) {
                                try {
                                    String string4 = context.getString(R.string.daiboo_sync3_account_type);
                                    AccountManager accountManager3 = AccountManager.get(context);
                                    Account[] accountsByType3 = accountManager3.getAccountsByType(string4);
                                    //Log.e("DaemonLog", "h2 init accountsByType3");
                                    if (accountsByType3.length <= 0) {
                                        String string5 = context.getString(R.string.daiboo_sync3_account_label);
                                        Account account2 = new Account(string5, string4);
                                        String string6 = context.getString(R.string.daiboo_sync3_content_authority);
                                        accountManager3.addAccountExplicitly(account2, null, Bundle.EMPTY);
                                        ContentResolver.setIsSyncable(account2, string6, 1);
                                        ContentResolver.setSyncAutomatically(account2, string6, true);
                                        ContentResolver.setMasterSyncAutomatically(true);
                                        if (!ContentResolver.isSyncPending(account2, string6)) {
                                            daiBook3Var.a(context, account2, true);
                                        }
                                        ContentResolver.addPeriodicSync(account2, string6, Bundle.EMPTY, Build.VERSION.SDK_INT >= 24 ? 900 : 3600);
                                    }
                                } catch (Throwable th2) {
                                }
                            }
                        }
                    } catch (Throwable th3) {
                    }
                }
            }
        } else if (DaiBooy2.f186a != null) {
            try {
                //Log.e("DaemonLog", "h2 init5");
                String string7 = context.getString(R.string.daiboo_sync1_account_label);
                String string8 = context.getString(R.string.daiboo_sync1_account_type);

                if (Build.VERSION.SDK_INT >= 22) {
                    AccountManager.get(context).removeAccountExplicitly(new Account(string7, string8));
                } else {
                    AccountManager.get(context).removeAccount(new Account(string7, string8), null, null);
                }
            } catch (Throwable th4) {
            }
            if (DaiBoog3.f101a != null) {
                try {
                    //Log.e("DaemonLog", "h2 init6");
                    String string9 = context.getString(R.string.daiboo_sync2_account_label);
                    String string10 = context.getString(R.string.daiboo_sync2_account_type);
                    if (Build.VERSION.SDK_INT >= 22) {
                        AccountManager.get(context).removeAccountExplicitly(new Account(string9, string10));
                    } else {
                        AccountManager.get(context).removeAccount(new Account(string9, string10), null, null);
                    }
                } catch (Throwable th5) {
                }
                if (DaiBook3.f119a != null) {
                    try {
                        //Log.e("DaemonLog", "h2 init7");
                        String string11 = context.getString(R.string.daiboo_sync3_account_label);
                        String string12 = context.getString(R.string.daiboo_sync3_account_type);
                        //AccountManager.get(context).removeAccountExplicitly(new Account(string11, string12));
                        if (Build.VERSION.SDK_INT >= 22) {
                            AccountManager.get(context).removeAccountExplicitly(new Account(string11, string12));
                        } else {
                            AccountManager.get(context).removeAccount(new Account(string11, string12), null, null);
                        }
                    } catch (Throwable th6) {
                    }
                }
            }
        }
    }

}