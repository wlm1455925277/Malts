package dev.jsinco.malts.obj;

import dev.jsinco.malts.storage.DataSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface CachedObject {

    @NotNull
    UUID getUuid();

    @Nullable
    Long getExpire();

    void setExpire(@Nullable Long expire);

    @NotNull
    CompletableFuture<Void> save(DataSource dataSource);

    default boolean isExpired() {
        Long expiration = this.getExpire();
        return expiration != null && expiration < System.currentTimeMillis();
    }

}
