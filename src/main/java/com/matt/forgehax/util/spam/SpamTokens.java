package com.matt.forgehax.util.spam;

/**
 * Created on 7/18/2017 by fr1kin
 */
public enum SpamTokens {
    PLAYER_NAME("PLAYER_NAME")

    ;
    final String token;

    SpamTokens(String token) {
        this.token = "\\{" + token + "\\}";
    }

    public String getToken() {
        return token;
    }

    public String fill(String str, String with) {
        return str.replaceAll(token, with);
    }
}
