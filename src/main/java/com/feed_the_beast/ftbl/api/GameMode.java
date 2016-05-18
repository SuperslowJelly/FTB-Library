package com.feed_the_beast.ftbl.api;

import com.feed_the_beast.ftbl.util.FTBLib;
import latmod.lib.util.FinalIDObject;

import java.io.File;

/**
 * Created by LatvianModder on 07.01.2016.
 */
public class GameMode extends FinalIDObject
{
    public GameMode(String id)
    { super(id); }
    
    public File getFolder()
    {
        File f = new File(FTBLib.folderModpack, getID());
        if(!f.exists()) { f.mkdirs(); }
        return f;
    }
    
    public File getFile(String path)
    { return new File(getFolder(), path); }
}