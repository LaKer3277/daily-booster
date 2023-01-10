package com.alive.union.longer;



import android.util.Base64;


import java.io.UnsupportedEncodingException;

public final class DaiBoox {

    public static byte[] a(boolean z, byte[] bArr, String str) {
        int length = bArr.length;
        int length2 = str.length();
        int i = 0;
        int i2 = 0;
        while (i < length) {
            if (i2 >= length2) {
                i2 = 0;
            }
            if (z) {
                bArr[i] = (byte) (((bArr[i] << 4) & 240) | ((bArr[i] >> 4) & 15));
            }
            bArr[i] = (byte) (bArr[i] ^ str.charAt(i2));
            if (!z) {
                bArr[i] = (byte) (((bArr[i] << 4) & 240) | ((bArr[i] >> 4) & 15));
            }
            i++;
            i2++;
        }
        return bArr;
    }

    public static String a(String str) {
        try {
            byte[] Y = Base64.decode(str, 2);
            a(false, Y, "fshpov19eu82828pknh1655sfcq4xfe9");
            return new String(Y, "UTF-8");
        } catch (UnsupportedEncodingException unused) {
            byte[] Y2 = Base64.decode(str, 2);
            a(false, Y2, "fshpov19eu82828pknh1655sfcq4xfe9");
            return new String(Y2);
        }
    }

}