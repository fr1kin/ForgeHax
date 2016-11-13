package com.matt.forgehax;

import com.google.common.collect.Maps;
import com.matt.forgehax.asm.ForgeHaxHooks;
import com.matt.forgehax.mods.*;
import com.matt.forgehax.mods.core.ContainersMod;
import com.matt.forgehax.util.container.ContainerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.io.*;
import java.util.Map;

@Mod(modid = ForgeHax.MODID, version = ForgeHax.VERSION, guiFactory = "com.matt.forgehax.ForgeHaxGuiFactory")
public class ForgeHax {
	public static final String MODID = "forgehax";
	public static final String VERSION = "1.0";

	public static final Minecraft MC = Minecraft.getMinecraft();

	public static final String CONFIG_FILE_NAME = "settings.json";

	private static final boolean isInDevMode = ForgeHaxHooks.isInDebugMode;

	public static ForgeHax INSTANCE;

	public static ForgeHax instance() {
		return INSTANCE;
	}

	private File baseFolder;
	private File configFolder;
	private ForgeHaxConfig config;

	public Logger log;

	public Map<String, BaseMod> mods = Maps.newLinkedHashMap();

	public boolean newProfile = false;

	public ForgeHax() {
		INSTANCE = this;
	}

	public Logger getLog() {
		return log;
	}

	public File getBaseDirectory() {
		return baseFolder;
	}

	public File getConfigFolder() {
		return configFolder;
	}

	public ForgeHaxConfig getConfig() {
		return config;
	}

	public void setupConfigFolder() {
		File userDir = new File(getBaseDirectory(), "users");
		userDir.mkdirs();
		if (!isInDevMode) {
			configFolder = new File(userDir, MC.getSession().getProfile().getId().toString());
			if (!configFolder.exists()) {
				newProfile = true;
				configFolder.mkdirs();
			}
		} else {
			configFolder = new File(userDir, "devmode");
		}
	}

	public void registerMod(BaseMod mod) {
		mods.put(mod.getModName(), mod);
	}

	/**
	 * No idea if I will end up using any of this
	 */

	private File getLastProfileFile() {
		return new File(getBaseDirectory(), "last_profile.txt");
	}

