package dev.fiki.forgehax.main.mods.misc;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import dev.fiki.forgehax.api.asm.MapField;
import dev.fiki.forgehax.api.cmd.settings.BooleanSetting;
import dev.fiki.forgehax.api.cmd.settings.IntegerSetting;
import dev.fiki.forgehax.api.cmd.settings.StringSetting;
import dev.fiki.forgehax.api.event.SubscribeListener;
import dev.fiki.forgehax.api.events.entity.LocalPlayerUpdateEvent;
import dev.fiki.forgehax.api.events.render.GuiChangedEvent;
import dev.fiki.forgehax.api.events.world.WorldLoadEvent;
import dev.fiki.forgehax.api.mod.Category;
import dev.fiki.forgehax.api.mod.ToggleMod;
import dev.fiki.forgehax.api.modloader.RegisterMod;
import dev.fiki.forgehax.api.modloader.di.Injected;
import dev.fiki.forgehax.api.reflection.types.ReflectionField;
import dev.fiki.forgehax.asm.events.packet.PacketInboundEvent;
import dev.fiki.forgehax.main.Common;
import lombok.RequiredArgsConstructor;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import sun.security.validator.ValidatorException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@RegisterMod(
    name = "MatrixNotifications",
    description = "Matrix notifications",
    category = Category.MISC
)
@RequiredArgsConstructor
public class MatrixNotifications extends ToggleMod {
  private static final Registry<ConnectionSocketFactory> SOCKET_FACTORY_REGISTRY;

