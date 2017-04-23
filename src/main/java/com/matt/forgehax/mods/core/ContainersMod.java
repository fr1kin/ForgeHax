package com.matt.forgehax.mods.core;

import com.google.common.collect.Lists;
import com.matt.forgehax.gui.categories.ItemListCategory;
import com.matt.forgehax.gui.categories.PlayerListCategory;
import com.matt.forgehax.mods.BaseMod;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.GuiConfigEntries;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.List;

public class ContainersMod extends BaseMod {
    public static List<IConfigElement> getContainers() {
        List<IConfigElement> elements = Lists.newArrayList();
        //elements.add(new DummyConfigElement.DummyCategoryElement("Blocks", "", ItemListCategory.class));
        elements.add(new DummyConfigElement.DummyCategoryElement("Players", "", PlayerListCategory.class));
        return elements;
    }

    public ContainersMod(String name, String desc) {
        super(name, desc);
        setHidden(true);
    }

    @Override
    public void onConfigBuildGui(List<IConfigElement> elements) {
        elements.add(new DummyConfigElement.DummyCategoryElement(getModName(), "", GuiContainer.class));
    }

    public static class GuiContainer extends GuiConfigEntries.CategoryEntry {
        public GuiContainer(GuiConfig owningScreen, GuiConfigEntries owningEntryList, IConfigElement configElement) {
            super(owningScreen, owningEntryList, configElement);
        }

        @Override
        protected GuiScreen buildChildScreen()
        {
            // This GuiConfig object specifies the configID of the object and as such will force-save when it is closed. The parent
            // GuiConfig object's entryList will also be refreshed to reflect the changes.
            return new GuiConfig(this.owningScreen,
                    getContainers(),
                    this.owningScreen.modID,
                    Configuration.CATEGORY_GENERAL,
                    this.configElement.requiresWorldRestart() || this.owningScreen.allRequireWorldRestart,
                    this.configElement.requiresMcRestart() || this.owningScreen.allRequireMcRestart,
                    ((this.owningScreen.titleLine2 == null ? "" : this.owningScreen.titleLine2) + " > " + this.name)
            );
        }
    }
}
