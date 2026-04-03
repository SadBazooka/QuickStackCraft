package net.zeronexus.quickstackcraft;

import net.zeronexus.quickstackcraft.network.ModNetworking;

public final class QuickStackCraft {
    public static final String MOD_ID = "quickstackcraft";

    public static void init() {
        ModNetworking.register();
    }
}
