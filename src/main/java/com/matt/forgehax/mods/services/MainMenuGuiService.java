package com.matt.forgehax.mods.services;

import com.google.common.util.concurrent.AtomicDouble;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.client.gui.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import static net.minecraft.util.text.TextFormatting.*;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Created by Babbaj on 4/10/2018.
 */
@RegisterMod
public class MainMenuGuiService extends ServiceMod {

    private GuiButton customButton;

    public MainMenuGuiService() {
        super("MainMenuGuiService");
    }

    @SubscribeEvent
    public void onGui(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof GuiMainMenu) {
            GuiMainMenu gui = (GuiMainMenu) event.getGui();

            event.getButtonList().stream()
                    .skip(4) // skip first 4 button
                    .forEach(button -> {
                        button.y += 24;
                    }); // lower the rest of the buttons to make room for ours

            event.getButtonList().add(customButton = new GuiButton(
                    666,
                    gui.width/2 - 100,
                    gui.height/4 + 48 + (24 * 3), // put button in 4th row
                    "Command Input"
            ));


        }
    }

    @SubscribeEvent
    public void onActionPerformed(GuiScreenEvent.ActionPerformedEvent event) {
        if (event.getButton() == customButton) {
            MC.displayGuiScreen(new CommandInputGui());
        }
    }

    public class CommandInputGui extends GuiScreen {

        GuiButton backButton;
        GuiTextField inputField;
        GuiButton modeButton;
        ClientMode mode = ClientMode.FORGEHAX;
        Deque<ITextComponent> messageHistory = new LinkedList<>();
        Deque<ITextComponent> inputHistory = new LinkedList<>();


        @Override
        public void initGui() {
            Keyboard.enableRepeatEvents(true);
            this.inputField = new GuiTextField(0, this.fontRenderer, 4, this.height - 12, this.width - 4, 12);
            inputField.setMaxStringLength(Integer.MAX_VALUE);
            this.inputField.setEnableBackgroundDrawing(false);
            this.inputField.setFocused(true);
            this.inputField.setCanLoseFocus(false);

            this.buttonList.add(modeButton = new GuiButton(
               0,
                    this.width - 100 - 2,
                    this.height - 20 - 2,
                    100,
                    20,
                    mode.getName()

            ));
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            this.drawDefaultBackground();
            drawRect(2, this.height - 16, this.width - 104, this.height - 4, Integer.MIN_VALUE);// input field
            drawRect(2, 2, this.width - 2, this.height - 38, 70 << 24); // messageHistory box
            this.inputField.drawTextBox();
            this.drawHistory();
            super.drawScreen(mouseX, mouseY, partialTicks);
        }

        @Override
        public void updateScreen() {
            this.inputField.updateCursorCounter();
            this.modeButton.displayString = mode.getName();
        }

        @Override
        protected void actionPerformed(GuiButton button) {
            if (button == modeButton) {
                if (mode.ordinal() == ClientMode.values().length-1) {
                    mode = ClientMode.values()[0];
                }
                else {
                    mode = ClientMode.values()[mode.ordinal() + 1];
                }
            }
            if(button == backButton) {
                MC.displayGuiScreen(null);
            }
        }

        private void drawHistory() {
            AtomicDouble offset = new AtomicDouble();
            messageHistory.stream()
                    .limit(100)
                    .map(ITextComponent::getFormattedText)
                    .forEach(str -> {
                        MC.fontRenderer.drawString(str, 5, (this.height - 50 - offset.intValue()), Utils.Colors.WHITE);
                        offset.addAndGet(10D);
                    });

        }

        @Override
        protected void keyTyped(char typedChar, int keyCode) throws IOException
        {
            if (keyCode == 1)
            {
                this.mc.displayGuiScreen(null);
            }
            else if (keyCode != 28 && keyCode != 156)
            {
                if (keyCode == 200)
                {
                    //this.getSentHistory(-1);
                }
                else if (keyCode == 208)
                {
                    //this.getSentHistory(1);
                }
                else if (keyCode == 201)
                {
                    //this.mc.ingameGUI.getChatGUI().scroll(this.mc.ingameGUI.getChatGUI().getLineCount() - 1);
                }
                else if (keyCode == 209)
                {
                    //this.mc.ingameGUI.getChatGUI().scroll(-this.mc.ingameGUI.getChatGUI().getLineCount() + 1);
                }
                else
                {
                    this.inputField.textboxKeyTyped(typedChar, keyCode);
                }
            }
            else
            {
                String str = this.inputField.getText().trim();

                if (!str.isEmpty())
                {
                    //this.print("> " + str);
                    this.inputField.setText("");
                    this.runCommand(str);
                }

            }
        }

        public void print(String message) {
            if (!message.isEmpty()) {
                for (String str : message.split("\n")) {
                    messageHistory.push(new TextComponentString(str));
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
