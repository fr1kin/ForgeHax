package com.matt.forgehax.mods.services;

import static com.matt.forgehax.Globals.*;
import static com.matt.forgehax.util.draw.SurfaceHelper.drawRect;
import static net.minecraft.util.text.TextFormatting.RED;

import com.google.common.util.concurrent.AtomicDouble;
import com.matt.forgehax.util.color.Colors;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Created by Babbaj on 4/10/2018.
 */
@RegisterMod
public class MainMenuGuiService extends ServiceMod {
  
  private Button customButton;
  
  public MainMenuGuiService() {
    super("MainMenuGuiService");
  }
  
  @SubscribeEvent
  public void onGui(GuiScreenEvent.InitGuiEvent.Post event) {
    if (event.getGui() instanceof MainMenuScreen) {
      MainMenuScreen gui = (MainMenuScreen) event.getGui();
      
      event.getWidgetList().stream()
          .skip(4) // skip first 4 button
          .forEach(
              button -> {
                button.y += 24;
              }); // lower the rest of the buttons to make room for ours
      
      event.getWidgetList()
          .add(customButton = new Button(
              666, gui.width / 2 - 100,
              gui.height / 4 + 48 + (24 * 3), 24,
              "Command Input", this::onPressed));
    }
  }

  private void onPressed(Button button) {
    setDisplayScreen(new CommandInputGui());
  }
  
  private static class CommandInputGui extends Screen {
    TextFieldWidget inputField;
    Button modeButton;
    ClientMode mode = ClientMode.FORGEHAX;
    Deque<String> messageHistory = new LinkedList<>();
    
    // ordered from oldest to newest
    List<String> inputHistory = new ArrayList<>();
    int sentHistoryCursor = 0;
    String historyBuffer = "";

    protected CommandInputGui() {
      super(new StringTextComponent("ForgeHaxCommandInput"));
    }

    private void onModePressed(Button button) {
      if (mode.ordinal() == ClientMode.values().length - 1) {
        mode = ClientMode.values()[0];
      } else {
        mode = ClientMode.values()[mode.ordinal() + 1];
      }
    }

    private void onBackPressed(Button button) {
      closeDisplayScreen();
    }

    @Override
    public void init() {
      //Keyboard.enableRepeatEvents(true);
      inputField = new TextFieldWidget(getFontRenderer(),
          4, this.height - 12,
          this.width - 4, 12,
          "");
      inputField.setMaxStringLength(Integer.MAX_VALUE);
      inputField.setEnableBackgroundDrawing(false);
      inputField.setCanLoseFocus(false);

      modeButton = new Button(0, this.width - 100 - 2,
          this.height - 20 - 2, 100,
          mode.getName(), this::onModePressed);

      // TODO: add back button?

      addButton(inputField);
      addButton(modeButton);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
      renderBackground();

      drawRect(2, this.height - 16,
          this.width - 104, this.height - 4,
          Integer.MIN_VALUE); // input field
      drawRect(2, 2,
          this.width - 2, this.height - 38,
          70 << 24); // messageHistory box

      this.drawHistory();

      super.render(mouseX, mouseY, partialTicks);
    }
    
    private void drawHistory() {
      AtomicDouble offset = new AtomicDouble();
      messageHistory.stream()
          .limit(100)
          .forEach(str -> {
            getFontRenderer().drawString(str, 5, (this.height - 50 - offset.intValue()), Colors.WHITE.toBuffer());
            offset.addAndGet(10);
          });
    }
    
    @Override
    public boolean charTyped(char typedChar, int keyCode) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
        closeDisplayScreen();
      } else if (keyCode != GLFW.GLFW_KEY_ENTER && keyCode != GLFW.GLFW_KEY_KP_ENTER) {
        if (keyCode == GLFW.GLFW_KEY_UP) // up arrow
        {
          // older
          String sent = getSentHistory(-1);
          if (sent != null) {
            inputField.setText(sent);
          }
        } else if (keyCode == GLFW.GLFW_KEY_DOWN) // down arrow
        {
          // newer
          String sent = getSentHistory(1);
          if (sent != null) {
            inputField.setText(sent);
          }
        }
      } else // on enter
      {
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

      return super.charTyped(typedChar, keyCode);
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
        // TODO: Future client api
        switch (mode) {
          case FORGEHAX:
            ChatCommandService.handleCommand(s);
            break;
          case FUTURE:
            print(RED + "Unsupported");
            break;
        }
      } catch (Throwable t) {
        print(RED + t.toString());
      }
    }
  }
  
  private enum ClientMode {
    FORGEHAX("Forgehax"),
    FUTURE("Future");
    
    private final String name;
    
    public String getName() {
      return this.name;
    }
    
    ClientMode(String nameIn) {
      this.name = nameIn;
    }
  }
}
