package dev.compactmods.machines.room.spawn;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import dev.compactmods.machines.api.room.spatial.IRoomBoundaries;
import dev.compactmods.machines.api.room.spawn.IRoomSpawn;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManager;
import dev.compactmods.machines.api.room.spawn.IRoomSpawns;
import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.data.CodecBackedSavedData;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SpawnManager extends CodecBackedSavedData<SpawnManager> implements IRoomSpawnManager {

    private final Logger LOGS = LogManager.getLogger();

    private static final UnboundedMapCodec<UUID, RoomSpawn> PLAYER_SPAWNS_CODEC = Codec.unboundedMap(UUIDUtil.CODEC, RoomSpawn.CODEC);
    private static final Codec<SpawnManager> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("roomCode").forGetter(x -> x.roomCode),
            PLAYER_SPAWNS_CODEC.fieldOf("player_spawns").forGetter(x -> x.playerSpawns),
            RoomSpawn.CODEC.fieldOf("default_spawn").forGetter(x -> x.defaultSpawn)
    ).apply(inst, SpawnManager::new));

    private final String roomCode;
    private RoomSpawn defaultSpawn;
    private final Map<UUID, RoomSpawn> playerSpawns;
    private AABB roomBounds;

    public SpawnManager(String roomCode) {
        this(roomCode, Collections.emptyMap(), null);
        this.defaultSpawn = null;
    }

    public SpawnManager(String roomCode, Map<UUID, RoomSpawn> playerSpawns, RoomSpawn defaultSpawn) {
        super(CodecBackedSavedData.codecFactory(CODEC, () -> new SpawnManager(roomCode)));
        this.roomCode = roomCode;
        this.playerSpawns = new HashMap<>(playerSpawns);
        this.defaultSpawn = defaultSpawn;
    }

    public static SpawnManager forRoom(MinecraftServer server, String roomCode, IRoomBoundaries roomBounds) throws MissingDimensionException {
        String roomFilename = Constants.MOD_ID + "_room_" + roomCode;
        var manager = CompactDimension.forServer(server)
                .getDataStorage()
                .computeIfAbsent(CodecBackedSavedData.codecFactory(CODEC, () -> new SpawnManager(roomCode)).asSDFactory(), roomFilename);

        manager.setBoundaries(roomBounds.innerBounds());
        manager.setDefaultSpawn(roomBounds.defaultSpawn(), Vec2.ZERO);
        return manager;
    }

    private void setBoundaries(AABB roomBounds) {
        this.roomBounds = roomBounds;
    }

    @Override
    public void resetPlayerSpawn(UUID player) {
        playerSpawns.remove(player);
    }

    @Override
    public void setDefaultSpawn(Vec3 position, Vec2 rotation) {
        defaultSpawn = new RoomSpawn(position, rotation);
    }

    @Override
    public IRoomSpawns spawns() {
        final var ps = new HashMap<UUID, RoomSpawn>();
        playerSpawns.forEach(ps::putIfAbsent);
        return new RoomSpawns(defaultSpawn, ps);
    }

    @Override
    public void setPlayerSpawn(UUID player, Vec3 location, Vec2 rotation) {
        if(!roomBounds.contains(location))
            return;

        if (playerSpawns.containsKey(player))
            playerSpawns.replace(player, new RoomSpawn(location, rotation));
        else
            playerSpawns.put(player, new RoomSpawn(location, rotation));
    }

    private record RoomSpawns(RoomSpawn defaultSpawn, Map<UUID, RoomSpawn> playerSpawnsSnapshot) implements IRoomSpawns {

        @Override
        public Optional<IRoomSpawn> forPlayer(UUID player) {
            return Optional.ofNullable(playerSpawnsSnapshot.get(player));
        }
    }
}
