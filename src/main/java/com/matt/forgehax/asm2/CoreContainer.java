package com.matt.forgehax.asm2;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import org.apache.logging.log4j.LogManager;

import java.util.Arrays;

public class CoreContainer extends DummyModContainer {
    public CoreContainer() {
        super(new ModMetadata());

        ModMetadata metaData = super.getMetadata();
        metaData.authorList = Arrays.asList("fr1kin");
        metaData.description = "CoreMod for ForgeHax.";
        metaData.modId = "forgehaxcore";
        metaData.version = "2.0";
        metaData.name = "ForgeHax Core";
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }
}
