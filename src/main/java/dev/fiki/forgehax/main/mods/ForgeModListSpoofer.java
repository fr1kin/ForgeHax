package dev.fiki.forgehax.main.mods;

import com.google.common.collect.Sets;
import dev.fiki.forgehax.main.util.cmd.argument.Arguments;
import dev.fiki.forgehax.main.util.cmd.flag.EnumFlag;
import dev.fiki.forgehax.main.util.cmd.settings.collections.SimpleSettingSet;
import dev.fiki.forgehax.main.util.mod.Category;
import dev.fiki.forgehax.main.util.mod.ToggleMod;
import dev.fiki.forgehax.main.util.mod.loader.RegisterMod;
import dev.fiki.forgehax.main.util.reflection.fasttype.FastClass;
import dev.fiki.forgehax.main.util.reflection.fasttype.FastField;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectArrayMap;
import lombok.SneakyThrows;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.FMLHandshakeMessages;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.simple.IndexedMessageCodec;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.lang.reflect.Constructor;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static dev.fiki.forgehax.main.Common.getLogger;

/**
 * The jenkins server won't include this class in its artifacts
 */

@RegisterMod
public class ForgeModListSpoofer extends ToggleMod {
  private final SimpleSettingSet<String> spoofing = newSimpleSettingSet(String.class)
      .name("spoofing")
      .description("List of mods to remove from forges modlist handshake packet")
      .argument(Arguments.newStringArgument()
          .label("modid")
          .build())
      .supplier(Sets::newHashSet)
      .defaultsTo("forgehax")
      .build();

  private Object oldMessageHandler = null;
  private short handlerIndex = 0;

  public ForgeModListSpoofer() {
    super(Category.MISC, "ForgeModListSpoofer",
        true, "Hide mods from forge handshake");
    addFlag(EnumFlag.HIDDEN);
  }

  private IndexedMessageCodec getCodec() {
    SimpleChannel handshakeChannel = Objects.requireNonNull(FMLNetworkConstants_handshakeChannel.getStatic(),
        "Could not find handshake channel");
    return Objects.requireNonNull(SimpleChannel_indexedCodec.get(handshakeChannel), "Channel Codec is null");
  }

  @SneakyThrows
  @Override
  protected void onEnabled() {
    try {
      // get channel codec
      IndexedMessageCodec codec = getCodec();

      final Class<?> type = FMLHandshakeMessages.S2CModList.class;

      // get handler by class type
      Object handler = IndexedMessageCodec_types.get(codec).get(type);
      Objects.requireNonNull(handler, "Could not find MessageHandler for S2CModList type");

      // keep the old handler
      oldMessageHandler = handler;

      // MessageHandler fields that need to be copied
      BiConsumer<Object, PacketBuffer> encoder = MessageHandler_encoder.get(handler).orElse(null);
      Function<PacketBuffer, Object> decoder = MessageHandler_decoder.get(handler).orElse(null);
      int index = MessageHandler_index.get(handler);
      BiConsumer<Object, Supplier<NetworkEvent.Context>> messageConsumer = MessageHandler_messageConsumer.get(handler);
      Optional<NetworkDirection> networkDirection = MessageHandler_networkDirection.get(handler);
      Optional<BiConsumer<Object, Integer>> loginIndexSetter = MessageHandler_loginIndexSetter.get(handler);
      Optional<Function<Object, Integer>> loginIndexGetter = MessageHandler_loginIndexGetter.get(handler);

      handlerIndex = (short) (index & 0xFF);

      // remove old handler from maps
      IndexedMessageCodec_indicies.get(codec).remove(handlerIndex);
      IndexedMessageCodec_types.get(codec).remove(type);

      // create new message handler
      Constructor<?> constructor = IndexedMessageCodec_MessageHandler.getInstance()
          .getDeclaredConstructor(IndexedMessageCodec.class, int.class,
              Class.class, BiConsumer.class, Function.class, BiConsumer.class, Optional.class);
      constructor.setAccessible(true);

      // create new encoder that removes the mod
      BiConsumer<Object, PacketBuffer> overrideEncoder = (o, buffer) -> {
        if (o instanceof FMLHandshakeMessages.S2CModList) {
          FMLHandshakeMessages.S2CModList instance = (FMLHandshakeMessages.S2CModList) o;
          instance.getModList().removeAll(spoofing);
        } else {
          getLogger().error("Encoder is trying to encode object that isn't S2CModList");
        }

        if (encoder != null) {
          encoder.accept(o, buffer);
        }
      };

      // create message handler with our own encoder
      Object instance = constructor.newInstance(codec, index, type,
          overrideEncoder, decoder, messageConsumer, networkDirection);

      // set the new MessageHandler's loginIndexSetter/Getter
      MessageHandler_loginIndexSetter.set(instance, loginIndexSetter);
      MessageHandler_loginIndexGetter.set(instance, loginIndexGetter);

      // the encoder should now be redirected to our custom encoder
    } catch (Throwable t) {
      getLogger().warn("Could load mod {}, probably not running newer forge version", getName());
      getLogger().error(t, t);
    }
  }

