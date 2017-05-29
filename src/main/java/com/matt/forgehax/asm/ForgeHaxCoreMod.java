package com.matt.forgehax.asm;

import com.matt.forgehax.asm.utils.environment.RuntimeState;
import com.matt.forgehax.asm.utils.remapping.FileDumper;
import com.matt.forgehax.asm.utils.remapping.ObfuscatedStateMapper;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class ForgeHaxCoreMod implements IFMLLoadingPlugin, ASMCommon {
    public static boolean isObfuscated = true;
    public static Logger logger;

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {ForgeHaxTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return "com.matt.forgehax.asm.ForgeHaxCoreContainer";
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        if(data.containsKey("runtimeDeobfuscationEnabled")) {
            try {
                Boolean isObfuscated = (Boolean)data.get("runtimeDeobfuscationEnabled");
                ForgeHaxCoreMod.isObfuscated = isObfuscated;
                if(isObfuscated) {
                    RuntimeState.markAsObfuscated();
                } else {
                    RuntimeState.markAsNormal();
                }
                //FileDumper.dumpAllFiles();
            } catch (Exception e) {
                ;
            }
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
