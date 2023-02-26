package dev.compactmods.machines.tunnel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.location.DimensionalPosCompat;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceLocation;

public record BaseTunnelWallData(GlobalPos connection, ResourceLocation tunnelType) {
    public static final String KEY_CONNECTION = "connection";
    public static final String KEY_TUNNEL_TYPE = "tunnel_type";

    public static final Codec<BaseTunnelWallData> CODEC = RecordCodecBuilder.create(i -> i.group(
            DimensionalPosCompat.CODEC.fieldOf(KEY_CONNECTION).forGetter(BaseTunnelWallData::connection),
            ResourceLocation.CODEC.fieldOf(KEY_TUNNEL_TYPE).forGetter(BaseTunnelWallData::tunnelType)
    ).apply(i, BaseTunnelWallData::new));
}
