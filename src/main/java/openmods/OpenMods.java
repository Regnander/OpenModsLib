package openmods;

import java.io.File;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import openmods.config.properties.CommandConfig;
import openmods.config.properties.ConfigProcessing;
import openmods.entity.DelayedEntityLoadManager;
import openmods.events.network.TileEntityEventHandler;
import openmods.events.network.TileEntityMessageEventPacket;
import openmods.fakeplayer.FakePlayerPool;
import openmods.integration.Integration;
import openmods.integration.modules.BuildCraftPipes;
import openmods.network.IdSyncManager;
import openmods.network.event.NetworkEventManager;
import openmods.network.rpc.RpcCallDispatcher;
import openmods.network.rpc.targets.EntityTargetWrapper;
import openmods.network.rpc.targets.TileEntityTargetWrapper;
import openmods.proxy.IOpenModsProxy;
import cpw.mods.fml.common.*;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.*;

@Mod(modid = "OpenMods", name = "OpenMods", version = "0.5", dependencies = "required-after:OpenModsCore")
public class OpenMods {

	@Instance(value = "OpenMods")
	public static OpenMods instance;

	@SidedProxy(clientSide = "openmods.proxy.OpenClientProxy", serverSide = "openmods.proxy.OpenServerProxy")
	public static IOpenModsProxy proxy;

	@EventHandler
	public void preInit(FMLPreInitializationEvent evt) {
		NetworkEventManager.INSTANCE.startRegistration()
				.register(TileEntityMessageEventPacket.class);

		RpcCallDispatcher.INSTANCE.startRegistration()
				.registerTargetWrapper(EntityTargetWrapper.class)
				.registerTargetWrapper(TileEntityTargetWrapper.class);

		final File configFile = evt.getSuggestedConfigurationFile();
		Configuration config = new Configuration(configFile);
		ConfigProcessing.processAnnotations(configFile, "OpenMods", config, LibConfig.class);
		if (config.hasChanged()) config.save();

		MinecraftForge.EVENT_BUS.register(new TileEntityEventHandler());

		MinecraftForge.EVENT_BUS.register(DelayedEntityLoadManager.instance);

		MinecraftForge.EVENT_BUS.register(FakePlayerPool.instance);

		proxy.preInit();
	}

	@EventHandler
	public void init(FMLInitializationEvent evt) {
		proxy.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent evt) {
		Integration.addModule(new BuildCraftPipes());
		Integration.loadModules();
		proxy.postInit();

		NetworkEventManager.INSTANCE.finalizeRegistration();
		RpcCallDispatcher.INSTANCE.finishRegistration();

		// must be after all builders are done
		IdSyncManager.INSTANCE.finishLoading();
	}

	@EventHandler
	public void severStart(FMLServerStartingEvent evt) {
		evt.registerServerCommand(new CommandConfig("om_config_s", true));
	}
}
