package com.feed_the_beast.ftbl.api.tile;

import com.feed_the_beast.ftbl.api.MouseButton;
import com.feed_the_beast.ftbl.api.client.FTBLibClient;
import com.feed_the_beast.ftbl.net.MessageClientTileAction;
import com.feed_the_beast.ftbl.net.MessageMarkTileDirty;
import com.feed_the_beast.ftbl.util.BlockDimPos;
import com.feed_the_beast.ftbl.util.LMNBTUtils;
import com.feed_the_beast.ftbl.util.PrivacyLevel;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IWorldNameable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.relauncher.Side;

import java.util.UUID;

public class TileLM extends TileEntity implements ITileEntity, IWorldNameable, ITickable
{
    protected enum EnumSync
    {
        OFF,
        SYNC,
        RERENDER;
        
        boolean sync()
        { return this == SYNC || this == RERENDER; }
        
        boolean rerender()
        { return this == RERENDER; }
    }
    
    private boolean isDirty = true;
    public boolean isLoaded = false;
    public UUID ownerID;
    public boolean redstonePowered = false;
    private IBlockState currentState;
    
    public boolean useOwnerID()
    { return true; }
    
    @Override
    public final TileEntity getTile()
    { return this; }
    
    @Override
    public final void writeToNBT(NBTTagCompound tag)
    {
        super.writeToNBT(tag);
        writeTileData(tag);
        
        if(ownerID != null && useOwnerID())
        {
            LMNBTUtils.setUUID(tag, "OwnerID", ownerID, true);
        }
    }
    
    @Override
    public final void readFromNBT(NBTTagCompound tag)
    {
        super.readFromNBT(tag);
        ownerID = useOwnerID() ? LMNBTUtils.getUUID(tag, "OwnerID", true) : null;
        readTileData(tag);
    }
    
    @Override
    public final Packet getDescriptionPacket()
    {
        NBTTagCompound tag = new NBTTagCompound();
        writeTileClientData(tag);
        
        if(ownerID != null && useOwnerID())
        {
            LMNBTUtils.setUUID(tag, "OID", ownerID, false);
        }
        
        return new SPacketUpdateTileEntity(getPos(), 0, tag);
    }
    
    @Override
    public final void onDataPacket(NetworkManager m, SPacketUpdateTileEntity p)
    {
        NBTTagCompound data = p.getNbtCompound();
        ownerID = useOwnerID() ? LMNBTUtils.getUUID(data, "OID", false) : null;
        readTileClientData(data);
        onUpdatePacket();
        FTBLibClient.onGuiClientAction();
    }
    
    public void writeTileData(NBTTagCompound tag)
    {
    }
    
    public void readTileData(NBTTagCompound tag)
    {
    }
    
    public void writeTileClientData(NBTTagCompound tag)
    {
        writeTileData(tag);
    }
    
    public void readTileClientData(NBTTagCompound tag)
    {
        readTileData(tag);
    }
    
    public void onUpdatePacket()
    {
        if(getSync().rerender())
        {
            worldObj.markBlockRangeForRenderUpdate(getPos(), getPos());
        }
    }
    
    public EnumSync getSync()
    { return EnumSync.SYNC; }
    
    public boolean onRightClick(EntityPlayer ep, ItemStack is, EnumFacing side, EnumHand hand, float x, float y, float z)
    {
        return false;
    }
    
    @Override
    public void onLoad()
    { isLoaded = true; }
    
    @Override
    public void onChunkUnload()
    { isLoaded = false; }
    
    @Override
    public final void update()
    {
        onUpdate();
        
        if(isDirty)
        {
            if(getSide().isServer())
            {
                sendDirtyUpdate();
            }
            
            isDirty = false;
        }
    }
    
    public void onUpdate() { }
    
    @Override
    public void markDirty()
    { isDirty = true; }
    
    public void sendDirtyUpdate()
    {
        if(worldObj != null)
        {
            updateContainingBlockInfo();
            
            if(getSync().sync())
            {
                new MessageMarkTileDirty(this).sendToDimension(worldObj.provider.getDimensionType());
            }
            
            worldObj.markChunkDirty(pos, this);
            
            if(getBlockType() != Blocks.AIR)
            {
                worldObj.updateComparatorOutputLevel(pos, getBlockType());
            }
        }
    }
    
    @Override
    public void updateContainingBlockInfo()
    {
        super.updateContainingBlockInfo();
        currentState = null;
    }
    
    public IBlockState getBlockState()
    {
        if(currentState == null)
        {
            currentState = worldObj.getBlockState(getPos());
        }
        
        return currentState;
    }
    
    public void onPlacedBy(EntityPlayer ep, ItemStack is, IBlockState state)
    {
        if(!(ep instanceof FakePlayer))
        {
            ownerID = ep.getGameProfile().getId();
        }
        
        if(is.hasDisplayName()) { setName(is.getDisplayName()); }
        
        markDirty();
    }
    
    public void onBroken(IBlockState state)
    {
    }
    
    public PrivacyLevel getPrivacyLevel()
    { return PrivacyLevel.PUBLIC; }
    
    public boolean isExplosionResistant()
    { return !getPrivacyLevel().isPublic(); }
    
    public final void sendClientAction(TileClientAction action, NBTTagCompound data)
    { new MessageClientTileAction(this, action, data).sendToServer(); }
    
    public void clientPressButton(int button, MouseButton mouseButton, NBTTagCompound data)
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("I", button);
        tag.setByte("MB", (byte) mouseButton.ordinal());
        if(data != null && !data.hasNoTags()) { tag.setTag("D", data); }
        sendClientAction(TileClientActionRegistry.BUTTON_PRESSED, tag);
    }
    
    public void clientOpenGui(NBTTagCompound data)
    { sendClientAction(TileClientActionRegistry.OPEN_GUI, data); }
    
    public void clientCustomName(String name)
    {
        NBTTagCompound data = new NBTTagCompound();
        data.setString("N", name);
        sendClientAction(TileClientActionRegistry.CUSTOM_NAME, data);
    }
    
    public final Side getSide()
    { return (worldObj != null && !worldObj.isRemote) ? Side.SERVER : Side.CLIENT; }
    
    public void notifyNeighbors()
    { worldObj.notifyBlockOfStateChange(getPos(), getBlockType()); }
    
    public void onNeighborBlockChange(BlockPos pos)
    {
        if(worldObj != null)
        {
            redstonePowered = worldObj.isBlockPowered(getPos());
            updateContainingBlockInfo();
        }
    }
    
    public void setName(String s) { }
    
    @Override
    public String getName()
    { return ""; }
    
    @Override
    public boolean hasCustomName()
    { return !getName().isEmpty(); }
    
    @Override
    public ITextComponent getDisplayName()
    { return hasCustomName() ? new TextComponentString(getName()) : new TextComponentTranslation(getBlockType().getLocalizedName() + ".name"); }
    
    public void playSound(SoundEvent event, SoundCategory category, float volume, float pitch)
    { worldObj.playSound(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, event, category, volume, pitch); }
    
    @Override
    public BlockDimPos getDimPos()
    { return new BlockDimPos(pos, worldObj == null ? null : worldObj.provider.getDimensionType()); }
}