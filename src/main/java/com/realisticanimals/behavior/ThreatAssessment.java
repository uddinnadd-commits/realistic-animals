package com.realisticanimals.behavior;

import com.realisticanimals.RATags;
import com.realisticanimals.config.RAConfig;
import com.realisticanimals.state.AnimalState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Decides what an animal considers dangerous right now. */
public final class ThreatAssessment {
	private ThreatAssessment() {
	}

	/** Adult llamas and other defenders stand their ground instead of running. */
	public static boolean isDefender(Animal animal) {
		return RAConfig.get().llamasDefendHerd && !animal.isBaby() && RATags.is(animal, RATags.DEFENDS_HERD);
	}

	public static boolean isTamed(Animal animal) {
		return animal instanceof AbstractHorse horse && horse.isTamed()
				|| animal instanceof TamableAnimal tamable && tamable.isTame();
	}

	/** Finds the most pressing threat: a herd alarm first, then predators, then players. */
	public static LivingEntity findThreat(Animal animal) {
		RAConfig cfg = RAConfig.get();
		AnimalState state = AnimalState.of(animal);
		long now = animal.level().getGameTime();

		LivingEntity alarm = state.alarmThreat(now);
		if (alarm != null && !isDefender(animal) && animal.distanceToSqr(alarm) < cfg.safeDistance * cfg.safeDistance) {
			return alarm;
		}

		double range = Math.max(cfg.predatorDetectionRange, cfg.monsterDetectionRange);
		LivingEntity nearest = null;
		double nearestDist = Double.MAX_VALUE;
		for (LivingEntity other : animal.level().getEntitiesOfClass(LivingEntity.class, animal.getBoundingBox().inflate(range),
				e -> e != animal && e.isAlive() && !(e instanceof Player))) {
			double dist = animal.distanceToSqr(other);
			if (dist < nearestDist && isPredator(animal, other, dist) && animal.getSensing().hasLineOfSight(other)) {
				nearest = other;
				nearestDist = dist;
			}
		}
		if (nearest != null) {
			return nearest;
		}

		double playerRange = Math.max(cfg.attackerMemoryRange, cfg.sprintScareRange + cfg.babyExtraScareRange);
		for (Player player : animal.level().getEntitiesOfClass(Player.class, animal.getBoundingBox().inflate(playerRange), Player::isAlive)) {
			double dist = animal.distanceToSqr(player);
			if (dist < nearestDist && isScaryPlayer(animal, state, player, dist, now) && animal.getSensing().hasLineOfSight(player)) {
				nearest = player;
				nearestDist = dist;
			}
		}
		return nearest;
	}

	/** Whether the animal should keep running from a threat it is already fleeing. */
	public static boolean shouldKeepFleeing(Animal animal, LivingEntity threat) {
		if (!threat.isAlive() || threat instanceof Player player && player.isSpectator()) {
			return false;
		}
		long now = animal.level().getGameTime();
		if (AnimalState.of(animal).alarmThreat(now) == threat) {
			return true;
		}
		double safe = RAConfig.get().safeDistance;
		return animal.distanceToSqr(threat) < safe * safe;
	}

	static boolean isPredator(Animal animal, LivingEntity other, double distSqr) {
		RAConfig cfg = RAConfig.get();
		if (other.isInvisible()) {
			return false;
		}
		if (other instanceof Mob mob && mob.getTarget() == animal) {
			return true;
		}
		if (other.getType() == animal.getType() || other instanceof TamableAnimal tamable && tamable.isTame()) {
			return false;
		}
		if (cfg.predatorAvoidance && !isDefender(animal) && distSqr <= cfg.predatorDetectionRange * cfg.predatorDetectionRange) {
			if (RATags.is(animal, RATags.SMALL_PREY) && RATags.is(other, RATags.HUNTS_SMALL_PREY)) {
				return true;
			}
			if (RATags.is(animal, RATags.LIVESTOCK) && RATags.is(other, RATags.HUNTS_LIVESTOCK)) {
				return true;
			}
		}
		return cfg.fearMonsters && other instanceof Enemy && distSqr <= cfg.monsterDetectionRange * cfg.monsterDetectionRange;
	}

	static boolean isScaryPlayer(Animal animal, AnimalState state, Player player, double distSqr, long now) {
		RAConfig cfg = RAConfig.get();
		if (player.isSpectator() || player.isInvisible()) {
			return false;
		}
		if (state.fears(player.getUUID(), now)) {
			return distSqr <= cfg.attackerMemoryRange * cfg.attackerMemoryRange;
		}
		if (!cfg.waryOfPlayers || animal.isLeashed() || isTamed(animal) || state.trusts(player.getUUID())) {
			return false;
		}
		// Sneaking or offering food lets you approach calmly.
		if (player.isShiftKeyDown() || !player.isSprinting() || holdsFood(animal, player)) {
			return false;
		}
		double range = cfg.sprintScareRange + (animal.isBaby() ? cfg.babyExtraScareRange : 0.0);
		return distSqr <= range * range;
	}

	public static boolean holdsFood(Animal animal, Player player) {
		ItemStack main = player.getMainHandItem();
		ItemStack off = player.getOffhandItem();
		return !main.isEmpty() && animal.isFood(main) || !off.isEmpty() && animal.isFood(off);
	}
}
