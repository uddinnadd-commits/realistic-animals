package com.realisticanimals.goal;

import com.realisticanimals.RATags;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.animal.Animal;

public final class RealisticGoals {
	private RealisticGoals() {
	}

	/**
	 * Priorities sit alongside the vanilla ones: fleeing matches PanicGoal (1), foraging matches TemptGoal (3),
	 * and the idle behaviours (4) outrank random strolling (5-6) so they actually get a turn.
	 */
	public static void install(Animal animal, GoalSelector goals) {
		goals.addGoal(0, new WellbeingGoal(animal));
		goals.addGoal(1, new FleeThreatGoal(animal));
		goals.addGoal(3, new ForageGoal(animal));
		goals.addGoal(4, new StormShelterGoal(animal));
		goals.addGoal(4, new NightRestGoal(animal));
		if (RATags.is(animal, RATags.HERD_ANIMALS)) {
			goals.addGoal(4, new HerdCohesionGoal(animal));
		}
		if (RATags.is(animal, RATags.GRAZERS)) {
			goals.addGoal(4, new GrazeGoal(animal));
		}
	}
}
