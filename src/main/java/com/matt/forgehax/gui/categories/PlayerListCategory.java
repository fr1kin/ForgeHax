package com.matt.forgehax.gui.categories;

import com.matt.forgehax.gui.GuiPlayerList;
import com.matt.forgehax.gui.GuiPlayerListFiles;
import com.matt.forgehax.util.SurfaceUtils;
import com.matt.forgehax.util.container.ContainerManager;
import com.matt.forgehax.util.container.PlayerList;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import org.lwjgl.input.Mouse;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class PlayerListCategory extends GuiConfigEntries.CategoryEntry {
    public PlayerListCategory(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
        super(owningScreen, owningEntryList, configElement);
    }

    @Override
    protected GuiScreen buildChildScreen() {
        return new GuiPlayerManager(this.owningScreen,
                this.configElement.getChildElements(),
                this.owningScreen.modID,
                owningScreen.allRequireWorldRestart || this.configElement.requiresWorldRestart(),
                owningScreen.allRequireMcRestart || this.configElement.requiresMcRestart(),
                this.owningScreen.title,
                ((this.owningScreen.titleLine2 == null ? "" : this.owningScreen.titleLine2) + " > " + this.name)
        );
    }

    public static class GuiPlayerManager extends GuiConfig {
        private GuiPlayerListFiles playerFileList;
        private GuiPlayerList playerList;

        public PlayerList selectedList;

        private int fileListX;
        private int fileListY;
        private int fileListW;
        private int fileListH;

        private int left;
        private int right;
        private int top;
        private int bottom;

        private GuiButton buttonAdd;
        private GuiButton buttonRemove;

        private GuiButton buttonAddPlayer;
        private GuiButton buttonRemovePlayer;

        private GuiTextField textField;
        private GuiTextField textFieldPlayer;

        private final String title2Original;

        public GuiPlayerManager(GuiScreen parentScreen, List<IConfigElement> configElements, String modID, boolean allRequireWorldRestart, boolean allRequireMcRestart, String title, String titleLine2) {
            super(parentScreen, configElements, modID, allRequireWorldRestart, allRequireMcRestart, title, titleLine2);
            title2Original = titleLine2;
        }

        public void onSelectPlayerListFile(PlayerList selected) {
            selectedList = selected;
            createGuiList(selectedList);
        }

        public void createGuiList(@Nullable PlayerList selectedList) {
            int x = fileListX + fileListW + 10;
            int y = fileListY;
            int w = width - x - 5;
            int h = height - y - 40;
            playerList = new GuiPlayerList(this,
                    x,
                    y,
                    w,
                    h,
                    50,
                    parentScreen.width,
                    parentScreen.height,
                    selectedList
            );
            if(selectedList != null) {
                titleLine2 = title2Original + " > " + selectedList.getName();
            }
        }

        @Override
        public void initGui() {
            super.initGui();
            int x = 5;
            int y = 40;
            int w = parentScreen.width / 5;
            int h = parentScreen.height / 4;
            // list of files found
            playerFileList = new GuiPlayerListFiles(
                    this,
                    x, // x
                    y, // y
                    w, // w
                    h, // h
                    SurfaceUtils.getTextHeight() + 2, // entryHeight
                    parentScreen.width,
                    parentScreen.height,
                    (Collection<PlayerList>)ContainerManager.<PlayerList>getContainerCollection("players")
            );
            fileListX = x;
            fileListY = y;
            fileListW = w;
            fileListH = h;
            int buttonWidth = 50;
            // remove button
            buttonList.add(buttonRemove = new GuiButton(3000, x, y + h + 5, w, 20, "Remove"));
            // add button
            buttonList.add(buttonAdd = new GuiButton(3001, x, y + h + 30, buttonWidth, 20, "Add"));
            // file name
            textField = new GuiTextField(3002, mc.fontRendererObj, x + buttonWidth + 5, y + h + 30, w - (x + buttonWidth + 5), 20);
            textField.setMaxStringLength(256); // MAX_FILE_SIZE
            textField.setEnabled(true);
            textField.setText("");

            // player list button add
            textFieldPlayer = new GuiTextField(3002, mc.fontRendererObj, x + buttonWidth + 5, y + h + 30, w - (x + buttonWidth + 5), 20);
            textFieldPlayer.setMaxStringLength(256); // MAX_FILE_SIZE
            textFieldPlayer.setEnabled(true);
            textFieldPlayer.setText("");

            buttonList.add(buttonRemove = new GuiButton(3000, x, y + h + 5, w, 20, "Remove"));

            // list of players in selected file
            createGuiList(selectedList);
        }

        @Override
        protected void actionPerformed(GuiButton button) {
            super.actionPerformed(button);
            if(button.equals(buttonAdd) &&
                    !textField.getText().isEmpty()) {
                PlayerList playerList = ContainerManager.createContainer("players", textField.getText());
                if(playerList != null) {
                    playerList.save();
                    initGui();
                    return;
                }
            } else if(button.equals(buttonRemove) &&
                    selectedList != null) {

            }
        }

        @Override
        public void updateScreen() {
            super.updateScreen();
            textField.updateCursorCounter();
            textFieldPlayer.updateCursorCounter();
        }

        @Override
        public void handleMouseInput() throws IOException {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

            super.handleMouseInput();
            if (playerFileList != null)
                playerFileList.handleMouseInput(mouseX, mouseY);
            if(playerList != null)
                playerList.handleMouseInput(mouseX, mouseY);
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            super.drawScreen(mouseX, mouseY, partialTicks);
            textField.drawTextBox();
            textFieldPlayer.drawTextBox();
            playerFileList.drawScreen(mouseX, mouseY, partialTicks);
            if(playerList != null)
                playerList.drawScreen(mouseX, mouseY, partialTicks);
        }

        @Override
        protected void keyTyped(char eventChar, int eventKey) {
            super.keyTyped(eventChar, eventKey);
            textField.textboxKeyTyped(eventChar, eventKey);
            textFieldPlayer.textboxKeyTyped(eventChar, eventKey);
        }

        @Override
        protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
            super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
            textField.mouseClicked(mouseX, mouseY, clickedMouseButton);
            textFieldPlayer.mouseClicked(mouseX, mouseY, clickedMouseButton);
        }
    }
}
