package com.matt.forgehax.gui.categories;

import com.matt.forgehax.gui.GuiCategoryFileList;
import com.matt.forgehax.gui.GuiItemList;
import com.matt.forgehax.util.container.ContainerList;
import com.matt.forgehax.util.container.ContainerManager;
import com.matt.forgehax.util.container.lists.ItemList;
import com.matt.forgehax.util.draw.SurfaceUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import org.lwjgl.input.Mouse;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

public class ItemListCategory extends GuiConfigEntries.CategoryEntry {
    public ItemListCategory(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
        super(owningScreen, owningEntryList, configElement);
    }

    @Override
    protected GuiScreen buildChildScreen() {
        return new GuiItemManager(this.owningScreen,
                this.configElement.getChildElements(),
                this.owningScreen.modID,
                owningScreen.allRequireWorldRestart || this.configElement.requiresWorldRestart(),
                owningScreen.allRequireMcRestart || this.configElement.requiresMcRestart(),
                this.owningScreen.title,
                ((this.owningScreen.titleLine2 == null ? "" : this.owningScreen.titleLine2) + " > " + this.name)
        );
    }

    public static class GuiItemManager extends GuiConfig implements IGuiCategory {
        private final List<ItemStack> ALL_ITEMS = ItemList.getRegisteredItems();

        private GuiCategoryFileList guiCategoryFileList;
        private GuiItemList guiItemList;

        public ItemList currentlySelectedList;

        private int fileListX;
        private int fileListY;
        private int fileListW;
        private int fileListH;

        // For adding/removing files
        private GuiButton buttonAdd;
        private GuiButton buttonRemove;
        private GuiTextField textField;

        private final String title2Original;

        public GuiItemManager(GuiScreen parentScreen, List<IConfigElement> configElements, String modID, boolean allRequireWorldRestart, boolean allRequireMcRestart, String title, String titleLine2) {
            super(parentScreen, configElements, modID, allRequireWorldRestart, allRequireMcRestart, title, titleLine2);
            title2Original = titleLine2;
        }

        public void refreshGuiList(@Nullable ItemList selectedList) {
            int x = fileListX + fileListW + 10;
            int y = fileListY;
            int w = width - x - 5;
            int h = height - y - 40 - 25;
            guiItemList = new GuiItemList(this,
                    x,
                    y,
                    w,
                    h,
                    43,
                    width,
                    height,
                    ALL_ITEMS,
                    selectedList
            );
            if(selectedList != null) {
                titleLine2 = title2Original + " > " + selectedList.getName();
            }
        }

        public void updateButtonLocks() {
            buttonRemove.enabled = currentlySelectedList != null;
            buttonAdd.enabled = !textField.getText().isEmpty();
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
                    ContainerManager.getContainerCollection(ContainerManager.Category.ITEMS)
            );
            guiCategoryFileList.setSelected(currentlySelectedList);
            fileListX = x;
            fileListY = y;
            fileListW = w;
            fileListH = h;
            int ID = 3000;
            int buttonWidth = 50;
            // remove file button
            buttonList.add(buttonRemove = new GuiButton(ID++, x, y + h + 5, w, 20, "Remove"));
            // add file button
            buttonList.add(buttonAdd = new GuiButton(ID++, x, y + h + 30, buttonWidth, 20, "Add"));
            // file name
            textField = new GuiTextField(ID++, mc.fontRenderer, x + buttonWidth + 5, y + h + 30, w - (x + buttonWidth + 5), 20);
            textField.setMaxStringLength(256); // MAX_FILE_SIZE
            textField.setEnabled(true);
            textField.setFocused(true);
            textField.setText("");
            // list of players in selected file
            refreshGuiList(currentlySelectedList);
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
                    currentlySelectedList != null) {
                if(ContainerManager.removeContainerList(ContainerManager.Category.PLAYERS, currentlySelectedList)) {
                    // unselect element
                    currentlySelectedList = null;
                    // reload gui
                    initGui();
                }
            }
        }

        @Override
        public void updateScreen() {
            super.updateScreen();
            textField.updateCursorCounter();
        }

        @Override
        public void handleMouseInput() throws IOException {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;

            super.handleMouseInput();
            if (guiCategoryFileList != null)
                guiCategoryFileList.handleMouseInput(mouseX, mouseY);
            if(guiItemList != null)
                guiItemList.handleMouseInput(mouseX, mouseY);
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            super.drawScreen(mouseX, mouseY, partialTicks);
            guiCategoryFileList.drawScreen(mouseX, mouseY, partialTicks);
            if(guiItemList != null)
                guiItemList.drawScreen(mouseX, mouseY, partialTicks);
            textField.drawTextBox();
        }

        @Override
        protected void keyTyped(char eventChar, int eventKey) {
            super.keyTyped(eventChar, eventKey);
            textField.textboxKeyTyped(eventChar, eventKey);
        }

        @Override
        protected void mouseClicked(int x, int y, int mouseEvent) throws IOException {
            super.mouseClicked(x, y, mouseEvent);
            textField.mouseClicked(x, y, mouseEvent);
        }

        @Override
        public void onSelectedFileFromList(Object selected) {

        }

        @Override
        public void addToSelected(final Object obj) {

        }
    }
}
