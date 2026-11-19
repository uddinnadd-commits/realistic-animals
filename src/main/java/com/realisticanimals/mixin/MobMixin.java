package com.realisticanimals.mixin;

import com.realisticanimals.RATags;
import com.realisticanimals.goal.RealisticGoals;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobMixin {
	@Shadow
	@Final
	protected GoalSelector goalSelector;

	// Mob's constructor is where vanilla calls registerGoals() on the server, so this runs exactly once per entity.
	@Inject(method = "<init>", at = @At("TAIL"))
	private void realisticanimals$addGoals(EntityType<? extends Mob> type, Level level, CallbackInfo ci) {
		if (level.isClientSide() || !((Object) this instanceof Animal animal)) {
			return;
		}
		if (type.builtInRegistryHolder().is(RATags.REALISTIC_BEHAVIOR)) {
			RealisticGoals.install(animal, goalSelector);
		}
	}
}
