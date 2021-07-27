package dev.slohth.oitc.user.manager;

import dev.slohth.oitc.user.User;

import javax.annotation.Nonnull;
import java.util.*;

public class UserManager {

    private final Map<UUID, User> users;

    public UserManager() {
        this.users = new HashMap<>();
    }

    @Nonnull
    public Optional<User> findById(@Nonnull UUID uuid) {
        return Optional.ofNullable(users.get(uuid));
    }

    public boolean register(@Nonnull UUID uuid) {
        if (this.findById(uuid).isPresent()) return false;
        this.users.put(uuid, new User(uuid));
        return true;
    }

    public void unregister(@Nonnull UUID uuid) {
        this.users.remove(uuid);
    }

}
