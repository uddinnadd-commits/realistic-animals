package com.realisticanimals.behavior;

import com.realisticanimals.RATags;
import com.realisticanimals.config.RAConfig;
import com.realisticanimals.state.AnimalState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

/** Hand-feeding builds trust; cows and mooshrooms need time between milkings. */
public final class InteractionHandler {
	private InteractionHandler() {
	}

	public static InteractionResult onUseEntity(Player player, Level world, InteractionHand hand, Entity entity, EntityHitResult hit) {
		if (!(world instanceof ServerLevel level) || player.isSpectator() || !(entity instanceof Animal animal)
				|| !RATags.is(animal, RATags.REALISTIC_BEHAVIOR)) {
			return InteractionResult.PASS;
		}

		RAConfig cfg = RAConfig.get();
		ItemStack stack = player.getItemInHand(hand);
		AnimalState state = AnimalState.of(animal);
		long now = level.getGameTime();

		if (cfg.milkingCooldown && isMilking(animal, stack)) {
			if (now < state.milkReadyAt) {
				player.sendOverlayMessage(Component.translatableWithFallback("message.realisticanimals.milk_cooldown",
						"%s needs more time before it can be milked again", animal.getDisplayName()));
				return InteractionResult.FAIL;
			}
			state.milkReadyAt = now + cfg.milkingCooldownTicks;
			return InteractionResult.PASS;
		}

		if (cfg.trustFromFeeding && !stack.isEmpty() && animal.isFood(stack) && state.trust(player.getUUID())) {
			level.sendParticles(ParticleTypes.HAPPY_VILLAGER, animal.getX(), animal.getY() + animal.getBbHeight() + 0.3, animal.getZ(),
					6, 0.3, 0.2, 0.3, 0.0);
		}
		return InteractionResult.PASS;
	}

	private static boolean isMilking(Animal animal, ItemStack stack) {
		if (!(animal instanceof AbstractCow) || animal.isBaby()) {
			return false;
		}
		return stack.is(Items.BUCKET) || animal instanceof MushroomCow && stack.is(Items.BOWL);
	}
}
