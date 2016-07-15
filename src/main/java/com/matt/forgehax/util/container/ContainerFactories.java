package com.matt.forgehax.util.container;

import java.io.File;

public class ContainerFactories {
    public static class PlayerList implements IContainerFactory {
        @Override
        public Object newInstance(String name, File file) {
            return new com.matt.forgehax.util.container.PlayerList(name, file);
        }
    }

    public static class XrayList implements IContainerFactory {
        @Override
        public Object newInstance(String name, File file) {
            return null;
        }
    }
}
