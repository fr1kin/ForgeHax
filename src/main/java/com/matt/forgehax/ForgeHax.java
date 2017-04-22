package com.matt.forgehax;

import com.google.common.collect.Maps;
import com.matt.forgehax.asm.ForgeHaxHooks;
import com.matt.forgehax.mods.*;
import com.matt.forgehax.mods.core.ContainersMod;
import com.matt.forgehax.util.LagCompensator;
import com.matt.forgehax.util.container.ContainerManager;
import com.matt.forgehax.util.key.BindSerializer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.io.*;
import java.util.Map;

@Mod(modid = ForgeHax.MODID, version = ForgeHax.VERSION, guiFactory = "com.matt.forgehax.ForgeHaxGuiFactory", clientSideOnly = true)
public class ForgeHax {
	public static final String MODID = "forgehax";
	public static final String VERSION = "1.2";

	public static final Minecraft MC = Minecraft.getMinecraft();

	public static final String CONFIG_FILE_NAME = "settings.json";

	private static final boolean isInDevMode = ForgeHaxHooks.isInDebugMode;

	public static ForgeHax INSTANCE;

	public static ForgeHax getInstance() {
		return INSTANCE;
	}

	private File baseFolder;
	private File configFolder;
	private ForgeHaxConfig config;

	private BindSerializer bindSerializer;

	public Logger log;

	public Map<String, BaseMod> mods = Maps.newTreeMap();

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

	public BindSerializer getBindSerializer() {
		return bindSerializer;
	}

	public void setupConfigFolder() {
		File userDir = new File(getBaseDirectory(), "users");
		userDir.mkdirs();
		configFolder = new File(userDir, "devmode");
	}

	public void registerMod(BaseMod mod) {
		mods.put(mod.getModName(), mod);
	}

