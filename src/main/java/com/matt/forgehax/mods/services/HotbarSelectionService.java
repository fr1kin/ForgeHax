package com.matt.forgehax.mods.services;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getWorld;

import com.google.common.base.MoreObjects;
import com.google.common.base.Predicates;
import com.matt.forgehax.util.entity.LocalPlayerInventory;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.function.Predicate;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

@RegisterMod
public class HotbarSelectionService extends ServiceMod {
  
  private static HotbarSelectionService instance = null;
  
  public static HotbarSelectionService getInstance() {
    return instance;
  }
  
  private int originalIndex = -1;
  private long ticksElapsed = -1;
  
  private int lastSetIndex = -1;
  private Predicate<Long> resetCondition = Predicates.alwaysTrue();
  
  public HotbarSelectionService() {
    super("HotbarSelectionService");
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
        resetCondition = MoreObjects.firstNonNull(condition, Predicates.alwaysTrue());
  
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
    resetCondition = Predicates.alwaysTrue();
  }
  
  @SubscribeEvent
  public void onClientTick(ClientTickEvent event) {
    if (getWorld() == null || getLocalPlayer() == null) {
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
    if (getLocalPlayer() == null) {
      return;
    }
    LocalPlayerInventory.getInventory().currentItem = index;
  }
  
  private static int selected() {
    return getLocalPlayer() == null ? -1 : LocalPlayerInventory.getSelected().getIndex();
  }
  
  public interface ResetFunction {
    
    void revert();
  }
}
