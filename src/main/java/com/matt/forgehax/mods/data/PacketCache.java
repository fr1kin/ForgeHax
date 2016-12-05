package com.matt.forgehax.mods.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.network.Packet;
import net.minecraft.util.math.BlockPos;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Created on 12/5/2016 by fr1kin
 */
public class PacketCache {
    public static final Map<Class<?>, List<Field>> PACKET_CLASS_CACHE = Maps.newHashMap();

    public static List<Field> getBlockPosFields(Packet packetIn) {
        List<Field> fieldList = PACKET_CLASS_CACHE.get(packetIn.getClass());
        if(fieldList == null) {
            fieldList = Lists.newArrayList();
            for(Field field : packetIn.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if(field.getType().equals(BlockPos.class)) {
                    fieldList.add(field);
                }
            }
            PACKET_CLASS_CACHE.put(packetIn.getClass(), fieldList);
        }
        return fieldList;
    }
}
