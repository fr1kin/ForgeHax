package com.matt.forgehax.util.container;

import com.google.common.collect.Maps;
import com.matt.forgehax.ForgeHaxBase;
import net.minecraftforge.fml.common.ModContainer;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ContainerManager extends ForgeHaxBase {
    public final static File FILTER_DIR = new File(MOD.getBaseDirectory(), "fitlers");

    public final static Map<String, CategoryValue> CATEGORIES = Maps.newHashMap();

    static {
        CATEGORIES.put("xray",          new CategoryValue(new File(FILTER_DIR, "xray"       ), new ContainerFactories.XrayList()));
        CATEGORIES.put("players",       new CategoryValue(new File(FILTER_DIR, "players"    ), new ContainerFactories.PlayerList()));
    }

    public static void initialize() {
        FILTER_DIR.mkdirs();
        for(CategoryValue value : CATEGORIES.values())
            value.file.mkdirs();
    }

    public static Map<String, Object> getContainer(String name) {
        Map<String, Object> map = Maps.newHashMap();
        if(CATEGORIES.containsKey(name)) {
            CategoryValue value = CATEGORIES.get(name);
            File[] files = value.file.listFiles();
            if(files != null) {
                for (File file : files) {
                    String n = file.getName().replace(".json", "");
                    map.put(n, value.factory.newInstance(n, file));
                }
            }
        }
        return map;
    }

    public static Collection<?> getContainerCollection(String name) {
        return getContainer(name).values();
    }

    public static <E extends ContainerList> E createContainer(String category, String name) {
        if(CATEGORIES.containsKey(category)) {
            CategoryValue value = CATEGORIES.get(category);
            String fname = name;
            if(!fname.endsWith(".json")) fname += ".json";
            return (E)value.factory.newInstance(name, new File(value.file, fname));
        } else return null;
    }

    private static class CategoryValue {
        public File file;
        public IContainerFactory factory;

        public CategoryValue(File file, IContainerFactory factory) {
            this.file = file;
            this.factory = factory;
        }
    }
}
