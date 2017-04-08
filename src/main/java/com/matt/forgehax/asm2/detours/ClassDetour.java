package com.matt.forgehax.asm2.detours;

import com.fr1kin.asmhelper.detours.Detour;
import com.fr1kin.asmhelper.types.ASMClass;
import com.google.common.collect.Lists;
import com.matt.forgehax.asm2.ConstantHooks;
import com.matt.forgehax.asm2.ConstantMc;
import com.matt.forgehax.asm2.Constants;

import java.util.Collections;
import java.util.List;

/**
 * Created on 1/17/2017 by fr1kin
 */
public abstract class ClassDetour implements Constants {
    private final ASMClass detouredClass;
    private final List<Detour> detours = Lists.newArrayList();

    public ClassDetour(ASMClass detouredClass) {
        this.detouredClass = detouredClass;
        initialize();
    }

    public void registerAll(Detour... detours) {
        this.detours.addAll(Lists.newArrayList(detours));
    }

    public ASMClass getDetouredClass() {
        return detouredClass;
    }

    public List<Detour> getDetours() {
        return Collections.unmodifiableList(detours);
    }

    protected abstract void initialize();
}
