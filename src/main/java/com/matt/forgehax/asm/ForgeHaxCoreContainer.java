package com.matt.forgehax.asm;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import org.apache.logging.log4j.LogManager;

import java.util.Arrays;

public class ForgeHaxCoreContainer extends DummyModContainer {
    public ForgeHaxCoreContainer() {
        super(new ModMetadata());

        ModMetadata metaData = super.getMetadata();
        metaData.authorList = Arrays.asList("fr1kin");
        metaData.description = "CoreMod for ForgeHax.";
        metaData.modId = "forgehaxcore";
        metaData.version = "1.0";
        metaData.name = "ForgeHax Core";
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        ForgeHaxCoreMod.logger = LogManager.getLogger(FMLCommonHandler.instance().findContainerFor(this));
        return true;
    }
}
