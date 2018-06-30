package com.matt.forgehax.asm.events;

import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Created by Babbaj on 2/15/2018.
 */
public class SkinDownloadEvent extends Event {

    public Thread thread;

    public SkinDownloadEvent(Thread t) {
        this.thread = t;
    }

}
