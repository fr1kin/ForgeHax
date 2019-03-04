package com.matt.forgehax.launch;

import com.google.common.base.Strings;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.Agent;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import cpw.mods.modlauncher.Launcher;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.MarkerManager;

import java.io.File;
import java.lang.reflect.Field;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DevLauncher {

    private static Logger LOGGER = LogManager.getLogger("ForgehaxLaunch");


    public static void main(String[] args) throws InterruptedException
    {
        final String markerselection = System.getProperty("forge.logging.markers", "");
        Arrays.stream(markerselection.split(",")).forEach(marker-> {
            System.setProperty("forge.logging.marker." + marker.toLowerCase(Locale.ROOT), "ACCEPT");
            MarkerManager.getMarker(marker.toUpperCase(Locale.ROOT));
        });

        String assets = System.getenv().getOrDefault("assetDirectory", "assets");
        String target = System.getenv().get("target");

        if (target == null) {
            throw new IllegalArgumentException("Environment variable 'target' must be set to 'fmluserdevclient'.");
        }

        if (!Files.exists(Paths.get(assets))) {
            throw new IllegalArgumentException("Environment variable 'assetDirectory' must be set to a valid path.");
        }
        final Map<String, String> argMap = new LinkedHashMap<>(); // nicer to have args in the same order they're given
        argMap.put("assetsDir", assets);
        setDefaultArguments(argMap);

        final OptionParser optionparser = new OptionParser();
        optionparser.allowsUnrecognizedOptions();
        OptionSpec<String> usernameSpec = optionparser.accepts("username").withRequiredArg();
        OptionSpec<String> passwordSpec = optionparser.accepts("password").withRequiredArg();
        OptionSet optionSet = optionparser.parse(args);
        if (optionSet.has(usernameSpec) && optionSet.has(passwordSpec)) {
            attemptLogin(optionSet.valueOf(usernameSpec), optionSet.valueOf(passwordSpec), argMap);
        }

        Map<String, String> inputArgMap = optionSet.asMap().entrySet().stream()
            .filter(entry -> entry.getKey() != usernameSpec && entry.getKey() != passwordSpec)
            .map(entry -> {
                List<?> values = entry.getValue();
                return new AbstractMap.SimpleEntry<>(entry.getKey().options().get(0), values.size() > 0 ? values.get(0).toString() : null); // lol
            })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        argMap.putAll(inputArgMap); // add our args to the argMap

        String[] launchArgs = getArgsFromMap(argMap);
        //LOGGER.info("Launching minecraft with args: " + Arrays.toString(launchArgs));
        Launcher.main(launchArgs);
        Thread.sleep(10000);
    }

    private static String[] getArgsFromMap(Map<String, String> argMap) {
        return argMap.entrySet().stream()
            .flatMap(entry ->
                entry.getValue() != null ?
                    Stream.of("--" + entry.getKey(), entry.getValue()) :
                    Stream.of("--" + entry.getKey())
            )
            .toArray(String[]::new);
    }

    private static void setDefaultArguments(Map<String, String> argMap) {
        argMap.put("gameDir", ".");
        argMap.put("launchTarget", "fmluserdevclient");
        argMap.put("fml.forgeVersion", System.getenv("FORGE_VERSION"));
        argMap.put("fml.mcpVersion", System.getenv("MCP_VERSION"));
        argMap.put("fml.mcpMappings", System.getenv("MCP_MAPPINGS"));
        argMap.put("fml.mcVersion", System.getenv("MC_VERSION"));
        argMap.put("fml.forgeGroup", System.getenv("FORGE_GROUP"));

        argMap.put("accessToken", "blah");
        argMap.put("version", "FMLDev");
        argMap.put("assetIndex", System.getenv("assetIndex"));
        argMap.put("userProperties", "{}");
    }

    private static void hackNatives()
    {
        String paths = System.getProperty("java.library.path");
        String nativesDir = System.getenv().get("nativesDirectory");

        if (Strings.isNullOrEmpty(paths))
            paths = nativesDir;
        else
            paths += File.pathSeparator + nativesDir;

        System.setProperty("java.library.path", paths);

        // hack the classloader now.
        try
        {
            final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
            sysPathsField.setAccessible(true);
            sysPathsField.set(null, null);
        }
        catch(Throwable t) {}
    }

    private static void attemptLogin(String username, String password, Map<String, String> argMap)
    {
        YggdrasilUserAuthentication auth = (YggdrasilUserAuthentication) new YggdrasilAuthenticationService(Proxy.NO_PROXY, "1").createUserAuthentication(Agent.MINECRAFT);
        auth.setUsername(username);
        auth.setPassword(password);

        try {
            auth.logIn();
        }
        catch (AuthenticationException e)
        {
            LOGGER.error("-- Login failed!  " + e.getMessage());
            throw new RuntimeException(e);
        }

        LOGGER.info("Login Succesful!");
        argMap.put("accessToken", auth.getAuthenticatedToken());
        argMap.put("uuid", auth.getSelectedProfile().getId().toString().replace("-", ""));
        argMap.put("username", auth.getSelectedProfile().getName());
        argMap.put("userType", auth.getUserType().getName());

        // 1.8 only apperantly.. -_-
        argMap.put("userProperties", new GsonBuilder().registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer()).create().toJson(auth.getUserProperties()));
    }
}
