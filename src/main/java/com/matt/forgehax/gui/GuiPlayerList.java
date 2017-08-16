package com.matt.forgehax.gui;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.matt.forgehax.gui.categories.PlayerListCategory;
import com.matt.forgehax.util.draw.SurfaceUtils;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.container.lists.PlayerList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.GuiScrollingList;

import java.util.List;
import java.util.Map;

public class GuiPlayerList extends GuiScrollingList {
    private Minecraft MC;
    private GuiScreen parent;

    private PlayerList playerList;
    private List<Map.Entry<String, JsonElement>> selectedPlayerList = Lists.newArrayList();

    private List<ResourceLocation> playerSkins = Lists.newArrayList();

    private int selectedIndex = -1;

    public GuiPlayerList(GuiScreen parent, int x, int y, int width, int height, int slotHeight, int screenWidth, int screenHeight, PlayerList selectedList) {
        super(
                parent.mc,
                width,
                height,
                y,
                y + height,
                x,
                slotHeight,
                screenWidth,
                screenHeight
        );
        MC = parent.mc;
        this.parent = parent;
        if(selectedList != null) {
            playerList = selectedList;
            for (Map.Entry<String, JsonElement> entry : selectedList.entrySet()) {
                selectedPlayerList.add(entry);
                PlayerList.PlayerData data = new PlayerList.PlayerData(entry.getValue().getAsJsonObject()).setUuid(entry.getKey());
                ResourceLocation resourceLocation = AbstractClientPlayer.getLocationSkin(data.getName());
                AbstractClientPlayer.getDownloadImageSkin(resourceLocation, data.getName());
                playerSkins.add(resourceLocation);
            }
        }
    }

    public boolean isValidIndex(int index) {
        return index > -1 &&
                index < selectedPlayerList.size();
    }

    public Map.Entry<String, JsonElement> getCurrentlySelected() {
        return isValidIndex(selectedIndex) ? selectedPlayerList.get(selectedIndex) : null;
    }

    public int getSize() {
        return selectedPlayerList.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) {
        selectedIndex = index;
        if(parent instanceof PlayerListCategory.GuiPlayerManager)
            ((PlayerListCategory.GuiPlayerManager) parent).updateButtonLocks();
    }

    @Override
    protected boolean isSelected(int index) {
        return selectedIndex == index;
    }

    @Override
    protected void drawBackground() {
        int scale = 2;
        SurfaceUtils.drawRect(left - scale, top - scale, listWidth + 2 * scale, listHeight + 2 * scale, Utils.Colors.BLACK);
    }

    @Override
    protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
        int x = entryRight - listWidth + (slotBuffer / 2);
        int y = slotTop + 2;

        Map.Entry<String, JsonElement> selected = selectedPlayerList.get(slotIdx);
        if(selected != null) {
            PlayerList.PlayerData data = playerList.getPlayerData(selected.getKey());
            String name = data.getGuiName();
            String uuid = data.getUuid().toString();
            ResourceLocation skin = playerSkins.get(slotIdx);
            if(skin != null) {
                SurfaceUtils.drawHead(skin, x, y, 3);
                x += 3 * 12 + 2;
            }
            SurfaceUtils.drawText(name, x, y, Utils.Colors.WHITE);
            SurfaceUtils.drawText(uuid, x, y + SurfaceUtils.getTextHeight() + 1, Utils.Colors.WHITE);
        }
    }
}
