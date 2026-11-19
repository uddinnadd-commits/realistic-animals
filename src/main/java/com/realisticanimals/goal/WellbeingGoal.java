package com.realisticanimals.goal;

import com.realisticanimals.RealisticAnimals;
import com.realisticanimals.config.RAConfig;
import com.realisticanimals.state.AnimalState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;

import java.util.EnumSet;

/**
 * Background upkeep with no movement flags, so it runs alongside every other goal:
 * slow healing once the animal has been left alone, and a limp while badly hurt.
 */
public class WellbeingGoal extends Goal {
	private static final Identifier LIMP_ID = Identifier.fromNamespaceAndPath(RealisticAnimals.MOD_ID, "injured_limp");
	private static final int CHECK_INTERVAL = 20;

	private final Animal animal;
	private int cooldown;

	public WellbeingGoal(Animal animal) {
		this.animal = animal;
		this.setFlags(EnumSet.noneOf(Flag.class));
	}

	@Override
	public boolean canUse() {
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		return true;
	}

	@Override
	public boolean requiresUpdateEveryTick() {
		return true;
	}

	@Override
	public void tick() {
		if (--cooldown > 0) {
			return;
		}
		cooldown = CHECK_INTERVAL;

		RAConfig cfg = RAConfig.get();
		AnimalState state = AnimalState.of(animal);
		long now = animal.level().getGameTime();
		state.forgetExpiredFears(now);

		if (cfg.naturalRegeneration && animal.getHealth() < animal.getMaxHealth()
				&& now - state.lastDamagedAt >= cfg.regenDelayTicks && now >= state.nextRegenAt) {
			animal.heal(1.0F);
			state.nextRegenAt = now + cfg.regenIntervalTicks;
		}

		AttributeInstance speed = animal.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speed == null) {
			return;
		}
		boolean limping = cfg.injuredLimp && animal.getHealth() <= animal.getMaxHealth() * cfg.limpHealthFraction;
		if (limping && !speed.hasModifier(LIMP_ID)) {
			speed.addOrUpdateTransientModifier(new AttributeModifier(LIMP_ID, -cfg.limpSpeedPenalty, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		} else if (!limping && speed.hasModifier(LIMP_ID)) {
			speed.removeModifier(LIMP_ID);
		}
	}
}
