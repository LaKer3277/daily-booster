package com.alive.union.longer;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;


/* renamed from: com.e.j1 */
/* loaded from: classes.dex */
public final class DaiBoow1 extends ContentProvider {


    @Override // android.content.ContentProvider
    public Bundle call(String str, String str2, Bundle bundle) {

        return super.call(str, str2, bundle);
    }

    @Override // android.content.ContentProvider
    public int delete(Uri uri, String str, String[] strArr) {

        return 0;
    }

    @Override // android.content.ContentProvider
    public String getType(Uri uri) {

        return "";
    }

    @Override // android.content.ContentProvider
    public Uri insert(Uri uri, ContentValues contentValues) {

        return null;
    }

    @Override // android.content.ContentProvider
    public boolean onCreate() {

        return false;
    }

    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {

        return null;
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {

        return 0;
    }
}