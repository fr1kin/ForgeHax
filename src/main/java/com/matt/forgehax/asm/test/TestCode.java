package com.matt.forgehax.asm.test;

import com.matt.forgehax.asm.ForgeHaxHooks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.lwjgl.Sys;

/**
 * Created on 9/4/2016 by fr1kin
 */
public class TestCode {
    private boolean isSafeWalkActived = false;
    private boolean onGround = false;

    public boolean isSneaking() {
        return true;
    }

    public void moveEntity() {
        boolean flag = (this.onGround) && (this.isSneaking() || ForgeHaxHooks.isSafeWalkActivated) && this instanceof Object;
        if(flag) {
            System.out.printf("acadsa");
        } else {
            System.out.printf("fdsafdsafda");
        }
    }
}
