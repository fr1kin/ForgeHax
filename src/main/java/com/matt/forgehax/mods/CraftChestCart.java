package com.matt.forgehax.mods;

import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.List;

public class CraftChestCart extends ToggleMod {
    private Property sleep;

    public CraftChestCart(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    @Override
    public void loadConfig(Configuration configuration) {
        addSettings(
                sleep = configuration.get(getModName(),
                        "sleepTime",
                        75,
                        "Sleep time in ms"
                )
        );
    }
    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if (event.getGui() != null &&
                event.getGui() instanceof GuiInventory) {
            GuiInventoryOverride list = new GuiInventoryOverride(MC.thePlayer, (long)sleep.getInt());
            event.setGui(list);
            list.autoCraftCarts();
        }
    }

    public static class GuiInventoryOverride extends GuiInventory {
        public final static int CRAFTING_SLOT1_INDEX = 1;
        public final static int CRAFTING_SLOT2_INDEX = 2;
        public final static int CRAFTING_SLOT3_INDEX = 3;
        public final static int CRAFTING_SLOT4_INDEX = 4;

        public final static int CRAFTING_SLOT_RESULT_INDEX = 0;

        public final static int START_INDEX = 5;

        private long sleepTime = 0;

        public GuiInventoryOverride(EntityPlayer player, long sleep) {
            super(player);
            sleepTime = sleep;
        }

        @Override
        protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
            super.handleMouseClick(slotIn, slotId, mouseButton, type);
            //System.out.printf("\nslotID:%d\nmouseButton:%d\nclickType:%s\n", slotId, mouseButton, type.name());
        }

        public Container getInventory() {
            return MC.thePlayer.inventoryContainer;
        }

        public InventoryPlayer getPlayerInventory() {
            return MC.thePlayer.inventory;
        }

        public Slot getSlot(int index) {
            return getInventory().getSlot(index);
        }

        public void simulateClick(int id, int mouseButton, ClickType type) {
            this.handleMouseClick(null, id, mouseButton, type);
        }

        public int findItem(@Nullable ItemStack stack) {
            // getInventory().inventorySlots.size() - 1 dont include last slot
            for (int i = START_INDEX; i < getInventory().inventorySlots.size() - 1; i++) {
                Slot slot = getInventory().getSlot(i);
                if (stack == null && !slot.getHasStack())
                    return slot.slotNumber;
                else if (stack != null &&
                        stack.isItemEqual(slot.getStack()) &&
                        slot.getStack().stackSize >= stack.stackSize)
                    return slot.slotNumber;
            }
            return -1;
        }

        public int findEmptySlot() {
            return findItem(null);
        }

        public int findExtraInUsedSlot(ItemStack item) {
            for (int i = START_INDEX; i < getInventory().inventorySlots.size() - 1; i++) {
                Slot slot = getInventory().getSlot(i);
                if (slot.getHasStack() &&
                        item.isItemEqual(slot.getStack()) &&
                        slot.getStack().stackSize < slot.getStack().getMaxStackSize())
                    return slot.slotNumber;
            }
            return -1;
        }

        public void pickupSlot(int id) {
            simulateClick(id, 0, ClickType.PICKUP);
        }

        public void placeSlot(int id) {
            pickupSlot(id);
        }

        public void pickupAllSlot(int id) {
            simulateClick(id, 0, ClickType.PICKUP_ALL);
        }

        public void quickMove(int id) {
            simulateClick(id, 0, ClickType.QUICK_MOVE);
        }

        public void throwInHand() {
            simulateClick(-999, 0, ClickType.THROW);
        }

        public boolean isValidStacks(ItemStack recipeStack, ItemStack stack) {
            return recipeStack.isItemEqual(stack) &&
                    stack.stackSize >= recipeStack.stackSize;
        }

        public boolean fillSlotWithItemFromInv(int id, ItemStack stack) throws Exception {
            int buy = findItem(stack);
            // no more trade
            if (buy == -1)
                return false; // failed to find item
            // pick up item
            pickupSlot(buy);
            sleep();
            // place item in buy slot
            placeSlot(id);
            sleep();
            return true;
        }

        public boolean placeHeldItemIntoInventory() throws Exception {
            ItemStack stack = getPlayerInventory().getItemStack();
            while (stack != null) {
                // look for a slot with same item but not full stack
                int place = findExtraInUsedSlot(stack);
                if (place == -1)
                    place = findEmptySlot(); // if none found, find empty slot
                if (place == -1)
                    return false; // return false if there is no open slots
                placeSlot(place);
                stack = getPlayerInventory().getItemStack();
                sleep();
            }
            return true;
        }

        public void moveStackToInv(int index) throws Exception {
            if (getSlot(index).getHasStack()) {
                pickupSlot(index);
                sleep();
                placeHeldItemIntoInventory();
            }
        }

        public void dropHeldItem() {
            if(getPlayerInventory().getItemStack() != null) {
                throwInHand();
            }
        }

        public boolean hasStackInSlot(int index, ItemStack stack) {
            Slot slot = getSlot(index);
            if(slot.getHasStack()) {
                ItemStack slotStack = slot.getStack();
                return slotStack != null &&
                        slotStack.getItem().equals(stack.getItem()) &&
                        slotStack.stackSize < stack.stackSize;
            } else return false;
        }

        private final static Item CHEST = Item.getItemById(54);
        private final static Item CART = Item.getItemById(328);

        public synchronized void autoCraftCarts() {
            final GuiInventoryOverride INSTANCE = this;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (INSTANCE) {
                        try {
                            // initial wait
                            sleep();
                            ItemStack STACK_CHEST = new ItemStack(CHEST, 1);
                            ItemStack STACK_CART = new ItemStack(CART);
                            while (true) {
                                // get chests
                                if(!getSlot(CRAFTING_SLOT1_INDEX).getHasStack()) {
                                    if(!fillSlotWithItemFromInv(CRAFTING_SLOT1_INDEX, STACK_CHEST))
                                        break;
                                }
                                if(!getSlot(CRAFTING_SLOT3_INDEX).getHasStack()) {
                                    if(!fillSlotWithItemFromInv(CRAFTING_SLOT3_INDEX, STACK_CART))
                                        break;
                                }
                                // now pick up the result and put into inv
                                moveStackToInv(CRAFTING_SLOT_RESULT_INDEX);
                                sleep();
                            }
                            // finish
                            moveStackToInv(CRAFTING_SLOT1_INDEX);
                            sleep();
                            moveStackToInv(CRAFTING_SLOT3_INDEX);
                            sleep();
                            dropHeldItem();
                        } catch (Exception e) {
                            MOD.printStackTrace(e);
                        }
                    }
                }
            }).start();
        }

        private void sleep() throws Exception {
            Thread.sleep(sleepTime);
        }
    }
}
