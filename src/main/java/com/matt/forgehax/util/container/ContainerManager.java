package com.matt.forgehax.util.container;

import com.google.common.collect.Maps;
import com.matt.forgehax.ForgeHaxBase;

import java.io.File;
import java.util.*;

public class ContainerManager extends ForgeHaxBase {
    public final static File FILTER_DIR = new File(MOD.getBaseDirectory(), "fitlers");

    public enum Category {
        ITEMS,
        PLAYERS
    }

    public final static Map<Category, CategoryValue> CATEGORIES = Maps.newHashMap();

    static {
        CATEGORIES.put(Category.ITEMS,          new CategoryValue(new File(FILTER_DIR, Category.ITEMS.name().toLowerCase()),        new ContainerFactories.ItemListFactory()));
        CATEGORIES.put(Category.PLAYERS,        new CategoryValue(new File(FILTER_DIR, Category.PLAYERS.name().toLowerCase()),      new ContainerFactories.PlayerListFactory()));
    }

    /**
     * Create directories
     */
    public static void initialize() {
        FILTER_DIR.mkdirs();
        for(Map.Entry<Category, CategoryValue> entry : CATEGORIES.entrySet()) {
            // create directories
            entry.getValue().folder.mkdirs();
            // look up all files
            lookupFiles(entry.getKey());
        }
    }

    /**
     * Add all files in the category directory
     */
    public static void lookupFiles(Category category) {
        if(CATEGORIES.containsKey(category)) {
            CategoryValue data = CATEGORIES.get(category);
            File[] files = data.folder.listFiles();
            if(files != null) {
                for(File file : files) {
                    if(file.isFile() &&
                            file.getName().endsWith(".json")) {
                        // remove the .json ending for lookup
                        String displayName = file.getName().replace(".json", "");
                        data.files.putIfAbsent(displayName, data.factory.newInstance(displayName, file));
                    }
                }
            }
        }
    }

    /**
     * Get category files
     */
    public static Map<String, Object> getContainer(Category category) {
        if(CATEGORIES.containsKey(category)) {
            CategoryValue value = CATEGORIES.get(category);
            return Maps.newLinkedHashMap(value.files);
        }
        return null;
    }

    /**
     * Returns a collection containing
     */
    public static Collection<?> getContainerCollection(Category category) {
        return getContainer(category).values();
    }

    /**
     * Create a new container list
     */
    public static Object createContainerList(Category category, String displayName) {
        if(CATEGORIES.containsKey(category)) {
            CategoryValue data = CATEGORIES.get(category);
            String fileName = displayName;
            if(!fileName.endsWith(".json"))
                fileName += ".json";
            else
                displayName = displayName.replace(".json", "");
            Object newInstance;
            if(data.files.containsKey(displayName)) {
                // check if file already exists
                // if it does use that instance
                newInstance = data.files.get(displayName);
            } else {
                // otherwise create new instance
                newInstance = data.factory.newInstance(displayName, new File(data.folder, fileName));
                // put new instance into file map
                data.files.put(displayName, newInstance);
            }
            return newInstance;
        } else return null;
    }

    /**
     * Remove container list
     */
    public static boolean removeContainerList(Category category, String fileName) {
        if(CATEGORIES.containsKey(category)) {
            CategoryValue data = CATEGORIES.get(category);
            if(data.files.containsKey(fileName)) {
                ContainerList list = (ContainerList)data.files.get(fileName);
                data.files.remove(fileName);
                list.delete();
                return true;
            }
        }
        return false;
    }
    public static boolean removeContainerList(Category category, ContainerList list) {
        return removeContainerList(category, list.getName());
    }

    private static class CategoryValue {
        // base folder that all list files will be under
        public File folder;
        // factory instance to create new instances
        public IContainerFactory factory;

        // map that contains all the list files currently loaded
        public Map<String, Object> files = Collections.synchronizedMap(Maps.<String, Object>newLinkedHashMap());

        public CategoryValue(File file, IContainerFactory factory) {
            this.folder = file;
            this.factory = factory;
        }
    }
}
