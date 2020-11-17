package dev.fiki.forgehax.main.services;

import com.google.common.base.MoreObjects;
import dev.fiki.forgehax.api.entity.LocalPlayerInventory;
import dev.fiki.forgehax.api.mod.ServiceMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.main.Common;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.function.Predicate;

@RegisterMod
public class HotbarSelectionService extends ServiceMod {
  private static HotbarSelectionService instance = null;
  
  public static HotbarSelectionService getInstance() {
    return instance;
  }
  
  private int originalIndex = -1;
  private long ticksElapsed = -1;
  
  private int lastSetIndex = -1;
  private Predicate<Long> resetCondition = ticks -> true;

  {
    instance = this;
  }
  
  public ResetFunction setSelected(final int index, boolean reset, Predicate<Long> condition) {
    if (index < 0 || index > LocalPlayerInventory.getHotbarSize() - 1) {
      throw new IllegalArgumentException(
          "index must be between 0 and " + (LocalPlayerInventory.getHotbarSize() - 1));
    }
    
    final int current = selected();
    
    if (!reset) {
      select(index);
      if (originalIndex != -1) {
        originalIndex = index;
      }
      
      return () -> select(index);
    } else {
      if (current != index) {
        if (originalIndex == -1) {
          originalIndex = current;
        }
        
        lastSetIndex = index;
        resetCondition = MoreObjects.firstNonNull(condition, ticks -> true);
        
        select(index);
      }
      ticksElapsed = 0;
      
      return () -> {
        if (index == selected() && lastSetIndex == index) {
          select(current);
          reset();
        }
      };
    }
  }
  
  public void resetSelected() {
    if (originalIndex != -1 && selected() == lastSetIndex) {
      select(originalIndex);
    }
    reset();
  }
  
  private void reset() {
    originalIndex = -1;
    ticksElapsed = -1;
    lastSetIndex = -1;
    resetCondition = ticks -> true;
  }
  
  @SubscribeEvent
  public void onClientTick(TickEvent.ClientTickEvent event) {
    if (!Common.isInWorld()) {
      reset();
      return;
    }
    
    switch (event.phase) {
      case START: {
        if (originalIndex != -1 && resetCondition.test(ticksElapsed)) {
          resetSelected();
        }
        if (ticksElapsed != -1) {
          ++ticksElapsed;
        }
        break;
      }
    }
  }
  
  //
  //
  //
  
  private static void select(int index) {
    if (Common.getLocalPlayer() == null) {
      return;
    }
    LocalPlayerInventory.getInventory().currentItem = index;
  }
  
  private static int selected() {
    return Common.getLocalPlayer() == null ? -1 : LocalPlayerInventory.getSelected().getIndex();
  }
  
  public interface ResetFunction {
    void revert();
  }
}
