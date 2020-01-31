package dev.fiki.forgehax.main.mods;

import com.google.common.base.MoreObjects;
import dev.fiki.forgehax.common.events.packet.PacketOutboundEvent;
import dev.fiki.forgehax.main.Globals;
import dev.fiki.forgehax.main.events.DisconnectFromServerEvent;
import dev.fiki.forgehax.main.events.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.main.util.command.Setting;
import dev.fiki.forgehax.main.util.entity.LocalPlayerInventory;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.SimpleTimer;
import dev.fiki.forgehax.main.util.Utils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import dev.fiki.forgehax.main.util.reflection.ReflectionHelper;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.*;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@RegisterMod
public class ExtraInventory extends ToggleMod {
  
  private static final int GUI_INVENTORY_ID = 0;
  
  private final Setting<Boolean> auto_store =
      getCommandStub()
          .builders()
          .<Boolean>newSettingBuilder()
          .name("auto-store")
          .description(
              "Automatically store items in the extra inventory slots when the main inventory is full")
          .defaultTo(false)
          .build();
  
  private final Setting<Long> delay =
      getCommandStub()
          .builders()
          .<Long>newSettingBuilder()
          .name("delay")
          .description("Delay between window clicks (in MS)")
          .defaultTo(500L)
          .min(0L)
          .build();
  
  private TaskChain nextClickTask = null;
  
  private InventoryScreen openedGui = null;
  private AtomicBoolean guiNeedsClose = new AtomicBoolean(false);
  private boolean guiCloseGuard = false;
  
  private SimpleTimer clickTimer = new SimpleTimer();
  
  public ExtraInventory() {
    super(
        Category.PLAYER,
        "ExtraInventory",
        false,
        "Allows one to carry up to 5 extra items in their inventory");
  }
  
  private InventoryScreen createGuiWrapper(InventoryScreen gui) {
    try {
      GuiInventoryWrapper wrapper = new GuiInventoryWrapper();
      ReflectionHelper.copyOf(gui, wrapper); // copy all fields from the provided gui to the wrapper
      return wrapper;
    } catch (NoSuchFieldException | IllegalAccessException e) {
      e.printStackTrace();
      return null;
    }
  }
  
  private boolean isCraftingSlotsAvailable() {
    return getPlayerContainer().isPresent();
  }
  
  private TaskChain getSlotSettingTask(Slot source, EasyIndex destination) {
    final Slot dst = getSlot(destination);
    // copy the original slot for later integrity checks
    final Slot dstCopy = copyOfSlot(dst);
    final Slot srcCopy = copyOfSlot(source);
    switch (destination) {
      case HOLDING:
        return () -> {
          // pick up the item
          checkContainerIntegrity();
          checkSlotIntegrity(source, srcCopy);
          
          ItemStack moved =
              getCurrentContainer()
                  .slotClick(source.slotNumber, 0, ClickType.PICKUP, Globals.getLocalPlayer());
          Globals.getNetworkManager()
              .sendPacket(newClickPacket(source.slotNumber, 0, ClickType.PICKUP, moved));
          
          return null; // stop task
        };
      case CRAFTING_0:
      case CRAFTING_1:
      case CRAFTING_2:
      case CRAFTING_3:
        return dst == null
            ? null
            : () -> {
              // pick up the item
              checkContainerIntegrity();
              checkSlotIntegrity(source, srcCopy);
              checkSlotIntegrity(dst, dstCopy);
              
              ItemStack moved =
                  getCurrentContainer()
                      .slotClick(source.slotNumber, 0, ClickType.PICKUP, Globals.getLocalPlayer());
              Globals.getNetworkManager()
                  .sendPacket(newClickPacket(source.slotNumber, 0, ClickType.PICKUP, moved));
              
              final Slot srcCopy2 = copyOfSlot(source); // copy the new source
              
              return () -> {
                // place item in crafting inventory
                checkContainerIntegrity();
                checkSlotIntegrity(source, srcCopy2);
                checkSlotIntegrity(dst, dstCopy);
                
                final ItemStack moved2 =
                    getCurrentContainer()
                        .slotClick(dst.slotNumber, 0, ClickType.PICKUP, Globals.getLocalPlayer());
                Globals.getNetworkManager()
                    .sendPacket(newClickPacket(dst.slotNumber, 0, ClickType.PICKUP, moved2));
                
                return null; // stop task
              };
            };
      default:
        return null;
    }
  }
  
