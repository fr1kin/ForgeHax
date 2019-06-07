package bspkrs.mmv;

/*
Copyright (C) 2014 bspkrs
Portions Copyright (C) 2014 Alex "immibis" Campbell

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

public class McpBotCommand {
  public enum BotCommand {
    SF,
    SM,
    SP,
    FSF,
    FSM,
    FSP;
  }

  public enum MemberType {
    FIELD,
    METHOD,
    PARAM;
  }

  public static BotCommand getCommand(MemberType type, boolean isForced) {
    switch (type) {
      case METHOD:
        return isForced ? BotCommand.FSM : BotCommand.SM;
      case PARAM:
        return isForced ? BotCommand.FSP : BotCommand.SP;
      default:
        return isForced ? BotCommand.FSF : BotCommand.SF;
    }
  }

  private final BotCommand command;
  private final String srgName;
  private final String newName;
  private final String comment;

  public McpBotCommand(BotCommand command, String srgName, String newName, String comment) {
    this.command = command;
    this.srgName = srgName;
    this.newName = newName;
    this.comment = comment;
  }

  public McpBotCommand(BotCommand command, String srgName, String newName) {
    this(command, srgName, newName, "");
  }

  public static McpBotCommand getMcpBotCommand(
      MemberType type, boolean isForced, String srgName, String newName, String comment) {
    return new McpBotCommand(getCommand(type, isForced), srgName, newName, comment);
  }

  public BotCommand getCommand() {
    return command;
  }

  public String getNewName() {
    return newName;
  }

  @Override
  public String toString() {
    return String.format(
        "!%s %s %s %s", command.toString().toLowerCase(), srgName, newName, comment);
  }
}
