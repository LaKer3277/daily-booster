package com.alive.union.longer;

import android.accounts.Account;
import android.content.ContentResolver;
import android.os.Bundle;

public final class DaiBooj3 {
    public static final String d = DaiBoox.a("kvedRfqSBRqQQK3WDMcNRO86LGTyYRA=");
    public final String f113a;
    public final String b;
    public final Account f114c;
    public DaiBooj3(String str, String str2, String str3) {
        this.f113a = str2;
        this.b = str3;
        this.f114c = new Account(str, this.f113a);
    }

    public final void a(boolean z) {
        try {
            Bundle bundle = new Bundle();
            bundle.putBoolean(DaiBoox.a("MPRvJingdm8j"), true);
            bundle.putBoolean(DaiBoox.a("AIVPRjk="), true);
            bundle.putBoolean(DaiBoox.a("QSVfJig="), z);
            ContentResolver.requestSync(this.f114c, this.b, bundle);
        } catch (Throwable th) {
        }
    }
}