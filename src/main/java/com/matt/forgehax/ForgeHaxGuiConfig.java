package com.matt.forgehax;

import com.google.common.collect.Lists;
import com.matt.forgehax.mods.BaseMod;
import com.matt.forgehax.mods.ToggleMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.List;
import java.util.Map;

public class ForgeHaxGuiConfig extends GuiConfig {
    public ForgeHaxGuiConfig(GuiScreen parent)
    {
        super(parent,
                getConfigElements(),
                ForgeHax.MODID,
                false,
                false,
                "ForgeHax configuration");
        titleLine2 = "settings.txt";
    }

    private static List<IConfigElement> getConfigElements() {
        List<IConfigElement> elements = Lists.newArrayList();
        for(Map.Entry<String,BaseMod> entry : ForgeHax.instance().mods.entrySet()) {
            entry.getValue().onConfigBuildGui(elements);
        }
        return elements;
    }

    @Override
    public void initGui()
    {
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        super.actionPerformed(button);
    }
}
