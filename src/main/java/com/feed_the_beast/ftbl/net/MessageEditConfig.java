package com.feed_the_beast.ftbl.net;

import com.feed_the_beast.ftbl.client.GuiEditConfig;
import com.feed_the_beast.ftbl.lib.config.ConfigGroup;
import com.feed_the_beast.ftbl.lib.config.IConfigCallback;
import com.feed_the_beast.ftbl.lib.io.DataIn;
import com.feed_the_beast.ftbl.lib.io.DataOut;
import com.feed_the_beast.ftbl.lib.net.MessageToClient;
import com.feed_the_beast.ftbl.lib.net.NetworkWrapper;
import net.minecraft.entity.player.EntityPlayer;

/**
 * @author LatvianModder
 */
public class MessageEditConfig extends MessageToClient<MessageEditConfig>
{
	private static final IConfigCallback RX_CONFIG_TREE = (group, sender, json) -> new MessageEditConfigResponse(json.getAsJsonObject()).sendToServer();

	private ConfigGroup group;

	public MessageEditConfig()
	{
	}

	public MessageEditConfig(ConfigGroup _group)
	{
		group = _group;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBLibNetHandler.EDIT_CONFIG;
	}

	@Override
	public void writeData(DataOut data)
	{
		group.writeData(data);
	}

	@Override
	public void readData(DataIn data)
	{
		group = new ConfigGroup(null);
		group.readData(data);
	}

	@Override
	public void onMessage(final MessageEditConfig m, EntityPlayer player)
	{
		new GuiEditConfig(m.group, RX_CONFIG_TREE).openGuiLater();
	}
}