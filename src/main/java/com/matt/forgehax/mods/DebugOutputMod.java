package com.matt.forgehax.mods;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.matt.forgehax.asm.ForgeHaxCoreMod;
import com.matt.forgehax.asm.ForgeHaxHooks;

import java.util.List;
import java.util.Map;

public class DebugOutputMod extends ToggleMod {
    public DebugOutputMod(String modName, boolean defaultValue, String description, int key) {
        super(modName, defaultValue, description, key);
    }

    @Override
    public void onEnabled() {
        if(ForgeHaxHooks.isInDebugMode) {
            Map<String, List<String>> report = Maps.newLinkedHashMap();
            for (Map.Entry<String, ForgeHaxHooks.DebugData> entry : ForgeHaxHooks.responding.entrySet()) {
                //data.isResponding = false;
                for (String className : entry.getValue().parentClassNames) {
                    if(entry.getValue().log != null) {
                        if (!report.containsKey(className)) {
                            report.put(className, Lists.newArrayList(entry.getValue().log));
                        }
                    }
                }
            }
            List<String> printReport = Lists.newArrayList();
            for (List<String> reports : report.values()) {
                printReport.addAll(reports);
            }
            printReport.add(0, "##############################\n");
            printReport.add(1, "HOOK REPORT\n");
            printReport.add(2, "______________________________\n");
            printReport.add("##############################\n");
            ForgeHaxCoreMod.print(printReport);
        }
    }

    @Override
    public void onDisabled() {

    }
}
