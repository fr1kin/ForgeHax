package com.matt.forgehax.asm.utils.remapping;

import bspkrs.mmv.*;
import com.matt.forgehax.asm.utils.ASMStackLogger;
import java.io.File;
import java.io.IOException;
import java.security.DigestException;
import java.security.NoSuchAlgorithmException;

/** Credits to bspkrs */
public class MCPMappingLoader {
  private final File baseDir =
      new File(new File(System.getProperty("user.home")), ".cache/MCPMappingViewer");
  private final String baseSrgDir = "{mc_ver}";
  private final String baseMappingDir = "{mc_ver}/{channel}_{map_ver}";
  private final String baseMappingUrl =
      "http://export.mcpbot.bspk.rs/mcp_{channel}/{map_ver}-{mc_ver}/mcp_{channel}-{map_ver}-{mc_ver}.zip";
  private final String baseSrgUrl =
      "http://export.mcpbot.bspk.rs/mcp/{mc_ver}/mcp-{mc_ver}-srg.zip";

  private final File srgDir;
  private final File mappingDir;
  private final File srgFile;
  private final File excFile;
  private final File staticMethodsFile;

  private SrgFile srgFileData;
  private ExcFile excFileData;
  private StaticMethodsFile staticMethods;
  private CsvFile csvFieldData, csvMethodData;
  private ParamCsvFile csvParamData;

  public MCPMappingLoader(String mapping)
      throws IOException, CantLoadMCPMappingException, NoSuchAlgorithmException, DigestException {
    String[] tokens = mapping.split("_");
    if (tokens.length < 3)
      throw new CantLoadMCPMappingException("Invalid mapping string specified.");

    srgDir = getSubDirForZip(tokens, baseSrgUrl, baseSrgDir);
    mappingDir = getSubDirForZip(tokens, baseMappingUrl, baseMappingDir);

    srgFile = new File(srgDir, "joined.srg");
    excFile = new File(srgDir, "joined.exc");
    staticMethodsFile = new File(srgDir, "static_methods.txt");

    if (!srgFile.exists())
      throw new CantLoadMCPMappingException(
          "Unable to find joined.srg. Your MCP conf folder may be corrupt.");

    if (!excFile.exists())
      throw new CantLoadMCPMappingException(
          "Unable to find joined.exc. Your MCP conf folder may be corrupt.");

    if (!staticMethodsFile.exists())
      throw new CantLoadMCPMappingException(
          "Unable to find static_methods.txt. Your MCP conf folder may be corrupt.");

    staticMethods = new StaticMethodsFile(staticMethodsFile);
    excFileData = new ExcFile(excFile);
    srgFileData = new SrgFile(srgFile, excFileData, staticMethods);

    csvFieldData = new CsvFile(new File(mappingDir, "fields.csv"));
    csvMethodData = new CsvFile(new File(mappingDir, "methods.csv"));
    csvParamData = new ParamCsvFile(new File(mappingDir, "params.csv"));
  }

  public CsvFile getCsvMethodData() {
    return csvMethodData;
  }

  public CsvFile getCsvFieldData() {
    return csvFieldData;
  }

  public SrgFile getSrgFileData() {
    return srgFileData;
  }

  private File getSubDirForZip(String[] tokens, String baseZipUrl, String baseSubDir)
      throws CantLoadMCPMappingException, NoSuchAlgorithmException, DigestException, IOException {
    if (!baseDir.exists() && !baseDir.mkdirs())
      throw new CantLoadMCPMappingException(
          "Application data folder does not exist and cannot be created.");

    File subDir = new File(baseDir, replaceTokens(baseSubDir, tokens));
    if (!subDir.exists() && !subDir.mkdirs())
      throw new CantLoadMCPMappingException("Data folder does not exist and cannot be created.");

    try {
      RemoteZipHandler rzh =
          new RemoteZipHandler(replaceTokens(baseZipUrl, tokens), subDir, "SHA1");
      rzh.checkRemoteZip();
    } catch (Throwable t) {
      ASMStackLogger.printStackTrace(t);
    }

    return subDir;
  }

  private String replaceTokens(String s, String[] tokens) {
    return s.replace("{mc_ver}", tokens[0])
        .replace("{channel}", tokens[1])
        .replace("{map_ver}", tokens[2]);
  }

  public static class CantLoadMCPMappingException extends Exception {
    public CantLoadMCPMappingException(String msg) {
      super(msg);
    }
  }
}
