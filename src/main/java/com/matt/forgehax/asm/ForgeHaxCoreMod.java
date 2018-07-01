package com.matt.forgehax.asm;

import com.matt.forgehax.asm.utils.ASMStackLogger;
import com.matt.forgehax.asm.utils.environment.RuntimeState;
import net.futureclient.asm.AsmLibApi;
import net.futureclient.asm.obfuscation.IMapper;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.util.Map;

public class ForgeHaxCoreMod implements IFMLLoadingPlugin, ASMCommon {

    static {
        AsmLibApi.init();
        net.futureclient.asm.obfuscation.RuntimeState.setRemapper(new IMapper() {
            @Nullable
            @Override
            public String getClassName(String className) {
                switch(RuntimeState.getState()) {
                    case NORMAL:
                        return MAPPER.getMcpClassName(className);
                    case OBFUSCATED:
                        return MAPPER.getObfClassName(className);
                    default:
                        return className;
                }
            }

            @Nullable
            @Override
            public String getMethodName(String parentClassName, String methodName, String methodDescriptor) {
                switch(RuntimeState.getState()) {
                    case NORMAL:
                        return methodName;
                    case OBFUSCATED:
                        return MAPPER.getObfMethodName(parentClassName, methodName, methodDescriptor);
                    default:
                        return MAPPER.getSrgMethodName(parentClassName, methodName, methodDescriptor);
                }
            }

            @Nullable
            @Override
            public String getFieldName(String parentClassName, String fieldName) {
                switch(RuntimeState.getState()) {
                    case NORMAL:
                        return fieldName;
                    case OBFUSCATED:
                        return MAPPER.getObfFieldName(parentClassName, fieldName);
                    default:
                        return fieldName;
                }
            }
        });
        AsmLibApi.addConfig("forgehax_config.json");
    }

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
                    RuntimeState.markDefaultAsObfuscated();
                } else {
                    RuntimeState.markDefaultAsNormal();
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
