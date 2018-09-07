package com.matt.forgehax.util.command.v2.serializers;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.matt.forgehax.util.command.v2.ICmd;
import com.matt.forgehax.util.command.v2.IParentCmd;

import java.io.IOException;

public class CmdChildrenSerializer implements ICmdSerializer<IParentCmd> {
    private static final CmdChildrenSerializer INSTANCE = new CmdChildrenSerializer();

    public static CmdChildrenSerializer getInstance() {
        return INSTANCE;
    }

    //
    //
    //

    @Override
    public void serialize(IParentCmd instance, JsonWriter writer) throws IOException {
        writer.beginObject(); // {

        for (ICmd cmd : instance.getChildren()) {
            writer.name(cmd.getUniqueHeader());
            cmd.serialize(writer);
        }

        writer.endObject(); // }
    }

    @Override
    public void deserialize(IParentCmd instance, JsonReader reader) throws IOException {
        reader.beginObject(); // {

        while (reader.hasNext()) {
            String name = reader.nextName(); // name is by default the absolute name
            ICmd cmd = instance.findChild(name);
            if(cmd != null)
                cmd.deserialize(reader);
            else
                reader.skipValue(); // missing command, skip
        }

        reader.endObject(); // }
    }

    @Override
    public String heading() {
        return "children";
    }
}
