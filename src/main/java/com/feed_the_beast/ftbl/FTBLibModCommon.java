package com.feed_the_beast.ftbl;

import com.feed_the_beast.ftbl.api.item.IItemLM;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class FTBLibModCommon // FTBLibModClient
{
    public void preInit()
    {
    }

    public void postInit()
    {
    }

    public EntityPlayer getClientPlayer()
    {
        return null;
    }

    public double getReachDist(EntityPlayer ep)
    {
        if(ep instanceof EntityPlayerMP)
        {
            return ((EntityPlayerMP) ep).interactionManager.getBlockReachDistance();
        }

        return 0D;
    }

    public void spawnDust(World worldObj, double x, double y, double z, int i)
    {
    }

    public boolean openClientGui(EntityPlayer ep, String mod, int ID, NBTTagCompound data)
    {
        return false;
    }

    public void loadModels(IItemLM i)
    {
    }
}