  static {
    Registry<ConnectionSocketFactory> sf = null;
    try {
      SSLContextBuilder builder = SSLContexts.custom();
      builder.loadTrustMaterial(null, (chain, authType) -> true);
      SSLContext sslContext = builder.build();
      SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext,
          new AllowAllHostsVerifier());
      sf = RegistryBuilder.<ConnectionSocketFactory>create().register("https", sslsf).build();
    } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
      e.printStackTrace();
    } finally {
      SOCKET_FACTORY_REGISTRY = sf;
    }
  }

  @MapField(parentClass = DisconnectedScreen.class, value = "message")
  private final ReflectionField<ITextComponent> DisconnectedScreen_message;

  @Injected("async")
  private final ExecutorService async;

  private final StringSetting url = newStringSetting()
      .name("url")
      .description("URL to the Matrix web hook")
      .defaultTo("")
      .build();

  private final StringSetting user = newStringSetting()
      .name("user")
      .description("User to ping for high priority messages")
      .defaultTo("")
      .build();

  private final StringSetting skin_server_url = newStringSetting()
      .name("skin-server-url")
      .description("URL to the skin server. If left empty then no image will be used.")
      .defaultTo("https://visage.surgeplay.com/face/160/")
      .build();

  private final IntegerSetting queue_notify_pos = newIntegerSetting()
      .name("queue-notify-pos")
      .description("Position to start sending notifications at")
      .defaultTo(5)
      .build();

  private final BooleanSetting on_connected = newBooleanSetting()
      .name("on-connected")
      .description("Message on connected to server")
      .defaultTo(true)
      .build();

  private final BooleanSetting on_disconnected = newBooleanSetting()
      .name("on-disconnected")
      .description("Message on disconnected from server")
      .defaultTo(true)
      .build();

  private final BooleanSetting on_queue_move = newBooleanSetting()
      .name("on-queue-move")
      .description("Message when player moves in the queue")
      .defaultTo(true)
      .build();

  private boolean joined = false;
  private boolean once = false;
  private int position = 0;
  private String serverName = null;

  private static CloseableHttpClient createHttpClient() {
    final RequestConfig req = RequestConfig.custom()
        .setConnectTimeout(30 * 1000)
        .setConnectionRequestTimeout(30 * 1000)
        .build();

    if (SOCKET_FACTORY_REGISTRY == null) {
      return HttpClientBuilder.create()
          .setDefaultRequestConfig(req)
          .build();
    } else {
      return HttpClients.custom()
          .setDefaultRequestConfig(req)
          .setConnectionManager(new PoolingHttpClientConnectionManager(SOCKET_FACTORY_REGISTRY))
          .build();
    }
  }

  private static HttpResponse post(final String url, final JsonElement json) throws IOException {
    final Gson gson = new Gson();
    try (CloseableHttpClient client = createHttpClient()) {
      final HttpPost post = new HttpPost(url);
      StringEntity entity = new StringEntity(gson.toJson(json));
      post.setEntity(entity);
      post.setHeader("Content-type", "application/json");
      return client.execute(post);
    }
  }

  private void postAsync(final String url, final JsonElement json) {
    async.submit(() -> {
      try {
        HttpResponse res = post(url, json);
        if (res.getStatusLine().getStatusCode() != 200) {
          throw new Error("got response code " + res.getStatusLine().getStatusCode());
        }
      } catch (Throwable t) {
        if (t.getCause() instanceof ValidatorException) {
          Common.printError("Java JRE outdated. Change games to use the latest JRE.");
        } else {
          Common.printError("Failed to send message to url: " + t.getMessage());
        }
        t.printStackTrace();
      }
    });
  }

  private static String getServerName() {
    return Optional.ofNullable(Common.MC.getCurrentServer())
        .map(data -> data.name)
        .orElse("server");
  }

  private static String getUriUuid() {
    return Optional.of(Common.MC.getUser().getGameProfile())
        .map(GameProfile::getId)
        .map(UUID::toString)
        .map(id -> id.replaceAll("-", ""))
        .orElse(null);
  }

  private void notify(String message) {
    JsonObject object = new JsonObject();
    object.addProperty("text", message);
    object.addProperty("format", "plain");
    object.addProperty("displayName", Common.MC.getUser().getName());

    String id = getUriUuid();
    if (!skin_server_url.getValue().isEmpty() && id != null) {
      object.addProperty("avatarUrl", skin_server_url.getValue() + id);
    }

    postAsync(url.getValue(), object);
  }

  private void notify(String message, Object... args) {
    notify(String.format(message, args));
  }

  private void ping(String message, Object... args) {
    String msg = String.format(message, args);
    if (user.getValue().isEmpty()) {
      notify(msg);
    } else {
      notify("@" + user.getValue() + " " + msg);
    }
  }

  @Override
  protected void onEnabled() {
    joined = once = false;
    position = 0;

    if (url.getValue().isEmpty()) {
      Common.printError("Missing url");
    }

    if (SOCKET_FACTORY_REGISTRY == null) {
      Common.printError(
          "Custom socket factory has not been registered. All host SSL certificates must be trusted with the current JRE");
    }
  }

  @SubscribeListener
  public void onTick(LocalPlayerUpdateEvent event) {
    joined = true;

    if (!once) {
      once = true;

      if (on_connected.getValue()) {
        BlockPos pos = Common.getLocalPlayer().blockPosition();
        if (pos.getX() != 0 && pos.getZ() != 0) {
          ping("Connected to %s", getServerName());
        } else {
          notify("Connected to %s queue", getServerName());
        }
      }
    }
  }

  @SubscribeListener
  public void onWorldUnload(WorldLoadEvent event) {
    once = false;
    position = 0;

    if (Common.MC.getCurrentServer() != null) {
      serverName = getServerName();
    }
  }

  @SubscribeListener
  public void onGuiOpened(GuiChangedEvent event) {
    if (event.getGui() instanceof DisconnectedScreen && joined) {
      joined = false;

      if (on_disconnected.getValue()) {
        String reason = Optional.ofNullable(DisconnectedScreen_message.get(event.getGui()))
            .map(ITextComponent::getString)
            .orElse("");
        if (reason.isEmpty()) {
          notify("Disconnected from %s", serverName);
        } else {
          notify("Disconnected from %s. Reason: %s", serverName, reason);
        }
      }
    }
  }

  @SubscribeListener
  public void onPacketRecieve(PacketInboundEvent event) {
    if (event.getPacket() instanceof SChatPacket) {
      SChatPacket packet = (SChatPacket) event.getPacket();
      if (packet.getType() == ChatType.SYSTEM) {
        ITextComponent comp = packet.getMessage();
        if (comp.getSiblings().size() >= 2) {
          String text = comp.getSiblings().get(0).getString();
          if ("Position in queue: ".equals(text)) {
            try {
              int pos = Integer.parseInt(comp.getSiblings().get(1).getString());
              if (pos != position) {
                position = pos;
                if (on_queue_move.getValue() && position <= queue_notify_pos.getValue()) {
                  if (position == 1) {
                    ping("Position 1 in queue");
                  } else {
                    notify("Position %d in queue", position);
                  }
                }
              }
            } catch (Throwable t) {
              // ignore
            }
          }
        }
      }
    }
  }

  private static class AllowAllHostsVerifier implements X509HostnameVerifier {
    @Override
    public void verify(String host, SSLSocket ssl) throws IOException {
    }

    @Override
    public void verify(String host, X509Certificate cert) throws SSLException {
    }

    @Override
    public void verify(String host, String[] cns, String[] subjectAlts) throws SSLException {
    }

    @Override
    public boolean verify(String s, SSLSession sslSession) {
      return true;
    }
  }
}
