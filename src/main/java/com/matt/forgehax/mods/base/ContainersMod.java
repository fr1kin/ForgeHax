package com.matt.forgehax.mods.base;

import com.matt.forgehax.gui.BlockListCategory;
import com.matt.forgehax.mods.BaseMod;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.io.File;
import java.util.List;

public class ContainersMod extends BaseMod {
    private File fitlerDir = new File(MOD.getConfigFolder(), "fitlers");

    public ContainersMod(String name, String desc) {
        super(name, desc);
    }

    @Override
    public void onConfigBuildGui(List<IConfigElement> elements) {
        super.onConfigBuildGui(elements);
        elements.add(new DummyConfigElement.DummyCategoryElement("Blocks", "", BlockListCategory.class));
    }
}
