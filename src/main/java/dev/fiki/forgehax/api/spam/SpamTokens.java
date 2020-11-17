package dev.fiki.forgehax.api.spam;

import java.util.regex.Matcher;

/**
 * Created on 7/18/2017 by fr1kin
 */
public enum SpamTokens {
  /**
   * The main subjects name
   */
  PLAYER_NAME("PLAYER_NAME"),
  
  /**
   * History of the main subjects name
   */
  NAME_HISTORY("NAME_HISTORY"),
  
  /**
   * Person sending the message
   */
  SENDER_NAME("SENDER_NAME"),
  
  /**
   * Message
   */
  MESSAGE("MESSAGE"),
  ;
  
  public static SpamTokens[] ALL =
      new SpamTokens[]{PLAYER_NAME, NAME_HISTORY, SENDER_NAME, MESSAGE};
  public static SpamTokens[] PLAYERNAME_NAMEHISTORY = new SpamTokens[]{PLAYER_NAME, NAME_HISTORY};
  public static SpamTokens[] PLAYERNAME_SENDERNAME = new SpamTokens[]{PLAYER_NAME, SENDER_NAME};
  
  final String token;
  
  SpamTokens(String token) {
    this.token = "\\{" + token + "\\}";
  }
  
  public String fill(String str, String with) {
    return str.replaceAll(token, Matcher.quoteReplacement(with));
  }
  
  public static String fillAll(String str, SpamTokens[] tokens, String... replacements) {
    if (replacements.length != tokens.length) {
      throw new IllegalArgumentException("replacements length != tokens length");
    }
    for (int i = 0; i < replacements.length; i++) {
      str = tokens[i].fill(str, replacements[i]);
    }
    return str;
  }
}
