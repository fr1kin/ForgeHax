package dev.fiki.forgehax.main.mods;

import dev.fiki.forgehax.common.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.command.Setting;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.item.Items;
import net.minecraft.network.play.server.SSpawnPlayerPacket;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

@RegisterMod
public class AutoLog extends ToggleMod {
  
  public AutoLog() {
    super(Category.COMBAT, "AutoLog", false, "automatically disconnect");
  }
  
  public final Setting<Integer> threshold =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("threshold")
          .description("health to go down to to disconnect\"")
          .defaultTo(0)
          .build();
  public final Setting<Boolean> noTotem =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("NoTotem")
          .description("disconnect if not holding a totem")
          .defaultTo(false)
          .build();
  public final Setting<Boolean> disconnectOnNewPlayer =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("NewPlayer")
          .description("Disconnect if a player enters render distance")
          .defaultTo(false)
          .build();
  
  @SubscribeEvent
  public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
    if (Common.MC.player != null) {
      int health = (int) (Common.MC.player.getHealth() + Common.MC.player.getAbsorptionAmount());
      if (health <= threshold.get()
          || (noTotem.getAsBoolean()
          && !((Common.MC.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING)
          || Common.MC.player.getHeldItemMainhand().getItem() == Items.TOTEM_OF_UNDYING))) {
        AutoReconnectMod.hasAutoLogged = true;
        Common.getNetworkManager().closeChannel(new StringTextComponent("Health too low (" + health + ")"));
        disable();
      }
    }
  }
  
  @SubscribeEvent
  public void onPacketRecieved(PacketInboundEvent event) {
    if (event.getPacket() instanceof SSpawnPlayerPacket) {
      if (disconnectOnNewPlayer.getAsBoolean()) {
        AutoReconnectMod.hasAutoLogged = true; // dont automatically reconnect
        UUID id = ((SSpawnPlayerPacket) event.getPacket()).getUniqueId();
        
        NetworkPlayerInfo info = Common.MC.getConnection().getPlayerInfo(id);
        String name = info != null ? info.getGameProfile().getName() : "(Failed) " + id.toString();
        
        Common.getNetworkManager().closeChannel(new StringTextComponent(name + " entered render distance"));
        disable();
      }
    }
  }
}
