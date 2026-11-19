package com.realisticanimals.goal;

import com.realisticanimals.config.RAConfig;
import com.realisticanimals.state.AnimalState;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;

import java.util.EnumSet;

/** After dark, animals settle down and stay put instead of wandering all night. */
public class NightRestGoal extends Goal {
	private static final int CALM_AFTER_DAMAGE_TICKS = 300;

	private final Animal animal;

	public NightRestGoal(Animal animal) {
		this.animal = animal;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		// The random roll staggers bedtimes so a herd does not freeze in unison.
		return isCalmNight() && animal.getRandom().nextInt(60) == 0;
	}

	@Override
	public boolean canContinueToUse() {
		return isCalmNight();
	}

	@Override
	public void start() {
		animal.getNavigation().stop();
	}

	@Override
	public void tick() {
		if (animal.getRandom().nextInt(160) == 0) {
			double angle = animal.getRandom().nextDouble() * Math.PI * 2.0;
			animal.getLookControl().setLookAt(animal.getX() + Math.cos(angle) * 4.0, animal.getEyeY() - 0.5, animal.getZ() + Math.sin(angle) * 4.0);
		}
	}

	private boolean isCalmNight() {
		if (!RAConfig.get().nightRest || !animal.level().isDarkOutside() || !animal.onGround() || animal.isInWater()
				|| animal.isVehicle() || animal.isInLove()) {
			return false;
		}
		AnimalState state = AnimalState.of(animal);
		long now = animal.level().getGameTime();
		return state.alarmThreat(now) == null && now - state.lastDamagedAt > CALM_AFTER_DAMAGE_TICKS;
	}
}
