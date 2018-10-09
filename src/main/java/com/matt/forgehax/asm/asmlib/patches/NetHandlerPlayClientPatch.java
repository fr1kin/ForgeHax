package com.matt.forgehax.asm.asmlib.patches;

import com.matt.forgehax.ForgeHax;
import com.matt.forgehax.events.ServerConnectionEvent;
import net.futureclient.asm.transformer.AsmMethod;
import net.futureclient.asm.transformer.annotation.Inject;
import net.futureclient.asm.transformer.annotation.Transformer;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.util.text.ITextComponent;

@Transformer(NetHandlerPlayClient.class)
public class NetHandlerPlayClientPatch {

  @Inject(name = "onDisconnect", args = {ITextComponent.class})
  public void disconnectHook(AsmMethod method) {
    method.run(() -> ForgeHax.EVENT_BUS.post(new ServerConnectionEvent.Disconnect()));
  }

}
