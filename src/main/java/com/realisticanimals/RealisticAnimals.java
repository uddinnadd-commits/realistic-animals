package com.realisticanimals;

import com.realisticanimals.behavior.HerdAlarm;
import com.realisticanimals.behavior.InteractionHandler;
import com.realisticanimals.config.RAConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RealisticAnimals implements ModInitializer {
	public static final String MOD_ID = "realisticanimals";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		RAConfig.load();
		ServerLivingEntityEvents.AFTER_DAMAGE.register(HerdAlarm::onDamaged);
		UseEntityCallback.EVENT.register(InteractionHandler::onUseEntity);
		LOGGER.info("Realistic Animals loaded - animals are now a little more alive.");
	}
}
