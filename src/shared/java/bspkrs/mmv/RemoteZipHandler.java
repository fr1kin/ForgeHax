/*
 * Copyright (C) 2015 bspkrs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package bspkrs.mmv;

/*
Copyright (C) 2014 bspkrs
Portions Copyright (C) 2014 Alex "immibis" Campbell

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class RemoteZipHandler {
  private static final String MMV_VERSION = "1.0.1";

  private final URL zipUrl;
  private final URL digestUrl;
  private final File localDir;
  private final String digestType;
  private final String zipFileName;

  public RemoteZipHandler(String urlString, File dir, String digestType)
      throws MalformedURLException {
    zipUrl = new URL(urlString);
    if (digestType != null) digestUrl = new URL(urlString + "." + digestType.toLowerCase());
    else digestUrl = null;
    String[] tokens = urlString.split("/");
    zipFileName = tokens[tokens.length - 1];
    localDir = dir;
    this.digestType = digestType;
  }

  public void checkRemoteZip() throws IOException, NoSuchAlgorithmException, DigestException {
    // fetch zip file sha1
    boolean fetchZip = true;
    String remoteHash = null;
    File digestFile = null;
    if (digestType != null) {
      // check hash against local hash if exists
      remoteHash = loadTextFromURL(digestUrl, new String[] {""})[0];
      if (!remoteHash.isEmpty()) {
        digestFile = new File(localDir, zipFileName + "." + digestType.toLowerCase());

        // if local digest exists and hashes match skip getting the zip file
        if (digestFile.exists()) {
          String existingHash = loadTextFromFile(digestFile, new String[] {""})[0];
          if (!existingHash.isEmpty() && remoteHash.equals(existingHash)) fetchZip = false;
        }
      }
    }

    if (fetchZip) {
      // download zip
      File localZip = new File(localDir, zipFileName);
      if (localZip.exists()) localZip.delete();
      OutputStream output = new FileOutputStream(localZip);
      try {
        URLConnection uc = zipUrl.openConnection();
        uc.addRequestProperty("User-Agent", "MMV/" + MMV_VERSION);
        byte[] buffer = new byte[1024]; // Or whatever
        int bytesRead;
        try (InputStream is = uc.getInputStream()) {
          while ((bytesRead = is.read(buffer)) > 0) output.write(buffer, 0, bytesRead);
        }
      } finally {
        output.close();
      }

      // Check hash of downloaded file to ensure we received it correctly
      if (digestType != null && !remoteHash.isEmpty()) {
        String downloadHash = getFileDigest(new FileInputStream(localZip), digestType);
        if (!remoteHash.equals(downloadHash))
          throw new java.security.DigestException(
              "Remote digest does not match digest of downloaded file!");
      }

      // extract zip file
      extractZip(localZip, localDir);
      if (localZip.exists()) localZip.delete();

      // save new hash after successful extract
      if (digestType != null && !remoteHash.isEmpty()) {
        if (digestFile.exists()) digestFile.delete();
        digestFile.createNewFile();
        PrintWriter out = new PrintWriter(new FileWriter(digestFile));
        out.print(remoteHash);
        out.close();
      }
    }
  }

  public static String[] loadTextFromURL(URL url, String[] defaultValue) {
    List<String> arraylist = new ArrayList<String>();
    Scanner scanner = null;
    try {
      URLConnection uc = url.openConnection();
      uc.addRequestProperty("User-Agent", "MMV/" + MMV_VERSION);
      InputStream is = uc.getInputStream();
      scanner = new Scanner(is, "UTF-8");

      while (scanner.hasNextLine()) {
        arraylist.add(scanner.nextLine());
      }
    } catch (Throwable e) {
      return defaultValue;
    } finally {
      if (scanner != null) scanner.close();
    }
    return arraylist.toArray(new String[arraylist.size()]);
  }

  public static String[] loadTextFromFile(File file, String[] defaultValue) {
    ArrayList<String> lines = new ArrayList<String>();

    Scanner scanner = null;
    try {
      scanner = new Scanner(file);
      while (scanner.hasNextLine()) lines.add(scanner.nextLine());
    } catch (FileNotFoundException e) {
      return defaultValue;
    } finally {
      if (scanner != null) scanner.close();
    }

    return lines.toArray(new String[lines.size()]);
  }

  public static String getFileDigest(InputStream is, String digestType)
      throws NoSuchAlgorithmException, IOException {
    MessageDigest md = MessageDigest.getInstance(digestType);
    byte[] dataBytes = new byte[1024];

    int nread = 0;

    while ((nread = is.read(dataBytes)) != -1) md.update(dataBytes, 0, nread);

    is.close();

    byte[] mdbytes = md.digest();

    // convert the byte to hex format
    StringBuffer sb = new StringBuffer("");
    for (int i = 0; i < mdbytes.length; i++)
      sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
    return sb.toString();
  }

  public static void extractZip(File zipFile, File destDir) throws IOException {
    byte[] buffer = new byte[1024];
    if (!destDir.exists()) destDir.mkdirs();

    ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
    ZipEntry ze = zis.getNextEntry();
    try {
      while (ze != null) {
        String fileName = ze.getName();
        File newFile = new File(destDir, fileName);
        if (ze.isDirectory()) {
          if (newFile.exists()) deleteDirAndContents(newFile);
          newFile.mkdirs();
        } else {
          if (newFile.exists()) newFile.delete();
          if (newFile.getParentFile() != null && !newFile.getParentFile().exists())
            newFile.getParentFile().mkdirs();
          FileOutputStream fos = new FileOutputStream(newFile);
          int len;
          while ((len = zis.read(buffer)) > 0) fos.write(buffer, 0, len);

          fos.close();
        }
        ze = zis.getNextEntry();
      }
    } finally {
      zis.closeEntry();
      zis.close();
    }
  }

  public static boolean deleteDirAndContents(File dir) {
    if (dir.isDirectory()) {
      String[] children = dir.list();
      for (int i = 0; i < children.length; i++) {
        boolean success = deleteDirAndContents(new File(dir, children[i]));
        if (!success) return false;
      }
    }
    return dir.delete();
  }
}
