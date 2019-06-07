package com.matt.forgehax.asm.reflection.util.fasttype.builder;


import com.matt.forgehax.asm.utils.environment.RuntimeState;
import com.matt.forgehax.asm.reflection.util.fasttype.FastField;
import com.matt.forgehax.asm.utils.name.IName;
import com.matt.forgehax.asm.utils.name.NameBuilder;
import org.objectweb.asm.Type;

import java.util.Objects;

public class FastFieldBuilder<T> extends FastTypeBuilder<FastFieldBuilder<T>> {
    public static <T> FastFieldBuilder<T> create() {
        return new FastFieldBuilder<>();
    }

    private boolean stripFinal = false;

    public FastTypeBuilder definalize() {
        this.stripFinal = true;
        return this;
    }


    public <V> FastField<V> build() {
        Objects.requireNonNull(insideClass);
        Objects.requireNonNull(name);
        IName<String> iName;
        if (auto && RuntimeState.isSrg()) {
            String parentClassInternalName = Type.getType(insideClass).getInternalName();
            srgName = MAPPER.getSrgFieldName(parentClassInternalName, name);
            iName = NameBuilder.createMultiName(name, srgName);
        } else {
            iName = NameBuilder.createSingleName(name);
        }
        return new FastField<>(insideClass, iName, stripFinal);
    }

}
