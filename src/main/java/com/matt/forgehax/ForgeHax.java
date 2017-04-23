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
import java.util.Collections;
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

	public Map<String, BaseMod> getMods() {
		return Collections.unmodifiableMap(mods);
	}

	public BaseMod getMod(String name) {
		return mods.get(name);
	}

	public void setupConfigFolder() {
		File userDir = new File(getBaseDirectory(), "users");
		userDir.mkdirs();
		configFolder = new File(userDir, "devmode");
		configFolder.mkdirs();
	}

	protected void registerMod(BaseMod mod) {
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
				registerMod(new ActiveModListMod());
				registerMod(new AimbotMod());
				registerMod(new AntiAfkMod());
				registerMod(new AntiBatsMod());
				registerMod(new AntiEffectsMod());
				registerMod(new AntiFireMod());
				registerMod(new AntiFogMod());
				registerMod(new AntiHurtCamMod());
				registerMod(new AntiKnockbackMod());
				registerMod(new AntiOverlayMod());
				registerMod(new AutoBlockCraft());
				registerMod(new AutoEatMod());
				registerMod(new AutoFishMod());
				registerMod(new AutoProjectile());
				registerMod(new AutoReconnectMod());
				registerMod(new AutoRespawnMod());
				registerMod(new AutoSprintMod());
				registerMod(new AutoWalkMod());
				registerMod(new BedModeMod());
				registerMod(new ChamsMod());
				registerMod(new ChatSpammerMod());
				registerMod(new StorageESPMod());
				registerMod(new EntityEspMod());
				registerMod(new FastBreak());
				registerMod(new FastPlaceMod());
				registerMod(new FlyMod());
				registerMod(new FreecamMod());
				registerMod(new FullBrightMod());
				registerMod(new NoCaveCulling());
				registerMod(new NoclipMod());
				registerMod(new NoFallMod());
				registerMod(new NoSlowdown());
				registerMod(new ProjectilesMod());
				registerMod(new SafeWalkMod());
				registerMod(new SpawnerEspMod());
				registerMod(new StepMod());
				registerMod(new TeleportMod());
				registerMod(new XrayMod());
				registerMod(new YawLockMod());
				registerMod(new ElytraFlight());
				//registerMod(new DropInvMod()); // disabled
				registerMod(new AntiLevitationMod());
				//registerMod(new CoordHaxMod()); // disabled
				registerMod(new ElytraPlus());

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
