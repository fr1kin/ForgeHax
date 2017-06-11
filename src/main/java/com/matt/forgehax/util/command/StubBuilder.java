package com.matt.forgehax.util.command;

/**
 * Created on 6/8/2017 by fr1kin
 */
public class StubBuilder extends BaseCommandBuilder<StubBuilder, CommandStub> {
    @Override
    public CommandStub build() {
        return new CommandStub(data);
    }
}
