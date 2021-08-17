package dev.fiki.forgehax.main.ui;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.fiki.forgehax.api.cmd.execution.CommandExecutor;
import dev.fiki.forgehax.api.color.Color;
import dev.fiki.forgehax.api.color.Colors;
import dev.fiki.forgehax.api.draw.RenderTypeEx;
import dev.fiki.forgehax.api.draw.SurfaceHelper;
import dev.fiki.forgehax.api.extension.VectorEx;
import dev.fiki.forgehax.api.extension.VertexBuilderEx;
import lombok.Getter;
import lombok.experimental.ExtensionMethod;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.recipebook.RecipeBookGui;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static dev.fiki.forgehax.main.Common.*;

@ExtensionMethod({VectorEx.class, VertexBuilderEx.class})
public class ConsoleInputScreen extends Screen {
  private final ConsoleInterface ci;
  private final KeyBinding keyBinding;

  private volatile Screen previousScreen;
  private boolean processPreviousScreen = true;
  @Getter
  private boolean closing = false;

  private String text = "";
  private int maxStringLength = 1024;
  private int cursorCounter;
  private boolean isEnabled = true;
  private boolean shiftDown;
  private int lineScrollOffset;
  private int cursorPosition;
  private int selectionEnd;
  private String suggestion;
  private Consumer<String> guiResponder;
  private Predicate<String> validator = str -> true;
  private int historyIndex = 0;

  public ConsoleInputScreen(ConsoleInterface ci, KeyBinding keyBinding) {
    super(new StringTextComponent("Console"));
    this.ci = ci;
    this.keyBinding = keyBinding;
  }

  public synchronized void setPreviousScreen(Screen previousScreen) {
    if (previousScreen instanceof InventoryScreen) {
      RecipeBookGui gui = ((InventoryScreen) previousScreen).getRecipeBookComponent();
      if (gui.isVisible()) {
        gui.toggleVisibility();
      }
    }
    this.previousScreen = previousScreen;
    this.processPreviousScreen = previousScreen != null;
  }

  public int getX() {
    return (int) ci.getMargin();
  }

  public int getY() {
    return (int) (ci.getCurrentTotalHeight() + ci.getMargin() + 2.f);
  }

  public int getWidth() {
    return (int) (ci.getLineWidth());
  }

  public int getHeight() {
    return (int) (ci.getLineHeight() + 2.f);
  }

  public void setResponder(Consumer<String> responder) {
    this.guiResponder = responder;
  }

  @Override
  protected void init() {
    MC.keyboardHandler.setSendRepeatsToGui(true);
  }

  @Override
  public void resize(Minecraft mc, int screenWidth, int screenHeight) {
    if (processPreviousScreen) {
      synchronized (this) {
        if (previousScreen != null) {
          try {
            previousScreen.resize(mc, screenWidth, screenHeight);
          } catch (Throwable t) {
            processPreviousScreen = false;
          }
        }
      }
    }

    super.resize(mc, screenWidth, screenHeight);
  }

  @Override
  public void onClose() {
    MC.keyboardHandler.setSendRepeatsToGui(false);
  }

  @Override
  public void tick() {
    ++this.cursorCounter;

    if (processPreviousScreen) {
      synchronized (this) {
        if (previousScreen != null) {
          try {
            previousScreen.tick();
          } catch (Throwable t) {
            processPreviousScreen = false;
          }
        }
      }
    }
  }

  public void setText(String textIn) {
    if (textIn != null && this.validator.test(textIn)) {
      if (textIn.length() > this.maxStringLength) {
        this.text = textIn.substring(0, this.maxStringLength);
      } else {
        this.text = textIn;
      }

      this.setCursorPositionEnd();
      this.setSelectionPos(this.cursorPosition);
      this.onTextChanged(textIn);
    }
  }

  /**
   * Returns the contents of the textbox
   */
  public String getText() {
    return this.text;
  }

  /**
   * returns the text between the cursor and selectionEnd
   */
  public String getSelectedText() {
    int i = Math.min(this.cursorPosition, this.selectionEnd);
    int j = Math.max(this.cursorPosition, this.selectionEnd);
    return this.text.substring(i, j);
  }

  public void setValidator(Predicate<String> validatorIn) {
    this.validator = validatorIn;
  }

