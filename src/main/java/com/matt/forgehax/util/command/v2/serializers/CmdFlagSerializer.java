package com.matt.forgehax.util.command.v2.serializers;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.matt.forgehax.util.command.v2.ICmd;
import com.matt.forgehax.util.command.v2.flag.CmdFlags;
import com.matt.forgehax.util.command.v2.flag.ICmdFlag;

import java.io.IOException;

public class CmdFlagSerializer implements ICmdSerializer<ICmd> {
    private static final CmdFlagSerializer INSTANCE = new CmdFlagSerializer();

    public static CmdFlagSerializer getInstance() {
        return INSTANCE;
    }

    //
    //
    //

    @Override
    public void serialize(ICmd instance, JsonWriter writer) throws IOException {
        writer.beginArray(); // [

        for(Enum<? extends ICmdFlag> val : instance.getFlags()) {
            writer.value(CmdFlags.toString(val));
        }

        writer.endArray(); // ]
    }

    @Override
    public void deserialize(ICmd instance, JsonReader reader) throws IOException {
        // remove previous flags
        instance.clearFlags();

        reader.beginArray(); // [

        while(reader.hasNext()) {
            String next = reader.nextName();
            Enum<? extends ICmdFlag> value = CmdFlags.fromString(next);
            if(value != null) instance.addFlag(value);
        }

        reader.endArray(); // ]
    }

    @Override
    public String heading() {
        return "flags";
    }
}
