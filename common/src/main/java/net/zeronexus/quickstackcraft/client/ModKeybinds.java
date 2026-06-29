package net.zeronexus.quickstackcraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

/**
 * Keybind definitions shared across platforms.
 * Registration is platform-specific (Fabric: KeyBindingHelper, NeoForge: RegisterKeyMappingsEvent).
 */
public final class ModKeybinds {

    public static final String CATEGORY = "key.categories.quickstackcraft";

    public static final KeyMapping QUICK_STACK = new KeyMapping(
            "key.quickstackcraft.quick_stack",
            InputConstants.UNKNOWN.getValue(), // unbound by default
            CATEGORY
    );

    public static final KeyMapping DUMP_ALL = new KeyMapping(
            "key.quickstackcraft.dump_all",
            InputConstants.UNKNOWN.getValue(), // unbound by default
            CATEGORY
    );

    /** Highlight every eligible storage container in range (no transfer). */
    public static final KeyMapping PREVIEW_TARGETS = new KeyMapping(
            "key.quickstackcraft.preview_targets",
            InputConstants.UNKNOWN.getValue(), // unbound by default
            CATEGORY
    );

    /** Toggle the block you're looking at in the storage whitelist. */
    public static final KeyMapping TOGGLE_TARGET = new KeyMapping(
            "key.quickstackcraft.toggle_target",
            InputConstants.UNKNOWN.getValue(), // unbound by default
            CATEGORY
    );

    private ModKeybinds() {}
}
