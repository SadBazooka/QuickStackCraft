package net.zeronexus.quickstackcraft.neoforge.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.zeronexus.quickstackcraft.logic.RuntimeTargets;

import java.util.Set;

/**
 * {@code /quickstackcraft whitelist|blacklist add|remove|list|clear [block]}.
 * With no block argument, add/remove act on the block the player is looking at.
 */
public final class QscCommands {

    private QscCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("quickstackcraft")
                .requires(s -> s.hasPermission(2))
                .then(listSub("whitelist", true))
                .then(listSub("blacklist", false)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> listSub(String name, boolean whitelist) {
        return Commands.literal(name)
                .then(Commands.literal("add")
                        .executes(ctx -> editLookedAt(ctx.getSource(), whitelist, true))
                        .then(Commands.argument("block", ResourceLocationArgument.id())
                                .executes(ctx -> editId(ctx.getSource(), ResourceLocationArgument.getId(ctx, "block"), whitelist, true))))
                .then(Commands.literal("remove")
                        .executes(ctx -> editLookedAt(ctx.getSource(), whitelist, false))
                        .then(Commands.argument("block", ResourceLocationArgument.id())
                                .executes(ctx -> editId(ctx.getSource(), ResourceLocationArgument.getId(ctx, "block"), whitelist, false))))
                .then(Commands.literal("list").executes(ctx -> list(ctx.getSource(), whitelist)))
                .then(Commands.literal("clear").executes(ctx -> clear(ctx.getSource(), whitelist)));
    }

    private static int editLookedAt(CommandSourceStack source, boolean whitelist, boolean add) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Run this as a player, or pass a block id."));
            return 0;
        }
        ResourceLocation id = lookedAtBlockId(player);
        if (id == null) {
            source.sendFailure(Component.literal("You're not looking at a block."));
            return 0;
        }
        return editId(source, id, whitelist, add);
    }

    private static int editId(CommandSourceStack source, ResourceLocation id, boolean whitelist, boolean add) {
        boolean changed;
        if (whitelist) changed = add ? RuntimeTargets.addWhitelist(id) : RuntimeTargets.removeWhitelist(id);
        else changed = add ? RuntimeTargets.addBlacklist(id) : RuntimeTargets.removeBlacklist(id);

        String listName = whitelist ? "whitelist" : "blacklist";
        if (changed) {
            source.sendSuccess(() -> Component.literal((add ? "Added " : "Removed ") + id + (add ? " to the " : " from the ") + listName), true);
        } else {
            source.sendFailure(Component.literal(id + (add ? " is already in the " : " was not in the ") + listName));
        }
        return changed ? 1 : 0;
    }

    private static int list(CommandSourceStack source, boolean whitelist) {
        Set<ResourceLocation> ids = whitelist ? RuntimeTargets.whitelistView() : RuntimeTargets.blacklistView();
        String listName = whitelist ? "whitelist" : "blacklist";
        if (ids.isEmpty()) {
            source.sendSuccess(() -> Component.literal("In-game " + listName + " is empty."), false);
            return 0;
        }
        String joined = String.join(", ", ids.stream().map(ResourceLocation::toString).toList());
        source.sendSuccess(() -> Component.literal("In-game " + listName + " (" + ids.size() + "): " + joined), false);
        return ids.size();
    }

    private static int clear(CommandSourceStack source, boolean whitelist) {
        if (whitelist) RuntimeTargets.clearWhitelist();
        else RuntimeTargets.clearBlacklist();
        source.sendSuccess(() -> Component.literal("Cleared in-game " + (whitelist ? "whitelist" : "blacklist") + "."), true);
        return 1;
    }

    private static ResourceLocation lookedAtBlockId(ServerPlayer player) {
        Vec3 eye = player.getEyePosition(1.0f);
        Vec3 end = eye.add(player.getViewVector(1.0f).scale(6.0));
        BlockHitResult hit = player.level().clip(new ClipContext(eye, end,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK) return null;
        return BuiltInRegistries.BLOCK.getKey(player.level().getBlockState(hit.getBlockPos()).getBlock());
    }
}
