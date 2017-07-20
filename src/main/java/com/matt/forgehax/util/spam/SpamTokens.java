package com.matt.forgehax.util.spam;

import com.google.common.escape.CharEscaperBuilder;
import com.google.common.escape.Escaper;

/**
 * Created on 7/18/2017 by fr1kin
 */
public enum SpamTokens {
    PLAYER_NAME("PLAYER_NAME")

    ;
    public static Escaper BAD_GOY_PREVENTER = new CharEscaperBuilder()
            .addEscape('\\', "\\\\") //TODO: find out more bad goy methods for attempting escaping
            .toEscaper();

    final String token;

    SpamTokens(String token) {
        this.token = "\\{" + token + "\\}";
    }

    public String getToken() {
        return token;
    }

    public String fill(String str, String with) {
        return str.replaceAll(token, BAD_GOY_PREVENTER.escape(with));
    }
}
