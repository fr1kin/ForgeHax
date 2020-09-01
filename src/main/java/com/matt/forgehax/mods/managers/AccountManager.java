package com.matt.forgehax.mods.managers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.matt.forgehax.asm.reflection.FastReflection;
import com.matt.forgehax.util.AuthHelper;
import com.matt.forgehax.util.FileManager;
import com.matt.forgehax.util.SimpleTimer;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.mod.ServiceMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.util.Session;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;

import static com.matt.forgehax.Helper.*;
import static com.matt.forgehax.util.AuthHelper.*;

/**
 * Added by OverFloyd, july 2020
 * Additional credits to Tonio_Cartonio
 */
@RegisterMod
public class AccountManager extends ServiceMod {

  public final Setting<SecretKeyOptions> mode =
    getCommandStub()
      .builders()
      .<SecretKeyOptions>newSettingEnumBuilder()
      .name("pw-mode")
      .description("Master password (safer) or default (secretKey generated automatically and saved on disk).")
      .defaultTo(SecretKeyOptions.MASTERPASSWORD)
      .build();

  public final Setting<Integer> mpwDelay =
    getCommandStub()
      .builders()
      .<Integer>newSettingBuilder()
      .name("delay")
      .description("Delay in ms after which masterPassword is reset, \"0\" to disable.")
      .defaultTo(120000) // 2 min
      .min(0)
      .build();

  public enum SecretKeyOptions {
    MASTERPASSWORD,
    DEFAULT
  }

  public AccountManager() {
    super("AccountManager");
    INSTANCE = this;
  }

  private final AuthHelper auth = new AuthHelper();
  private final Session originalSession = FastReflection.Fields.Minecraft_session.get(MC);
  private final SimpleTimer mpwTimer = new SimpleTimer();
  public static AccountManager INSTANCE;
  public static char[] masterPassword;
  public String logInResponse;