  /**
   * Adds the given text after the cursor, or replaces the currently selected text if there is a selection.
   */
  public void writeText(String textToWrite) {
    String s = "";
    String s1 = SharedConstants.filterText(textToWrite);
    int i = Math.min(this.cursorPosition, this.selectionEnd);
    int j = Math.max(this.cursorPosition, this.selectionEnd);
    int k = this.maxStringLength - this.text.length() - (i - j);
    if (!this.text.isEmpty()) {
      s = s + this.text.substring(0, i);
    }

    int l;
    if (k < s1.length()) {
      s = s + s1.substring(0, k);
      l = k;
    } else {
      s = s + s1;
      l = s1.length();
    }

    if (!this.text.isEmpty() && j < this.text.length()) {
      s = s + this.text.substring(j);
    }

    if (this.validator.test(s)) {
      this.text = s;
      this.clampCursorPosition(i + l);
      this.setSelectionPos(this.cursorPosition);
      this.onTextChanged(this.text);
    }
  }

  private void onTextChanged(String newText) {
    if (this.guiResponder != null) {
      this.guiResponder.accept(newText);
    }
  }

  private void delete(int p_212950_1_) {
    if (Screen.hasControlDown()) {
      this.deleteWords(p_212950_1_);
    } else {
      this.deleteFromCursor(p_212950_1_);
    }

  }

  /**
   * Deletes the given number of words from the current cursor's position, unless there is currently a selection, in
   * which case the selection is deleted instead.
   */
  public void deleteWords(int num) {
    if (!this.text.isEmpty()) {
      if (this.selectionEnd != this.cursorPosition) {
        this.writeText("");
      } else {
        this.deleteFromCursor(this.getNthWordFromCursor(num) - this.cursorPosition);
      }
    }
  }

  /**
   * Deletes the given number of characters from the current cursor's position, unless there is currently a selection,
   * in which case the selection is deleted instead.
   */
  public void deleteFromCursor(int num) {
    if (!this.text.isEmpty()) {
      if (this.selectionEnd != this.cursorPosition) {
        this.writeText("");
      } else {
        boolean flag = num < 0;
        int i = flag ? this.cursorPosition + num : this.cursorPosition;
        int j = flag ? this.cursorPosition : this.cursorPosition + num;
        String s = "";
        if (i >= 0) {
          s = this.text.substring(0, i);
        }

        if (j < this.text.length()) {
          s = s + this.text.substring(j);
        }

        if (this.validator.test(s)) {
          this.text = s;
          if (flag) {
            this.moveCursorBy(num);
          }

          this.onTextChanged(this.text);
        }
      }
    }
  }

  /**
   * Gets the starting index of the word at the specified number of words away from the cursor position.
   */
  public int getNthWordFromCursor(int numWords) {
    return this.getNthWordFromPos(numWords, this.getCursorPosition());
  }

  /**
   * Gets the starting index of the word at a distance of the specified number of words away from the given position.
   */
  private int getNthWordFromPos(int n, int pos) {
    return this.getNthWordFromPosWS(n, pos, true);
  }

  /**
   * Like getNthWordFromPos (which wraps this), but adds option for skipping consecutive spaces
   */
  private int getNthWordFromPosWS(int n, int pos, boolean skipWs) {
    int i = pos;
    boolean flag = n < 0;
    int j = Math.abs(n);

    for (int k = 0; k < j; ++k) {
      if (!flag) {
        int l = this.text.length();
        i = this.text.indexOf(32, i);
        if (i == -1) {
          i = l;
        } else {
          while (skipWs && i < l && this.text.charAt(i) == ' ') {
            ++i;
          }
        }
      } else {
        while (skipWs && i > 0 && this.text.charAt(i - 1) == ' ') {
          --i;
        }

        while (i > 0 && this.text.charAt(i - 1) != ' ') {
          --i;
        }
      }
    }

    return i;
  }

  /**
   * Moves the text cursor by a specified number of characters and clears the selection
   */
  public void moveCursorBy(int num) {
    this.setCursorPosition(this.cursorPosition + num);
  }

  /**
   * Sets the current position of the cursor.
   */
  public void setCursorPosition(int pos) {
    this.clampCursorPosition(pos);
    if (!this.shiftDown) {
      this.setSelectionPos(this.cursorPosition);
    }

    this.onTextChanged(this.text);
  }

