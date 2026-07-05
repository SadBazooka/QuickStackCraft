package net.zeronexus.quickstackcraft.client.compat.itemlocks;

import java.lang.reflect.Method;

/**
 * Soft compatibility with the <b>ItemLocks</b> mod. When a player locks an inventory slot via
 * ItemLocks, Quick Stack / Dump should leave that item alone.
 *
 * <p>ItemLocks is queried purely by reflection (mirroring how ClientSort integrates with it), so
 * there's no compile-time or hard runtime dependency: if ItemLocks isn't installed, the first
 * reflective lookup fails, compat quietly disables itself, and no slots are reported as locked.
 *
 * <p>ItemLocks is the de-facto shared "lock layer" for inventory tools (ClientSort respects it too),
 * so honoring it here means locking an item once protects it across all of them.
 */
public final class ItemLocksCompat {

    private static final String COMPONENTS_CLASS = "com.kirdow.itemlocks.proxy.Components";
    private static final String LOCK_MANAGER_CLASS = "com.kirdow.itemlocks.client.LockManager";
    private static final String IS_LOCKED_SLOT_RAW = "isLockedSlotRaw";

    private static boolean hasFailed = false;
    private static Object lockManagerInstance = null;
    private static Method isLockedSlotRawMethod = null;

    private ItemLocksCompat() {}

    /**
     * @return a bitmask of the player-inventory slots (indices 0-35) that ItemLocks has locked;
     *         0 if ItemLocks isn't present or the lookup failed.
     */
    public static long lockedMask() {
        if (hasFailed) return 0L;
        long mask = 0L;
        for (int slot = 0; slot < 36; slot++) {
            if (isLocked(slot)) mask |= (1L << slot);
            if (hasFailed) return 0L; // lookup failed mid-loop (e.g. ItemLocks absent) - report nothing
        }
        return mask;
    }

    /**
     * @param invSlot vanilla player {@link net.minecraft.world.entity.player.Inventory} index
     *                (0-8 hotbar, 9-35 main)
     * @return whether ItemLocks has that slot locked
     */
    public static boolean isLocked(int invSlot) {
        if (hasFailed) return false;
        try {
            if (lockManagerInstance == null || isLockedSlotRawMethod == null) {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                Class<?> componentsClass = Class.forName(COMPONENTS_CLASS, false, cl);
                Method getComponent = componentsClass.getMethod("getComponent", Class.class);
                Class<?> lockManagerClass = Class.forName(LOCK_MANAGER_CLASS, false, cl);
                lockManagerInstance = getComponent.invoke(null, lockManagerClass);
                if (lockManagerInstance == null) throw new IllegalStateException("null LockManager component");
                isLockedSlotRawMethod = lockManagerClass.getMethod(IS_LOCKED_SLOT_RAW, int.class);
            }
            Object result = isLockedSlotRawMethod.invoke(lockManagerInstance, adjustForInventory(invSlot));
            return result instanceof Boolean b && b;
        } catch (Throwable t) {
            hasFailed = true; // ItemLocks not present, or its API changed - disable compat
            return false;
        }
    }

    /**
     * Convert a vanilla Inventory index to ItemLocks' "raw" slot index:
     * main inventory (9-35) maps to 0-26, hotbar (0-8) maps to 27-35.
     */
    private static int adjustForInventory(int slot) {
        if (slot >= 0 && slot <= 8) return slot + 27;
        if (slot >= 9 && slot <= 35) return slot - 9;
        return slot;
    }
}
