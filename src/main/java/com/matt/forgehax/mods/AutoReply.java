package com.matt.forgehax.mods;


import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@RegisterMod
public class AutoReply extends ToggleMod {
	private String modeString = "/r ";
	private Property reply;
	private Property mode;
	private Property search;
	private static final String[] MODE = {"REPLY", "CHAT"};
		    
    public AutoReply() {
        super("AutoReply", false, "Automatically talk in chat if finds a strings");
    }

    @SubscribeEvent
    public void onClientChat(ClientChatReceivedEvent event) {
    	
             String message = ( event.getMessage().getUnformattedText() );
             
             if ( message.contains(search.getString()) && !message.startsWith(MC.getSession().getUsername() )) {
            	 if (mode.getString().equals(MODE[0])) {
            		 modeString = "/r ";
            	 }
            	 else if (mode.getString().equals(MODE[1])) {
            		 modeString = "";
            	 }
                 MC.player.sendChatMessage(modeString + reply.getString());
             }
    }
    
    @Override
    public void loadConfig(Configuration configuration) {
        addSettings( search = configuration.get(getModName(), "search", "whispers: ", "text to search for") );
        addSettings( reply = configuration.get(getModName(), "text", "fuck off newfag", "text to reply with") );
        addSettings( mode = configuration.get(getModName(), "mode", MODE[0], "reply or chat", MODE) );
    }
}