  private Slot getSlot(EasyIndex index) {
    switch (index) {
      case HOLDING:
        return new Slot(LocalPlayerInventory.getInventory(), -999, 0, 0);
      case CRAFTING_0:
      case CRAFTING_1:
      case CRAFTING_2:
      case CRAFTING_3:
        return getPlayerContainer()
            .filter(container -> Utils.isInRange(container.inventorySlots, index.getSlotIndex()))
            .map(container -> container.inventorySlots.get(index.getSlotIndex()))
            .orElse(null);
      case NONE:
      default:
        return null;
    }
  }
  
  private ItemStack getItemStack(EasyIndex index) {
    switch (index) {
      case HOLDING:
        return LocalPlayerInventory.getInventory().getItemStack();
      default:
        return Optional.ofNullable(getSlot(index)).map(Slot::getStack).orElse(ItemStack.EMPTY);
    }
  }
  
  private EasyIndex getAvailableIndex() {
    return EasyIndex.ALL
        .stream()
        .filter(e -> getItemStack(e).isEmpty())
        .min(Comparator.reverseOrder())
        .orElse(EasyIndex.NONE);
  }
  
  private Slot getBestSlotCandidate() {
    return getMainInventory()
        .stream()
        .max(Comparator.comparingInt(ExtraInventory::getItemValue))
        .map(item -> LocalPlayerInventory.getInventory().getSlotFor(item))
        .map(getCurrentContainer()::getSlot)
        .orElse(null);
  }
  
  private void closeGui() {
    if (guiNeedsClose.compareAndSet(true, false)) {
      if (Globals.getLocalPlayer() != null) {
        guiCloseGuard = true;
        Globals.getLocalPlayer().closeScreen();
        if (openedGui != null) {
          openedGui.onClose();
          openedGui = null;
        }
        guiCloseGuard = false;
      }
    }
  }
  
  private void reset() {
    nextClickTask = null;
    openedGui = null;
    guiNeedsClose.set(false);
    guiCloseGuard = false;
    clickTimer.reset();
  }
  
  @Override
  protected void onDisabled() {
    Globals.addScheduledTask(() -> {
      closeGui();
      reset();
    });
  }
  
  @SubscribeEvent
  public void onUpdate(LocalPlayerUpdateEvent event) {
    if (auto_store.get() && (!clickTimer.isStarted() || clickTimer.hasTimeElapsed(delay.get()))) {
      // start a click task if one should be
      if (nextClickTask == null) {
        PlayerInventory inventory = LocalPlayerInventory.getInventory();
        // check if inventory is full
        if (inventory.getFirstEmptyStack() == -1) { // TODO: check only top part of inventory
          // find available slot
          EasyIndex next = getAvailableIndex();
          if (!next.isNone()) {
            // find best slot to replace
            Slot best = getBestSlotCandidate();
            if (best != null) {
              // open and close the gui to create open instance
              
              if (openedGui == null) {
                Globals.setDisplayScreen(new InventoryScreen(Globals.getLocalPlayer()));
                Globals.setDisplayScreen(null);
              }
              
              nextClickTask = getSlotSettingTask(best, next);
            }
          }
        }
      }
      
      if (nextClickTask != null) {
        try {
          // run the task
          nextClickTask = nextClickTask.run();
        } catch (ExecutionFailure e) {
          nextClickTask = null;
        } finally {
          // start timer (this maybe done twice due to the packet hook, but that is okay)
          clickTimer.start();
        }
      }
    }
  }
  
  @SubscribeEvent
  public void onDisconnectToServer(DisconnectFromServerEvent event) {
    onDisabled();
  }
  
  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onGuiOpen(GuiOpenEvent event) {
    if (guiCloseGuard) {
      // do not close the gui when this mod executes closeWindow()
      event.setCanceled(true);
    } else if (event.getGui() instanceof InventoryScreen) {
      // create a wrapper and replace the gui
      event.setGui(openedGui = createGuiWrapper((InventoryScreen) event.getGui()));
      // server doesn't need to be informed the gui has been closed
      guiNeedsClose.set(false);
    }
  }
  
  @SubscribeEvent
  public void onPacketSent(PacketOutboundEvent event) {
    if (event.getPacket() instanceof CClickWindowPacket) {
      clickTimer.start();
    }
  }
  
  class GuiInventoryWrapper extends InventoryScreen {
    GuiInventoryWrapper() {
      super(Globals.getLocalPlayer());
      // provide anything that doesn't cause a nullpointer exception, will be
      // overwritten anyway
    }
    
    @Override
    public boolean charTyped(char typedChar, int keyCode) {
      if (isEnabled() && (keyCode == 1 || Globals.getGameSettings().keyBindInventory
          .isActiveAndMatches(InputMappings.getInputByCode(keyCode, 0)))) {
        guiNeedsClose.set(true);
        Globals.setDisplayScreen(null);
        return true;
      } else {
        return super.charTyped(typedChar, keyCode);
      }
    }

