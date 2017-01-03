package com.matt.forgehax.asm.test;

import com.matt.forgehax.asm.ForgeHaxHooks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.lwjgl.Sys;

import java.util.Random;

/**
 * Created on 9/4/2016 by fr1kin
 */
public class TestCode {
    private static boolean isNoSlowOn = false;

    private boolean movementInput = false;
    private boolean onGround = false;

    private double stepHeight = 0;

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
        double d3 = 0, y = 0, d2 = 0, d4 = 0, x = 0, z = 0;

        boolean flag = this.onGround || d3 != y && d3 < 0.0D;

        if (this.stepHeight > 0.0F && flag && (d2 != x || d4 != z)) {
            this.movementInput = false;
        }
    }
}
