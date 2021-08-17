package dev.fiki.forgehax.main.mods.ui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.api.cmd.settings.KeyBindingSetting;
import dev.fiki.forgehax.api.color.Color;
import dev.fiki.forgehax.api.color.Colors;
import dev.fiki.forgehax.api.common.PriorityEnum;
import dev.fiki.forgehax.api.draw.SurfaceHelper;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.render.GuiChangedEvent;
import dev.fiki.forgehax.api.events.render.GuiRenderEvent;
import dev.fiki.forgehax.api.events.render.TooltipRenderEvent;
import dev.fiki.forgehax.api.extension.ItemEx;
import dev.fiki.forgehax.api.extension.LocalPlayerEx;
import dev.fiki.forgehax.api.extension.VertexBuilderEx;
import dev.fiki.forgehax.api.key.KeyConflictContexts;
import dev.fiki.forgehax.api.key.KeyInputs;
import dev.fiki.forgehax.api.mock.EmptyPlayerInventory;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import lombok.experimental.ExtensionMethod;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static dev.fiki.forgehax.main.Common.*;

@RegisterMod(
    name = "ShulkerViewer",
    description = "View the contents of a shulker box",
    category = Category.UI
)
@ExtensionMethod({ItemEx.class, LocalPlayerEx.class, VertexBuilderEx.class})
public class ShulkerViewer extends ToggleMod {

  private static final ResourceLocation SHULKER_GUI_TEXTURE =
      new ResourceLocation("textures/gui/container/shulker_box.png");
  private static final int SHULKER_GUI_SIZE = 16 + 54 + 6;

  private static final int CACHE_HOVERING_INDEX = 0;
  private static final int CACHE_HOLDING_INDEX = 1;
  private static final int CACHE_RESERVE_SIZE = 2;

  private final BooleanSetting help_text = newBooleanSetting()
      .name("help-text")
      .description("Text to inform new users that the shulker contents can be viewed by holding a key down")
      .defaultTo(true)
      .build();

  private final BooleanSetting toggle_lock = newBooleanSetting()
      .name("toggle-lock")
      .description("GUI will remain locked in place until key is pressed again")
      .defaultTo(false)
      .build();

  private final IntegerSetting tooltip_opacity = newIntegerSetting()
      .name("tooltip-opacity")
      .description("Opacity of the Shulker GUI when displaying")
      .defaultTo(200)
      .min(0)
      .max(255)
      .build();

  private final IntegerSetting locked_opacity = newIntegerSetting()
      .name("locked-opacity")
      .description("Opacity of the Shulker GUI when locked in place")
      .defaultTo(255)
      .min(0)
      .max(255)
      .build();

  private final IntegerSetting x_offset = newIntegerSetting()
      .name("x-offset")
      .description("X Offset for the tool tip")
      .defaultTo(8)
      .build();

  private final IntegerSetting y_offset = newIntegerSetting()
      .name("y-offset")
      .description("Y Offset for the tool tip")
      .defaultTo(0)
      .build();

  private final KeyBindingSetting lockDownKey = newKeyBindingSetting()
      .name("hold-bind")
      .description("Bind for holding down the shulker view tooltip")
      .keyName("Hold")
      .defaultKeyCategory()
      .key(KeyInputs.KEY_LEFT_ALT)
      .keyPressedListener(this::onLockPressed)
      .keyReleasedListener(this::onLockReleased)
      .conflictContext(KeyConflictContexts.inContainerGui())
      .build();

  private final List<GuiShulkerViewer> guiCache =
      Lists.newArrayListWithExpectedSize(CACHE_RESERVE_SIZE);
  private final Lock cacheLock = new ReentrantLock();

  private boolean locked = false;
  private boolean updated = false;

  private boolean isKeySet = false;

  private boolean isMouseInShulkerGui = false;
  private boolean isModGeneratedToolTip = false;

  private int lastX = -1;
  private int lastY = -1;

