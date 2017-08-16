package com.matt.forgehax.util.command;

/**
 * Created on 6/26/2017 by fr1kin
 */
public class CommandTest {
    public void testBuildCommand() {
        Command command = CommandBuilders.getInstance().newCommandBuilder()
                .name("testCommand")
                .description("a test command")
                .build();
    }
}
