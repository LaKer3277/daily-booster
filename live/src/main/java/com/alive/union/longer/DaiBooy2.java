package com.alive.union.longer;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import com.alive.union.R;


public final class DaiBooy2 {

    /* renamed from: a  reason: collision with root package name */
    public static final DaiBooy2 f186a = new DaiBooy2();

    public final void a(Context context, Account account) {

        try {
            String string = context.getString(R.string.daiboo_sync1_content_authority);
            Bundle bundle = new Bundle();
            bundle.putBoolean(DaiBoox.a("AIVPRjk="), true);
            bundle.putBoolean(DaiBoox.a("QSV/J/lRZ8xT8y4VTqTeBg=="), true);
            if (account == null) {
                String string2 = context.getString(R.string.daiboo_sync1_account_label);
                String string3 = context.getString(R.string.daiboo_sync1_account_type);
                account = new Account(string2, string3);
            }
            ContentResolver.requestSync(account, string, bundle);
        } catch (Throwable th) {
        }
    }
}