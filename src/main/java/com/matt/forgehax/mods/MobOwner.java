package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.util.mod.loader.RegisterMod;

import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.PlayerInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.util.text.TextFormatting;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


@RegisterMod
public class MobOwner extends ToggleMod {
  public final Setting<Integer> lookup_cooldown =
    getCommandStub()
      .builders()
      .<Integer>newSettingBuilder()
      .name("cooldown")
      .description("Seconds to wait before looking up an username again")
      .defaultTo(10)
      .build();

  public final Setting<Boolean> nametag =
    getCommandStub()
      .builders()
      .<Boolean>newSettingBuilder()
      .name("nametag")
      .description("Set the nametag to always render")
      .defaultTo(true)
      .build();

  private final Setting<TextFormatting> color =
    getCommandStub()
      .builders()
      .<TextFormatting>newSettingEnumBuilder()
      .name("Color")
      .description("Set the color shown on the nametag")
      .defaultTo(TextFormatting.WHITE)
      .build();
  
  public MobOwner() {
    super(Category.WORLD, "MobOwner", true, "Add MobOwner in entities nametags");
  }

  @Override
  public void onEnabled() {
    if (getWorld() == null) return;
    for (Entity ent : getWorld().loadedEntityList) {
      if (ent instanceof EntityTameable) {
        
        setName((EntityTameable) ent);
      } else if (ent instanceof EntityHorse) {
        if (nametag.get()) ent.setAlwaysRenderNameTag(true);
        setName((EntityHorse) ent); 
      }
    }
  }

  @Override
  public void onDisabled() {
    if (getWorld() == null) return;
    for (Entity mob : getWorld().loadedEntityList) {
      if (mob instanceof EntityTameable || mob instanceof EntityHorse)
        mob.setAlwaysRenderNameTag(false);
        mob.setCustomNameTag("");
    }
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
    if (getWorld() == null) return;
    if (event.getEntity() instanceof EntityTameable) {
      setName((EntityTameable) event.getEntity());
    } else if (event.getEntity() instanceof EntityHorse) {
      setName((EntityHorse) event.getEntity());
    }
  }

  private Map<UUID, PlayerInfo> UUIDcache = new ConcurrentHashMap<UUID, PlayerInfo>();

  private void setName(EntityHorse mob) {
    if (mob.getOwnerUniqueId() != null) {
      if (mob.getCustomNameTag() == "") {
        if (UUIDcache.get(mob.getOwnerUniqueId()) != null) {
          mob.setCustomNameTag(String.format("%s (%s)", mob.getName(),color.get() +
                        UUIDcache.get(mob.getOwnerUniqueId()).getName() + TextFormatting.RESET));
        } else {
          try {
            PlayerInfo owner = new PlayerInfo(mob.getOwnerUniqueId());
            UUIDcache.put(mob.getOwnerUniqueId(), owner);
            mob.setCustomNameTag(String.format("%s (%s)", mob.getName(), color.get() + owner.getName() + TextFormatting.RESET));
            if (nametag.get()) mob.setAlwaysRenderNameTag(true);
          } catch (Exception e) {
            // ignore
          }
        }
      }
    }
  }

  private void setName(EntityTameable mob) {
    if (mob.getOwnerId() != null) {
      if (mob.getCustomNameTag() == "") {
        if (UUIDcache.get(mob.getOwnerId()) != null) {
          mob.setCustomNameTag(String.format("%s (%s)", mob.getName(),color.get() +
                        UUIDcache.get(mob.getOwnerId()).getName() + TextFormatting.RESET));
        } else {
          try {
            PlayerInfo owner = new PlayerInfo(mob.getOwnerId());
            UUIDcache.put(mob.getOwnerId(), owner);
            mob.setCustomNameTag(String.format("%s (%s)", mob.getName(), color.get() + owner.getName() + TextFormatting.RESET));
            if (nametag.get()) mob.setAlwaysRenderNameTag(true);
          } catch (Exception e) {
            // ignore
          }
        }
      }
    }
  }
}