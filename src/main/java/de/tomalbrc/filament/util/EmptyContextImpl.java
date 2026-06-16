package de.tomalbrc.filament.util;

import de.tomalbrc.filament.Filament;
import eu.pb4.polymer.common.impl.CommonImplPacketKeys;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.RegistryAccess;
import org.jspecify.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@SuppressWarnings("all")
public class EmptyContextImpl implements PacketContext {
	public static final ScopedValue<PacketContext> VALUE = ScopedValue.newInstance();

	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Map<PacketContext.Key<?>, Object> contextMap = new IdentityHashMap<>();

    public EmptyContextImpl() {
        contextMap.put(CommonImplPacketKeys.HOLDER_LOOKUP, Filament.SERVER.registryAccess());
	}

    public void setup(RegistryAccess registryAccess) {
        contextMap.put(CommonImplPacketKeys.HOLDER_LOOKUP, registryAccess);
    }

	@Override
	public @Nullable <T> T get(ReadKey<T> key) {
		this.lock.readLock().lock();

		try {
			return (T) this.contextMap.get(key);
		} finally {
			this.lock.readLock().unlock();
		}
	}

	@Override
	public <T> void set(PacketContext.Key<T> key, T value) {
		this.lock.writeLock().lock();

		if (value == null) {
			this.contextMap.remove(key);
		} else {
			this.contextMap.put(key, value);
		}

		this.lock.writeLock().unlock();
	}
}
