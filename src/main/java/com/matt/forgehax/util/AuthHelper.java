package com.matt.forgehax.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.mods.managers.AccountManager;
import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.util.Session;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

import static com.matt.forgehax.Globals.MC;
import static com.matt.forgehax.Helper.getLog;
import static com.matt.forgehax.mods.managers.AccountManager.masterPassword;

/**
 * Added by OverFloyd, july 2020
 */
public final class AuthHelper extends YggdrasilUserAuthentication {

  private final YggdrasilAuthenticationService authService;
  public static final File directory = FileManager.getInstance().getMkBaseDirectory("/config/SavedAccounts").toFile();

  public AuthHelper() {
    super(new YggdrasilAuthenticationService(MC.getProxy(), null), Agent.MINECRAFT);
    authService = new YggdrasilAuthenticationService(MC.getProxy(), UUID.randomUUID().toString());
  }

  @Override
  public YggdrasilAuthenticationService getAuthenticationService() {
    return authService;
  }

  public void setSession(Session s) {
    FastReflection.Fields.Minecraft_session.set(MC, s);
  }

  public void newLogin(String login, String password) throws AuthenticationException {
    setUsername(login);
    setPassword(password);

    // For new client token.
    logInWithPassword();
    Session newSession = new Session(getSelectedProfile().getName(),
      UUIDTypeAdapter.fromUUID(getSelectedProfile().getId()), getAuthenticatedToken(), getUserType().getName());

    newSession.setProperties(getUserProperties());
    logOut();
    setSession(newSession);
  }

  public static String encrypt(String encryptString, String iv, String key, String keySalt) {
    try {
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      KeySpec spec = new PBEKeySpec(key.toCharArray(), keySalt.getBytes(), 65536, 128);
      SecretKey tmp = factory.generateSecret(spec);
      SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

      return Base64.getEncoder().encodeToString(cipher.doFinal(encryptString.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      e.printStackTrace();
      getLog().error("Encryption error: " + e.toString());
    }

    return null;
  }

  public static String decrypt(String decryptString, String iv, String key, String keySalt) {
    try {
      SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      KeySpec spec = new PBEKeySpec(key.toCharArray(), keySalt.getBytes(), 65536, 128);
      SecretKey tmp = factory.generateSecret(spec);
      SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
      IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
      cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

      return new String(cipher.doFinal(Base64.getDecoder().decode(decryptString.getBytes(StandardCharsets.UTF_8))));
    } catch (Exception e) {
      e.printStackTrace();
      getLog().error("Decryption error: " + e.toString());
    }

    return null;
  }

  // Returns the secret key
  // SecretKey in .auth if mode is DEFAULT, master password if mode is MASTERPASSWORD
  public static String getSecretKey() throws IOException {
    if (AccountManager.INSTANCE.mode.get() == AccountManager.SecretKeyOptions.DEFAULT) {
      final File secretKeyFile = new File(directory + File.separator + ".auth");
      final FileReader fileReader = new FileReader(secretKeyFile);
      final JsonObject object = new JsonParser().parse(fileReader).getAsJsonObject();
      fileReader.close();

      return object.get("keyField").getAsString();
    } else if (masterPassword == null) {
      getLog().error("getSecretKey() returned null.");
      return null;
    } else return Arrays.toString(masterPassword);
  }

  // Gets the salt from file
  public static String getSalt() throws IOException {
    final File saltFile = new File(directory + File.separator + ".auth");

    final FileReader fileReader = new FileReader(saltFile);
    final JsonObject object = new JsonParser().parse(fileReader).getAsJsonObject();
    fileReader.close();

    return object.get("saltField").getAsString();
  }

  public static String getIV(String alias) throws IOException {
    final File ivFile = new File(directory + File.separator + alias + ".json");

    final FileReader fileReader = new FileReader(ivFile);
    final JsonObject object = new JsonParser().parse(fileReader).getAsJsonObject();
    fileReader.close();

    return object.get("iv").getAsString();
  }
}
