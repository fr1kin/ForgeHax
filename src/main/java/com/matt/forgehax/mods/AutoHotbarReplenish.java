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
import java.util.function.Supplier;
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

  private TaskChain<Supplier<Boolean>> tasks = TaskChain.empty();

  public AutoHotbarReplenish() {
    super(
        Category.PLAYER,
        "AutoHotbarReplenish",
        false,
        "Will replenish tools or block stacks automatically");
  }

  private boolean isTool(InvItem item) {
    return item.getItemStack().isItemStackDamageable();
  }

  private boolean isStackable(InvItem item) {
    return item.getItemStack().isStackable();
  }

  private boolean isMonitoring(InvItem item) {
    return isTool(item) || isStackable(item);
  }

  private void verifyHotbar(InvItem staticItem)
      throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
    InvItem current = LocalPlayerInventory.getHotbarInventory().get(staticItem.getIndex() - 36);
    if (!staticItem.getItemStack().isItemEqualIgnoreDurability(current.getItemStack()))
      throw new IllegalArgumentException();
  }

  private boolean isAboveThreshold(InvItem item) {
    if (isTool(item))
      return (item.getItemStack().getMaxDamage() - item.getItemStack().getItemDamage())
          > durability_threshold.get();
    else return item.getItemStack().getCount() > stack_threshold.get();
  }

  private boolean isItemsEqual(InvItem inv1, InvItem inv2) {
    return inv1.getItemStack().isItemEqualIgnoreDurability(inv2.getItemStack());
  }

  private boolean isStackBelowMax(InvItem inv) {
    return inv.getItemStack().getCount() < inv.getItemStack().getMaxStackSize();
  }

  private int getDamageOrCount(InvItem item) {
    return item.isNull()
        ? 0
        : isTool(item)
            ? (item.getItemStack().getMaxDamage() - item.getItemStack().getItemDamage())
            : item.getItemStack().getCount();
  }

  private boolean tryPlacingHeldItem() {
    InvItem holding =
        LocalPlayerInventory.newInvItem(LocalPlayerInventory.getInventory().getItemStack(), -999);

    InvItem item;
    if (isTool(holding)) {
      item =
          LocalPlayerInventory.getMutatingStorageInventory()
              .stream()
              .filter(InvItem::isNull)
              .filter(this::isAboveThreshold)
              .findAny()
              .orElse(InvItem.EMPTY);
    } else {
      item =
          LocalPlayerInventory.getMutatingStorageInventory()
              .stream()
              .filter(inv -> inv.isNull() || isItemsEqual(holding, inv))
              .filter(inv -> inv.isNull() || isStackBelowMax(inv))
              .max(Comparator.comparing(this::getDamageOrCount))
              .orElse(InvItem.EMPTY);
    }

    if (item == InvItem.EMPTY) {
      click(holding, 0, ClickType.PICKUP);
      return true; // cannot find any slot
    } else {
      click(item, 0, ClickType.PICKUP);
      return LocalPlayerInventory.getInventory().getItemStack().isEmpty();
    }
  }

  @Override
  protected void onDisabled() {
    MC.addScheduledTask(() -> tasks = TaskChain.empty());
  }

  @SubscribeEvent
  public void onTick(ClientTickEvent event) {
    if (!Phase.START.equals(event.phase) || getLocalPlayer() == null) return;

    // only process when a gui isn't opened by the player
    if (MC.currentScreen != null) return;

    if (tasks.isEmpty()) {
      final List<InvItem> storage = LocalPlayerInventory.getMutatingStorageInventory();

      tasks =
          LocalPlayerInventory.getHotbarInventory()
              .stream()
              .filter(InvItem::nonNull)
              .filter(this::isMonitoring)
              .filter(item -> !isAboveThreshold(item))
              .filter(
                  item ->
                      storage
                          .stream()
                          .filter(this::isMonitoring)
                          .filter(inv -> !isTool(inv) || isAboveThreshold(inv))
                          .anyMatch(
                              inv ->
                                  inv.getItemStack()
                                      .isItemEqualIgnoreDurability(item.getItemStack())))
              .max(Comparator.comparingInt(LocalPlayerInventory::getHotbarDistance))
              .map(
                  item ->
                      LocalPlayerInventory.newInvItem(item.getItemStack(), 36 + item.getIndex()))
              .map(
                  hotbarItem ->
                      TaskChain.<Supplier<Boolean>>builder()
                          .then(
                              () -> {
                                verifyHotbar(hotbarItem);
                                click(
                                    storage
                                        .stream()
                                        .filter(InvItem::nonNull)
                                        .filter(this::isMonitoring)
                                        .filter(inv -> !isTool(inv) || isAboveThreshold(inv))
                                        .filter(
                                            inv ->
                                                hotbarItem
                                                    .getItemStack()
                                                    .isItemEqualIgnoreDurability(
                                                        inv.getItemStack()))
                                        .max(Comparator.comparingInt(this::getDamageOrCount))
                                        .orElseThrow(RuntimeException::new),
                                    0,
                                    ClickType.PICKUP);
                                return true;
                              })
                          .then(
                              () -> {
                                verifyHotbar(hotbarItem);
                                click(hotbarItem, 0, ClickType.PICKUP);
                                return true;
                              })
                          .then(
                              () -> {
                                throw new RuntimeException();
                              }) // jump to the exception handler
                          .build())
              .orElse(TaskChain.empty());
    }

    // process the next click task
    if (tasks.hasNext()) {
      try {
        Supplier<Boolean> next = tasks.next();

        if (!next.get())
          tasks = TaskChain.<Supplier<Boolean>>builder().then(next).collect(tasks).build();
      } catch (Throwable t) {
        if (!tryPlacingHeldItem()) tasks = TaskChain.singleton(this::tryPlacingHeldItem);
        else tasks = TaskChain.empty();
      }
    }
  }

  //
  //
  //

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
        item.getIndex(),
        usedButtonIn,
        modeIn,
        ret =
            LocalPlayerInventory.getOpenContainer()
                .slotClick(item.getIndex(), usedButtonIn, modeIn, getLocalPlayer()));
    return ret;
  }
}
