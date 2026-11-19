package com.realisticanimals.state;

import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Per-animal memory. Trust and the milking timer are saved with the entity;
 * alarms and fear memories are short-lived and intentionally not persisted.
 */
public final class AnimalState {
	private static final int MAX_TRUSTED = 16;
	private static final String TRUSTED_KEY = "realisticanimals_trusted";
	private static final String MILK_KEY = "realisticanimals_milk_ready_at";

	private LivingEntity alarmThreat;
	private long alarmUntil;
	private final Map<UUID, Long> fearedUntil = new HashMap<>();
	private final Set<UUID> trusted = new LinkedHashSet<>();

	public long milkReadyAt;
	public long nextForageAt;
	public long nextRegenAt;
	public long lastDamagedAt = Long.MIN_VALUE / 2;

	public static AnimalState of(Animal animal) {
		return ((AnimalStateHolder) animal).realisticanimals$getState();
	}

	public void alarm(LivingEntity threat, long until) {
		if (until > alarmUntil || alarmThreat != threat) {
			alarmThreat = threat;
			alarmUntil = until;
		}
	}

	public LivingEntity alarmThreat(long now) {
		if (alarmThreat == null) {
			return null;
		}
		if (now > alarmUntil || !alarmThreat.isAlive()) {
			alarmThreat = null;
			return null;
		}
		return alarmThreat;
	}

	public void fear(UUID id, long until) {
		fearedUntil.merge(id, until, Math::max);
		trusted.remove(id);
	}

	public boolean fears(UUID id, long now) {
		Long until = fearedUntil.get(id);
		return until != null && until > now;
	}

	public void forgetExpiredFears(long now) {
		fearedUntil.values().removeIf(until -> until <= now);
	}

	/** @return true if this player was not trusted before */
	public boolean trust(UUID id) {
		fearedUntil.remove(id);
		if (trusted.contains(id)) {
			return false;
		}
		if (trusted.size() >= MAX_TRUSTED) {
			trusted.remove(trusted.iterator().next());
		}
		trusted.add(id);
		return true;
	}

	public boolean trusts(UUID id) {
		return trusted.contains(id);
	}

	public void save(ValueOutput output) {
		if (!trusted.isEmpty()) {
			output.store(TRUSTED_KEY, UUIDUtil.CODEC_LINKED_SET, trusted);
		}
		if (milkReadyAt != 0L) {
			output.putLong(MILK_KEY, milkReadyAt);
		}
	}

	public void load(ValueInput input) {
		trusted.clear();
		input.read(TRUSTED_KEY, UUIDUtil.CODEC_LINKED_SET).ifPresent(trusted::addAll);
		milkReadyAt = input.getLongOr(MILK_KEY, 0L);
	}
}
