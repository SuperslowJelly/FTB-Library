package com.feed_the_beast.ftbl.gui.friends;

import com.feed_the_beast.ftbl.api.EnumSelf;
import com.feed_the_beast.ftbl.api.ForgePlayerSP;
import com.feed_the_beast.ftbl.api.ForgeWorldSP;
import com.feed_the_beast.ftbl.api.MouseButton;
import com.feed_the_beast.ftbl.api.PlayerAction;
import com.feed_the_beast.ftbl.api.gui.GuiLM;
import com.feed_the_beast.ftbl.api.gui.PlayerActionRegistry;
import com.feed_the_beast.ftbl.api.info.InfoPage;
import com.feed_the_beast.ftbl.gui.info.ButtonInfoPage;
import com.feed_the_beast.ftbl.gui.info.GuiInfo;
import com.feed_the_beast.ftbl.net.MessageRequestPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by LatvianModder on 24.03.2016.
 */
@SideOnly(Side.CLIENT)
public class InfoFriendsGUIPage extends InfoPage
{
    public final ForgePlayerSP playerLM;
    
    public InfoFriendsGUIPage(ForgePlayerSP p)
    {
        super(p.getProfile().getName());
        playerLM = p;
    }
    
    @Override
    public void refreshGui(GuiInfo gui)
    {
        clear();
        
        text.add(new InfoPlayerViewLine(this, playerLM));
        
        if(!playerLM.clientInfo.isEmpty())
        {
            for(String s : playerLM.clientInfo)
            {
                printlnText(s);
            }
            
            text.add(null);
        }
        
        for(PlayerAction a : PlayerActionRegistry.getPlayerActions(EnumSelf.OTHER, ForgeWorldSP.inst.clientPlayer, playerLM, true, true))
        {
            text.add(new InfoPlayerActionLine(this, playerLM, a));
        }
        
		/*
		if(LMWorldClient.inst.clientPlayer.isFriend(playerLM))
		{
			text.add(null);
			text.add(new InfoPlayerInventoryLine(this, playerLM));
		}
		*/
    }
    
    @Override
    public ButtonInfoPage createButton(GuiInfo gui)
    { return new Button(gui, this); }
    
    private class Button extends ButtonInfoPage
    {
        public Button(GuiInfo g, InfoFriendsGUIPage p)
        {
            super(g, p, null);
            height = 20;
        }
        
        @Override
        public void updateTitle()
        {
            title = playerLM.getProfile().getName();
            hover = null;
            
            if(gui.getFontRenderer().getStringWidth(title) > width - 24)
            {
                hover = title + "";
                title = gui.getFontRenderer().trimStringToWidth(title, width - 22) + "...";
            }
        }
        
        @Override
        public void onClicked(MouseButton button)
        {
            new MessageRequestPlayerInfo(playerLM.getProfile().getId()).sendToServer();
            super.onClicked(button);
        }
        
        @Override
        public void renderWidget()
        {
            int ay = getAY();
            if(ay < -height || ay > guiInfo.mainPanel.height) { return; }
            int ax = getAX();
            
            double z = gui.getZLevel();
            
            if(mouseOver())
            {
                GlStateManager.color(1F, 1F, 1F, 0.2F);
                GuiLM.drawBlankRect(ax, ay, z, width, height);
            }
            
            boolean raw1 = playerLM.isFriendRaw(ForgeWorldSP.inst.clientPlayer);
            boolean raw2 = ForgeWorldSP.inst.clientPlayer.isFriendRaw(playerLM);
            
            GlStateManager.color(0F, 0F, 0F, 1F);
            if(raw1 && raw2) { GlStateManager.color(0.18F, 0.74F, 0.18F, 1F); }
            //else if(raw1 || raw2) GlStateManager.color(raw1 ? 0xFFE0BE00 : 0xFF00B6ED);
            else if(raw1) { GlStateManager.color(0.87F, 0.74F, 0F, 1F); }
            else if(raw2) { GlStateManager.color(0F, 0.71F, 0.92F, 1F); }
            
            GuiLM.drawBlankRect(ax + 1, ay + 1, z, 18, 18);
            
            GlStateManager.color(1F, 1F, 1F, 1F);
            GuiLM.drawPlayerHead(playerLM.getProfile().getName(), ax + 2, ay + 2, 16, 16, z);
            
            gui.getFontRenderer().drawString(title, ax + 22, ay + 6, playerLM.isOnline() ? 0xFF11FF11 : 0xFFFFFFFF);
        }
    }
}
