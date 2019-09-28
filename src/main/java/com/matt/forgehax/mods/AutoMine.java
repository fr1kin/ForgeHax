package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;

import com.matt.forgehax.asm.events.BlockControllerProcessEvent;
import com.matt.forgehax.asm.events.LeftClickCounterUpdateEvent;
import com.matt.forgehax.util.entity.LocalPlayerUtils;
import com.matt.forgehax.util.key.Bindings;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@RegisterMod
public class AutoMine extends ToggleMod {
  
  private boolean pressed = false;
  
  public AutoMine() {
    super(Category.PLAYER, "AutoMine", false, "Auto mine blocks");
  }
  
  private void setPressed(boolean state) {
    Bindings.attack.setPressed(state);
    pressed = state;
  }
  
  @Override
  protected void onEnabled() {
    Bindings.attack.bind();
  }
  
  @Override
  protected void onDisabled() {
    setPressed(false);
    Bindings.attack.unbind();
  }
  
  @SubscribeEvent
  public void onTick(TickEvent.ClientTickEvent event) {
    if (getLocalPlayer() == null || getWorld() == null) {
      return;
    }
    
    switch (event.phase) {
      case START: {
        RayTraceResult tr = LocalPlayerUtils.getMouseOverBlockTrace();
        
        if (tr == null) {
          setPressed(false);
          return;
        }
        
        setPressed(true);
        break;
      }
      case END:
        setPressed(false);
        break;
    }
  }
  
  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onGuiOpened(GuiOpenEvent event) {
    // process keys and mouse input even if this gui is open
    if (getWorld() != null && getLocalPlayer() != null && event.getGui() != null) {
      event.getGui().allowUserInput = true;
    }
  }
  
  @SubscribeEvent
  public void onLeftClickCouterUpdate(LeftClickCounterUpdateEvent event) {
    // prevent the leftClickCounter from changing
    event.setCanceled(true);
  }
  
  @SubscribeEvent
  public void onBlockCounterUpdate(BlockControllerProcessEvent event) {
    // bug fix - left click is actually false after processing the key bindings
    // this will set that boolean to the correct value
    if (pressed) {
      event.setLeftClicked(true);
    }
  }
}
