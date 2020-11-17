package dev.fiki.forgehax.api.classloader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static dev.fiki.forgehax.api.FileHelper.asFilePath;

/**
 * Created on 2/16/2018 by fr1kin
 */
public class CustomClassLoaders {
  
  public static ClassLoader newFsClassLoader(ClassLoader parent, FileSystem fs)
      throws RuntimeException {
    try {
      return new FsClassLoader(parent, fs);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
  
  private static class FsClassLoader extends URLClassLoader {
    
    private final Path root;
    
    private FsClassLoader(ClassLoader parent, Path path) throws MalformedURLException {
      super(new URL[]{path.toUri().toURL()}, parent);
      this.root = path;
    }
    
    public FsClassLoader(ClassLoader parent, FileSystem fileSystem) throws MalformedURLException {
      this(parent, fileSystem.getRootDirectories().iterator().next());
    }
    
    public Path getRoot() {
      return root;
    }
    
    public FileSystem getFileSystem() {
      return root.getFileSystem();
    }
    
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
      try {
        Path location = getRoot().resolve(asFilePath(name, "/").concat(".class"));
        if (Files.exists(location)) {
          byte[] classData = Files.readAllBytes(location);
          return defineClass(name, classData, 0, classData.length);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      return null;
    }
  }
}
