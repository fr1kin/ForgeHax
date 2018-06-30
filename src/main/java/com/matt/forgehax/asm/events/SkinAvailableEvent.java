package com.matt.forgehax.asm.events;

import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Created by Babbaj on 2/17/2018.
 */
public class SkinAvailableEvent extends Event {
    public ThreadDownloadImageData texture;

    public SkinAvailableEvent(ThreadDownloadImageData textureIn) {
        this.texture = textureIn;
    }
}
