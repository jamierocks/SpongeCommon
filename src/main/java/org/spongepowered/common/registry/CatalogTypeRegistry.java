package org.spongepowered.common.registry;

import org.spongepowered.api.CatalogType;

import java.util.Optional;

public interface CatalogTypeRegistry<T extends CatalogType> {

    Optional<T> getFor(String id);

    void registerTypes();

}
