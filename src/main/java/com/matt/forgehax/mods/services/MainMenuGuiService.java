package com.matt.forgehax.mods.services;

import static net.minecraft.util.text.TextFormatting.*;

import com.google.common.util.concurrent.AtomicDouble;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.client.gui.*;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

/** Created by Babbaj on 4/10/2018. */
@RegisterMod
public class MainMenuGuiService extends ServiceMod {


  public MainMenuGuiService() {
    super("MainMenuGuiService");
  }

  @SubscribeEvent
  public void onGui(GuiScreenEvent.InitGuiEvent.Post event) {
    if (event.getGui() instanceof GuiMainMenu) {
      GuiMainMenu gui = (GuiMainMenu) event.getGui();

      final GuiButton newButton = new GuiButton(
          666,
          gui.width / 2 - 100,
          gui.height / 4 + 48 + (24 * 3), // put button in 4th row
          "Command Input") {
        @Override
        public void onClick(double mouseX, double mouseY) {
          MC.displayGuiScreen(new CommandInputGui());
        }
      };

      event.getButtonList()
          .stream()
          .skip(4) // skip first 4 button
          .forEach(button -> {
            button.y += 24;
          }); // lower the rest of the buttons to make room for ours

      event.addButton(newButton);
    }
  }


  public class CommandInputGui extends GuiScreen {

    GuiTextField inputField;
    Deque<String> messageHistory = new LinkedList<>();

    // ordered from oldest to newest
    List<String> inputHistory = new ArrayList<>();
    int sentHistoryCursor = 0;
    String historyBuffer = "";

    @Override
    public void initGui() {
      //Keyboard.enableRepeatEvents(true);
      this.inputField =
          new GuiTextField(0, this.fontRenderer, 4, this.height - 12, this.width - 4, 12);
      inputField.setMaxStringLength(Integer.MAX_VALUE);
      this.inputField.setEnableBackgroundDrawing(false);
      this.inputField.setFocused(true);
      this.inputField.setCanLoseFocus(false);

      //this.buttonList.add(modeButton = new GuiButton(0, this.width - 100 - 2, this.height - 20 - 2, 100, 20, mode.getName()));
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
      this.drawDefaultBackground();
      drawRect(2, this.height - 16, this.width - 104, this.height - 4, Integer.MIN_VALUE); // input field
      drawRect(2, 2, this.width - 2, this.height - 38, 70 << 24); // messageHistory box
      this.inputField.drawTextField(mouseX, mouseY, partialTicks);
      this.drawHistory();
      super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public void tick() {
      this.inputField.tick();
    }

    private void drawHistory() {
      AtomicDouble offset = new AtomicDouble();
      messageHistory.stream()
          .limit(100)
          .forEach(str -> {
            MC.fontRenderer.drawString(str, 5, (this.height - 50 - offset.intValue()), Utils.Colors.WHITE);offset.addAndGet(10);
          });
    }

    @Override
    public boolean keyPressed(int key, int scancode, int modifiers)  {
      if (key == GLFW.GLFW_KEY_ESCAPE) {
        this.mc.displayGuiScreen(null);
      } else if (key != GLFW.GLFW_KEY_ENTER && key != GLFW.GLFW_KEY_KP_ENTER) {
        if (key == GLFW.GLFW_KEY_UP) { // up arrow
          // older
          String sent = getSentHistory(-1);
          if (sent != null) inputField.setText(sent);
        } else if (key == GLFW.GLFW_KEY_DOWN) { // down arrow
          // newer
          String sent = getSentHistory(1);
          if (sent != null) inputField.setText(sent);
          //} else if (keyCode == KEY_PRIOR) {
            // this.mc.ingameGUI.getChatGUI().scroll(this.mc.ingameGUI.getChatGUI().getLineCount() - 1); // commented before 1.13 changes
          //} else if (keyCode == KEY_NEXT) {
            // this.mc.ingameGUI.getChatGUI().scroll(-this.mc.ingameGUI.getChatGUI().getLineCount() + 1);
          //} else {
        }
        this.inputField.keyPressed(key, scancode, modifiers);
      } else { // on enter
        String str = this.inputField.getText().trim();

        if (!str.isEmpty()) {
          // this.print("> " + str);
          this.inputField.setText("");
          if (this.inputHistory.isEmpty()
              || !this.inputHistory.get(this.inputHistory.size() - 1).equals(str)) {
            this.inputHistory.add(str);
          }
          this.sentHistoryCursor = inputHistory.size();
          this.runCommand(str);
        }
      }
      return true; // TODO: figure out what return value does
    }

    @Override
    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
      this.inputField.charTyped(p_charTyped_1_, p_charTyped_2_);
      return true;
    }

    @Nullable
    private String getSentHistory(int offset) {
      int pos = this.sentHistoryCursor + offset;
      final int max = this.inputHistory.size();
      pos = MathHelper.clamp(pos, 0, max);
      if (pos != sentHistoryCursor) {
        if (pos == max) {
          this.sentHistoryCursor = max;
          return this.historyBuffer;
        }
        if (this.sentHistoryCursor == max) {
          this.historyBuffer = inputField.getText();
        }
        this.sentHistoryCursor = pos;
        return inputHistory.get(pos);
      }
      return null; // if cursor is out of bounds or there is no history
    }

    public void print(String message) {
      if (!message.isEmpty()) {
        for (String str : message.split("\n")) {
          messageHistory.push(str);
        }
      }
    }

    private void runCommand(String s) {
      try {
        ChatCommandService.handleCommand(s);
      } catch (Throwable t) {
        print(RED + t.toString());
      }
    }
  }

}
