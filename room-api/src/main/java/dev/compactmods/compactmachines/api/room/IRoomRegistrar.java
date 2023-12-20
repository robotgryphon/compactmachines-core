package dev.compactmods.compactmachines.api.room;

import dev.compactmods.compactmachines.api.room.registration.IRoomBuilder;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public interface IRoomRegistrar {

    RoomInstance createNew(Function<IRoomBuilder, IRoomBuilder> build);

    boolean isRegistered(String room);

    Optional<RoomInstance> get(String room);

    long count();

    Stream<String> allRoomCodes();

    Stream<RoomInstance> allRooms();
}