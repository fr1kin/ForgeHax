package com.matt.forgehax.util.spam;

import com.google.common.escape.CharEscaperBuilder;
import com.google.common.escape.Escaper;

/**
 * Created on 7/18/2017 by fr1kin
 */
public enum SpamTokens {
    PLAYER_NAME("PLAYER_NAME"),
    NAME_HISTORY("NAME_HISTORY"),
    MESSAGE("MESSAGE"),
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

    public static String fillAll(String str, SpamTokens[] tokens, String... replacements) {
        if(replacements.length != tokens.length) throw new IllegalArgumentException("replacements length != tokens length");
        for(int i = 0; i < replacements.length; i++) {
            str = tokens[i].fill(str, replacements[i]);
        }
        return str;
    }
}
