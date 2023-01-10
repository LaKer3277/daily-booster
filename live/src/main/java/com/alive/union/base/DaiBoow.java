package com.alive.union.base;



import android.os.Binder;

import android.os.IBinder;

import android.os.IInterface;

import android.os.Parcel;

import android.os.RemoteException;



/* loaded from: classes.dex */

public interface DaiBoow extends IInterface {



    /* loaded from: classes.dex */

    public static abstract class a extends Binder implements DaiBoow {



        /* renamed from: c.o.a.e.w$a$a  reason: collision with other inner class name */

        /* loaded from: classes.dex */

        public static class C0007a implements DaiBoow {



            /* renamed from: a  reason: collision with root package name */

            public IBinder f173a;


            public C0007a(IBinder iBinder) {

                this.f173a = iBinder;

            }


            @Override // android.os.IInterface

            public IBinder asBinder() {

                return this.f173a;

            }


            @Override // c.o.a.e.w

            public void e() throws RemoteException {

                Parcel obtain = Parcel.obtain();

                Parcel obtain2 = Parcel.obtain();

                try {

                    obtain.writeInterfaceToken("com.oh.master.IDaemonService");

                    this.f173a.transact(1, obtain, obtain2, 0);

                    obtain2.readException();

                } finally {

                    obtain2.recycle();

                    obtain.recycle();

                }

            }

        }


        public a() {

            attachInterface(this, "com.oh.master.IDaemonService");

        }
        public static DaiBoow C(IBinder iBinder) {

            if (iBinder == null) {

                return null;

            }

            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.oh.master.IDaemonService");

            return (queryLocalInterface == null || !(queryLocalInterface instanceof DaiBoow)) ? new C0007a(iBinder) : (DaiBoow) queryLocalInterface;

        }



        @Override // android.os.IInterface

        public IBinder asBinder() {

            return this;

        }



        @Override // android.os.Binder

        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {

            String a2 = "com.oh.master.IDaemonService";

            if (i == 1598968902) {

                parcel2.writeString(a2);

                return true;

            } else if (i != 1) {

                return super.onTransact(i, parcel, parcel2, i2);

            } else {

                parcel.enforceInterface(a2);

                e();

                parcel2.writeNoException();

                return true;

            }

        }

    }



//    static {
//
//        x.a("UIW+kpnw0+9zQn9kH9CsNH04vsfQAGNUAfVHYg==");
//
//    }



    void e() throws RemoteException;

}


