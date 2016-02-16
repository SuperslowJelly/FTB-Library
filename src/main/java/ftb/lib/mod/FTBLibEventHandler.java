package ftb.lib.mod;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import ftb.lib.*;
import ftb.lib.api.*;
import ftb.lib.api.friends.*;
import ftb.lib.api.item.ICreativeSafeItem;
import ftb.lib.api.tile.ISecureTile;
import latmod.lib.*;
import latmod.lib.json.UUIDTypeAdapterLM;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.*;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;
import java.util.*;

public class FTBLibEventHandler
{
	public static final List<ServerTickCallback> callbacks = new ArrayList<>();
	public static final List<ServerTickCallback> pendingCallbacks = new ArrayList<>();
	
	@SubscribeEvent
	public void onWorldLoaded(WorldEvent.Load event)
	{
		if(event.world.provider.getDimensionId() == 0 && event.world instanceof WorldServer)
		{
			FTBLib.reload(FTBLib.getServer(), false, false);
			
			NBTTagCompound tag = LMNBTUtils.readTag(new File(LMWorldMP.inst.latmodFolder, "LMPlayers.dat"));
			
			LMWorldMP.inst.playerMap.clear();
			
			if(tag.hasNoTags()) return;
			
			boolean oldFormat = tag.getCompoundTag(tag.getKeySet().iterator().next()).hasKey("UUID");
			
			for(Map.Entry<String, NBTBase> e : LMNBTUtils.entrySet(tag))
			{
				NBTTagCompound tag1 = (NBTTagCompound) e.getValue();
				UUID id = UUIDTypeAdapterLM.getUUID(oldFormat ? tag1.getString("UUID") : e.getKey());
				
				if(id != null)
				{
					LMPlayerMP p = new LMPlayerMP(new GameProfile(id, tag1.getString("Name")));
					if(!oldFormat) p.readFromServer(tag1);
					LMWorldMP.inst.playerMap.put(id, p);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onWorldSaved(WorldEvent.Save event)
	{
		if(event.world.provider.getDimensionId() == 0 && event.world instanceof WorldServer)
		{
			JsonObject group = new JsonObject();
			LMWorldMP.inst.save(group);
			LMJsonUtils.toJson(new File(LMWorldMP.inst.latmodFolder, "world.json"), group);
			
			NBTTagCompound tag = new NBTTagCompound();
			
			for(LMPlayer p : LMMapUtils.values(LMWorldMP.inst.playerMap, null))
			{
				NBTTagCompound tag1 = new NBTTagCompound();
				p.toPlayerMP().writeToServer(tag1);
				tag1.setString("Name", p.getProfile().getName());
				tag.setTag(p.getStringUUID(), tag1);
			}
			
			LMNBTUtils.writeTag(new File(LMWorldMP.inst.latmodFolder, "LMPlayers.dat"), tag);
			
			// Export player list //
			
			try
			{
				ArrayList<String> l = new ArrayList<>();
				ArrayList<LMPlayer> players1 = new ArrayList<>();
				players1.addAll(LMWorldMP.inst.playerMap.values());
				Collections.sort(players1);
				
				for(LMPlayer p : players1)
				{
					StringBuilder sb = new StringBuilder();
					sb.append(p.getStringUUID());
					sb.append(" :: ");
					sb.append(p.getProfile().getName());
					l.add(sb.toString());
				}
				
				LMFileUtils.save(new File(LMWorldMP.inst.latmodFolder, "LMPlayers.txt"), l);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}
	
	@SubscribeEvent
	public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent e)
	{
		if(e.player instanceof EntityPlayerMP)
		{
			EntityPlayerMP ep = (EntityPlayerMP) e.player;
			
			LMPlayerMP p = LMWorldMP.inst.getPlayer(ep);
			
			boolean firstLogin = p == null;
			
			if(firstLogin)
			{
				p = new LMPlayerMP(ep.getGameProfile());
				LMWorldMP.inst.playerMap.put(p.getProfile().getId(), p);
				
				ForgePlayerDataEvent event = new ForgePlayerDataEvent(p);
			}
			else if(!p.getProfile().getName().equals(ep.getName()))
			{
				p.setProfile(ep.getGameProfile());
			}
			
			p.setPlayer(ep);
			p.onLoggedIn(firstLogin);
		}
	}
	
	@SubscribeEvent
	public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent e)
	{
		if(e.player instanceof EntityPlayerMP)
		{
			LMWorldMP.inst.getPlayer(e.player).onLoggedOut();
		}
	}
	
	@SubscribeEvent
	public void onPlayerDeath(LivingDeathEvent e)
	{
		if(e.entity instanceof EntityPlayerMP)
		{
			LMWorldMP.inst.getPlayer(e.entity).onDeath();
		}
	}
	
	@SubscribeEvent
	public void onWorldTick(TickEvent.WorldTickEvent e)
	{
		if(!e.world.isRemote && e.side == Side.SERVER && e.phase == TickEvent.Phase.END && e.type == TickEvent.Type.WORLD)
		{
			if(e.world.provider.getDimensionId() == 0)
			{
				if(!pendingCallbacks.isEmpty())
				{
					callbacks.addAll(pendingCallbacks);
					pendingCallbacks.clear();
				}
				
				if(!callbacks.isEmpty())
				{
					for(int i = callbacks.size() - 1; i >= 0; i--)
						if(callbacks.get(i).incAndCheck()) callbacks.remove(i);
				}
			}
			
			if(e.world instanceof WorldServer && !LMWorldMP.inst.ticking.isEmpty())
			{
				WorldServer ws = (WorldServer) e.world;
				long now = LMUtils.millis();
				
				for(ITickingLMWorld t : LMWorldMP.inst.ticking)
				{
					t.onTick(ws, now);
				}
			}
			
			if(FTBLib.ftbu != null) FTBLib.ftbu.onServerTick(e.world);
		}
	}
	
	@SubscribeEvent
	public void onRightClick(PlayerInteractEvent e)
	{
		if(e.entityPlayer instanceof FakePlayer || e.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) return;
		else if(!canInteract(e.entityPlayer, e.pos, e.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK))
			e.setCanceled(true);
		else if(FTBLib.ftbu != null) FTBLib.ftbu.onRightClick(e);
	}
	
	private boolean canInteract(EntityPlayer ep, BlockPos pos, boolean leftClick)
	{
		ItemStack heldItem = ep.getHeldItem();
		
		if(ep.capabilities.isCreativeMode && leftClick && heldItem != null && heldItem.getItem() instanceof ICreativeSafeItem)
		{
			if(!ep.worldObj.isRemote) ep.worldObj.markBlockRangeForRenderUpdate(pos, pos);
			else ep.worldObj.markBlockForUpdate(pos);
			return false;
		}
		
		if(!ep.worldObj.isRemote)
		{
			IBlockState state = ep.worldObj.getBlockState(pos);
			
			if(state.getBlock().hasTileEntity(state))
			{
				TileEntity te = ep.worldObj.getTileEntity(pos);
				if(te instanceof ISecureTile && !te.isInvalid() && !((ISecureTile) te).canPlayerInteract(ep, leftClick))
				{
					((ISecureTile) te).onPlayerNotOwner(ep, leftClick);
					return false;
				}
			}
		}
		
		return true;
	}
}