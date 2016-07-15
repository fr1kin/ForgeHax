package com.matt.forgehax.gui.categories;

import com.matt.forgehax.gui.GuiBlockList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class BlockListCategory extends GuiConfigEntries.CategoryEntry {
    public BlockListCategory(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
        super(owningScreen, owningEntryList, configElement);
    }

    @Override
    protected GuiScreen buildChildScreen() {
        /*
        return new GuiBlockManager(this.owningScreen,
                this.configElement.getChildElements(),
                this.owningScreen.modID,
                owningScreen.allRequireWorldRestart || this.configElement.requiresWorldRestart(),
                owningScreen.allRequireMcRestart || this.configElement.requiresMcRestart(),
                this.owningScreen.title,
                ((this.owningScreen.titleLine2 == null ? "" : this.owningScreen.titleLine2) + " > " + this.name)
        );*/
        return new GuiBlockManager(mc, owningScreen);
    }

    public static class GuiBlockManager extends GuiScreen {
        private final GuiScreen parent;
        private GuiBlockList blockList;

        private int left;
        private int right;
        private int top;
        private int bottom;

        public GuiBlockManager(Minecraft mc, GuiScreen parent) {
            this.mc = mc;
            this.parent = parent;
        }

        /*
        public GuiBlockManager(GuiScreen parentScreen, List<IConfigElement> configElements, String modID, boolean allRequireWorldRestart, boolean allRequireMcRestart, String title, String titleLine2) {
            super(parentScreen, configElements, modID, allRequireWorldRestart, allRequireMcRestart, title, titleLine2);
        }
        */

        @Override
        public void initGui() {
            super.initGui();
            width = parent.width;
            height = parent.height;
            left = 0;
            right = 0;
            top = 63;
            bottom = height  - 32;
            blockList = new GuiBlockList(mc, this);
        }

        @Override
        public void onGuiClosed() {
            if (parent instanceof GuiConfig) {
                GuiConfig parentGuiConfig = (GuiConfig) this.parent;
                parentGuiConfig.needsRefresh = true;
                parentGuiConfig.initGui();
            }
            if (!(this.parent instanceof GuiConfig))
                Keyboard.enableRepeatEvents(false);
        }

        @Override
        public void handleKeyboardInput() throws IOException {
            super.handleKeyboardInput();
        }

        @Override
        public void handleMouseInput() throws IOException {
            super.handleInput();
            blockList.handleMouseInput();
        }

        @Override
        protected void mouseClicked(int x, int y, int mouseEvent) throws IOException {
            super.mouseClicked(x, y, mouseEvent);
            blockList.mouseClicked(x, y, mouseEvent);
        }

        @Override
        protected void mouseReleased(int x, int y, int mouseEvent) {
            super.mouseReleased(x, y, mouseEvent);
            blockList.mouseReleased(x, y, mouseEvent);
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            super.drawScreen(mouseX, mouseY, partialTicks);
            blockList.drawScreen(mouseX, mouseY, partialTicks);
            GlStateManager.disableDepth();
            this.overlayBackground(0, top, 255, 255);
            this.overlayBackground(bottom, this.height, 255, 255);
            GlStateManager.enableDepth();
        }

        protected void overlayBackground(int startY, int endY, int startAlpha, int endAlpha)
        {
            Tessellator tessellator = Tessellator.getInstance();
            VertexBuffer vertexbuffer = tessellator.getBuffer();
            this.mc.getTextureManager().bindTexture(Gui.OPTIONS_BACKGROUND);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            float f = 32.0F;
            vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            vertexbuffer.pos((double)left, (double)endY, 0.0D).tex(0.0D, (double)((float)endY / 32.0F)).color(64, 64, 64, endAlpha).endVertex();
            vertexbuffer.pos((double)(left + this.width), (double)endY, 0.0D).tex((double)((float)this.width / 32.0F), (double)((float)endY / 32.0F)).color(64, 64, 64, endAlpha).endVertex();
            vertexbuffer.pos((double)(left + this.width), (double)startY, 0.0D).tex((double)((float)this.width / 32.0F), (double)((float)startY / 32.0F)).color(64, 64, 64, startAlpha).endVertex();
            vertexbuffer.pos((double)left, (double)startY, 0.0D).tex(0.0D, (double)((float)startY / 32.0F)).color(64, 64, 64, startAlpha).endVertex();
            tessellator.draw();
        }
    }
}
