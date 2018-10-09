package com.matt.forgehax.mods;

import com.google.common.eventbus.Subscribe;
import com.matt.forgehax.events.ChatMessageEvent;
import com.matt.forgehax.events.ServerConnectionEvent;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

import static com.matt.forgehax.Helper.getFileManager;

/**
 * Created on 8/1/2017 by fr1kin
 */
public class ChatLogger extends ToggleMod {
    private static final File CHATLOGS_DIR = getFileManager().getFileInBaseDirectory("chatlogs");

    private static final SimpleDateFormat FORMAT_PUBLIC_CHAT_FILE = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");

    static { //[MM/dd/yyyy::HH:mm:ss]
        CHATLOGS_DIR.mkdirs();
    }

    private File publicLog;

    public ChatLogger() {
        super(Category.MISC, "ChatLogger", false, "Logs chat messages");
    }

    private File getServerDir() {
        return new File(CHATLOGS_DIR, MC.getCurrentServerData() != null ? MC.getCurrentServerData().serverName : ".null");
    }

    private File getPublicDir() {
        return new File(getPublicDir(), ".public");
    }

    private File getOrSetPublicLog() {
        if(publicLog == null) {
            File pubDir = getPublicDir();
            pubDir.mkdirs();
            publicLog = new File(pubDir, FORMAT_PUBLIC_CHAT_FILE.format(LocalDate.now()) + ".txt");
        }
        return publicLog;
    }

    @Subscribe
    public void onConnect(ServerConnectionEvent.Connect event) {
        getOrSetPublicLog(); // set public log file
    }

    @Subscribe
    public void onDisconnect(ServerConnectionEvent.Disconnect event) {
        publicLog = null;
    }

    @Subscribe
    public void onChat(ChatMessageEvent event) {
        if(event.isWhispering()) {
        }
    }
}