  private boolean isLocked() {
    return locked && updated;
  }

  private boolean setInCache(int index, @Nullable GuiShulkerViewer viewer) {
    if (index < 0) {
      return false;
    } else if (viewer == null && index > (CACHE_RESERVE_SIZE - 1) && index == guiCache.size() - 1) {
      guiCache.remove(index); // remove non-reserved extras
      int previous = index - 1;
      if (previous > (CACHE_RESERVE_SIZE - 1)
          && !getInCache(previous)
          .isPresent()) // check if previous entry is null and remove it recursively if it is
      {
        return setInCache(previous, null);
      } else {
        return true;
      }
    } else if (index > guiCache.size() - 1) { // array not big enough
      for (int i = Math.max(guiCache.size(), 1); i < index; ++i) {
        guiCache.add(i, null); // fill with nulls up to the index
      }
      guiCache.add(index, viewer);
      return true;
    } else {
      guiCache.set(index, viewer);
      return true;
    }
  }

  private boolean appendInCache(@Nonnull GuiShulkerViewer viewer) {
    return setInCache(Math.max(guiCache.size() - 1, CACHE_RESERVE_SIZE), viewer);
  }

  private Optional<GuiShulkerViewer> getInCache(int index) {
    return Optional.ofNullable(guiCache.get(index));
  }

  private void clearCache() {
    for (int i = 0; i < CACHE_RESERVE_SIZE; ++i) {
      setInCache(
          i, null); // set all reserve slots to null, and add them if they don't already exist
    }
    while (guiCache.size() > CACHE_RESERVE_SIZE) {
      setInCache(guiCache.size() - 1, null); // clear the rest
    }
  }

  private void reset() {
    locked = updated = isKeySet = isMouseInShulkerGui = isModGeneratedToolTip = false;
    lastX = lastY = -1;
    clearCache();
  }

  private GuiShulkerViewer newShulkerGui(ItemStack parentShulker, int priority) {
    return new GuiShulkerViewer(
        new ShulkerContainer(new ShulkerInventory(parentShulker.getShulkerContents()), 27),
        parentShulker,
        priority);
  }

  private boolean isInRegion(int x, int y, int width, int height, int testingX, int testingY) {
    return testingX >= x && testingY >= y && testingX <= x + width && testingY <= y + height;
  }

  private boolean isItemShulkerBox(Item item) {
    return Optional.of(item)
        .filter(BlockItem.class::isInstance)
        .map(BlockItem.class::cast)
        .map(BlockItem::getBlock)
        .filter(ShulkerBoxBlock.class::isInstance)
        .isPresent();

  }

  @Override
  protected void onEnabled() {
    cacheLock.lock();
    try {
      reset();
    } finally {
      cacheLock.unlock();
    }
  }

  @Override
  protected void onDisabled() {
    onEnabled();
  }

  @Override
  public String getDebugDisplayText() {
    return super.getDebugDisplayText() + " " + String.format("[size = %d]", guiCache.size());
  }

  private void onLockPressed(KeyBinding binding) {
    if (toggle_lock.getValue()) {
      if (!isKeySet) {
        locked = !locked;
        if (!locked) {
          updated = false;
        }
        isKeySet = true;
      }
    } else {
      locked = true;
    }
  }

  private void onLockReleased(KeyBinding binding) {
    if (toggle_lock.getValue()) {
      isKeySet = false;
    } else {
      locked = updated = false;
    }
  }

  @SubscribeListener
  public void onPreTooptipRender(TooltipRenderEvent.Pre event) {
    if (!(getDisplayScreen() instanceof ContainerScreen) || isModGeneratedToolTip) {
      return;
    }

    if (isMouseInShulkerGui) {
      // do not render tool tips that are inside the region of our shulker gui
      event.setCanceled(true);
    } else if (isItemShulkerBox(event.getItemStack().getItem())) {
      event.setCanceled(true); // do not draw normal tool tip
    }
  }

