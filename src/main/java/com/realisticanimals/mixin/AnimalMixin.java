package com.realisticanimals.mixin;

import com.realisticanimals.state.AnimalState;
import com.realisticanimals.state.AnimalStateHolder;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Animal.class)
public abstract class AnimalMixin implements AnimalStateHolder {
	@Unique
	private final AnimalState realisticanimals$state = new AnimalState();

	@Override
	public AnimalState realisticanimals$getState() {
		return realisticanimals$state;
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void realisticanimals$save(ValueOutput output, CallbackInfo ci) {
		realisticanimals$state.save(output);
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void realisticanimals$load(ValueInput input, CallbackInfo ci) {
		realisticanimals$state.load(input);
	}
}
