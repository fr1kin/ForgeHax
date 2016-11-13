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
    private static boolean isNoSlowOn = false;

    private boolean movementInput = false;
    private boolean onGround = false;

    public boolean isSneaking() {
        return true;
    }

    public boolean isHandActive() {
        return true;
    }

    public boolean isRiding() {
        return true;
    }

    public void moveEntity() {
        this.isSneaking();

        if (this.isHandActive() && !this.isRiding() && !isNoSlowOn)
        {
            this.movementInput = false;
        }

        boolean flag3 = false;
    }
}
