package com.matt.forgehax.gui;

import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.container.lists.PlayerList;
import com.matt.forgehax.util.draw.SurfaceUtils;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.config.GuiConfig;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;
import java.io.IOException;

public class GuiEditPlayer extends GuiScreen {
    private final static String STRING_ADD_NICK = "Enter nick name";

    private final GuiScreen parent;
    private final PlayerList.PlayerData playerData;

    private ResourceLocation playerSkin;

    private GuiButton buttonSave;
    private GuiButton buttonBack;

    private GuiTextField textFieldPlayerNickName;

    public GuiEditPlayer(@Nullable GuiScreen parent, PlayerList.PlayerData playerData) {
        this.parent = parent;
        if(parent != null)
            this.mc = parent.mc;
        this.playerData = playerData;
        ResourceLocation resourceLocation = AbstractClientPlayer.getLocationSkin(playerData.getName());
        AbstractClientPlayer.getDownloadImageSkin(resourceLocation, playerData.getName());
        playerSkin = resourceLocation;
    }

    public void exitScreen() {
        FMLClientHandler.instance().displayGuiScreen(mc.player, parent);
        if(parent instanceof GuiConfig) {
            ((GuiConfig) parent).needsRefresh = true;
        }
    }

    public boolean isValidTextInField() {
        return !textFieldPlayerNickName.getText().isEmpty() &&
                !textFieldPlayerNickName.getText().equals(STRING_ADD_NICK);
    }

    @Override
    public void initGui() {
        width = parent.width;
        height = parent.height;

        int x = (width / 2);
        int y = (height / 2) + 100;

        int textBoxWidth = 200;
        int textBoxHeight = 20;

        int posX = x - (textBoxWidth / 2);
        int posY = y - textBoxHeight;

        textFieldPlayerNickName = new GuiTextField(4000, mc.fontRendererObj, posX, posY, textBoxWidth, textBoxHeight);
        textFieldPlayerNickName.setMaxStringLength(256); // MAX_FILE_SIZE
        textFieldPlayerNickName.setEnabled(true);
        textFieldPlayerNickName.setFocused(true);
        textFieldPlayerNickName.setText(!playerData.getNickName().isEmpty() ? playerData.getNickName() : STRING_ADD_NICK);

        posX = x - (105 / 2);
        posY = y + 25;

        buttonList.add(buttonSave = new GuiButton(4001, posX, posY, 50, 20, "Save"));
        buttonList.add(buttonBack = new GuiButton(4002, posX + 55, posY, 50, 20, "Back"));
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
    protected void actionPerformed(GuiButton button) throws IOException {
        if(button.equals(buttonSave)) {
            if(isValidTextInField()) {
                playerData.setNickName(textFieldPlayerNickName.getText());
                playerData.save();
            }
            exitScreen();
        } else if(button.equals(buttonBack)) {
            exitScreen();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if(keyCode == Keyboard.KEY_ESCAPE) {
            FMLClientHandler.instance().displayGuiScreen(mc.player, parent);
        } else {
            textFieldPlayerNickName.textboxKeyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        textFieldPlayerNickName.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        textFieldPlayerNickName.updateCursorCounter();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        textFieldPlayerNickName.drawTextBox();

        String nickName = playerData.getNickName();
        if(isValidTextInField())
            nickName = textFieldPlayerNickName.getText();

        String renderName = !nickName.isEmpty() ? String.format("%s (%s)", playerData.getName(), nickName) : playerData.getName();
        String uuid = playerData.getUuid().toString();

        // get longest width
        int longestWidth = SurfaceUtils.getTextWidth(uuid);
        if(SurfaceUtils.getTextWidth(renderName) > longestWidth)
            longestWidth = SurfaceUtils.getTextWidth(renderName);

        int faceWidth = SurfaceUtils.getHeadWidth(3.f);

        // player details width and height
        // 2 = buffer between face and text
        int pdWidth = longestWidth + faceWidth + 2;

        int x = (width / 2);
        int y = (height / 2);

        // player details
        int posX = x - (pdWidth / 2);
        int posY = y - 200;

        if(playerSkin != null)
            SurfaceUtils.drawHead(playerSkin, posX, posY, 3.f);

        SurfaceUtils.drawText(renderName, posX + SurfaceUtils.getHeadWidth(3.f) + 2, posY, Utils.Colors.WHITE);
        SurfaceUtils.drawText(uuid, posX + SurfaceUtils.getHeadWidth(3.f) + 2, posY + SurfaceUtils.getTextHeight() + 1, Utils.Colors.WHITE);
    }
}
