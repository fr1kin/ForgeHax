package com.matt.forgehax.asm2.detours;

import com.fr1kin.asmhelper.detours.Detour;
import com.fr1kin.asmhelper.types.ASMClass;
import com.google.common.collect.Lists;
import com.matt.forgehax.asm2.constants.ForgeHaxConstants;
import com.matt.forgehax.asm2.constants.McpConstants;

import java.util.Collections;
import java.util.List;

/**
 * Created on 1/17/2017 by fr1kin
 */
public abstract class ClassDetour {
    protected static final McpConstants MCP            = McpConstants.getInstance();
    protected static final ForgeHaxConstants HOOKS     = ForgeHaxConstants.getInstance();

    private final ASMClass detouredClass;
    private final List<Detour> detours = Lists.newArrayList();

    public ClassDetour(ASMClass detouredClass) {
        this.detouredClass = detouredClass;
        initialize();
    }

    public void register(Detour detour) {
        detours.add(detour);
    }

    public ASMClass getDetouredClass() {
        return detouredClass;
    }

    public List<Detour> getDetours() {
        return Collections.unmodifiableList(detours);
    }

    protected abstract void initialize();
}
