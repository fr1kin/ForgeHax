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
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.matt.forgehax.Helper.getLocalPlayer;
import static com.matt.forgehax.Helper.getNetworkManager;

@RegisterMod
public class ExtraInventory extends ToggleMod {
    private static final int GUI_INVENTORY_ID = 0;

    private final Queue<Runnable> inventoryTasks = Queues.newArrayDeque();

    private GuiInventory guiBackground = null;
    private AtomicBoolean guiNeedsClose = new AtomicBoolean(false);
    private boolean guiCloseGuard = false;

    public ExtraInventory() {
        super(Category.PLAYER, "ExtraInventory", false, "Allows one to carry up to 5 extra items in their inventory");
    }

    private boolean isCraftingSlotsAvailable() {
        return getPlayerContainer().isPresent();
    }

    private Queue<Runnable> getSlotSettingTasks(final Queue<Runnable> tasks, Slot source, EasyIndex destination) {
        switch (destination) {
            case HOLDING: {
                final Slot original = copyOfSlot(source); // copy the original slot for later integrity checks
                tasks.offer(() -> {
                    ContainerPlayer container = getPlayerContainer().orElseThrow(ExecutionFailure::new);
                    checkSlotIntegrity(original, source);

                    ItemStack moved = getCurrentContainer().slotClick(source.slotNumber, 0, ClickType.PICKUP, getLocalPlayer());
                    getNetworkManager().sendPacket(newClickPacket(source.slotNumber, 0, ClickType.PICKUP, moved));
                });
                break;
            }
            case CRAFTING_0:
            case CRAFTING_1:
            case CRAFTING_2:
            case CRAFTING_3: {
                getPlayerContainer()
                        .filter(container -> Utils.isInRange(container.inventorySlots, destination.ordinal()))
                        .map(container -> container.inventorySlots.get(destination.ordinal()))
                        .ifPresent(slot -> {
                            final Slot original = copyOfSlot(source); // copy the original slot for later integrity checks
                            tasks.offer(() -> {

                            });
                        });
                break;
            }
            default:
                break;
        }
        return tasks;
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
        inventoryTasks.clear();
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
        return new DuplicateSlot(slot);
    }

    private static void checkContainer() throws ExecutionFailure {
        if(!getPlayerContainer().isPresent()) fail();
    }

    private static void checkSlotIntegrity(Slot s1, Slot s2) throws ExecutionFailure {
        // compare references (yes i realize im doing ItemStack == ItemStack)
        if(s1.inventory != s2.inventory || s1.getSlotIndex() != s2.getSlotIndex() || s1.slotNumber != s2.slotNumber || s1.getStack() != s2.getStack()) fail();
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