    @Override
    public void onClose() {
      if (guiCloseGuard || !isEnabled()) {
        super.onClose();
      }
    }
  }
  
  private static List<ItemStack> getMainInventory() {
    List<ItemStack> inventory = LocalPlayerInventory.getInventory().mainInventory;
    return inventory.subList(PlayerInventory.getHotbarSize(), inventory.size());
  }
  
  private static int getItemValue(ItemStack stack, boolean loopGuard) {
    Item item = stack.getItem();
    if (stack.isEmpty()) {
      return 0;
    } else if (ItemGroup.COMBAT.equals(item.getGroup())
        || ItemGroup.TOOLS.equals(item.getGroup())
        || ItemGroup.FOOD.equals(item.getGroup())
        || Items.TOTEM_OF_UNDYING.equals(item)) {
      return 100 * stack.getCount(); // very important
    } else if (item instanceof BlockItem && ((BlockItem)(item)).getBlock() instanceof ShulkerBoxBlock) {
      return 5 + (loopGuard ? 0
          : Utils.getShulkerContents(stack)
              .stream()
              .mapToInt(ExtraInventory::getItemValueSafe)
              .sum());
    } else {
      return 5;
    }
  }
  
  private static int getItemValue(ItemStack stack) {
    return getItemValue(stack, false);
  }
  
  private static int getItemValueSafe(ItemStack stack) {
    return getItemValue(stack, true);
  }
  
  private static Container getCurrentContainer() {
    return MoreObjects.firstNonNull(LocalPlayerInventory.getOpenContainer(), LocalPlayerInventory.getContainer());
  }
  
  private static Optional<PlayerContainer> getPlayerContainer() {
    return Optional.ofNullable(getCurrentContainer())
        .filter(PlayerContainer.class::isInstance)
        .map(PlayerContainer.class::cast);
  }
  
  private static CClickWindowPacket newClickPacket(
      int slotIdIn, int usedButtonIn, ClickType modeIn, ItemStack clickedItemIn) {
    return new CClickWindowPacket(
        GUI_INVENTORY_ID,
        slotIdIn,
        usedButtonIn,
        modeIn,
        clickedItemIn,
        getCurrentContainer().getNextTransactionID(LocalPlayerInventory.getInventory()));
  }
  
  private static Slot copyOfSlot(Slot slot) {
    return (slot == null || slot.getSlotIndex() == -999) ? null : new DuplicateSlot(slot);
  }
  
  private static void checkContainerIntegrity() throws ExecutionFailure {
    if (!getPlayerContainer().isPresent()) {
      fail();
    }
  }
  
  private static void checkSlotIntegrity(Slot s1, Slot s2) throws ExecutionFailure {
    // compare references (yes i realize im doing ItemStack == ItemStack)
    if (s1 != null
        && s2 != null
        && (s1.inventory != s2.inventory
        || s1.getSlotIndex() != s2.getSlotIndex()
        || s1.slotNumber != s2.slotNumber
        || s1.getStack() != s2.getStack())) {
      fail();
    }
  }
  
  private static void fail() {
    throw new ExecutionFailure();
  }
  
  private static class ExecutionFailure extends RuntimeException {
  
  }
  
  private static class DuplicateSlot extends Slot {
    
    private final ItemStack stack;
    
    private DuplicateSlot(Slot original) {
      super(original.inventory, original.getSlotIndex(), original.xPos, original.yPos);
      this.slotNumber = original.slotNumber;
      this.stack = original.getStack();
    }
    
    @Override
    public ItemStack getStack() {
      return stack;
    }
  }
  
  private interface TaskChain {
    
    TaskChain run();
  }
  
  enum EasyIndex {
    /**
     * Nothing
     */
    NONE(Integer.MIN_VALUE),
    
    /**
     * Item the player is holding
     */
    HOLDING(-999),
    
    /**
     * Item the player has in the top left crafting slot
     */
    CRAFTING_0(1),
    
    /**
     * Item the player has in the bottom left crafting slot
     */
    CRAFTING_1(2),
    
    /**
     * Item the player has in the top right crafting slot
     */
    CRAFTING_2(3),
    
    /**
     * Item the player has in the bottom right crafting slot
     */
    CRAFTING_3(4),
    ;
    
    final int slotIndex;
    
    EasyIndex(int slotIndex) {
      this.slotIndex = slotIndex;
    }
    
    public int getSlotIndex() {
      return slotIndex;
    }
    
    public boolean isNone() {
      return ordinal() == 0;
    }
    
    static final EnumSet<EasyIndex> ALL =
        Arrays.stream(values())
            .skip(1)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(EasyIndex.class)));
  }
}
