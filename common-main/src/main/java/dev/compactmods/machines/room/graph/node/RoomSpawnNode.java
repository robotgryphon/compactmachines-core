package dev.compactmods.machines.room.graph.node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.codec.CodecExtensions;
import dev.compactmods.machines.graph.IGraphNode;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public record RoomSpawnNode(Vec3 position, Vec2 rotation) implements IGraphNode<RoomSpawnNode> {

    public static final Codec<RoomSpawnNode> CODEC = RecordCodecBuilder.create(i -> i.group(
            Vec3.CODEC.fieldOf("position").forGetter(RoomSpawnNode::position),
            CodecExtensions.VEC2.fieldOf("rotation").forGetter(RoomSpawnNode::rotation)
    ).apply(i, RoomSpawnNode::new));

    @Override
    public @NotNull Codec<RoomSpawnNode> codec() {
        return CODEC;
    }
}