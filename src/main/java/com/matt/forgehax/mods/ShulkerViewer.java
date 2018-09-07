package com.matt.forgehax.mods;

import com.google.common.collect.Lists;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.draw.SurfaceHelper;
import com.matt.forgehax.util.entity.LocalPlayerInventory;
import com.matt.forgehax.util.gui.events.GuiKeyEvent;
import com.matt.forgehax.util.gui.events.GuiMouseEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RegisterMod
public class ShulkerViewer extends ToggleMod {
    private static final ResourceLocation SHULKER_GUI_TEXTURE = new ResourceLocation("textures/gui/container/shulker_box.png");
    private static final int SHULKER_VIEWER_SIZE = 16 + 54 + 6;
    private static final int CACHE_SIZE = 2;

    private final List<GuiShulkerViewer> guiCache = Lists.newArrayListWithExpectedSize(CACHE_SIZE);
    private final Lock cacheLock = new ReentrantLock();

    private boolean locked = false;
    private boolean updated = false;

    private int lastX = -1;
    private int lastY = -1;

    public ShulkerViewer() {
        super(Category.RENDER, "ShulkerViewer", false, "View the contents of a shulker box. Hold LSHIFT to keep the gui in place and view the tooltips of its contents");
    }

    private boolean isLocked() {
        return locked && updated;
    }

    private Optional<GuiShulkerViewer> lookupGui(int index) {
        return index > 0 && index < guiCache.size() ? Optional.ofNullable(guiCache.get(index)) : Optional.empty();
    }

    private List<ItemStack> getShulkerContents(ItemStack stack) {
        NonNullList<ItemStack> contents = NonNullList.withSize(27, ItemStack.EMPTY);

        NBTTagCompound compound = stack.getTagCompound();
        if(compound != null && compound.hasKey("BlockEntityTag", 10)) {
            NBTTagCompound tags = compound.getCompoundTag("BlockEntityTag");
            if (tags.hasKey("Items", 9)) {
                // load in the items
                ItemStackHelper.loadAllItems(tags, contents);
            }
        }
        return contents;
    }

    private GuiShulkerViewer newShulkerGui(ItemStack parentShulker) {
        List<ItemStack> contents = getShulkerContents(parentShulker);
        ShulkerInventory inventory = new ShulkerInventory(contents);
        ShulkerContainer container = new ShulkerContainer(inventory, contents);
        return new GuiShulkerViewer(container, parentShulker);
    }

    @Override
    protected void onEnabled() {
        locked = updated = false;
        lastX = lastY = -1;
        cacheLock.lock();
        try {
            while(guiCache.size() < CACHE_SIZE) guiCache.add(null);
            for(int i = 0; i < CACHE_SIZE; ++i) guiCache.set(i, null);
        } finally {
            cacheLock.unlock();
        }
    }

    @Override
    protected void onDisabled() {
        onEnabled();
    }

    @SubscribeEvent
    public void onKeyboardInput(GuiScreenEvent.KeyboardInputEvent event) {
        if(Keyboard.getEventKey() == Keyboard.KEY_LSHIFT) {
            if(Keyboard.getEventKeyState())
                locked = true;
            else
                locked = updated = false;
        }
    }