  @Override
  protected void onLoad() {
    super.onLoad();
    StringBuilder loginBuilder = new StringBuilder("Login with provided alias");
    StringBuilder saveBuilder = new StringBuilder("For saving or editing an account, <alias> <email> <password>");

    // TODO: make this happen on setting change/on printing somehow
    if (mode.get() == SecretKeyOptions.MASTERPASSWORD && masterPassword == null) {
      String append = TextFormatting.GOLD + " [requires master password first]";
      loginBuilder.append(append);
      saveBuilder.append(append);
    }


    // MASTER PASSWORD
    getCommandStub()
      .builders()
      .newCommandBuilder()
      .name("master-password")
      .description("Password to submit to allow credentials encryption/decryption.")
      .processor(
        data -> {
          if (masterPassword == null) {
            data.requiredArguments(1);
            char[] candidatePassword = data.getArgumentAsString(0).toCharArray();

            if (!checkForMpwFile() && !checkForAuthFile()) {
              try {

                // Saves .check file for masterPassword && .auth file if not present.
                generateSecretKey();
                saveEncryptCheck(candidatePassword);
              } catch (IOException e) {
                e.printStackTrace();
              }
            }

            final File checkFile = new File(directory + File.separator + ".check");

            try {
              final FileReader fileReader = new FileReader(checkFile);
              final JsonObject object = new JsonParser().parse(fileReader).getAsJsonObject();
              fileReader.close();

              String check = decrypt(object.get("mpwCheckField").getAsString(),
                object.get("ivField").getAsString(), Arrays.toString(candidatePassword), getSalt());

              if (check != null && check.equals("Correct!")) {
                masterPassword = candidatePassword;
                mpwTimer.start();
                printInform("Master password has been set correctly.");
              } else printError("Submitted master password is incorrect.");

            } catch (IOException e) {
              e.printStackTrace();
            }
          } else printError("Master password has already been defined.");
        })
      .build();


    // LOGIN
    getCommandStub()
      .builders()
      .newCommandBuilder()
      .name("login")
      .description(loginBuilder.toString() + ".")
      .processor(data -> {
        data.requiredArguments(1);
        final String alias = data.getArgumentAsString(0);

        login(alias);
      })
      .build();


    // SAVE
    getCommandStub()
      .builders()
      .newCommandBuilder()
      .name("save")
      .description(saveBuilder.toString() + ".")
      .processor(
        data -> {
          data.requiredArguments(3);
          final String alias = data.getArgumentAsString(0);
          final String email = data.getArgumentAsString(1);
          final String password = data.getArgumentAsString(2);
          final String newIV = generateIV();

          if (alias.startsWith(".")) {
            printWarning("Invalid alias.");
            return;
          }

          // Checks if .auth is present, if not it generates a new one
          if (!checkForAuthFile()) {
            printError("Auth file is missing, regenerating keys...");
            printInform("If you're saving an account for the first time, it's all good.");
            generateSecretKey();
          }

          // Check if the delay for timer has elapsed
          checkTimer();

          final File account = new File(directory.getAbsolutePath() + "/" + alias + ".json");
          final JsonObject object = new JsonObject();

          // Creates new file if it doesn't exist
          if (!account.exists()) {
            try {
              object.addProperty("alias", (String) data.arguments().get(0));
              object.addProperty("iv", newIV);
              object.addProperty("credentials", encrypt(createAccount(email, password), newIV, getSecretKey(), getSalt()));

              if (directory.exists()) {
                if (getSecretKey() != null) {
                  FileManager.save(account, object);
                  printInform("Saved new account: %s.", alias);
                } else printError("Master password or default password is blank.");
              } else printError("Failed to locate SavedAccounts folder.");

            } catch (IOException e) {
              e.printStackTrace();
              printError("Failed to locate .auth file.");
              printInform("The exception is: %s.", e.getMessage());
            }
          } else {
            try {
              final FileReader fileReader = new FileReader(account);
              final JsonObject objectReader = new JsonParser().parse(fileReader).getAsJsonObject();
              fileReader.close();

              String credentials = decrypt(objectReader.get("credentials").getAsString(), getIV(alias), getSecretKey(), getSalt());
              getLog().info("Credentials decrypted (for credentials comparison).");

              // Gets credentials
              if (credentials != null) {
                final JsonObject objectCred = new JsonParser().parse(credentials).getAsJsonObject();

                String emailIn = objectCred.get("email").getAsString();
                String passwordIn = objectCred.get("password").getAsString();

                // Compare submitted data with already existing data
                if (email.equalsIgnoreCase(emailIn) && password.equals(passwordIn)) {
                  printMessage("Credentials for \"%s\" didn't change.", alias);
                } else {
                  if (email.equalsIgnoreCase(emailIn)) {
                    getLog().info("Email for \"" + alias + "\" didn't change.");
                  } else if (password.equals(passwordIn)) {
                    getLog().info("Password for \"" + alias + "\" didn't change.");
                  }

                  // Saves the data that did change
                  object.addProperty("alias", (String) data.arguments().get(0));
                  object.addProperty("iv", getIV(alias));
                  object.addProperty("credentials", encrypt(createAccount(email, password), getIV(alias), getSecretKey(), getSalt()));
                  getLog().info("Credentials encrypted (account was edited).");

                  if (directory.exists()) {
                    if (getSecretKey() != null) {
                      FileManager.save(account, object);
                      printInform("Successfully edited account \"%s\".", alias);
                    } else printError("Master password or default password is blank.");
                  } else printError("Failed to locate SavedAccounts folder.");
                }
              } else printError("Failed to decrypt credentials.");
            } catch (IOException e) {
              e.printStackTrace();
              printError("Failed to load \"%s\".json file.", alias);
              printInform("The exception is: %s.", e.getMessage());
            }
          }
        })
      .build();


    // DELETE
    getCommandStub()
      .builders()
      .newCommandBuilder()
      .name("delete")
      .description("<alias> for a single account, \"*\" for all files.")
      .processor(
        data -> {
          data.requiredArguments(1);
          String alias = data.getArgumentAsString(0);
          final File account = new File(directory + File.separator + alias + ".json");

          if (alias.equals("*")) {
            for (final File fileEntry : Objects.requireNonNull(directory.listFiles())) {
              if (!fileEntry.isDirectory()) {
                fileEntry.getAbsoluteFile().delete();
                getLog().info("Deleted " + fileEntry.getName());
              }
            }

            printInform("Deleted all saved accounts.");
          } else if (account.exists()) {
            account.delete();
            printInform("Deleted account \"%s\".", alias);
          } else printError("Couldn't find \"%s\".", alias);
        })
      .build();


    // RESTORE
    getCommandStub()
      .builders()
      .newCommandBuilder()
      .name("restore")
      .description("Switch back to the original session.")
      .processor(
        data -> {
          String getSessionUsername = FastReflection.Fields.Minecraft_session.get(MC).getUsername();

          if (!getSessionUsername.equals(originalSession.getUsername())) {
            auth.setSession(originalSession);
            printInform("Successfully switched to the original session.");
          } else printMessage("Session didn't change.");
        })
      .build();


    // LIST
    getCommandStub()
      .builders()
      .newCommandBuilder()
      .name("list")
      .description("Lists all saved accounts.")
      .processor(
        data -> {
          if (directory.exists()) {

            if (Objects.requireNonNull(new File(String.valueOf(directory)).listFiles()).length < 3) {
              printMessage("No accounts found.");
            } else {

              // Accounts found
              printMessage("Saved accounts (by alias):");

              for (final File fileEntry : Objects.requireNonNull(directory.listFiles())) {
                if (!fileEntry.isDirectory() && fileEntry.getName().endsWith(".json")) {
                  data.write(fileEntry.getName().replace(".json", ""));
                }
              }
            }
          } else printError("Failed to locate SavedAccounts folder.");
        })
      .build();


    // COUNT
    getCommandStub()
      .builders()
      .newCommandBuilder()
      .name("count")
      .description("Prints the number of all saved accounts.")
      .processor(
        data -> {
          if (directory.exists()) {
            int count = 0;

            for (final File fileEntry : Objects.requireNonNull(directory.listFiles())) {
              if (!fileEntry.isDirectory() && fileEntry.getName().endsWith(".json")) {
                count++;
              }
            }

            if (count != 0) {
              printInform("Number of saved accounts: %s.", count);
            } else printMessage("No accounts found.");
          } else printError("Failed to locate SavedAccounts folder.");
        })
      .build();


    // WHO AM I
    getCommandStub()
      .builders()
      .newCommandBuilder()
      .name("whoami")
      .description("Prints the name of the account you're currently using.")
      .processor(data -> printInform("Currently logged in as: %s.", FastReflection.Fields.Minecraft_session.get(MC).getUsername()))
      .build();


    // RESET KEYS
    getCommandStub()
      .builders()
      .newCommandBuilder()
      .name("regen-key")
      .description("Resets secretKey & salt (useful for debugging).")
      .processor(
        data -> {
          if (directory.exists()) {

            // Checks for auth files
            if (checkForAuthFile()) {
              printInform(".auth file is already present.");
              return;
            }

            generateSecretKey();
            printInform("Successfully regenerated .auth file.");
          } else printError("Failed to locate SavedAccounts folder.");
        })
      .build();
  }

