package com.matt.forgehax.util.entity.mobtypes;

/**
 * Created on 6/27/2017 by fr1kin
 */
public enum MobTypeEnum {
    /**
     * Mob does not attack by default, but will under certain circumstances
     */
    INVALID,

    /**
     * Mob is friendly and will not harm the player
     */
    FRIENDLY,

    /**
     * Mob will attack the player
     */
    HOSTILE
    ;

    public boolean isValid() {
        return ordinal() > 0;
    }
}
