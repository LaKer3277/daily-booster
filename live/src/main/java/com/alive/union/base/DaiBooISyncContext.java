package com.alive.union.base;

import android.content.SyncResult;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import com.alive.union.longer.DaiBoox;


public interface DaiBooISyncContext extends IInterface {

    public static class Default implements DaiBooISyncContext {
        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }

        @Override // android.content.ISyncContext
        public void onFinished(SyncResult syncResult) throws RemoteException {
        }

        @Override // android.content.ISyncContext
        public void sendHeartbeat() throws RemoteException {
        }
    }

    public static abstract class Stub extends Binder implements DaiBooISyncContext {
        public static final String DESCRIPTOR = DaiBoox.a("cJUuV5ngd9tTg951btR/kv9b/9cAAcOVITX2cw==");
        public static final int TRANSACTION_onFinished = 2;
        public static final int TRANSACTION_sendHeartbeat = 1;

        public static class Proxy implements DaiBooISyncContext {
            public static DaiBooISyncContext sDefaultImpl;
            public IBinder mRemote;

            public Proxy(IBinder iBinder) {
                this.mRemote = iBinder;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return DaiBoox.a("cJUuV5ngd9tTg951btR/kv9b/9cAAcOVITX2cw==");
            }

            @Override // android.content.ISyncContext
            public void onFinished(SyncResult syncResult) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(DaiBoox.a("cJUuV5ngd9tTg951btR/kv9b/9cAAcOVITX2cw=="));
                    if (syncResult != null) {
                        obtain.writeInt(1);
                        syncResult.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    if (this.mRemote.transact(2, obtain, obtain2, 0) || Stub.getDefaultImpl() == null) {
                        obtain2.readException();
                    } else {
                        Stub.getDefaultImpl().onFinished(syncResult);
                    }
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            @Override // android.content.ISyncContext
            public void sendHeartbeat() throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken(DaiBoox.a("cJUuV5ngd9tTg951btR/kv9b/9cAAcOVITX2cw=="));
                    if (this.mRemote.transact(1, obtain, obtain2, 0) || Stub.getDefaultImpl() == null) {
                        obtain2.readException();
                    } else {
                        Stub.getDefaultImpl().sendHeartbeat();
                    }
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DaiBoox.a("cJUuV5ngd9tTg951btR/kv9b/9cAAcOVITX2cw=="));
        }

        public static DaiBooISyncContext asInterface(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface(DESCRIPTOR);
            return (queryLocalInterface == null || !(queryLocalInterface instanceof DaiBooISyncContext)) ? new Proxy(iBinder) : (DaiBooISyncContext) queryLocalInterface;
        }

        public static DaiBooISyncContext getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }

        public static boolean setDefaultImpl(DaiBooISyncContext iSyncContext) {
            if (Proxy.sDefaultImpl != null) {
                throw new IllegalStateException(DaiBoox.a("USUvNDkQJ26jMqzkP/S64mlYfvfwY3NxIRTnAi4="));
            } else if (iSyncContext == null) {
                return false;
            } else {
                Proxy.sDefaultImpl = iSyncContext;
                return true;
            }
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
            String str = DESCRIPTOR;
            if (i != 1598968902) {
                if (i == 1) {
                    parcel.enforceInterface(str);
                    sendHeartbeat();
                } else if (i != 2) {
                    return super.onTransact(i, parcel, parcel2, i2);
                } else {
                    parcel.enforceInterface(str);
                    onFinished(parcel.readInt() != 0 ? (SyncResult) SyncResult.CREATOR.createFromParcel(parcel) : null);
                }
                parcel2.writeNoException();
                return true;
            }
            parcel2.writeString(str);
            return true;
        }
    }

    void onFinished(SyncResult syncResult) throws RemoteException;

    void sendHeartbeat() throws RemoteException;
}