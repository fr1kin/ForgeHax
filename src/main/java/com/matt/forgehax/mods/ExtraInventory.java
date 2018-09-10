package com.matt.forgehax.mods;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Queues;
import com.matt.forgehax.asm.utils.ReflectionHelper;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.entity.LocalPlayerInventory;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketClickWindow;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getNetworkManager;

@RegisterMod
public class ExtraInventory extends ToggleMod {
    private static final int GUI_INVENTORY_ID = 0;

    private TaskChain service = null;

    private GuiInventory guiBackground = null;
    private AtomicBoolean guiNeedsClose = new AtomicBoolean(false);
    private boolean guiCloseGuard = false;

    public ExtraInventory() {
        super(Category.PLAYER, "ExtraInventory", false, "Allows one to carry up to 5 extra items in their inventory");
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

                    ItemStack moved = getCurrentContainer().slotClick(source.slotNumber, 0, ClickType.PICKUP, getLocalPlayer());
                    getNetworkManager().sendPacket(newClickPacket(source.slotNumber, 0, ClickType.PICKUP, moved));

                    return null; // stop task
                };
            case CRAFTING_0:
            case CRAFTING_1:
            case CRAFTING_2:
            case CRAFTING_3:
                return dst == null ? null : () -> {
                    // pick up the item
                    checkContainerIntegrity();
                    checkSlotIntegrity(source, srcCopy);
                    checkSlotIntegrity(dst, dstCopy);

                    ItemStack moved = getCurrentContainer().slotClick(source.slotNumber, 0, ClickType.PICKUP, getLocalPlayer());
                    getNetworkManager().sendPacket(newClickPacket(source.slotNumber, 0, ClickType.PICKUP, moved));

                    final Slot srcCopy2 = copyOfSlot(source); // copy the new source

                    return () -> {
                        // place item in crafting inventory
                        checkContainerIntegrity();
                        checkSlotIntegrity(source, srcCopy2);
                        checkSlotIntegrity(dst, dstCopy);

                        final ItemStack moved2 = getCurrentContainer().slotClick(dst.slotNumber, 0, ClickType.PICKUP, getLocalPlayer());
                        getNetworkManager().sendPacket(newClickPacket(dst.slotNumber, 0, ClickType.PICKUP, moved2));

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
                        .filter(container -> Utils.isInRange(container.inventorySlots, index.ordinal()))// just a coincidence that the ordinal is at the correct index
                        .map(container -> container.inventorySlots.get(index.ordinal()))
                        .orElse(null);
            default:
                return null;
        }
    }

    private ItemStack getItemStack(EasyIndex index) {
        switch (index) {
            case HOLDING:
                return LocalPlayerInventory.getInventory().getItemStack();
            default:
                return Optional.ofNullable(getSlot(index))
                        .map(Slot::getStack)
                        .orElse(ItemStack.EMPTY);
        }
    }

    private void closeGui() {
        if(guiNeedsClose.compareAndSet(true, false)) {
            if(getLocalPlayer() != null) {
                guiCloseGuard = true;
                getLocalPlayer().closeScreen();
                if(guiBackground != null) {
                    guiBackground.onGuiClosed();
                    guiBackground = null;
                }
                guiCloseGuard = false;
            }
        }
    }

    private void reset() {
        service = null;
        guiBackground = null;
        guiNeedsClose.set(false);
        guiCloseGuard = false;
    }

    @Override
    protected void onDisabled() {
        MC.addScheduledTask(() -> {
            closeGui();
            reset();
        });
    }

    @SubscribeEvent
    public void onDisconnectToServer(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        onDisabled();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGuiOpen(GuiOpenEvent event) {
        if(guiCloseGuard) {
            event.setCanceled(true);
        } else if(event.getGui() instanceof GuiInventory) {
            try {
                GuiInventoryWrapper wrapper = new GuiInventoryWrapper();
                ReflectionHelper.copyOf(event.getGui(), wrapper); // copy all fields from the provided gui to the wrapper
                event.setGui(wrapper);
                guiNeedsClose.set(false);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    class GuiInventoryWrapper extends GuiInventory {
        GuiInventoryWrapper() {
            super(getLocalPlayer()); // provide anything that doesn't cause a nullpointer exception, will be overwritten anyway
        }

        @Override
        protected void keyTyped(char typedChar, int keyCode) throws IOException {
            if(isEnabled() && (keyCode == 1 || this.mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode))) {
                guiNeedsClose.set(true);
                MC.displayGuiScreen(null);
            } else
                super.keyTyped(typedChar, keyCode);
        }

        @Override
        public void onGuiClosed() {
            if(guiCloseGuard || !isEnabled()) super.onGuiClosed();
        }
    }

    private static Container getCurrentContainer() {
        return MoreObjects.firstNonNull(LocalPlayerInventory.getOpenContainer(), LocalPlayerInventory.getContainer());
    }

    private static Optional<ContainerPlayer> getPlayerContainer() {
        return Optional.ofNullable(getCurrentContainer())
                .filter(ContainerPlayer.class::isInstance)
                .map(ContainerPlayer.class::cast);
    }

    private static CPacketClickWindow newClickPacket(int slotIdIn, int usedButtonIn, ClickType modeIn, ItemStack clickedItemIn) {
        return new CPacketClickWindow(GUI_INVENTORY_ID, slotIdIn, usedButtonIn, modeIn, clickedItemIn, getCurrentContainer().getNextTransactionID(LocalPlayerInventory.getInventory()));
    }

    private static Slot copyOfSlot(Slot slot) {
        return (slot == null || slot.slotNumber == -999) ? null : new DuplicateSlot(slot);
    }

    private static void checkContainerIntegrity() throws ExecutionFailure {
        if(!getPlayerContainer().isPresent()) fail();
    }

    private static void checkSlotIntegrity(Slot s1, Slot s2) throws ExecutionFailure {
        // compare references (yes i realize im doing ItemStack == ItemStack)
        if(s1 != null && s2 != null && (s1.inventory != s2.inventory || s1.getSlotIndex() != s2.getSlotIndex() || s1.slotNumber != s2.slotNumber || s1.getStack() != s2.getStack())) fail();
    }

    private static void fail() {
        throw new ExecutionFailure();
    }

    private static class ExecutionFailure extends RuntimeException {}

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
         * Item the player is holding
         */
        HOLDING,

        /**
         * Item the player has in the top left crafting slot
         */
        CRAFTING_0,

        /**
         * Item the player has in the bottom left crafting slot
         */
        CRAFTING_1,

        /**
         * Item the player has in the top right crafting slot
         */
        CRAFTING_2,

        /**
         * Item the player has in the bottom right crafting slot
         */
        CRAFTING_3,
        ;
    }
}
