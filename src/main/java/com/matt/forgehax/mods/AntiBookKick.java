package com.matt.forgehax.mods;

import com.matt.forgehax.Helper;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.item.ItemWrittenBook;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


/**
 * Created on 2020-03-22 by IronException
 *
 */

@RegisterMod
public class AntiBookKick extends ToggleMod {

  public AntiBookKick() {
    super(Category.MISC, "AntiBookKick", false, "Doesn't let you click on slots that have books");
  }

  @SubscribeEvent
  public void onPacket(final PacketEvent.Outgoing.Pre event) {
    if (!(event.getPacket() instanceof CPacketClickWindow)) {
      return;
    }

    final CPacketClickWindow packet = event.getPacket();
    if(!(packet.getClickedItem().getItem() instanceof ItemWrittenBook)) {
      return;
    }

    event.setCanceled(true);
    Helper.printInform("Don't press the book \"" + packet.getClickedItem().getDisplayName() + "\"!");
    MC.player.openContainer.slotClick(packet.getSlotId(), packet.getUsedButton(), packet.getClickType(), MC.player);
  }


}