  @SubscribeListener
  public void onGuiChanged(GuiChangedEvent event) {
    if (event.getGui() == null) {
      reset();
    }
  }

  @SubscribeListener(priority = PriorityEnum.LOWEST)
  public void onRender(GuiRenderEvent.Post event) {
    if (!(getDisplayScreen() instanceof ContainerScreen)) {
      return;
    }

    cacheLock.lock();
    try {
      ContainerScreen gui = (ContainerScreen) event.getGui();

      if (!isLocked()) {
        // show stats for the item being hovered over
        Slot slotUnder = gui.getSlotUnderMouse();
        if (slotUnder == null
            || !slotUnder.hasItem()
            || slotUnder.getItem().isEmpty()
            // TODO: 1.15 detect if item is a shulkerbox
            || !isItemShulkerBox(slotUnder.getItem().getItem())) {
          setInCache(CACHE_HOVERING_INDEX, null);
        } else if (!ItemStack.isSame(
            getInCache(0).map(GuiShulkerViewer::getParentShulker).orElse(ItemStack.EMPTY),
            slotUnder.getItem())) {
          setInCache(CACHE_HOVERING_INDEX, newShulkerGui(slotUnder.getItem(), 1));
        }

        // show stats for held item
        ItemStack stackHeld = getLocalPlayer().getMouseHeldItem();
        if (stackHeld.isEmpty() || !isItemShulkerBox(stackHeld.getItem())) {
          setInCache(CACHE_HOLDING_INDEX, null);
        } else if (!ItemStack.isSame(
            getInCache(1).map(GuiShulkerViewer::getParentShulker).orElse(ItemStack.EMPTY),
            stackHeld)) {
          setInCache(CACHE_HOLDING_INDEX, newShulkerGui(stackHeld, 0));
        }

        if (locked && !updated && guiCache.stream().anyMatch(Objects::nonNull)) {
          updated = true;
        }
      }

      int offsetX, offsetY;
      if (!isLocked() || (lastX == -1 && lastY == -1)) {
        int count = (int) guiCache.stream().filter(Objects::nonNull).count();
        offsetX = lastX = event.getMouseX() + x_offset.getValue();
        offsetY = lastY = event.getMouseY() - (SHULKER_GUI_SIZE * count) / 2 + y_offset.getValue();
      } else {
        offsetX = lastX;
        offsetY = lastY;
      }

      isMouseInShulkerGui = false; // recheck

      for (GuiShulkerViewer ui : guiCache) {
        if (ui != null) {
          ui.posX = offsetX;
          ui.posY = offsetY;
          ui.render(event.getStack(), event.getMouseX(), event.getMouseY(), event.getRenderPartialTicks());
          offsetY += SHULKER_GUI_SIZE + 1;
        }
      }
    } finally {
      cacheLock.unlock();
    }

    if (help_text.getValue()) {
      RenderSystem.disableLighting();
      RenderSystem.disableDepthTest();

      SurfaceHelper.drawTextShadow(
          "Hold "
              + lockDownKey.getKeyName()
              + " to view the tooltips of a Shulker boxes content!",
          5,
          getScreenHeight() - (int) (SurfaceHelper.getStringHeight() + 2) * 3 - 2,
          Colors.RED.toBuffer(),
          1);
      SurfaceHelper.drawTextShadow(
          "The activation key can be configured under Minecraft's Options -> Controls -> ForgeHax -> ShulkerViewer Lock.",
          5,
          getScreenHeight() - (int) (SurfaceHelper.getStringHeight() + 2) * 2 - 2,
          Colors.GREEN.toBuffer(),
          1);
      SurfaceHelper.drawTextShadow(
          "Type in console \""
              + getName()
              + "\" for more options, and \""
              + getName()
              + " "
              + help_text.getName()
              + " false\" to disable this help message.",
          5,
          getScreenHeight() - (int) (SurfaceHelper.getStringHeight() + 2) - 2,
          Colors.YELLOW.toBuffer(),
          1);

      RenderSystem.enableDepthTest();
    }

    RenderSystem.enableLighting();
    RenderSystem.color4f(1.f, 1.f, 1.f, 1.0f);
  }