	public void printStackTrace(Exception exception) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		exception.printStackTrace(pw);
		getLog().error(sw);
		if (MC.player != null) {
			MC.player.sendChatMessage("ERROR: " + exception.getMessage());
		}
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		switch (event.getSide()) {
			case CLIENT: {
				//---- get log ----//
				log = event.getModLog();

				//---- initialize configuration ----//
				// create folder containing all account settings
				baseFolder = new File(event.getModConfigurationDirectory(), "forgehax");
				baseFolder.mkdirs();
				// setup folder that contains settings (supports multiple accounts)
				setupConfigFolder();
				// initialize bind serializer
				bindSerializer = new BindSerializer(getConfigFolder());
				// add shutdown hook to serialize all binds
				Runtime.getRuntime().addShutdownHook(new Thread(bindSerializer::serialize));

				//---- initialize mods ----//
				registerMod(new ContainersMod("Containers", "Mod containers for xray and entity lists"));
				if (isInDevMode) {
					/*
	                registerMod(new DebugModeMod("debugmode", false, "Enables debug mode", Keyboard.KEY_END));
                    registerMod(new DebugDisplayMod("debugdisplay", false, "Display transformer hook reports", Keyboard.KEY_END));
                    registerMod(new DebugOutputMod("debugoutput", false, "Output debug info on hooks", Keyboard.KEY_HOME));
                    //*/
				}
				registerMod(new ActiveModListMod("activemods", false, "Shows list of all active mods", Keyboard.KEY_END));
				registerMod(new AimbotMod("aimbot", false, "Auto aim/attack entities", Keyboard.KEY_END));
				registerMod(new AntiAfkMod("antiafk", false, "Moves automatically to prevent being kicked", Keyboard.KEY_END));
				registerMod(new AntiBatsMod("antibats", false, "666 KILL BATS 666", Keyboard.KEY_END));
				registerMod(new AntiEffectsMod("antipotioneffects", false, "Removes potion effects", Keyboard.KEY_END));
				registerMod(new AntiFireMod("antifire", false, "Removes fire", Keyboard.KEY_END));
				registerMod(new AntiFogMod("antifog", false, "Removes fog", Keyboard.KEY_END));
				registerMod(new AntiHurtCamMod("antihurtcam", false, "Removes hurt camera effect", Keyboard.KEY_END));
				registerMod(new AntiKnockbackMod("antiknockback", false, "Removes knockback movement", Keyboard.KEY_END));
				registerMod(new AntiOverlayMod("antioverlay", false, "Removes screen overlays", Keyboard.KEY_END));
				registerMod(new AutoBlockCraft("autoblockcraft", false, "Automatically crafts blocks for you", Keyboard.KEY_END));
				registerMod(new AutoEatMod("autoeat", false, "Auto eats when you get hungry", Keyboard.KEY_END));
				registerMod(new AutoFishMod("autofish", false, "Auto fish", Keyboard.KEY_END));
				registerMod(new AutoProjectile("autoprojectile", false, "Automatically sets pitch to best trajectory", Keyboard.KEY_END));
				registerMod(new AutoReconnectMod("autoreconnect", false, "Automatically reconnects to server"));
				registerMod(new AutoRespawnMod("autorespawn", false, "Auto respawn on death", Keyboard.KEY_END));
				registerMod(new AutoSprintMod("autosprint", false, "Automatically sprints", Keyboard.KEY_END));
				registerMod(new AutoWalkMod("autowalk", false, "Automatically walks forward", Keyboard.KEY_END));
				registerMod(new BedModeMod("bedmode", false, "Sleep walking", Keyboard.KEY_END));
				registerMod(new ChamsMod("chams", false, "Render living models behind walls", Keyboard.KEY_END));
				registerMod(new ChatSpammerMod("chatspammer", false, "Add lines of spam into forgehax/spam.txt", Keyboard.KEY_END));
				registerMod(new StorageESPMod("storageesp", false, "Shows storage", Keyboard.KEY_END));
				registerMod(new EntityEspMod("entityesp", false, "Shows entity locations and info", Keyboard.KEY_END));
				registerMod(new FastBreak("fastbreak", false, "Fast break retard", Keyboard.KEY_END));
				registerMod(new FastPlaceMod("fastplace", false, "Fast place", Keyboard.KEY_END));
				registerMod(new FlyMod("fly", false, "Enables flying", Keyboard.KEY_END));
				registerMod(new FreecamMod("freecam", false, "Freecam mode", Keyboard.KEY_END));
				registerMod(new FullBrightMod("fullbright", false, "Makes everything render with maximum brightness", Keyboard.KEY_END));
				registerMod(new NoCaveCulling("nocaveculling", false, "Disables mojangs dumb cave culling shit", Keyboard.KEY_END));
				registerMod(new NoclipMod("noclip", false, "Enables player noclip", Keyboard.KEY_END));
				registerMod(new NoFallMod("nofall", false, "Prevents fall damage from being taken", Keyboard.KEY_END));
				registerMod(new NoSlowdown("noslowdown", false, "Disables block slowdown", Keyboard.KEY_END));
				registerMod(new ProjectilesMod("projectiles", false, "Draws projectile path", Keyboard.KEY_END));
				registerMod(new SafeWalkMod("safewalk", false, "Prevents you from falling off blocks", Keyboard.KEY_END));
				registerMod(new SpawnerEspMod("spawneresp", false, "Spawner esp", Keyboard.KEY_END));
				registerMod(new StepMod("step", false, "Step up blocks", Keyboard.KEY_END));
				registerMod(new TeleportMod("teleport", false, "Type '.setpos [x] [y] [z] [onGround]' in chat to use", Keyboard.KEY_END));
				registerMod(new XrayMod("xray", true, "See blocks through walls", Keyboard.KEY_END));
				registerMod(new YawLockMod("yawlock", false, "Locks yaw to prevent moving into walls", Keyboard.KEY_END));
				registerMod(new ElytraFlight("elytraflight", false, "Elytra Flight", Keyboard.KEY_END));
				registerMod(new DropInvMod("dropinvmod", false, "hax", Keyboard.KEY_END));
				registerMod(new AntiLevitationMod("antilevitation", false, "No levitation", Keyboard.KEY_END));
				registerMod(new CoordHaxMod("coordhax", false, "hax", Keyboard.KEY_END));
				registerMod(new ElytraPlus("elytraplus", false, "fly faster", Keyboard.KEY_END));

				//---- initialize configuration part 2 ----//
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
				// registerAll config
				MinecraftForge.EVENT_BUS.register(config);
				// registerAll event handler
				MinecraftForge.EVENT_BUS.register(new ForgeHaxEventHandler());
				// registerAll lag compenstator
				MinecraftForge.EVENT_BUS.register(LagCompensator.getInstance().getEventHandler());
				// registerAll mod events
				for (Map.Entry<String, BaseMod> entry : mods.entrySet()) {
					if (entry.getValue().isEnabled()) {
						entry.getValue().onEnabled();
						entry.getValue().register();
					}
				}
				// load all previous binds
				bindSerializer.deserialize();
				break;
			}
			default:
				break;
		}
	}
}
