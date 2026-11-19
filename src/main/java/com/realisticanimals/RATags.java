package com.realisticanimals;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/** Entity type tags; all of them can be overridden with a data pack. */
public final class RATags {
	/** Animals that receive the realistic behaviour goals. Must be goal-based (not Brain-based) mobs. */
	public static final TagKey<EntityType<?>> REALISTIC_BEHAVIOR = create("realistic_behavior");
	public static final TagKey<EntityType<?>> HERD_ANIMALS = create("herd_animals");
	public static final TagKey<EntityType<?>> GRAZERS = create("grazers");
	public static final TagKey<EntityType<?>> SMALL_PREY = create("small_prey");
	public static final TagKey<EntityType<?>> LIVESTOCK = create("livestock");
	public static final TagKey<EntityType<?>> HUNTS_SMALL_PREY = create("hunts_small_prey");
	public static final TagKey<EntityType<?>> HUNTS_LIVESTOCK = create("hunts_livestock");
	public static final TagKey<EntityType<?>> DEFENDS_HERD = create("defends_herd");

	private RATags() {
	}

	public static boolean is(Entity entity, TagKey<EntityType<?>> tag) {
		return entity.getType().builtInRegistryHolder().is(tag);
	}

	private static TagKey<EntityType<?>> create(String path) {
		return TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(RealisticAnimals.MOD_ID, path));
	}
}