  class GuiShulkerViewer extends ContainerScreen<Container> implements Comparable<GuiShulkerViewer> {
    private static final int Z_DEPTH = 500;
    private final ItemStack parentShulker;
    private final int priority;

    public int posX = 0;
    public int posY = 0;

    public GuiShulkerViewer(Container inventorySlotsIn, ItemStack parentShulker, int priority) {
      super(inventorySlotsIn, new EmptyPlayerInventory(), new StringTextComponent("ShulkerViewer"));
      this.minecraft = MC;
      this.font = getFontRenderer();
      this.parentShulker = parentShulker;
      this.priority = priority;
      this.width = getScreenWidth();
      this.height = getScreenHeight();
      this.imageWidth = 176;
      this.imageHeight = SHULKER_GUI_SIZE;
    }

    public ItemStack getParentShulker() {
      return parentShulker;
    }

    public int getPosX() {
      return posX;
    }

    public int getPosY() {
      return posY;
    }

    public int getWidth() {
      return imageWidth;
    }

    public int getHeight() {
      return imageHeight;
    }

    @Override
    public void renderBackground(MatrixStack stack) {
      final BufferBuilder buffer = Tessellator.getInstance().getBuilder();

      buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
      {
        // width 176        = width of container
        // height 16        = top of the gui
        // height 54        = gui item boxes
        // height 6         = bottom of the gui
        buffer.texturedModalRect(0, 0, 0, 0, 176, 16, 0, stack.getLastMatrix());
        buffer.texturedModalRect(0, 16, 0, 16, 176, 54, 0, stack.getLastMatrix());
        buffer.texturedModalRect(0, 16 + 54, 0, 160, 176, 6, 0, stack.getLastMatrix());
      }

      MC.getTextureManager().bind(SHULKER_GUI_TEXTURE);

      RenderSystem.enableTexture();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();

      Colors.WHITE.setAlpha(!isLocked() ? tooltip_opacity.intValue() : locked_opacity.intValue())
          .glSetColor4f();

      buffer.draw();

      RenderSystem.disableTexture();
      RenderSystem.disableBlend();

      SurfaceHelper.renderString(buffer, stack.last().pose(),
          parentShulker.getDisplayName().getString(), 8.f, 6.f, Colors.BLACK, false).endBatch();
    }