	private File getLastUsedProfileFolder() {
		File profile = getLastProfileFile();
		if (profile.exists()) {
			FileReader reader = null;
			BufferedReader buffer = null;
			try {
				reader = new FileReader(profile);
				buffer = new BufferedReader(reader);
				String profileName = buffer.readLine();
				for (File file : getBaseDirectory().listFiles()) {
					if (file.getName().equals(profileName) && file.isDirectory()) {
						return file;
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (reader != null) {
						reader.close();
					}
					if (buffer != null) {
						buffer.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	private void updateLastUsedProfile(File lastUsedProfile) {
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(getLastProfileFile());
			output.write(lastUsedProfile.getName().getBytes());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (output != null) {
					output.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isNewProfile() {
		return newProfile;
	}

	public void printStackTrace(Exception exception) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		exception.printStackTrace(pw);
		getLog().error(sw);
		if (MC.thePlayer != null) {
			MC.thePlayer.addChatMessage(new TextComponentString("ERROR: " + exception.getMessage()));
		}
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		switch (event.getSide()) {
			case CLIENT: {
				//---- get log ----//
				log = event.getModLog();
				//---- initialize mods ----//
				registerMod(new ContainersMod("Containers", "Mod containers for xray and entity lists"));
				if (isInDevMode) {
	                /*registerMod(new DebugModeMod("debugmode", true, "Enables debug mode", Keyboard.KEY_END));
                    registerMod(new DebugDisplayMod("debugdisplay", true, "Display transformer hook reports", Keyboard.KEY_END));
                    registerMod(new DebugOutputMod("debugoutput", true, "Output debug info on hooks", Keyboard.KEY_HOME));*/
				}
				registerMod(new ActiveModListMod("activemods", true, "Shows list of all active mods", Keyboard.KEY_END));
				registerMod(new AimbotMod("aimbot", true, "Auto aim/attack entities", Keyboard.KEY_END));
				registerMod(new AntiAfkMod("antiafk", false, "Moves automatically to prevent being kicked", Keyboard.KEY_END));
				registerMod(new AntiBatsMod("antibats", true, "666 KILL BATS 666", Keyboard.KEY_END));
				registerMod(new AntiEffectsMod("antipotioneffects", true, "Removes potion effects", Keyboard.KEY_END));
				registerMod(new AntiFireMod("antifire", true, "Removes fire", Keyboard.KEY_END));
				registerMod(new AntiFogMod("antifog", true, "Removes fog", Keyboard.KEY_END));
				registerMod(new AntiHurtCamMod("antihurtcam", true, "Removes hurt camera effect", Keyboard.KEY_END));
				registerMod(new AntiKnockbackMod("antiknockback", true, "Removes knockback movement", Keyboard.KEY_END));
				registerMod(new AntiOverlayMod("antioverlay", true, "Removes screen overlays", Keyboard.KEY_END));
				registerMod(new AutoBlockCraft("autoblockcraft", true, "Automatically crafts blocks for you", Keyboard.KEY_END));
				registerMod(new AutoEatMod("autoeat", true, "Auto eats when you get hungry", Keyboard.KEY_END));
				registerMod(new AutoFishMod("autofish", false, "Auto fish", Keyboard.KEY_END));
				registerMod(new AutoProjectile("autoprojectile", true, "Automatically sets pitch to best trajectory", Keyboard.KEY_END));
				registerMod(new AutoReconnectMod("autoreconnect", true, "Automatically reconnects to server"));
				registerMod(new AutoRespawnMod("autorespawn", true, "Auto respawn on death", Keyboard.KEY_END));
				registerMod(new AutoSprintMod("autosprint", false, "Automatically sprints", Keyboard.KEY_END));
				registerMod(new AutoWalkMod("autowalk", false, "Automatically walks forward", Keyboard.KEY_END));
				registerMod(new BedModeMod("bedmode", true, "Sleep walking", Keyboard.KEY_END));
				registerMod(new ChamsMod("chams", true, "Render living models behind walls", Keyboard.KEY_END));
				registerMod(new ChatSpammerMod("chatspammer", false, "Add lines of spam into forgehax/spam.txt", Keyboard.KEY_END));
				registerMod(new StorageESPMod("storageesp", true, "Shows storage", Keyboard.KEY_END));
				registerMod(new EntityEspMod("entityesp", true, "Shows entity locations and info", Keyboard.KEY_END));
				registerMod(new FastBreak("fastbreak", true, "Fast break retard", Keyboard.KEY_END));
				registerMod(new FastPlaceMod("fastplace", true, "Fast place", Keyboard.KEY_END));
				registerMod(new FlyMod("fly", false, "Enables flying", Keyboard.KEY_END));
				registerMod(new FreecamMod("freecam", false, "Freecam mode", Keyboard.KEY_END));
				registerMod(new FullBrightMod("fullbright", true, "Makes everything render with maximum brightness", Keyboard.KEY_END));
				registerMod(new NoCaveCulling("nocaveculling", false, "Disables mojangs dumb cave culling shit", Keyboard.KEY_END));
				registerMod(new NoclipMod("noclip", false, "Enables player noclip", Keyboard.KEY_END));
				registerMod(new NoFallMod("nofall", false, "Prevents fall damage from being taken", Keyboard.KEY_END));
				registerMod(new NoSlowdown("noslowdown", true, "Disables block slowdown", Keyboard.KEY_END));
				registerMod(new ProjectilesMod("projectiles", true, "Draws projectile path", Keyboard.KEY_END));
				registerMod(new SafeWalkMod("safewalk", false, "Prevents you from falling off blocks", Keyboard.KEY_END));
				registerMod(new SpawnerEspMod("spawneresp", true, "Spawner esp", Keyboard.KEY_END));
				registerMod(new StepMod("step", true, "Step up blocks", Keyboard.KEY_END));
				registerMod(new TeleportMod("teleport", true, "Type '.setpos [x] [y] [z] [onGround]' in chat to use", Keyboard.KEY_END));
				registerMod(new XrayMod("xray", true, "See blocks through walls", Keyboard.KEY_END));
				registerMod(new YawLockMod("yawlock", false, "Locks yaw to prevent moving into walls", Keyboard.KEY_END));
				registerMod(new ElytraFlight("elytraflight", false, "Elytra Flight", Keyboard.KEY_END));

				//---- initialize configuration ----//
				// create folder containing all account settings
				baseFolder = new File(event.getModConfigurationDirectory(), "forgehax");
				baseFolder.mkdirs();
				// setup folder that contains settings (supports multiple accounts)
				setupConfigFolder();
				// setup config
				config = new ForgeHaxConfig(new File(getConfigFolder(), CONFIG_FILE_NAME));
				// init containers
				ContainerManager.initialize();
			}
			default:
				break;
		}
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		switch (event.getSide()) {
			case CLIENT: {
				// register config
				MinecraftForge.EVENT_BUS.register(config);
				// register event handler
				MinecraftForge.EVENT_BUS.register(new ForgeHaxEventHandler());
				// register mod events
				for (Map.Entry<String, BaseMod> entry : mods.entrySet()) {
					if (entry.getValue().isEnabled()) {
						entry.getValue().onEnabled();
						entry.getValue().register();
					}
				}
				break;
			}
			default:
				break;
		}
	}
}
