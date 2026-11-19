package com.realisticanimals.goal;

import com.realisticanimals.config.RAConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.EnumSet;

/** Cows, horses and llamas stop to graze on grass, healing a little (sheep already do this in vanilla). */
public class GrazeGoal extends Goal {
	private static final int GRAZE_TICKS = 40;
	/** Vanilla level event that plays block-break particles and sound. */
	private static final int BLOCK_BREAK_EVENT = 2001;

	private final Animal animal;
	private BlockPos plantPos;
	private BlockPos pasturePos;
	private int timer;

	public GrazeGoal(Animal animal) {
		this.animal = animal;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
	}

	@Override
	public boolean canUse() {
		if (!RAConfig.get().grazing || !animal.onGround() || animal.isVehicle() || animal.isInLove()) {
			return false;
		}
		int chance = animal.isBaby() ? 100 : animal.getHealth() < animal.getMaxHealth() ? 250 : 800;
		if (animal.getRandom().nextInt(chance) != 0) {
			return false;
		}
		BlockPos pos = animal.blockPosition();
		plantPos = isGrazingPlant(animal.level().getBlockState(pos)) ? pos : null;
		pasturePos = animal.level().getBlockState(pos.below()).is(Blocks.GRASS_BLOCK) ? pos.below() : null;
		return plantPos != null || pasturePos != null;
	}

	@Override
	public void start() {
		timer = adjustedTickDelay(GRAZE_TICKS);
		animal.getNavigation().stop();
	}

	@Override
	public boolean canContinueToUse() {
		return timer > 0;
	}

	@Override
	public void stop() {
		timer = 0;
	}

	@Override
	public void tick() {
		timer = Math.max(0, timer - 1);
		BlockPos lookAt = plantPos != null ? plantPos : pasturePos;
		animal.getLookControl().setLookAt(lookAt.getX() + 0.5, lookAt.getY() + 0.5, lookAt.getZ() + 0.5);
		if (timer == adjustedTickDelay(4)) {
			graze();
		}
	}

	private void graze() {
		if (!(animal.level() instanceof ServerLevel level)) {
			return;
		}
		boolean mayGrief = RAConfig.get().grazingEatsPlants && level.getGameRules().get(GameRules.MOB_GRIEFING);
		if (plantPos != null && isGrazingPlant(level.getBlockState(plantPos)) && mayGrief) {
			level.destroyBlock(plantPos, false);
		} else if (pasturePos != null && level.getBlockState(pasturePos).is(Blocks.GRASS_BLOCK)) {
			// Nibbling the pasture only plays particles; it never turns grass into dirt.
			level.levelEvent(BLOCK_BREAK_EVENT, pasturePos, Block.getId(Blocks.GRASS_BLOCK.defaultBlockState()));
		} else {
			return;
		}
		animal.heal(1.0F);
		if (animal.isBaby()) {
			animal.ageUp(60);
		}
	}

	private static boolean isGrazingPlant(BlockState state) {
		return state.is(Blocks.SHORT_GRASS) || state.is(Blocks.FERN) || state.is(Blocks.SHORT_DRY_GRASS);
	}
}
