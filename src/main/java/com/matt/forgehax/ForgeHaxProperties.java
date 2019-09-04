package com.matt.forgehax;

import com.google.common.base.Strings;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created on 6/27/2017 by fr1kin
 */
public class ForgeHaxProperties {
  
  private static final Properties CONFIG_PROPERTIES = new Properties();
  
  static {
    InputStream input = null;
    try {
      input = ForgeHaxProperties.class.getClassLoader().getResourceAsStream("config.properties");
      CONFIG_PROPERTIES.load(input);
    } catch (Throwable t) {
      ;
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (Throwable t) {
          ;
        }
      }
    }
  }
  
  public static Properties getConfigProperties() {
    return CONFIG_PROPERTIES;
  }
  
  public static String getVersion() {
    return Strings.nullToEmpty(getConfigProperties().getProperty("forgehax.version"));
  }
  
  public static String getMcVersion() {
    return Strings.nullToEmpty(getConfigProperties().getProperty("forgehax.mc.version"));
  }
  
  public static String getForgeVersion() {
    return Strings.nullToEmpty(getConfigProperties().getProperty("forgehax.forge.version"));
  }
  
  public static String getMcpVersion() {
    return Strings.nullToEmpty(getConfigProperties().getProperty("forgehax.mcp.version"));
  }
  
  public static String getMcpChannel() {
    return Strings.nullToEmpty(getConfigProperties().getProperty("forgehax.mcp.channel"));
  }
  
  public static String getMcpMapping() {
    return Strings.nullToEmpty(getConfigProperties().getProperty("forgehax.mcp.mapping"));
  }
  
  public static String getMcpMappingUrl() {
    return String.format("%s_%s_%s", getMcpVersion(), getMcpChannel(), getMcpMapping());
  }
}