  public void clampCursorPosition(int pos) {
    this.cursorPosition = MathHelper.clamp(pos, 0, this.text.length());
  }

  /**
   * Moves the cursor to the very start of this text box.
   */
  public void setCursorPositionZero() {
    this.setCursorPosition(0);
  }

  /**
   * Moves the cursor to the very end of this text box.
   */
  public void setCursorPositionEnd() {
    this.setCursorPosition(this.text.length());
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int keyAction) {
    if (cursorCounter <= 1) {
      return false;
    }

    this.shiftDown = Screen.hasShiftDown();
    if (keyBinding.getKey().getValue() == keyCode) {
      closing = true;
      setDisplayScreen(previousScreen);
      return true;
    } else if (Screen.isSelectAll(keyCode)) {
      this.setCursorPositionEnd();
      this.setSelectionPos(0);
      return true;
    } else if (Screen.isCopy(keyCode)) {
      MC.keyboardHandler.setClipboard(this.getSelectedText());
      return true;
    } else if (Screen.isPaste(keyCode)) {
      if (this.isEnabled) {
        this.writeText(Minecraft.getInstance().keyboardHandler.getClipboard());
      }

      return true;
    } else if (Screen.isCut(keyCode)) {
      Minecraft.getInstance().keyboardHandler.setClipboard(this.getSelectedText());
      if (this.isEnabled) {
        this.writeText("");
      }

      return true;
    } else {
      switch (keyCode) {
        case GLFW.GLFW_KEY_ENTER:
        case GLFW.GLFW_KEY_KP_ENTER:
          String text = getText().trim();
          if (!text.isEmpty()) {
            CommandExecutor.builder()
                .console(ci)
                .exceptionHandler(((throwable, output) -> {
                  output.error(MoreObjects.firstNonNull(throwable.getMessage(), throwable.getClass().getSimpleName()));
                  getLogger().debug(throwable, throwable);
                }))
                .build()
                .runLine(text);
            setText("");
            historyIndex = 0;
            ci.addEntered(text);
          }
        case GLFW.GLFW_KEY_ESCAPE:
          closing = true;
          setDisplayScreen(previousScreen);
          return true;
        case GLFW.GLFW_KEY_BACKSPACE:
          if (this.isEnabled) {
            this.shiftDown = false;
            this.delete(-1);
            this.shiftDown = Screen.hasShiftDown();
          }
          return true;
//        case GLFW.GLFW_KEY_INSERT:
        case GLFW.GLFW_KEY_DOWN:
        case GLFW.GLFW_KEY_PAGE_DOWN:
          if (ci.hasEnteredHistory()) {
            setText(ci.getEnteredByRollingIndex(historyIndex--));
          }
          return true;
        case GLFW.GLFW_KEY_UP:
        case GLFW.GLFW_KEY_PAGE_UP:
          if (ci.hasEnteredHistory()) {
            setText(ci.getEnteredByRollingIndex(historyIndex++));
          }
          return true;
        case GLFW.GLFW_KEY_DELETE:
          if (this.isEnabled) {
            this.shiftDown = false;
            this.delete(1);
            this.shiftDown = Screen.hasShiftDown();
          }
          return true;
        case GLFW.GLFW_KEY_RIGHT:
          if (Screen.hasControlDown()) {
            this.setCursorPosition(this.getNthWordFromCursor(1));
          } else {
            this.moveCursorBy(1);
          }
          return true;
        case GLFW.GLFW_KEY_LEFT:
          if (Screen.hasControlDown()) {
            this.setCursorPosition(this.getNthWordFromCursor(-1));
          } else {
            this.moveCursorBy(-1);
          }
          return true;
        case GLFW.GLFW_KEY_HOME:
          this.setCursorPositionZero();
          return true;
        case GLFW.GLFW_KEY_END:
          this.setCursorPositionEnd();
          return true;
        default:
          return false;
      }
    }
  }

  public boolean canWrite() {
    return this.getVisible() && this.isEnabled();
  }

