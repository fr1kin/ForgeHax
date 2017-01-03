package com.matt.forgehax.gui;

import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.gui.categories.IGuiCategory;
import com.matt.forgehax.util.container.lists.PlayerList;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.config.GuiConfig;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.io.IOException;

public class GuiAddPlayer extends GuiScreen {
    private final static String STRING_ADD_FRIEND = "Enter player name to add";
    private final GuiScreen parent;

    private GuiButton buttonAddPlayer;
    private GuiButton buttonBack;

    private GuiTextField textFieldPlayerName;

    public GuiAddPlayer(@Nullable GuiScreen parent) {
        this.parent = parent;
        if(parent != null)
            this.mc = parent.mc;
    }

    public void exitScreen() {
        FMLClientHandler.instance().displayGuiScreen(mc.player, parent);
        if(parent instanceof GuiConfig) {
            ((GuiConfig) parent).needsRefresh = true;
        }
    }

    public String getTextFieldContents() {
        if(textFieldPlayerName != null &&
                !textFieldPlayerName.getText().isEmpty() &&
                !textFieldPlayerName.getText().equals(STRING_ADD_FRIEND)) {
            return textFieldPlayerName.getText();
        } else return null;
    }

    @Override
    public void initGui() {
        this.width = parent.width;
        this.height = parent.height;

        int x = width / 2;
        int y = height / 2;

        int textBoxHeight = 20;
        int textBoxWidth = 200;

        int buttonHeight = 20;
        int buttonWidth = 200;

        textFieldPlayerName = new GuiTextField(4000, mc.fontRendererObj, x - (textBoxWidth / 2), y - textBoxHeight, textBoxWidth, textBoxHeight);
        textFieldPlayerName.setMaxStringLength(256); // MAX_FILE_SIZE
        textFieldPlayerName.setEnabled(true);
        textFieldPlayerName.setFocused(true);
        textFieldPlayerName.setText(STRING_ADD_FRIEND);

        buttonList.add(buttonAddPlayer = new GuiButton(4001, x - (buttonWidth / 2), y + 5, buttonWidth, buttonHeight, "Add"));
        buttonList.add(buttonBack = new GuiButton(4002, x - (buttonWidth / 2), y + buttonHeight + 10, buttonWidth, buttonHeight, "Back"));
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.equals(buttonAddPlayer)) {
            if (parent instanceof IGuiCategory) {
                IGuiCategory guiCategory = (IGuiCategory) parent;
                guiCategory.addToSelected(getTextFieldContents());
            }
            exitScreen();
        } else if (button.equals(buttonBack)) {
            exitScreen();
        }
    }

    @Override
    public void onGuiClosed()
    {
        if (parent != null && parent instanceof GuiConfig)
        {
            GuiConfig parentGuiConfig = (GuiConfig)parent;
            parentGuiConfig.needsRefresh = true;
            parentGuiConfig.initGui();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if(keyCode == Keyboard.KEY_ESCAPE) {
            FMLClientHandler.instance().displayGuiScreen(mc.player, parent);
        } else {
            textFieldPlayerName.textboxKeyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        textFieldPlayerName.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        textFieldPlayerName.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        textFieldPlayerName.drawTextBox();
    }
}
