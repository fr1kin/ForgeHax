package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.util.mod.loader.RegisterMod;

import org.apache.commons.io.IOUtils;

import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

import com.google.gson.JsonParser;
import com.matt.forgehax.util.command.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityTameable;

import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;

import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


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
  
  public MobOwner() {
    super(Category.WORLD, "MobOwner", true, "Add MobOwner in entities nametags");
  }

  private Map<UUID, String> UUIDcache = new HashMap<UUID, String>();
  private Map<UUID, Long> UUIDcooldown = new HashMap<UUID, Long>();
  
  private static String getNameFromUUID(String uuid) {
    try {
        String jsonUrl = IOUtils.toString(new URL("https://api.mojang.com/user/profiles/" + uuid.replace("-", "") + "/names"));
        JsonParser parser = new JsonParser();
        return parser.parse(jsonUrl)
                     .getAsJsonArray()
                     .get(parser.parse(jsonUrl).getAsJsonArray().size() - 1)
                     .getAsJsonObject()
                     .get("name")
                     .toString()
                     .replace("\"", "");
    } catch (Exception ex) {
      return null;
    }
  }

  private String getName(UUID uuid) {
    String name = UUIDcache.get(uuid);
    
    if (name == null) {
      if (UUIDcooldown.get(uuid) == null || (
          Instant.now().getEpochSecond() - UUIDcooldown.get(uuid) > lookup_cooldown.get())) {
        UUIDcooldown.put(uuid, Instant.now().getEpochSecond());
        name = getNameFromUUID(uuid.toString());
        if (name != null) UUIDcache.put(uuid, name);
      } 
    }
    return name;
  }

  @Override
  public void onDisabled() {
    if (getWorld() == null) return;
    for (Entity mob : getWorld().loadedEntityList) {
      if (mob instanceof EntityTameable || mob instanceof EntityHorse)
        mob.setAlwaysRenderNameTag(false);
    }
  }

  @SubscribeEvent(priority = EventPriority.LOW)
  public void clientTick(ClientTickEvent event) {
    if (getWorld() == null) return;
    for (Entity mob : getWorld().loadedEntityList) {
      if (mob == null) continue;
      if (mob instanceof EntityTameable) {
        if (((EntityTameable) mob).getOwnerId() != null) {
          if (nametag.get()) ((EntityTameable) mob).setAlwaysRenderNameTag(true);
          if (((EntityTameable) mob).getCustomNameTag() == "") {
            String username = getName(((EntityTameable) mob).getOwnerId());
            if (username != null) {
              ((EntityTameable) mob).setCustomNameTag(String.format("%s (%s)",
                                        ((EntityTameable) mob).getName(), username));
            }
          }
        }
      }
      if (mob instanceof EntityHorse) {
        if (((EntityHorse) mob).getOwnerUniqueId() != null) {
          if (nametag.get()) ((EntityHorse) mob).setAlwaysRenderNameTag(true);
          if (((EntityHorse) mob).getCustomNameTag() == "") {
            String username = getName(((EntityHorse) mob).getOwnerUniqueId());
            if (username != null) {
              ((EntityHorse) mob).setCustomNameTag(String.format("%s (%s)",
                                  ((EntityHorse) mob).getName(), username));
            }
          }
        }
      }
    }
  }
}