  public boolean login(String alias) {
    // Checks for auth files
    if (!checkForAuthFile()) {
      logInResponse = "Failed to locate .auth file.";
      printError(logInResponse);
      return false;
    }

    final File account = new File(directory + File.separator + alias + ".json");

    if (account.exists()) {
      try {
        final FileReader fileReader = new FileReader(account);
        final JsonObject object = new JsonParser().parse(fileReader).getAsJsonObject();
        fileReader.close();

        // Check if the delay set has elapsed
        checkTimer();

        String name = object.get("alias").getAsString();
        String credentials = decrypt(object.get("credentials").getAsString(), getIV(name), getSecretKey(), getSalt());
        getLog().info("Credentials decrypted (for login or reauth).");

        // Gets credentials
        if (credentials != null) {
          final JsonObject objectCred = new JsonParser().parse(credentials).getAsJsonObject();

          String email = objectCred.get("email").getAsString();
          String password = objectCred.get("password").getAsString();

          // Sets new session with the new credentials
          try {
            auth.newLogin(email, password);
            logInResponse = String.format("Successfully logged in as \"%s\".", FastReflection.Fields.Minecraft_session.get(MC).getUsername());
            printInform(logInResponse);
          } catch (Exception e) {
            logInResponse = String.format("Could not login as \"%s\". The exception is: %s.", name, e.getMessage());
            printError(logInResponse);
            return false;
          }
        } else {
          logInResponse = "Failed to decrypt credentials.";
          printError(logInResponse);
          return false;
        }
      } catch (IOException e) {
        e.printStackTrace();
        logInResponse = String.format("Failed to load \"%s.json\" file, with exception %s", alias, e.getMessage());
        printError(logInResponse);
        return false;
      }
    } else {
      logInResponse = String.format("Couldn't find data for \"%s\".", alias);
      printError(logInResponse);
      return false;
    }

    return true;
  }