  @Override
  public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
    if (!this.canWrite()) {
      return false;
    } else if (SharedConstants.isAllowedChatCharacter(p_charTyped_1_)) {
      if (this.isEnabled) {
        this.writeText(Character.toString(p_charTyped_1_));
      }
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (!this.getVisible()) {
      return false;
    } else {
      boolean flag = mouseX >= (double) getX()
          && mouseX < (double) (getX() + getWidth())
          && mouseY >= (double) getY()
          && mouseY < (double) (getY() + getHeight());

      if (flag && button == 0) {
        int i = MathHelper.floor(mouseX) - getX();
//        String s = ci.getFontRenderer().trimStringToWidth(this.text.substring(this.lineScrollOffset), this.getAdjustedWidth());
//        this.setCursorPosition(ci.getFontRenderer().trimStringToWidth(s, i).length() + this.lineScrollOffset);
        return true;
      } else {
        return false;
      }
    }
  }

  @Override
  public boolean mouseScrolled(double handle, double scrollX, double scrollY) {
    return false;
  }

  @Override
  public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
    if (processPreviousScreen) {
      synchronized (this) {
        if (previousScreen != null
            && previousScreen.getMinecraft() != null) {
          stack.pushPose();
          stack.translate(0.f, 0.f, -150.f);
          try {
            previousScreen.render(stack, 0, 0, partialTicks);
          } catch (Throwable t) {
            processPreviousScreen = false;
          }
          stack.popPose();
        }
      }
    }

    val buffers = getBufferProvider();
    val source = buffers.getBufferSource();
    val main = buffers.getBuffer(RenderTypeEx.glTriangle());

    main.rect(GL11.GL_TRIANGLES, getX(), getY(), getWidth(), getHeight(),
        Colors.BLACK.setAlpha(200), stack.getLastMatrix());

    stack.pushPose();
    stack.translate(2.f, 2.f, 0.f);

    val color = Color.of(224, 224, 224, 0);
    int cursorOffset = this.cursorPosition - this.lineScrollOffset;
    int selectionLength = this.selectionEnd - this.lineScrollOffset;

    String visibleText = ci.getFontRenderer().plainSubstrByWidth(this.text.substring(this.lineScrollOffset),
        this.getAdjustedWidth());

    boolean selectedTextVisible = cursorOffset >= 0 && cursorOffset <= visibleText.length();
    boolean markerVisible = this.cursorCounter / 6 % 2 == 0 && selectedTextVisible;

    int x = getX();
    int y = getY();
    int offsetX = x;

    if (selectionLength > visibleText.length()) {
      selectionLength = visibleText.length();
    }

    if (!visibleText.isEmpty()) {
      String renderText = selectedTextVisible ? visibleText.substring(0, cursorOffset) : visibleText;
      SurfaceHelper.renderString(source, stack.last().pose(), renderText,
          (float) getX(), (float) getY(),
          Colors.WHITE, true);
      offsetX += SurfaceHelper.getStringWidth(renderText) + 1;
    }

    boolean endStringVisible = this.cursorPosition < this.text.length()
        || this.text.length() >= this.getMaxStringLength();
    int endX = offsetX;
    if (!selectedTextVisible) {
      endX = cursorOffset > 0 ? x + getWidth() : x;
    } else if (endStringVisible) {
      endX = offsetX - 1;
      --offsetX;
    }

    if (!visibleText.isEmpty() && selectedTextVisible && cursorOffset < visibleText.length()) {
      SurfaceHelper.renderString(source, stack.last().pose(), visibleText.substring(cursorOffset),
          offsetX, y, color, true);
    }

    if (!endStringVisible && this.suggestion != null) {
      SurfaceHelper.renderString(source, stack.last().pose(), this.suggestion,
          endX - 1, y, Colors.GRAY, true);
    }

    if (markerVisible) {
      if (endStringVisible) {
        stack.pushPose();
        stack.translate(0.f, 0.f, 100.f);
        main.rect(GL11.GL_TRIANGLES, endX, y - 1, 1, 10,
            Color.of(208, 208, 208, 255), stack.getLastMatrix());
        stack.popPose();
      } else {
        SurfaceHelper.renderString(source, stack.last().pose(), "_",
            endX, y, color, true);
      }
    }

    stack.popPose();

    stack.pushPose();
    stack.translate(0.f, 0.f, 50.f);
    source.endBatch();

