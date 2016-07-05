package com.matt.forgehax.asm;

import net.minecraftforge.fml.common.asm.transformers.AccessTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.io.IOException;

@IFMLLoadingPlugin.SortingIndex(value = 1000)
public class ForgeHaxAccessTransformer extends AccessTransformer {
    public ForgeHaxAccessTransformer() throws IOException {
        super("forgehax_at.cfg");
    }
}
