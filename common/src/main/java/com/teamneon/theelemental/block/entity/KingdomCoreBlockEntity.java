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
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBeamOwner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.CommandStorage;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueOutput.ValueOutputList;
import net.minecraft.world.level.storage.ValueInput.ValueInputList;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class KingdomCoreBlockEntity extends BlockEntity implements BeaconBeamOwner {


    private final List<Section> beamSections = new ArrayList<>();
    private final Set<UUID> playersInKingdom = new HashSet<>();


    private UUID owner;
    private final Set<UUID> members = new HashSet<>();
    private int element;
    private int tickCounter = 0;
    private float radius = 32.0f;

    public KingdomCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.KINGDOM_CORE_REG.asSupplier().get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, KingdomCoreBlockEntity be) {
        if (level.isClientSide()) return;

        be.serverTick();
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 6);
        }

        sync();
    }

    private void serverTick() {
        if (level == null || level.isClientSide()) return;

        if (++tickCounter % 20 != 0) return; // every second, adjust as needed

        int range = (int) radius;
        AABB box = new AABB(worldPosition).inflate(range);

        Set<UUID> playersCurrentlyInRadius = new HashSet<>();

        for (Player player : level.getEntitiesOfClass(Player.class, box)) {
            if (player.isSpectator() || !player.isAlive()) continue;

            // ✅ Check distance from core to make circular radius
            double dx = player.getX() - (worldPosition.getX() + 0.5);
            double dz = player.getZ() - (worldPosition.getZ() + 0.5);
            double distanceSq = dx * dx + dz * dz;
            if (distanceSq > radius * radius) continue; // skip players outside circle

            playersCurrentlyInRadius.add(player.getUUID());

            // If player is entering the kingdom
            if (!playersInKingdom.contains(player.getUUID())) {
                sendEnteringTitle(player);
                playersInKingdom.add(player.getUUID());
            }

            // Apply effect only if element matches
            int playerElement = ElementalDataHandler.get(player).getElement();
            if (playerElement == this.element) {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, 25, 0, true, true));
            }
        }

        // Remove players who have left the kingdom
        playersInKingdom.removeIf(uuid -> !playersCurrentlyInRadius.contains(uuid));
    }


    private void sendEnteringTitle(Player player) {
        if (level instanceof ServerLevel) {
            String kingdomName = ElementRegistry.getName(element);
            int color = ElementRegistry.getColor(element);

            // Create styled components
            Component prefix = Component.literal("Now entering the ").withStyle(style -> style.withColor(0xFFFFFF)); // white
            Component kingdom = Component.literal(kingdomName + " Kingdom").withStyle(style -> style.withColor(color));

            // Combine them
            Component message = Component.empty().append(prefix).append(kingdom);

            // Send to player as action bar
            player.displayClientMessage(message, true);
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

        // Clear and add a new permanent section based on the element color
        this.beamSections.clear();
        // Get the color from your registry or a helper
        int color = ElementRegistry.getColor(element);
        this.beamSections.add(new BeaconBeamOwner.Section(color));

        setChanged();
        sync();
    }

    // 2. Implement the required interface method
    @Override
    public List<BeaconBeamOwner.Section> getBeamSections() {
        return this.beamSections;
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
        output.putFloat("Radius", radius);

        ValueOutputList list = output.childrenList("Members");
        for (UUID id : members) {
            list.addChild().putString("UUID", id.toString());
        }
    }

    public void loadAdditional(ValueInput input) {
        input.getString("Owner").ifPresent(s -> owner = UUID.fromString(s));
        element = input.getIntOr("Element", 0);
        radius = input.getFloatOr("Radius", 32.0f);

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