    @Override
    protected void renderBg(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
      final BufferBuilder buffer = Tessellator.getInstance().getBuilder();
      final IRenderTypeBuffer.Impl buffers = MC.renderBuffers().bufferSource();

      stack.pushPose();
      stack.translate(8, -1, 0.f);

      for (Slot slot : menu.slots) {
        if (slot.hasItem()) {
          stack.pushPose();
          stack.translate(slot.x, slot.y, 50);

          if (isInRegion(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
            stack.pushPose();
            stack.translate(0.f, 0.f, -5.f);

            hoveredSlot = slot;

            RenderSystem.disableLighting();
            RenderSystem.disableDepthTest();
            RenderSystem.colorMask(true, true, true, false);

            Color col = Colors.WHITE.setAlpha(200);
            buffer.gradientRect(0, 0, 16, 16, col, col, stack.getLastMatrix());

//            GuiUtils.drawGradientRect(stack.last().pose(),
//                0, 0, 0, 16, 16,
//                Colors.WHITE.setAlpha(200).toBuffer(),
//                Colors.WHITE.setAlpha(200).toBuffer());

            RenderSystem.enableLighting();
            RenderSystem.enableDepthTest();

            stack.popPose();
          }

          ItemStack itemStack = slot.getItem();
          if (!SurfaceHelper.renderItemInGui(itemStack, stack, buffers)) {
            RenderHelper.setupForFlatItems();
          }

          buffers.endBatch();
          RenderHelper.setupFor3DItems();

          SurfaceHelper.renderItemOverlay(buffer, stack, font, itemStack, 0, 0, null);

          stack.popPose();
        }
      }

      stack.popPose();
    }

    @Override
    protected void renderTooltip(MatrixStack stack, int x, int y) {
      stack.pushPose();
      stack.translate(-posX, -posY, 100);

      isModGeneratedToolTip = true;
      try {
        Colors.WHITE.glSetColor4f();
        super.renderTooltip(stack, x, y);
      } finally {
        isModGeneratedToolTip = false;
      }

      stack.popPose();
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
      stack.pushPose();
      stack.translate(posX, posY, Z_DEPTH);

      this.leftPos = posX + 8;
      this.topPos = posY - 1;

      renderBackground(stack);

      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
          new net.minecraftforge.client.event.GuiContainerEvent.DrawBackground(this, stack, mouseX, mouseY));

      renderBg(stack, partialTicks, mouseX, mouseY);

      net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(
          new net.minecraftforge.client.event.GuiContainerEvent.DrawForeground(this, stack, mouseX, mouseY));

      renderTooltip(stack, mouseX + 8, mouseY + 8);

      stack.popPose();

      if (isInRegion(this.posX, this.posY, getWidth(), getHeight(), mouseX, mouseY)) {
        isMouseInShulkerGui = true;
      }

      RenderSystem.disableBlend();
      RenderSystem.color4f(1.f, 1.f, 1.f, 1.f);
    }

    @Override
    public int compareTo(GuiShulkerViewer o) {
      return Integer.compare(priority, o.priority);
    }
  }

  static class ShulkerContainer extends Container {
    public ShulkerContainer(ShulkerInventory inventory, int size) {
      super(ContainerType.GENERIC_9x3, -1);
      for (int i = 0; i < size; ++i) {
        int x = i % 9 * 18;
        int y = ((i / 9 + 1) * 18) + 1;
        addSlot(new Slot(inventory, i, x, y));
      }
    }

    @Override
    public boolean stillValid(PlayerEntity playerIn) {
      return false;
    }
  }

  static class ShulkerInventory implements IInventory {

    private final List<ItemStack> contents;

    public ShulkerInventory(List<ItemStack> contents) {
      this.contents = contents;
    }

    @Override
    public int getMaxStackSize() {
      return contents.size();
    }

    @Override
    public void startOpen(PlayerEntity p_174889_1_) {
    }

    @Override
    public void stopOpen(PlayerEntity p_174886_1_) {
    }

    @Override
    public boolean canPlaceItem(int p_94041_1_, ItemStack p_94041_2_) {
      return false;
    }

    @Override
    public int countItem(Item p_213901_1_) {
      return contents.stream()
          .filter(stack -> p_213901_1_ == stack.getItem())
          .mapToInt(ItemStack::getCount)
          .sum();
    }

    @Override
    public boolean hasAnyOf(Set<Item> p_213902_1_) {
      return contents.stream()
          .map(ItemStack::getItem)
          .anyMatch(p_213902_1_::contains);
    }

    @Override
    public int getContainerSize() {
      return contents.size();
    }

    @Override
    public boolean isEmpty() {
      return contents.isEmpty();
    }

    @Override
    public ItemStack getItem(int p_70301_1_) {
      return contents.get(p_70301_1_);
    }

    @Override
    public ItemStack removeItem(int p_70298_1_, int p_70298_2_) {
      return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int p_70304_1_) {
      return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int p_70299_1_, ItemStack p_70299_2_) {

    }

    @Override
    public void setChanged() {

    }

    @Override
    public boolean stillValid(PlayerEntity p_70300_1_) {
      return false;
    }

    @Override
    public void clearContent() {

    }
  }
}
