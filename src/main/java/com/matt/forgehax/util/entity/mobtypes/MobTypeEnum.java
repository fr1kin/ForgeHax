package com.matt.forgehax.util.entity.mobtypes;

/**
 * Created on 6/27/2017 by fr1kin
 */
public enum MobTypeEnum {
    /**
     * Is a player
     */
    PLAYER,

    /**
     * Mob will attack the player
     */
    HOSTILE,

    /**
     * Mob is friendly and will not harm the player
     */
    FRIENDLY,

    /**
     * Mob does not attack by default, but will under certain circumstances
     */
    INVALID,
    ;

    public boolean isValid() {
        return ordinal() > 0;
    }
}