  public void checkTimer() {
    if (mpwTimer.hasTimeElapsed(mpwDelay.get()) && mpwDelay.get() != 0) {
      masterPassword = null;
    }
  }

  public static boolean checkForAuthFile() {
    boolean isFilePresent = false;

    if (directory.exists()) {
      for (final File fileEntry : Objects.requireNonNull(directory.listFiles())) {
        if (!fileEntry.isDirectory() && fileEntry.getName().equals(".auth")) {
          isFilePresent = true;
        }
      }
    } else printError("Failed to locate SavedAccounts folder.");

    return isFilePresent;
  }

  public static boolean checkForMpwFile() {
    boolean isFilePresent = false;

    if (directory.exists()) {
      for (final File fileEntry : Objects.requireNonNull(directory.listFiles())) {
        if (!fileEntry.isDirectory() && fileEntry.getName().equals(".check")) {
          isFilePresent = true;
        }
      }
    } else printError("Failed to locate SavedAccounts folder.");

    return isFilePresent;
  }

  // IV unique per account
  public static String generateIV() {
    return RandomStringUtils.random(16, 0, 0, true, true, null, new SecureRandom());
  }

  public static void generateSecretKey() {
    final File authFile = new File(directory + File.separator + ".auth");
    final JsonObject authObject = new JsonObject();

    String secretKey = RandomStringUtils.random(64, 0, 0, true, true, null, new SecureRandom());
    String keySalt = RandomStringUtils.random(64, 0, 0, true, true, null, new SecureRandom());

    authObject.addProperty("keyField", secretKey);
    authObject.addProperty("saltField", keySalt);

    FileManager.save(authFile, authObject);
    getLog().info("Saved " + authFile.getName() + " file.");
  }

  public static void saveEncryptCheck(char[] masterPw) throws IOException {
    final File checkFile = new File(directory + File.separator + ".check");
    final JsonObject checkObject = new JsonObject();
    String mpwIV = generateIV();
    String mpwCheck = encrypt("Correct!", mpwIV, Arrays.toString(masterPw), getSalt());

    checkObject.addProperty("ivField", mpwIV);
    checkObject.addProperty("mpwCheckField", mpwCheck);

    FileManager.save(checkFile, checkObject);
    getLog().info("Saved " + checkFile.getName() + " file.");
  }

  // Serialized json with email & password
  public String createAccount(String email, String password) {
    final JsonObject object = new JsonObject();
    object.addProperty("email", email);
    object.addProperty("password", password);

    Gson gson = new Gson();
    return gson.toJson(object);
  }
}
