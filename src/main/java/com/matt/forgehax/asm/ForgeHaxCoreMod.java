package com.matt.forgehax.asm;

import com.matt.forgehax.asm.utils.ASMStackLogger;
import com.matt.forgehax.asm.utils.environment.RuntimeState;
import com.matt.forgehax.asm.utils.remapping.FileDumper;
import com.matt.forgehax.asm.utils.remapping.ObfuscatedStateMapper;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class ForgeHaxCoreMod implements IFMLLoadingPlugin, ASMCommon {
    @Override
    public String[] getASMTransformerClass() {
        return new String[] {ForgeHaxTransformer.class.getName()};
    }

    @Override
    public String getModContainerClass() {
        return null;
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
                if(isObfuscated) {
                    RuntimeState.markAsObfuscated();
                } else {
                    RuntimeState.markAsNormal();
                }
                //FileDumper.dumpAllFiles();
            } catch (Exception e) {
                LOGGER.error("Failed to obtain runtimeDeobfuscationEnabled: " + e.getMessage());
                ASMStackLogger.printStackTrace(e);
            }
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
