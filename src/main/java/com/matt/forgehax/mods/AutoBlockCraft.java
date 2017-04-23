package com.matt.forgehax.mods;

import com.matt.forgehax.util.Utils;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;

public class AutoBlockCraft extends ToggleMod {
    public Property blockToCraft;
    public Property sleepTime;

    public enum CraftableBlocks {
        GOLD_BLOCK("minecraft:gold_ingot"),
        DIAMOND_BLOCK("minecraft:diamond"),
        IRON_BLOCK("minecraft:iron_ingot"),
        // TODO: lapis
        EMERALD_BLOCK("minecraft:emerald"),
        COAL_BLOCK("minecraft:coal"),
        REDSTONE_BLOCK("minecraft:redstone"),
        HAYBALE_BLOCK("minecraft:wheat"),
        SLIME_BLOCK("minecraft:slime_ball"),
        MELON_BLOCK("minecraft:melon"),
        GOLD_INGOT("minecraft:gold_nugget");

        private final ResourceLocation recipeBlock;

        CraftableBlocks(String recipe) {
            recipeBlock = new ResourceLocation(recipe);
        }

        public ResourceLocation getRecipeBlock() {
            return recipeBlock;
        }
    }

    public AutoBlockCraft() {
        super("AutoBlockCraft", false, "Automatically crafts blocks for you");
    }

    public CraftableBlocks getSelectedOption() {
        for(CraftableBlocks block : CraftableBlocks.values())
            if(blockToCraft.getString().equals(block.name()))
                return block;
        return null;
    }

    @Override
    public void loadConfig(Configuration configuration) {
        addSettings(
                blockToCraft = configuration.get(getModName(),
                        "crafting_block",
                        CraftableBlocks.GOLD_BLOCK.name(),
                        "Block to craft",
                        Utils.toArray(CraftableBlocks.values())
                ),
                sleepTime = configuration.get(getModName(),
                        "sleep_delay",
                        100,
                        "Time between clicks in ms"
                )
        );
    }

    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        if(event.getGui() instanceof GuiCrafting) {
            try {
                GuiCraftingOverride override = new GuiCraftingOverride(MC.player.inventory, MC.world, getSelectedOption().getRecipeBlock(), sleepTime.getInt());
                event.setGui(override);
            } catch (Exception e) {
                MOD.printStackTrace(e);
            }
        }
    }

    private static class GuiCraftingOverride extends GuiCrafting {
        private long sleepTime;
        private ResourceLocation toCraft;

        public GuiCraftingOverride(InventoryPlayer playerInv, World worldIn, ResourceLocation blockToCraft, int sleepTime) {
            super(playerInv, worldIn);
            toCraft = blockToCraft;
            this.sleepTime = sleepTime;
            autoCraft();
        }

        public final static int CRAFTING_RESULT_SLOT = 0;

        /**
         * slots 1-9 are the crafting slots
         */

        public final static int START_INDEX = 10;

        @Override
        protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
            super.handleMouseClick(slotIn, slotId, mouseButton, type);
            //System.out.printf("slotID: %d, mouseButton: %d, type: %s\n", slotId, mouseButton, type.name());
        }

        public Container getInventory() {
            return this.inventorySlots;
        }

        public InventoryPlayer getPlayerInventory() {
            return MC.player.inventory;
        }

        public Slot getSlot(int index) {
            return getInventory().getSlot(index);
        }

        public void simulateClick(int id, int mouseButton, ClickType type) {
            this.handleMouseClick(null, id, mouseButton, type);
        }

        public int findItem(@Nullable ItemStack stack, int startIndex) {
            // getInventory().inventorySlots.size() - 1 dont include last slot
            for (int i = startIndex; i < getInventory().inventorySlots.size(); i++) {
                Slot slot = getInventory().getSlot(i);
                if (stack == null && !slot.getHasStack())
                    return slot.slotNumber;
                else if (stack != null &&
                        stack.isItemEqual(slot.getStack()) &&
                        slot.getStack().getCount() >= stack.getCount())
                    return slot.slotNumber;
            }
            return -1;
        }

        public int findItem(@Nullable ItemStack stack) {
            return findItem(stack, START_INDEX);
        }

        public int findEmptySlot() {
            return findItem(null);
        }

        public int findExtraInUsedSlot(ItemStack item) {
            for (int i = START_INDEX; i < getInventory().inventorySlots.size(); i++) {
                Slot slot = getInventory().getSlot(i);
                if (slot.getHasStack() &&
                        item.isItemEqual(slot.getStack()) &&
                        slot.getStack().getCount() < slot.getStack().getMaxStackSize())
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
                    stack.getCount() >= recipeStack.getCount();
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
                        slotStack.getCount() < stack.getCount();
            } else return false;
        }

        public synchronized void autoCraft() {
            try {
                // finalized instance of this class
                final GuiCraftingOverride INSTANCE = this;
                // main recipe item required to craft the block selected
                final Item itemToCraft = Item.getByNameOrId(toCraft.toString());
                // stack
                final ItemStack recipeStack = new ItemStack(itemToCraft, 64);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            loop: while (INSTANCE.equals(MC.currentScreen)) {
                                sleep();
                                // check to make sure there are atleast 9 stacks of the item
                                int nextStartIndex = 1;
                                for (int i = 0; i < 9; i++) {
                                    int slotFound = findItem(recipeStack, nextStartIndex);
                                    if (slotFound != -1) {
                                        nextStartIndex = slotFound + 1;
                                    } else break loop;
                                }
                                // move 9 stacks into crafting table
                                for(int slotIndex = 1; slotIndex <= 9; slotIndex++) {
                                    if(!getSlot(slotIndex).getHasStack())
                                        if(!fillSlotWithItemFromInv(slotIndex, recipeStack))
                                            break;
                                }
                                // move result stack into inv
                                quickMove(CRAFTING_RESULT_SLOT);
                                sleep();
                            }
                        } catch (Exception e) {
                            MOD.printStackTrace(e);
                        }
                    }
                }).start();
            } catch (Exception e) {
                MOD.printStackTrace(e);
            }
        }

        private void sleep() throws Exception {
            Thread.sleep(sleepTime);
        }
    }
}
