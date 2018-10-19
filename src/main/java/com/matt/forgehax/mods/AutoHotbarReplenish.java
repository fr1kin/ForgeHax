package com.matt.forgehax.mods;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getNetworkManager;

import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.entity.LocalPlayerInventory;
import com.matt.forgehax.util.entity.LocalPlayerInventory.InvItem;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.matt.forgehax.util.task.TaskChain;
import java.util.Comparator;
import java.util.List;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

@RegisterMod
public class AutoHotbarReplenish extends ToggleMod {
  private final Setting<Integer> durability_threshold =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("durability-threshold")
          .description("Will auto replace tools when they hit this damage value")
          .defaultTo(5)
          .min(0)
          .max((int) Short.MAX_VALUE)
          .build();

  private final Setting<Integer> stack_threshold =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("stack-threshold")
          .description("Will replace stacks when there only remains this many")
          .defaultTo(10)
          .min(1)
          .max((int) Short.MAX_VALUE)
          .build();

  private final Setting<Integer> tick_delay =
      getCommandStub()
          .builders()
          .<Integer>newSettingBuilder()
          .name("tick-delay")
          .description(
              "Number of ticks between each window click packet. 0 will have no limit and a negative value will send n packets per tick")
          .defaultTo(1)
          .build();

  private TaskChain<Runnable> tasks = TaskChain.empty();
  private long tickCount = 0;

  public AutoHotbarReplenish() {
    super(
        Category.PLAYER,
        "AutoHotbarReplenish",
        false,
        "Will replenish tools or block stacks automatically");
  }

  private boolean processing(int index) {
    if (tick_delay.get() == 0) return true; // process all
    else if (tick_delay.get() < 0)
      return index < Math.abs(tick_delay.get()); // process n tasks per tick
    else return index == 0 && tickCount % tick_delay.get() == 0;
  }

  private boolean isMonitoring(InvItem item) {
    return item.isItemDamageable() || item.isStackable();
  }

  private boolean isAboveThreshold(InvItem item) {
    return item.isItemDamageable()
        ? item.getDamage() > durability_threshold.get()
        : item.getStackCount() > stack_threshold.get();
  }

  private int getDamageOrCount(InvItem item) {
    return item.isNull() ? 0 : item.isItemDamageable() ? item.getDamage() : item.getStackCount();
  }

  private void tryPlacingHeldItem() {
    InvItem holding = LocalPlayerInventory.getMouseHeld();

    if (holding.isEmpty()) // all is good
    return;

    InvItem item;
    if (holding.isDamageable()) {
      item =
          LocalPlayerInventory.getSlotStorageInventory()
              .stream()
              .filter(InvItem::isNull)
              .findAny()
              .orElse(InvItem.EMPTY);
    } else {
      item =
          LocalPlayerInventory.getSlotStorageInventory()
              .stream()
              .filter(inv -> inv.isNull() || holding.isItemsEqual(inv))
              .filter(inv -> inv.isNull() || !inv.isStackMaxed())
              .max(Comparator.comparing(InvItem::getStackCount))
              .orElse(InvItem.EMPTY);
    }

    if (item == InvItem.EMPTY) click(holding, 0, ClickType.PICKUP);
    else {
      click(item, 0, ClickType.PICKUP);
      if (LocalPlayerInventory.getMouseHeld().nonEmpty()) throw new RuntimeException();
    }
  }

  @Override
  protected void onDisabled() {
    MC.addScheduledTask(
        () -> {
          tasks = TaskChain.empty();
          tickCount = 0;
        });
  }

  @SubscribeEvent
  public void onTick(ClientTickEvent event) {
    if (!Phase.START.equals(event.phase) || getLocalPlayer() == null) return;

    // only process when a gui isn't opened by the player
    if (MC.currentScreen != null) return;

    if (tasks.isEmpty()) {
      final List<InvItem> slots = LocalPlayerInventory.getSlotStorageInventory();

      tasks =
          LocalPlayerInventory.getHotbarInventory()
              .stream()
              .filter(InvItem::nonNull)
              .filter(this::isMonitoring)
              .filter(item -> !isAboveThreshold(item))
              .filter(
                  item ->
                      slots
                          .stream()
                          .filter(this::isMonitoring)
                          .filter(inv -> !inv.isItemDamageable() || isAboveThreshold(inv))
                          .anyMatch(item::isItemsEqual))
              .max(Comparator.comparingInt(LocalPlayerInventory::getHotbarDistance))
              .map(
                  hotbarItem ->
                      TaskChain.<Runnable>builder()
                          .then(
                              () -> {
                                // pick up item

                                verifyHotbar(hotbarItem);
                                click(
                                    slots
                                        .stream()
                                        .filter(InvItem::nonNull)
                                        .filter(this::isMonitoring)
                                        .filter(hotbarItem::isItemsEqual)
                                        .filter(inv -> !inv.isDamageable() || isAboveThreshold(inv))
                                        .max(Comparator.comparingInt(this::getDamageOrCount))
                                        .orElseThrow(RuntimeException::new),
                                    0,
                                    ClickType.PICKUP);
                              })
                          .then(
                              () -> {
                                // place item into hotbar

                                verifyHotbar(hotbarItem);
                                click(hotbarItem, 0, ClickType.PICKUP);
                              })
                          .then(this::tryPlacingHeldItem)
                          .build())
              .orElse(TaskChain.empty());
    }

    // process the next click task
    int n = 0;
    while (processing(n++) && tasks.hasNext()) {
      try {
        tasks.next().run();
      } catch (Throwable t) {
        tasks = TaskChain.singleton(this::tryPlacingHeldItem);
      }
    }

    ++tickCount;
  }

  //
  //
  //

  private static void verifyHotbar(InvItem hotbarItem) {
    InvItem current = LocalPlayerInventory.getHotbarInventory().get(hotbarItem.getIndex());
    if (!hotbarItem.isItemsEqual(current)) throw new IllegalArgumentException();
  }

  private static void verifyHeldItem(InvItem staticItem) {}

  private static void clickWindow(
      int slotIdIn, int usedButtonIn, ClickType modeIn, ItemStack clickedItemIn) {
    getNetworkManager()
        .sendPacket(
            new CPacketClickWindow(
                0,
                slotIdIn,
                usedButtonIn,
                modeIn,
                clickedItemIn,
                LocalPlayerInventory.getOpenContainer()
                    .getNextTransactionID(LocalPlayerInventory.getInventory())));
  }

  private static ItemStack click(InvItem item, int usedButtonIn, ClickType modeIn) {
    if (item.getIndex() == -1) throw new IllegalArgumentException();
    ItemStack ret;
    clickWindow(
        item.getSlotNumber(),
        usedButtonIn,
        modeIn,
        ret =
            LocalPlayerInventory.getOpenContainer()
                .slotClick(item.getSlotNumber(), usedButtonIn, modeIn, getLocalPlayer()));
    return ret;
  }
}
