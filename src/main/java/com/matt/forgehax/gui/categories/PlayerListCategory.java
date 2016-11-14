package com.matt.forgehax.gui.categories;

import com.google.gson.JsonElement;
import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.gui.GuiAddPlayer;
import com.matt.forgehax.gui.GuiEditPlayer;
import com.matt.forgehax.gui.GuiPlayerList;
import com.matt.forgehax.gui.GuiCategoryFileList;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.draw.SurfaceUtils;
import com.matt.forgehax.util.container.ContainerList;
import com.matt.forgehax.util.container.ContainerManager;
import com.matt.forgehax.util.container.lists.PlayerList;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import org.lwjgl.input.Mouse;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    public static class GuiPlayerManager extends GuiConfig implements IGuiCategory {
        private GuiCategoryFileList guiCategoryFileList;
        private GuiPlayerList guiPlayerList;

        public PlayerList selectedList;

        private int fileListX;
        private int fileListY;
        private int fileListW;
        private int fileListH;

        private GuiButton buttonAdd;
        private GuiButton buttonRemove;
        private GuiTextField textField;

        private GuiButton buttonAddPlayer;
        private GuiButton buttonRemovePlayer;
        private GuiButton buttonEditPlayer;

        private final String title2Original;

        // status of adding a player
        public String statusMessage = "";
        // error when trying to add a player
        public String errorMessage = "";

        // set in the add player thread to refresh the gui when
        // complete
        public boolean doRefresh = false;

        // check if a player is currently being added to the list
        public boolean isCurrentlyAddingPlayer = false;

        public GuiPlayerManager(GuiScreen parentScreen, List<IConfigElement> configElements, String modID, boolean allRequireWorldRestart, boolean allRequireMcRestart, String title, String titleLine2) {
            super(parentScreen, configElements, modID, allRequireWorldRestart, allRequireMcRestart, title, titleLine2);
            title2Original = titleLine2;
        }

        public void refreshGuiList(@Nullable PlayerList selectedList) {
            int x = fileListX + fileListW + 10;
            int y = fileListY;
            int w = width - x - 5;
            int h = height - y - 40 - 25;
            guiPlayerList = new GuiPlayerList(this,
                    x,
                    y,
                    w,
                    h,
                    43,
                    width,
                    height,
                    selectedList
            );
            if(selectedList != null) {
                titleLine2 = title2Original + " > " + selectedList.getName();
            }
            updateButtonLocks();
        }

        public void updateButtonLocks() {
            buttonRemove.enabled = selectedList != null;
            buttonAdd.enabled = !textField.getText().isEmpty();
            buttonAddPlayer.enabled = selectedList != null && !isCurrentlyAddingPlayer;
            buttonRemovePlayer.enabled = buttonEditPlayer.enabled = guiPlayerList != null && guiPlayerList.getCurrentlySelected() != null;
        }

        @Override
        public void initGui() {
            buttonList.clear();
            super.initGui();
            int x = 5;
            int y = 40;
            int w = parentScreen.width / 5;
            int h = parentScreen.height / 4;
            // list of files found
            guiCategoryFileList = new GuiCategoryFileList(
                    this,
                    x, // x
                    y, // y
                    w, // w
                    h, // h
                    SurfaceUtils.getTextHeight() + 2, // entryHeight
                    width,
                    height,
                    ContainerManager.getContainerCollection(ContainerManager.Category.PLAYERS)
            );
            guiCategoryFileList.setSelected(selectedList);

            fileListX = x;
            fileListY = y;
            fileListW = w;
            fileListH = h;

            // starting button ID index
            int ID = 3000;
            // width of the add button
            int buttonWidth = 50;

            // remove file button
            buttonList.add(buttonRemove = new GuiButton(ID++, x, y + h + 5, w, 20, "Remove"));
            // add file button
            buttonList.add(buttonAdd = new GuiButton(ID++, x, y + h + 30, buttonWidth, 20, "Add"));

            // file name text field
            textField = new GuiTextField(ID++, mc.fontRendererObj, x + buttonWidth + 5, y + h + 30, w - (x + buttonWidth + 5), 20);
            textField.setMaxStringLength(256); // MAX_FILE_SIZE
            textField.setEnabled(true);
            textField.setFocused(true);
            textField.setText("");

            // button to add player to list
            buttonList.add(buttonAddPlayer = new GuiButton(ID++,
                            fileListX + fileListW + 10,
                            height - y - 20,
                            100,
                            20,
                            "Add")
            );
            // button to remove player selected from list
            buttonList.add(buttonRemovePlayer = new GuiButton(ID++,
                            fileListX + fileListW + 10 + 105,
                            height - y - 20,
                            100,
                            20,
                            "Remove")
            );
            // button to edit player selected in list
            buttonList.add(buttonEditPlayer = new GuiButton(ID++,
                            fileListX + fileListW + 10 + 105 + 105,
                            height - y - 20,
                            100,
                            20,
                            "Edit")
            );
            // list of players in selected file
            refreshGuiList(selectedList);
        }

        @Override
        protected void actionPerformed(GuiButton button) {
            super.actionPerformed(button);
            if(button.equals(buttonAdd) &&
                    !textField.getText().isEmpty()) {
                ContainerList list = (ContainerList)ContainerManager.createContainerList(ContainerManager.Category.PLAYERS, textField.getText());
                if(list != null) {
                    // save file
                    list.save();
                    // reload gui
                    initGui();
                }
            } else if(button.equals(buttonRemove) &&
                    selectedList != null) {
                if(ContainerManager.removeContainerList(ContainerManager.Category.PLAYERS, selectedList)) {
                    // unselect element
                    selectedList = null;
                    // reload gui
                    initGui();
                }
            } else if(button.equals(buttonAddPlayer) &&
                    selectedList != null) {
                // pop up a new screen to insert players name
                FMLClientHandler.instance().displayGuiScreen(mc.thePlayer, new GuiAddPlayer(this));
            } else if(button.equals(buttonRemovePlayer) &&
                    selectedList != null) {
                // get currently selected player in list
                Map.Entry<String, JsonElement> selectedPlayer = guiPlayerList.getCurrentlySelected();
                if(selectedPlayer != null) {
                    // if not null, remove player from list
                    selectedList.removePlayerByUUID(selectedPlayer.getKey());
                    // reload gui
                    initGui();
                }
            } else if(button.equals(buttonEditPlayer) &&
                    selectedList != null) {
                // get currently selected player in list
                Map.Entry<String, JsonElement> selectedPlayer = guiPlayerList.getCurrentlySelected();
                FMLClientHandler.instance().displayGuiScreen(mc.thePlayer, new GuiEditPlayer(this, selectedList.getPlayerData(selectedPlayer.getKey())));
            }
        }

        @Override
        public void updateScreen() {
            if(doRefresh) {
                initGui();
                doRefresh = false;
                statusMessage = "";
                errorMessage = "";
            } else {
                super.updateScreen();
                textField.updateCursorCounter();
            }
        }

        @Override
        public void handleMouseInput() throws IOException {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

            super.handleMouseInput();
            if (guiCategoryFileList != null)
                guiCategoryFileList.handleMouseInput(mouseX, mouseY);
            if(guiPlayerList != null)
                guiPlayerList.handleMouseInput(mouseX, mouseY);
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            super.drawScreen(mouseX, mouseY, partialTicks);
            guiCategoryFileList.drawScreen(mouseX, mouseY, partialTicks);
            if(guiPlayerList != null)
                guiPlayerList.drawScreen(mouseX, mouseY, partialTicks);
            textField.drawTextBox();
            int x = 1;
            int y = 1;
            if(!statusMessage.isEmpty()) {
                SurfaceUtils.drawTextShadow(statusMessage, x, y, Utils.Colors.GREEN);
                y += SurfaceUtils.getTextHeight() + 1;
            }
            if(!errorMessage.isEmpty()) {
                SurfaceUtils.drawTextShadow(errorMessage, x, y, Utils.Colors.RED);
            }
        }

        @Override
        protected void keyTyped(char eventChar, int eventKey) {
            super.keyTyped(eventChar, eventKey);
            textField.textboxKeyTyped(eventChar, eventKey);
            updateButtonLocks();
        }

        @Override
        protected void mouseClicked(int x, int y, int mouseEvent) throws IOException {
            super.mouseClicked(x, y, mouseEvent);
            textField.mouseClicked(x, y, mouseEvent);
        }

        /**
         * Called when a new file is selected
         * @param selected
         */
        @Override
        public void onSelectedFileFromList(Object selected) {
            if(selected instanceof PlayerList) {
                selectedList = (PlayerList)selected;
                refreshGuiList(selectedList);
            } else if(selected == null) {
                selectedList = null;
                refreshGuiList(null);
            }
        }

        @Override
        public void addToSelected(final Object obj) {
            if(obj instanceof String &&
                    selectedList != null) {
                isCurrentlyAddingPlayer = true;
                statusMessage = "Getting player data from Mojang servers...";
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (this) {
                            try {
                                String name = (String) obj;
                                selectedList.addPlayerByName(name);
                            } catch (Exception e) {
                                errorMessage = e.getMessage();
                                ForgeHax.instance().printStackTrace(e);
                            } finally {
                                doRefresh = true;
                                isCurrentlyAddingPlayer = false;
                            }
                        }
                    }
                }).start();
            }
        }
    }
}
