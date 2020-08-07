package dev.fiki.forgehax.main.mods.services;

import com.google.common.util.concurrent.AtomicDouble;
import com.mojang.blaze3d.matrix.MatrixStack;
import dev.fiki.forgehax.main.Common;
import dev.fiki.forgehax.main.util.cmd.execution.CommandExecutor;
import dev.fiki.forgehax.main.util.cmd.execution.IConsole;
import dev.fiki.forgehax.main.util.color.Colors;
import dev.fiki.forgehax.main.util.draw.SurfaceHelper;
import dev.fiki.forgehax.main.util.mod.ServiceMod;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import static dev.fiki.forgehax.main.Common.getLogger;
import static net.minecraft.util.text.TextFormatting.RED;

//@RegisterMod
public class MainMenuGuiService extends ServiceMod {
  
  private Button customButton;
  
  @SubscribeEvent
  public void onGui(GuiScreenEvent.InitGuiEvent.Post event) {
    if (event.getGui() instanceof MainMenuScreen) {
      MainMenuScreen gui = (MainMenuScreen) event.getGui();
      
      event.getWidgetList().stream()
          .skip(4) // skip first 4 button
          .forEach(button -> {
            button.y += 24;
          }); // lower the rest of the buttons to make room for ours

      event.addWidget(customButton = new Button(
          gui.width / 2 - 100, // x
          gui.height / 4 + 48 + (24 * 3), // y
          200,
          20,
          new StringTextComponent("Command Input"), this::onPressed
      ));
    }
  }

  private void onPressed(Button button) {
    Common.setDisplayScreen(new CommandInputGui());
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
      Common.closeDisplayScreen();
    }

    @Override
    public void init() {
      //Keyboard.enableRepeatEvents(true);
      inputField = new TextFieldWidget(Common.getFontRenderer(),
          4, this.height - 12,
          this.width - 4, 12,
          new StringTextComponent(""));
      inputField.setMaxStringLength(Integer.MAX_VALUE);
      inputField.setEnableBackgroundDrawing(false);
      inputField.setCanLoseFocus(false);

      modeButton = new Button(0, this.width - 100 - 2,
          this.height - 20 - 2, 100,
          new StringTextComponent(mode.getName()), this::onModePressed);

      // TODO: add back button?

      addButton(inputField);
      addButton(modeButton);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
      renderBackground(stack);

      SurfaceHelper.drawRect(2, this.height - 16,
          this.width - 104, this.height - 4,
          Integer.MIN_VALUE); // input field
      SurfaceHelper.drawRect(2, 2,
          this.width - 2, this.height - 38,
          70 << 24); // messageHistory box

      this.drawHistory(stack);

      super.render(stack, mouseX, mouseY, partialTicks);
    }
    
    private void drawHistory(MatrixStack stack) {
      AtomicDouble offset = new AtomicDouble();
      messageHistory.stream()
          .limit(100)
          .forEach(str -> {
            Common.getFontRenderer().drawString(stack, str, 5, (this.height - 50 - offset.intValue()), Colors.WHITE.toBuffer());
            offset.addAndGet(10);
          });
    }
    
    @Override
    public boolean charTyped(char typedChar, int keyCode) {
      if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
        Common.closeDisplayScreen();
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
    
    private void runCommand(String line) {
      try {
        // TODO: Future client api
        switch (mode) {
          case FORGEHAX:
            CommandExecutor.builder()
                .console(new IConsole() {
                  @Override
                  public void inform(String message, Object... args) {
                    print(String.format(message, args));
                  }

                  @Override
                  public void warn(String message, Object... args) {
                    print(String.format(message, args));
                  }

                  @Override
                  public void error(String message, Object... args) {
                    print(String.format(message, args));
                  }
                })
                .exceptionHandler(((throwable, output) -> {
                  output.error("Error: %s", throwable.getMessage());
                  getLogger().error(throwable, throwable);
                }))
                .build()
                .runLine(line);
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
