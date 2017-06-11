package com.matt.forgehax.util.typeconverter.types;


import com.matt.forgehax.util.jopt.SafeConverter;
import com.matt.forgehax.util.typeconverter.TypeConverter;

import javax.annotation.Nullable;
import java.util.Comparator;

/**
 * Created on 3/23/2017 by fr1kin
 */
public class ByteType extends TypeConverter<Byte> {
    @Override
    public String label() {
        return "byte";
    }

    @Override
    public Class<Byte> type() {
        return Byte.class;
    }

    @Override
    public Byte parse(String value) {
        return SafeConverter.toByte(value);
    }

    @Override
    public String toString(Byte value) {
        return Byte.toString(value);
    }

    @Nullable
    @Override
    public Comparator<Byte> comparator() {
        return Byte::compare;
    }
}
