package com.realisticanimals.behavior;

import com.realisticanimals.RATags;
import com.realisticanimals.config.RAConfig;
import com.realisticanimals.state.AnimalState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

import java.util.List;

/** When one animal is attacked, the rest of its herd reacts. */
public final class HerdAlarm {
	private HerdAlarm() {
	}

	public static void onDamaged(LivingEntity entity, DamageSource source, float baseDamage, float damageTaken, boolean blocked) {
		if (!(entity instanceof Animal victim) || !(victim.level() instanceof ServerLevel level) || !RATags.is(victim, RATags.REALISTIC_BEHAVIOR)) {
			return;
		}

		RAConfig cfg = RAConfig.get();
		long now = level.getGameTime();
		AnimalState state = AnimalState.of(victim);
		state.lastDamagedAt = now;

		if (!(source.getEntity() instanceof LivingEntity attacker) || attacker == victim || blocked && damageTaken <= 0.0F) {
			return;
		}
		boolean attackerIsPlayer = attacker instanceof Player;
		if (attackerIsPlayer) {
			state.fear(attacker.getUUID(), now + cfg.attackerMemoryTicks);
		}
		state.alarm(attacker, now + cfg.alarmDurationTicks);

		if (!cfg.herdAlarm) {
			return;
		}

		List<Animal> herd = level.getEntitiesOfClass(Animal.class, victim.getBoundingBox().inflate(cfg.herdAlarmRadius),
				a -> a != victim && a.isAlive() && a.getType() == victim.getType());
		boolean calledOut = false;
		for (Animal member : herd) {
			if (ThreatAssessment.isDefender(member)) {
				if (!(attacker instanceof Player player && (player.isCreative() || player.isSpectator()))) {
					member.setTarget(attacker);
				}
				continue;
			}
			AnimalState memberState = AnimalState.of(member);
			memberState.alarm(attacker, now + cfg.alarmDurationTicks + member.getRandom().nextInt(60));
			if (attackerIsPlayer) {
				memberState.fear(attacker.getUUID(), now + cfg.attackerMemoryTicks);
			}
			if (!calledOut && member.getRandom().nextInt(3) == 0) {
				member.playAmbientSound();
				calledOut = true;
			}
		}
	}
}
