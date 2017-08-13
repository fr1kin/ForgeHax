package com.matt.forgehax.asm.events;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Created on 6/15/2017 by fr1kin
 */
public class LocalPlayerUpdateMovementEvent extends Event {
    public static class Pre extends LocalPlayerUpdateMovementEvent {}
    public static class Post extends LocalPlayerUpdateMovementEvent {}
}
