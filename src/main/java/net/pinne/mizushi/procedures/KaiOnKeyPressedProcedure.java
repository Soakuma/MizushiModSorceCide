package net.pinne.mizushi.procedures;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class KaiOnKeyPressedProcedure {
    private static final Map<UUID, Integer> damageCounter = new HashMap<>();
    private static final Map<UUID, Integer> tickCounter = new HashMap<>();
    private static final Map<UUID, ResourceKey<net.minecraft.world.level.Level>> entityDimensions = new HashMap<>();

    private static final float DAMAGE_AMOUNT = 2.0f;

    public static void execute(LevelAccessor world, Entity entity) {
        if (entity == null || !(world instanceof ServerLevel))
            return;

        ServerLevel serverWorld = (ServerLevel) world;
        Vec3 targetPos = entity.getEyePosition(1f).add(entity.getViewVector(1f).scale(5));
        AABB searchArea = AABB.ofSize(new Vec3(targetPos.x, targetPos.y, targetPos.z), 5, 5, 5);

        List<LivingEntity> nearbyEntities = serverWorld.getEntitiesOfClass(LivingEntity.class, searchArea, e -> e != entity);

        for (LivingEntity target : nearbyEntities) {
            UUID targetId = target.getUUID();
            damageCounter.put(targetId, 0);
            tickCounter.put(targetId, 0);
            entityDimensions.put(targetId, target.level().dimension());
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            for (UUID entityId : new HashMap<>(tickCounter).keySet()) {
                int currentTick = tickCounter.get(entityId);
                if (currentTick % 2 == 0) {  // Every 2 ticks
                    ResourceKey<net.minecraft.world.level.Level> dimension = entityDimensions.get(entityId);
                    if (dimension == null) {
                        // 차원 정보가 없으면 이 엔티티에 대한 처리를 중단
                        damageCounter.remove(entityId);
                        tickCounter.remove(entityId);
                        entityDimensions.remove(entityId);
                        continue;
                    }

                    ServerLevel targetWorld = event.getServer().getLevel(dimension);
                    if (targetWorld == null) {
                        // 해당 차원이 로드되지 않았다면 처리를 중단
                        continue;
                    }

                    Entity entity = targetWorld.getEntity(entityId);
                    if (entity instanceof LivingEntity livingEntity) {
                        int currentDamage = damageCounter.get(entityId);
                        if (currentDamage < 10) {  // Up to 10 times
                            DamageSource damageSource = new DamageSource(livingEntity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                                    .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation("mizushi:mizushi_damage"))));
                            
                            livingEntity.invulnerableTime = 0;
                            livingEntity.hurt(damageSource, DAMAGE_AMOUNT);
                            
                            if (livingEntity.level() instanceof ServerLevel serverLevel) {
                                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, livingEntity.getX(), livingEntity.getY() + 1, livingEntity.getZ(), 5, 0.5, 0.5, 0.5, 1);
                            }
                            
                            damageCounter.put(entityId, currentDamage + 1);
                        } else {
                            damageCounter.remove(entityId);
                            tickCounter.remove(entityId);
                            entityDimensions.remove(entityId);
                            continue;
                        }
                    } else {
                        // 엔티티가 더 이상 존재하지 않으면 제거
                        damageCounter.remove(entityId);
                        tickCounter.remove(entityId);
                        entityDimensions.remove(entityId);
                        continue;
                    }
                }
                tickCounter.put(entityId, currentTick + 1);
            }
        }
    }
}