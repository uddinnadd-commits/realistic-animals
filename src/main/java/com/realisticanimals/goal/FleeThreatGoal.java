package com.realisticanimals.goal;

import com.realisticanimals.behavior.ThreatAssessment;
import com.realisticanimals.config.RAConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** Runs from predators, monsters, startling players and whatever just attacked the herd. */
public class FleeThreatGoal extends Goal {
	private static final int MAX_FLEE_TICKS = 200;

	private final Animal animal;
	private LivingEntity threat;
	private int scanCooldown;
	private int repathCooldown;
	private int fleeTicks;

	public FleeThreatGoal(Animal animal) {
		this.animal = animal;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (animal.isVehicle() || animal.isPassenger() || --scanCooldown > 0) {
			return false;
		}
		scanCooldown = 3 + animal.getRandom().nextInt(3);
		threat = ThreatAssessment.findThreat(animal);
		return threat != null;
	}

	@Override
	public boolean canContinueToUse() {
		return threat != null && fleeTicks < MAX_FLEE_TICKS && !animal.isVehicle() && ThreatAssessment.shouldKeepFleeing(animal, threat);
	}

	@Override
	public void start() {
		fleeTicks = 0;
		repathCooldown = 0;
	}

	@Override
	public void stop() {
		threat = null;
		animal.getNavigation().stop();
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		fleeTicks++;
		if (--repathCooldown <= 0 || animal.getNavigation().isDone()) {
			repathCooldown = 20 + animal.getRandom().nextInt(10);
			Vec3 away = DefaultRandomPos.getPosAway(animal, 16, 7, threat.position());
			if (away != null) {
				animal.getNavigation().moveTo(away.x, away.y, away.z, RAConfig.get().fleeSpeed);
			}
		}
	}
}
