package com.feed_the_beast.ftbl.gui.friends;

import com.feed_the_beast.ftbl.api.ForgePlayer;
import com.feed_the_beast.ftbl.api.ForgePlayerComparators;
import com.feed_the_beast.ftbl.api.ForgeWorldSP;
import com.feed_the_beast.ftbl.api.info.InfoPage;
import com.feed_the_beast.ftbl.api.info.InfoPageTheme;
import latmod.lib.LMColor;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by LatvianModder on 23.03.2016.
 */
@SideOnly(Side.CLIENT)
public class InfoFriendsGUI extends InfoPage
{
    public InfoFriendsGUI()
    {
        super("friends_gui");
        setTitle(new TextComponentString("FriendsGUI"));
        theme = new InfoPageTheme().setBackgroundColor(new LMColor.RGB(30, 30, 30)).setTextColor(new LMColor.RGB(200, 200, 200)).setUseUnicodeFont(false);

        List<ForgePlayer> tempPlayerList = new ArrayList<>();
        tempPlayerList.addAll(ForgeWorldSP.inst.playerMap.values());

        tempPlayerList.remove(ForgeWorldSP.inst.clientPlayer);

        //if(FTBUClient.sort_friends_az.getMode()) Collections.sort(tempPlayerList, LMPNameComparator.instance);
        //else Collections.sort(tempPlayerList, new LMPStatusComparator(LMWorldClient.inst.clientPlayer));

        Collections.sort(tempPlayerList, new ForgePlayerComparators.ByStatus(ForgeWorldSP.inst.clientPlayer));

        addSub(new InfoFriendsGUISelfPage());

        for(ForgePlayer p : tempPlayerList)
        {
            addSub(new InfoFriendsGUIPage(p.toPlayerSP()));
        }
    }
}