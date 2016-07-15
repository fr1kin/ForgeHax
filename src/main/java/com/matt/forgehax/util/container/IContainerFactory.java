package com.matt.forgehax.util.container;

import java.io.File;

public interface IContainerFactory {
    Object newInstance(String name, File file);
}
