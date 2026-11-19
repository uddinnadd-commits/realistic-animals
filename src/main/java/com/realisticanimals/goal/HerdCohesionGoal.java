package com.realisticanimals.goal;

import com.realisticanimals.config.RAConfig;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/** An animal that has strayed from its herd walks back towards the group. */
public class HerdCohesionGoal extends Goal {
	private static final int MAX_TICKS = 300;
	private static final int MAX_CONSIDERED = 6;

	private final Animal animal;
	private Vec3 herdCenter;
	private int scanCooldown;
	private int ticks;

	public HerdCohesionGoal(Animal animal) {
		this.animal = animal;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		RAConfig cfg = RAConfig.get();
		if (!cfg.herdCohesion || animal.isBaby() || animal.isLeashed() || animal.isVehicle() || animal.isInLove() || --scanCooldown > 0) {
			return false;
		}
		scanCooldown = 40 + animal.getRandom().nextInt(40);

		List<Animal> herd = animal.level().getEntitiesOfClass(Animal.class, animal.getBoundingBox().inflate(cfg.herdSearchRange, 8.0, cfg.herdSearchRange),
				a -> a != animal && a.isAlive() && !a.isBaby() && a.getType() == animal.getType());
		if (herd.isEmpty()) {
			return false;
		}
		double comfort = cfg.herdComfortDistance * cfg.herdComfortDistance;
		herd.sort((a, b) -> Double.compare(animal.distanceToSqr(a), animal.distanceToSqr(b)));
		if (animal.distanceToSqr(herd.getFirst()) <= comfort) {
			return false;
		}

		int count = Math.min(MAX_CONSIDERED, herd.size());
		double x = 0, y = 0, z = 0;
		for (int i = 0; i < count; i++) {
			Animal member = herd.get(i);
			x += member.getX();
			y += member.getY();
			z += member.getZ();
		}
		herdCenter = new Vec3(x / count, y / count, z / count);
		return true;
	}

	@Override
	public void start() {
		ticks = 0;
		animal.getNavigation().moveTo(herdCenter.x, herdCenter.y, herdCenter.z, 1.0);
	}

	@Override
	public boolean canContinueToUse() {
		double comfort = RAConfig.get().herdComfortDistance * 0.5;
		return ++ticks < MAX_TICKS && !animal.getNavigation().isDone() && animal.position().distanceToSqr(herdCenter) > comfort * comfort;
	}

	@Override
	public void stop() {
		herdCenter = null;
		animal.getNavigation().stop();
	}
}