  @Override
  protected void onDisabled() {
    Objects.requireNonNull(oldMessageHandler, "Old message handler is null");

    // get channel codec
    IndexedMessageCodec codec = getCodec();

    // remove custom handler from maps
    IndexedMessageCodec_indicies.get(codec).remove(handlerIndex);
    IndexedMessageCodec_types.get(codec).remove(FMLHandshakeMessages.S2CModList.class);

    // add back old handler
    IndexedMessageCodec_indicies.get(codec).put(handlerIndex, oldMessageHandler);
    IndexedMessageCodec_types.get(codec).put(FMLHandshakeMessages.S2CModList.class, oldMessageHandler);

    // yeet
    oldMessageHandler = null;
  }

  //
  //
  //

  private static final FastClass<?> IndexedMessageCodec_MessageHandler =
      FastClass.builder()
          .className("net.minecraftforge.fml.network.simple.IndexedMessageCodec$MessageHandler")
          .build();

  private static final FastField<Short2ObjectArrayMap<Object>> IndexedMessageCodec_indicies =
      FastField.builder()
          .parent(IndexedMessageCodec.class)
          .name("indicies")
          .build();

  private static final FastField<Object2ObjectArrayMap<Class<?>, Object>> IndexedMessageCodec_types =
      FastField.builder()
          .parent(IndexedMessageCodec.class)
          .name("types")
          .build();

  //

  private static final FastField<Optional<BiConsumer<Object, PacketBuffer>>> MessageHandler_encoder =
      FastField.builder()
          .parent(IndexedMessageCodec_MessageHandler.getInstance())
          .name("encoder")
          .build();

  private static final FastField<Optional<Function<PacketBuffer, Object>>> MessageHandler_decoder =
      FastField.builder()
          .parent(IndexedMessageCodec_MessageHandler.getInstance())
          .name("decoder")
          .build();

  private static final FastField<Integer> MessageHandler_index =
      FastField.builder()
          .parent(IndexedMessageCodec_MessageHandler.getInstance())
          .name("index")
          .build();

  private static final FastField<BiConsumer<Object, Supplier<NetworkEvent.Context>>> MessageHandler_messageConsumer =
      FastField.builder()
          .parent(IndexedMessageCodec_MessageHandler.getInstance())
          .name("messageConsumer")
          .build();

  private static final FastField<Class<Object>> MessageHandler_messageType =
      FastField.builder()
          .parent(IndexedMessageCodec_MessageHandler.getInstance())
          .name("messageType")
          .build();

  private static final FastField<Optional<NetworkDirection>> MessageHandler_networkDirection =
      FastField.builder()
          .parent(IndexedMessageCodec_MessageHandler.getInstance())
          .name("networkDirection")
          .build();

  private static final FastField<Optional<BiConsumer<Object, Integer>>> MessageHandler_loginIndexSetter =
      FastField.builder()
          .parent(IndexedMessageCodec_MessageHandler.getInstance())
          .name("loginIndexSetter")
          .build();

  private static final FastField<Optional<Function<Object, Integer>>> MessageHandler_loginIndexGetter =
      FastField.builder()
          .parent(IndexedMessageCodec_MessageHandler.getInstance())
          .name("loginIndexGetter")
          .build();

  //

  private static final FastField<IndexedMessageCodec> SimpleChannel_indexedCodec =
      FastField.builder()
          .parent(SimpleChannel.class)
          .name("indexedCodec")
          .build();

  //

  private static final FastField<SimpleChannel> FMLNetworkConstants_handshakeChannel =
      FastField.builder()
          .parent(FMLNetworkConstants.class)
          .name("handshakeChannel")
          .build();
}
