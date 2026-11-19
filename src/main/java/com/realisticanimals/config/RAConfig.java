package com.realisticanimals.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.realisticanimals.RealisticAnimals;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Server-side settings, stored in {@code config/realisticanimals.json}.
 * Missing keys keep their defaults and are written back on load.
 */
public final class RAConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static RAConfig instance = new RAConfig();

	// Fear and fleeing
	public boolean herdAlarm = true;
	public double herdAlarmRadius = 16.0;
	public int alarmDurationTicks = 140;
	public boolean llamasDefendHerd = true;
	public boolean predatorAvoidance = true;
	public double predatorDetectionRange = 12.0;
	public boolean fearMonsters = true;
	public double monsterDetectionRange = 6.0;
	public double fleeSpeed = 1.5;
	public double safeDistance = 18.0;

	// Players
	public boolean waryOfPlayers = true;
	public double sprintScareRange = 8.0;
	public double babyExtraScareRange = 3.0;
	public int attackerMemoryTicks = 2400;
	public double attackerMemoryRange = 16.0;
	public boolean trustFromFeeding = true;

	// Daily life
	public boolean herdCohesion = true;
	public double herdSearchRange = 24.0;
	public double herdComfortDistance = 6.0;
	public boolean forageDroppedFood = true;
	public double forageRange = 10.0;
	public int forageCooldownTicks = 600;
	public float forageHealAmount = 2.0F;
	public boolean grazing = true;
	public boolean grazingEatsPlants = true;
	public boolean nightRest = true;
	public boolean stormShelter = true;

	// Health
	public boolean naturalRegeneration = true;
	public int regenDelayTicks = 600;
	public int regenIntervalTicks = 400;
	public boolean injuredLimp = true;
	public double limpHealthFraction = 0.35;
	public double limpSpeedPenalty = 0.3;

	// Farming
	public boolean milkingCooldown = true;
	public int milkingCooldownTicks = 6000;

	public static RAConfig get() {
		return instance;
	}

	public static void load() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(RealisticAnimals.MOD_ID + ".json");
		RAConfig loaded = null;

		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				loaded = GSON.fromJson(reader, RAConfig.class);
			} catch (Exception e) {
				RealisticAnimals.LOGGER.error("Could not read {}, using defaults", path, e);
			}
		}

		instance = loaded != null ? loaded : new RAConfig();

		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(instance, writer);
			}
		} catch (IOException e) {
			RealisticAnimals.LOGGER.error("Could not write {}", path, e);
		}
	}
}
