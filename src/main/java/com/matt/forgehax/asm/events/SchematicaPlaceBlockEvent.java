package com.matt.forgehax.asm.events;

import com.matt.forgehax.util.event.Cancelable;
import com.matt.forgehax.util.event.Event;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * Created by Babbaj on 9/20/2017.
 */
public class SchematicaPlaceBlockEvent extends Event implements Cancelable {

    private ItemStack item;
    private BlockPos pos;
    private Vec3d vec;

    public SchematicaPlaceBlockEvent(ItemStack itemIn, BlockPos posIn, Vec3d vecIn) {
        this.item = itemIn;
        this.pos = posIn;
        this.vec = vecIn;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public Vec3d getVec() {
        return this.vec;
    }
}
