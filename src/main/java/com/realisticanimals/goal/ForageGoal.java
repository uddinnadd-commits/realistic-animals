package com.realisticanimals.goal;

import com.realisticanimals.config.RAConfig;
import com.realisticanimals.state.AnimalState;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.EnumSet;

/** Walks over to food lying on the ground (wheat, seeds, carrots...) and eats it. */
public class ForageGoal extends Goal {
	private static final int GIVE_UP_TICKS = 200;

	private final Animal animal;
	private ItemEntity target;
	private int scanCooldown;
	private int ticks;
	private boolean eaten;

	public ForageGoal(Animal animal) {
		this.animal = animal;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}

	@Override
	public boolean canUse() {
		RAConfig cfg = RAConfig.get();
		if (!cfg.forageDroppedFood || animal.isVehicle() || animal.isInLove() || --scanCooldown > 0) {
			return false;
		}
		scanCooldown = 10 + animal.getRandom().nextInt(10);
		if (animal.level().getGameTime() < AnimalState.of(animal).nextForageAt) {
			return false;
		}
		target = animal.level().getEntitiesOfClass(ItemEntity.class, animal.getBoundingBox().inflate(cfg.forageRange, 3.0, cfg.forageRange),
						item -> item.isAlive() && !item.getItem().isEmpty() && animal.isFood(item.getItem()))
				.stream()
				.min(Comparator.comparingDouble(animal::distanceToSqr))
				.orElse(null);
		return target != null;
	}

	@Override
	public boolean canContinueToUse() {
		return !eaten && ticks < GIVE_UP_TICKS && target != null && target.isAlive() && !target.getItem().isEmpty();
	}

	@Override
	public void start() {
		ticks = 0;
		eaten = false;
		animal.getNavigation().moveTo(target, 1.15);
	}

	@Override
	public void stop() {
		target = null;
		animal.getNavigation().stop();
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		// Running goals can tick once more before canContinueToUse is re-checked.
		if (eaten) {
			return;
		}
		ticks++;
		animal.getLookControl().setLookAt(target);
		double reach = animal.getBbWidth() + 1.0;
		if (animal.distanceToSqr(target) <= reach * reach) {
			eat();
		} else if (ticks % 10 == 0) {
			animal.getNavigation().moveTo(target, 1.15);
		}
	}

	private void eat() {
		RAConfig cfg = RAConfig.get();
		ItemStack remaining = target.getItem().copy();
		ItemStack eatenStack = remaining.split(1);
		if (remaining.isEmpty()) {
			target.discard();
		} else {
			target.setItem(remaining);
		}

		animal.playSound(SoundEvents.GENERIC_EAT.value(), 0.6F, 0.9F + animal.getRandom().nextFloat() * 0.2F);
		if (animal.level() instanceof ServerLevel level) {
			level.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, eatenStack.getItem()),
					animal.getX(), animal.getEyeY() - 0.2, animal.getZ(), 6, 0.2, 0.1, 0.2, 0.05);
		}
		animal.heal(cfg.forageHealAmount);
		if (animal.isBaby()) {
			animal.ageUp(AgeableMob.getSpeedUpSecondsWhenFeeding(-animal.getAge()), true);
		}

		AnimalState.of(animal).nextForageAt = animal.level().getGameTime()
				+ (animal.isBaby() ? cfg.forageCooldownTicks / 3 : cfg.forageCooldownTicks);
		eaten = true;
	}
}
