package com.matt.forgehax.asm2.replace;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import javax.annotation.Nullable;
import java.io.File;

/**
 * Created on 1/29/2017 by fr1kin
 */
public class Test {
    public static void main(String[] args) {
        File workDir = new File(System.getProperty("user.dir"), "storage_test");
        workDir.mkdirs();

        MapStorage mapStorage = new MapStorage(new TestSaveHandler(workDir));

        for(int i = 0; i < Short.MAX_VALUE; i++) {
            System.out.println("i=" + i + ", id=" + mapStorage.getUniqueDataId("map"));
        }
    }

    private static class TestSaveHandler implements ISaveHandler {
        private final File testDir;

        public TestSaveHandler(File testDir) {
            this.testDir = testDir;
        }

        @Nullable
        @Override
        public WorldInfo loadWorldInfo() {
            return null;
        }

        @Override
        public void checkSessionLock() throws MinecraftException {

        }

        @Override
        public IChunkLoader getChunkLoader(WorldProvider provider) {
            return null;
        }

        @Override
        public void saveWorldInfoWithPlayer(WorldInfo worldInformation, NBTTagCompound tagCompound) {

        }

        @Override
        public void saveWorldInfo(WorldInfo worldInformation) {

        }

        @Override
        public IPlayerFileData getPlayerNBTManager() {
            return null;
        }

        @Override
        public void flush() {

        }

        @Override
        public File getWorldDirectory() {
            return null;
        }

        @Override
        public File getMapFileFromName(String mapName) {
            return new File(testDir, mapName + ".dat");
        }

        @Override
        public TemplateManager getStructureTemplateManager() {
            return null;
        }
    }
}