    if (selectionLength != cursorOffset) {
      stack.pushPose();
      stack.translate(2.f, 2.f, 0.f);
      int highlightX = x + ci.getFontRenderer().width(visibleText.substring(0, selectionLength));
      this.drawSelectionBox(endX, y - 1, highlightX - 1, y + 1 + 9);
      stack.popPose();
    }
    stack.popPose();
  }

  /**
   * Draws the blue selection box.
   */
  private void drawSelectionBox(int startX, int startY, int endX, int endY) {
    if (startX < endX) {
      int i = startX;
      startX = endX;
      endX = i;
    }

    if (startY < endY) {
      int j = startY;
      startY = endY;
      endY = j;
    }

    if (endX > getX() + getWidth()) {
      endX = getX() + getWidth();
    }

    if (startX > getX() + getWidth()) {
      startX = getX() + getWidth();
    }

    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder bufferbuilder = tessellator.getBuilder();
    RenderSystem.color4f(0.0F, 0.0F, 255.0F, 255.0F);
    RenderSystem.disableTexture();
    RenderSystem.enableColorLogicOp();
    RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
    bufferbuilder.begin(7, DefaultVertexFormats.POSITION);
    bufferbuilder.vertex((double) startX, (double) endY, 0.0D).endVertex();
    bufferbuilder.vertex((double) endX, (double) endY, 0.0D).endVertex();
    bufferbuilder.vertex((double) endX, (double) startY, 0.0D).endVertex();
    bufferbuilder.vertex((double) startX, (double) startY, 0.0D).endVertex();
    tessellator.end();
    RenderSystem.disableColorLogicOp();
    RenderSystem.enableTexture();
  }

  /**
   * Sets the maximum length for the text in this text box. If the current text is longer than this length, the current
   * text will be trimmed.
   */
  public void setMaxStringLength(int length) {
    this.maxStringLength = length;
    if (this.text.length() > length) {
      this.text = this.text.substring(0, length);
      this.onTextChanged(this.text);
    }
  }

  /**
   * returns the maximum number of character that can be contained in this textbox
   */
  private int getMaxStringLength() {
    return this.maxStringLength;
  }

  /**
   * returns the current position of the cursor
   */
  public int getCursorPosition() {
    return this.cursorPosition;
  }

  @Override
  public boolean changeFocus(boolean p_changeFocus_1_) {
    return this.isEnabled && super.changeFocus(p_changeFocus_1_);
  }

  @Override
  public boolean isPauseScreen() {
    return previousScreen != null && previousScreen.isPauseScreen();
  }

  @Override
  public boolean isMouseOver(double p_isMouseOver_1_, double p_isMouseOver_3_) {
    return p_isMouseOver_1_ >= (double) getX()
        && p_isMouseOver_1_ < (double) (getX() + getWidth())
        && p_isMouseOver_3_ >= (double) getY()
        && p_isMouseOver_3_ < (double) (getY() + getHeight());
  }

  protected void onFocusedChanged(boolean p_onFocusedChanged_1_) {
    if (p_onFocusedChanged_1_) {
      this.cursorCounter = 0;
    }
  }

  private boolean isEnabled() {
    return true;
  }

  public int getAdjustedWidth() {
    return getWidth();
  }

  public void setSelectionPos(int position) {
    int i = this.text.length(); // TODO: 1.16
    this.selectionEnd = MathHelper.clamp(position, 0, i);
    if (ci.getFontRenderer() != null) {
      if (this.lineScrollOffset > i) {
        this.lineScrollOffset = i;
      }

      int j = this.getAdjustedWidth();
      String s = ci.getFontRenderer().plainSubstrByWidth(this.text.substring(this.lineScrollOffset), j);
      int k = s.length() + this.lineScrollOffset;
      if (this.selectionEnd == this.lineScrollOffset) {
        this.lineScrollOffset -= ci.getFontRenderer().plainSubstrByWidth(this.text, j, true).length();
      }

      if (this.selectionEnd > k) {
        this.lineScrollOffset += this.selectionEnd - k;
      } else if (this.selectionEnd <= this.lineScrollOffset) {
        this.lineScrollOffset -= this.lineScrollOffset - this.selectionEnd;
      }

      this.lineScrollOffset = MathHelper.clamp(this.lineScrollOffset, 0, i);
    }

  }

  public boolean getVisible() {
    return true;
  }

  public void setSuggestion(@Nullable String p_195612_1_) {
    this.suggestion = p_195612_1_;
  }

  public int func_195611_j(int p_195611_1_) {
    return p_195611_1_ > this.text.length() ? getX() : getX() + ci.getFontRenderer().width(this.text.substring(0, p_195611_1_));
  }
}
