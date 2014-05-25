package openmods.network.targets;

import java.util.Collection;

import net.minecraft.entity.player.EntityPlayerMP;
import openmods.network.IPacketTargetSelector;
import openmods.utils.NetUtils;
import cpw.mods.fml.common.network.handshake.NetworkDispatcher;
import cpw.mods.fml.relauncher.Side;

public class SelectMultiplePlayers implements IPacketTargetSelector {

	@Override
	public boolean isAllowedOnSide(Side side) {
		return side == Side.SERVER;
	}

	@Override
	public void listDispatchers(Object arg, Collection<NetworkDispatcher> result) {
		try {
			@SuppressWarnings("unchecked")
			Collection<EntityPlayerMP> players = (Collection<EntityPlayerMP>)arg;
			for (Object o : players) {
				EntityPlayerMP player = (EntityPlayerMP)o;
				NetworkDispatcher dispatcher = NetUtils.getPlayerDispatcher(player);
				result.add(dispatcher);
			}
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Argument must be collection of EntityPlayerMP");
		}
	}

}
