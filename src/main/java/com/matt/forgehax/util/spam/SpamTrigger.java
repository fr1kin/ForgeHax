package com.matt.forgehax.util.spam;

/**
 * Created on 7/19/2017 by fr1kin
 */
public enum SpamTrigger {
    /**
     * Triggered every X amount of time
     */
    SPAM,
    /**
     * Triggered when a player enters the keyword
     */
    REPLY,
    REPLY_WITH_INPUT,

    PLAYER_CONNECT,
    PLAYER_DISCONNECT
    ;
}
