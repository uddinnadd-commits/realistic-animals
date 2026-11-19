package com.realisticanimals.goal;

import com.realisticanimals.config.RAConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** During thunderstorms animals look for cover (trees, overhangs, barns) and wait it out there. */
public class StormShelterGoal extends Goal {
	private static final int SHELTER_WAIT_TICKS = 200;

	private final Animal animal;
	private Vec3 shelter;
	private int waitTicks;

	public StormShelterGoal(Animal animal) {
		this.animal = animal;
		this.setFlags(EnumSet.of(Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		if (!RAConfig.get().stormShelter || !animal.level().isThundering() || animal.isVehicle() || animal.isLeashed()
				|| animal.getRandom().nextInt(40) != 0) {
			return false;
		}
		if (!animal.level().canSeeSky(animal.blockPosition())) {
			shelter = null;
			return true;
		}
		Vec3 candidate = LandRandomPos.getPos(animal, 12, 4, pos -> animal.level().canSeeSky(pos) ? -1.0 : 1.0);
		if (candidate == null || animal.level().canSeeSky(BlockPos.containing(candidate))) {
			return false;
		}
		shelter = candidate;
		return true;
	}

	@Override
	public void start() {
		waitTicks = adjustedTickDelay(SHELTER_WAIT_TICKS);
		if (shelter != null) {
			animal.getNavigation().moveTo(shelter.x, shelter.y, shelter.z, 1.1);
		} else {
			animal.getNavigation().stop();
		}
	}

	@Override
	public boolean canContinueToUse() {
		if (!animal.level().isThundering() || animal.isVehicle()) {
			return false;
		}
		return shelter != null && !animal.getNavigation().isDone() || waitTicks-- > 0;
	}

	@Override
	public void stop() {
		shelter = null;
	}
}
