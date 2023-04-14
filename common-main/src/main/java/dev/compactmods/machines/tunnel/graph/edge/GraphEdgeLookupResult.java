package dev.compactmods.machines.tunnel.graph.edge;

import com.google.common.graph.EndpointPair;
import dev.compactmods.machines.graph.IGraphEdge;
import dev.compactmods.machines.graph.IGraphNode;

@SuppressWarnings("UnstableApiUsage")
public record GraphEdgeLookupResult<E extends IGraphEdge<E>, S extends IGraphNode<S>, T extends IGraphNode<T>>
        (EndpointPair<IGraphNode<?>> endpoints, E edgeValue) {

    @SuppressWarnings("unchecked")
    public S source() {
        return (S) endpoints.source();
    }

    @SuppressWarnings("unchecked")
    public T target() {
        return (T) endpoints.target();
    }
}