    @SubscribeEvent
    public void onPreTooptipRender(RenderTooltipEvent.Pre event) {
        if(event.getStack().getItem() instanceof ItemShulkerBox && MC.currentScreen instanceof GuiContainer)
            event.setCanceled(true); // do not draw normal tool tip
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRender(GuiScreenEvent.DrawScreenEvent.Post event) {
        if(!(MC.currentScreen instanceof GuiContainer))
            return;

        GuiContainer gui = (GuiContainer)event.getGui();

        cacheLock.lock();
        try {
            if (!isLocked()) {
                // show stats for the item being hovered over
                Slot slotUnder = gui.getSlotUnderMouse();
                if(slotUnder == null || !slotUnder.getHasStack() || slotUnder.getStack().isEmpty())
                    guiCache.set(0, null);
                else if (slotUnder.getStack().getItem() instanceof ItemShulkerBox && !ItemStack.areItemStacksEqual(lookupGui(0).map(GuiShulkerViewer::getParentShulker).orElse(ItemStack.EMPTY), slotUnder.getStack())) {
                    guiCache.set(0, newShulkerGui(slotUnder.getStack()));
                }

                // show stats for held item
                ItemStack stackHeld = LocalPlayerInventory.getPlayerInventory().getItemStack();
                if(stackHeld.isEmpty())
                    guiCache.set(1, null);
                if (stackHeld.getItem() instanceof ItemShulkerBox && !ItemStack.areItemStacksEqual(lookupGui(1).map(GuiShulkerViewer::getParentShulker).orElse(ItemStack.EMPTY), stackHeld)) {
                    guiCache.set(1, newShulkerGui(stackHeld));
                }

                if(locked && !updated) {
                    updated = true;
                }
            }

            int count = (int)guiCache.stream().filter(Objects::nonNull).count();

            AtomicInteger renderX;
            AtomicInteger renderY;
            if(!isLocked() || (lastX == -1 && lastY == -1)) {
                renderX = new AtomicInteger(lastX = event.getMouseX() + 8);
                renderY = new AtomicInteger(lastY = event.getMouseY() - (SHULKER_VIEWER_SIZE * count) / 2);
            } else {
                renderX = new AtomicInteger(lastX);
                renderY = new AtomicInteger(lastY);
            }

            guiCache.stream()
                    .filter(Objects::nonNull)
                    .sorted()
                    .forEach(ui -> {
                        ui.offsetX = renderX.get();
                        ui.offsetY = renderY.get();
                        ui.drawScreen(event.getMouseX(), event.getMouseY(), event.getRenderPartialTicks());
                        renderY.set(renderY.get() + SHULKER_VIEWER_SIZE + 1);
                    });
        } finally {
            cacheLock.unlock();
        }
    }

    class GuiShulkerViewer extends GuiContainer implements Comparable<GuiShulkerViewer> {
        private final ItemStack parentShulker;
        private final long createdTime;

        private int offsetX = 0;
        private int offsetY = 0;

        public GuiShulkerViewer(Container inventorySlotsIn, ItemStack parentShulker) {
            super(inventorySlotsIn);
            this.parentShulker = parentShulker;
            this.createdTime = System.currentTimeMillis();
            this.mc = MC;
            this.fontRenderer = MC.fontRenderer;
            this.width = MC.displayWidth;
            this.height = MC.displayHeight;
        }

        public ItemStack getParentShulker() {
            return parentShulker;
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            final int DEPTH = 500;

            int x = offsetX;
            int y = offsetY;

            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.color(1.f, 1.f, 1.f, !isLocked() ? 0.85f : 1.f);

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

            MC.getTextureManager().bindTexture(SHULKER_GUI_TEXTURE);

            // width 176        = width of container
            // height 16        = top of the gui
            // height 54        = gui item boxes
            // height 6         = bottom of the gui
            SurfaceHelper.drawTexturedRect(x, y, 0,0, 176, 16, DEPTH);
            SurfaceHelper.drawTexturedRect(x, y + 16, 0,16, 176, 54, DEPTH);
            SurfaceHelper.drawTexturedRect(x, y + 16 + 54, 0,160, 176, 6, DEPTH);

            GlStateManager.disableDepth();
            SurfaceHelper.drawText(parentShulker.getDisplayName(), x + 8, y + 6, Colors.BLACK.toBuffer());
            GlStateManager.enableDepth();

            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.enableRescaleNormal();
            GlStateManager.enableColorMaterial();
            GlStateManager.enableLighting();

            Slot hoveringOver = null;

            int rx = x + 8;
            int ry = y - 1;

            for(Slot slot : inventorySlots.inventorySlots) {
                if(slot.getHasStack()) {
                    int px = rx + slot.xPos;
                    int py = ry + slot.yPos;
                    if(isPointInRegion(px, py, 16, 16, mouseX, mouseY)) {
                        hoveringOver = slot;
                        GlStateManager.disableLighting();
                        GlStateManager.disableDepth();
                        GlStateManager.colorMask(true, true, true, false);
                        this.drawGradientRect(px, py, px + 16, py + 16, -2130706433, -2130706433);
                        GlStateManager.colorMask(true, true, true, true);
                        GlStateManager.enableLighting();
                        GlStateManager.enableDepth();
                    }
                    MC.getRenderItem().zLevel = DEPTH + 1;
                    SurfaceHelper.drawItem(slot.getStack(), px, py);
                    SurfaceHelper.drawItemOverlay(slot.getStack(), px, py);
                    MC.getRenderItem().zLevel = 0.f;
                }
            }

            GlStateManager.disableLighting();

            if(hoveringOver != null) {
                GlStateManager.color(1.f, 1.f, 1.f, 1.0f);
                GlStateManager.disableDepth();
                GlStateManager.pushMatrix();
                renderToolTip(hoveringOver.getStack(), mouseX + 7, mouseY + 7);
                GlStateManager.popMatrix();
                GlStateManager.enableDepth();
            }

            GlStateManager.disableBlend();
            GlStateManager.color(1.f, 1.f, 1.f, 1.0f);
        }

        @Override
        protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) { }

        @Override
        public int compareTo(GuiShulkerViewer o) {
            return Long.compare(createdTime, o.createdTime);
        }
    }

    static class ShulkerContainer extends Container {
        public ShulkerContainer(ShulkerInventory inventory, List<ItemStack> contents) {
            for(int i = 0; i < contents.size(); ++i) {
                int x = i % 9 * 18;
                int y = ((i / 9 + 1) * 18) + 1;
                addSlotToContainer(new Slot(inventory, i, x, y));
            }
        }

        @Override
        public boolean canInteractWith(EntityPlayer playerIn) {
            return false;
        }
    }

    static class ShulkerInventory implements IInventory {
        private final List<ItemStack> contents;

        public ShulkerInventory(List<ItemStack> contents) {
            this.contents = contents;
        }

        @Override
        public int getSizeInventory() {
            return contents.size();
        }

        @Override
        public boolean isEmpty() {
            return contents.isEmpty();
        }

        @Override
        public ItemStack getStackInSlot(int index) {
            return contents.get(index);
        }

        @Override
        public ItemStack decrStackSize(int index, int count) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ItemStack removeStackFromSlot(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setInventorySlotContents(int index, ItemStack stack) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getInventoryStackLimit() {
            return 27;
        }

        @Override
        public void markDirty() {}

        @Override
        public boolean isUsableByPlayer(EntityPlayer player) {
            return false;
        }

        @Override
        public void openInventory(EntityPlayer player) {}

        @Override
        public void closeInventory(EntityPlayer player) {}

        @Override
        public boolean isItemValidForSlot(int index, ItemStack stack) {
            return index > 0 && index < contents.size() && contents.get(index).equals(stack);
        }

        @Override
        public int getField(int id) {
            return 0;
        }

        @Override
        public void setField(int id, int value) {}

        @Override
        public int getFieldCount() {
            return 0;
        }

        @Override
        public void clear() {}

        @Override
        public String getName() {
            return "";
        }

        @Override
        public boolean hasCustomName() {
            return false;
        }

        @Override
        public ITextComponent getDisplayName() {
            return new TextComponentString("");
        }
    }
}
