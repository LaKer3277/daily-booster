package com.alive.union.longer;

public final class DaiBoof implements Runnable {

    public final DaiBooi3 daiBooi3;

    public DaiBoof(DaiBooi3 daiBooi3Var) {
        this.daiBooi3 = daiBooi3Var;
    }
    public final void run() {
        DaiBooi3.a(this.daiBooi3);
    }
}