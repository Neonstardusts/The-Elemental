package com.teamneon.theelemental.block.entity;

import com.teamneon.theelemental.block.ModBlocks;
import com.teamneon.theelemental.data.ElementalDataHandler;
import com.teamneon.theelemental.helpers.ElementRegistry;
import com.teamneon.theelemental.helpers.KingdomAnchorHelper;
import com.teamneon.theelemental.kingdoms.KingdomSavedData;
import net.blay09.mods.balm.world.level.block.entity.BalmBlockEntityUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.ValueOutputList;
import net.minecraft.world.level.storage.ValueInput.ValueInputList;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class KingdomCoreBlockEntity extends BlockEntity {

    private UUID owner;
    private final Set<UUID> members = new HashSet<>();
    private int element;
    private int tickCounter = 0;

    public KingdomCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.KINGDOM_CORE_REG.asSupplier().get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, KingdomCoreBlockEntity be) {
        if (level.isClientSide()) return;

        be.serverTick();
    }

    private void serverTick() {
        if (level == null || level.isClientSide()) return;

        if (++tickCounter % 80 != 0) return; // every 4 seconds

        int range = 40; // beacon radius
        AABB box = new AABB(worldPosition).inflate(range);

        for (Player player : level.getEntitiesOfClass(Player.class, box)) {

            if (player.isSpectator() || !player.isAlive()) continue;

            int playerElement = ElementalDataHandler.get(player).getElement(); // <- your element check

            if (playerElement != this.element) continue; // only match element

            // Apply effect
            player.addEffect(new MobEffectInstance(
                    MobEffects.SPEED, // example effect
                    100,                        // duration (longer than tick interval)
                    0,                          // amplifier
                    true,                       // ambient
                    true                        // show particles
            ));
        }
    }





    /* ---------------- API ---------------- */
    public void delete() {
        if (level == null || level.isClientSide()) return;
        if (!(level instanceof ServerLevel serverLevel)) return;

        // 1️⃣ Remove from saved data
        KingdomSavedData data = KingdomSavedData.get(serverLevel);
        data.removeCore(element);

        // 2️⃣ Send message to players
        serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal("The " + ElementRegistry.getName(element) + " Kingdom core has been destroyed!"),
                false
        );

        // 3️⃣ Remove the block in the world safely
        level.removeBlock(worldPosition, false);

        BlockPos anchorPos = KingdomAnchorHelper.getAnchorPos(serverLevel, element);
        if (anchorPos != null) {
            serverLevel.setBlock(anchorPos, Blocks.BEDROCK.defaultBlockState(), 3);
        }
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
        sync();
    }

    public UUID getOwner() {
        return owner;
    }

    public void addMember(UUID uuid) {
        members.add(uuid);
        setChanged();
        sync();
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public void setElement(int element) {
        this.element = element;
        setChanged();
        sync();
    }

    public int getElement() {
        return element;
    }

    /* ---------------- NBT / Balm storage ---------------- */

    public void saveAdditional(ValueOutput output) {
        if (owner != null) {
            output.putString("Owner", owner.toString());
        }
        output.putInt("Element", element);

        ValueOutputList list = output.childrenList("Members");
        for (UUID id : members) {
            list.addChild().putString("UUID", id.toString());
        }
    }

    public void loadAdditional(ValueInput input) {
        input.getString("Owner").ifPresent(s -> owner = UUID.fromString(s));
        element = input.getIntOr("Element", 0);

        input.childrenList("Members").ifPresent(list -> {
            members.clear();
            for (ValueInput child : list) {
                child.getString("UUID").ifPresent(s -> members.add(UUID.fromString(s)));
            }
        });
    }

    /* ---------------- Sync ---------------- */

    @Nullable
    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return BalmBlockEntityUtils.createUpdateTag(registries, this::saveAdditional);
    }

    public void sync() {
        BalmBlockEntityUtils.sync(this);
    }
}
