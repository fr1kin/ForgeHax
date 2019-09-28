package com.matt.forgehax.mods;

import com.google.common.collect.Sets;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.Set;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class NoSoundLagMod extends ToggleMod {
  
  private static final Set<SoundEvent> BLACKLIST = Sets.newHashSet(
      SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,
      SoundEvents.ITEM_ARMOR_EQIIP_ELYTRA,
      SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND,
      SoundEvents.ITEM_ARMOR_EQUIP_IRON,
      SoundEvents.ITEM_ARMOR_EQUIP_GOLD,
      SoundEvents.ITEM_ARMOR_EQUIP_CHAIN,
      SoundEvents.ITEM_ARMOR_EQUIP_LEATHER
  );
  
  public NoSoundLagMod() {
    super(Category.MISC, "NoSoundLag", false, "lag exploit fix");
  }
  
  @SubscribeEvent
  public void onPacketReceived(PacketEvent.Incoming.Pre event) {
    if (event.getPacket() instanceof SPacketSoundEffect) {
      SPacketSoundEffect packet = event.getPacket();
      if (BLACKLIST.contains(packet.getSound())) {
        event.setCanceled(true);
      }
      
    }
  }
}
