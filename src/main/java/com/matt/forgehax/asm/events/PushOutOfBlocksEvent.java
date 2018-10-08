package com.matt.forgehax.asm.events;

import com.matt.forgehax.util.event.Cancelable;
import com.matt.forgehax.util.event.Event;

/**
 * Created by Babbaj on 8/4/2017.
 *
 * Called every time the player updates
 * cancel to stop getting pushed out of blocks
 */
public class PushOutOfBlocksEvent extends Event implements Cancelable {
}
