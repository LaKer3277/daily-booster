package com.alive.union.longer;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;

import com.alive.union.R;

public final class DaiBook3 {

    /* renamed from: a  reason: collision with root package name */
    public static final DaiBook3 f119a = new DaiBook3();
    public final void a(Context context, Account account, boolean z) {
        try {
            String string = context.getString(R.string.daiboo_sync3_content_authority);
            Bundle bundle = new Bundle();
            bundle.putBoolean(DaiBoox.a("AIVPRjk="), true);
            if (z) {
                bundle.putBoolean(DaiBoox.a("QSV/J/lRZ8xT8y4VTqTeBg=="), true);
            }
            if (account == null) {
                String string2 = context.getString(R.string.daiboo_sync3_account_label);
                String string3 = context.getString(R.string.daiboo_sync3_account_type);
                account = new Account(string2, string3);
            }
            ContentResolver.requestSync(account, string, bundle);
        } catch (Throwable th) {
        }
    }
}