package com.matt.forgehax.mods;

import com.github.lunatrius.core.client.renderer.GeometryMasks;
import com.github.lunatrius.core.client.renderer.GeometryTessellator;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.matt.forgehax.asm.events.PacketEvent;
import com.matt.forgehax.events.LocalPlayerUpdateEvent;
import com.matt.forgehax.events.RenderEvent;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.draw.SurfaceUtils;
import com.matt.forgehax.util.math.VectorUtils;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Objects;
import java.util.UUID;


@RegisterMod
public class LogoutSpot extends ToggleMod {
    public LogoutSpot() { super("LogoutSpot", false, "show where a player logs out"); }


    public final Setting<Boolean> renderPosition = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("RenderPosition").description("Create a cloned player entity where the player logged out")
            .defaultTo(true).build();
    public final Setting<Integer> maxDistance = getCommandStub().builders().<Integer>newSettingBuilder()
            .name("MaxDistance").description("distance from cloned entity before deleting it")
            .defaultTo(50).build();
    public final Setting<Boolean> outputToChat = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("OutputToChat").description("print coords to chat")
            .defaultTo(true).build();


    private List<logoutPos> logoutPositions = Lists.newArrayList();

    @SubscribeEvent
    public void onPacketRecieved(PacketEvent.Incoming.Pre event) {
        if(event.getPacket() instanceof SPacketPlayerListItem) {
            SPacketPlayerListItem playerListPacket = (SPacketPlayerListItem)event.getPacket();
            if (playerListPacket.getAction().equals(SPacketPlayerListItem.Action.REMOVE_PLAYER) || playerListPacket.getAction().equals(SPacketPlayerListItem.Action.ADD_PLAYER)) {
                try {
                    playerListPacket.getEntries()
                            .stream()
                            .filter(Objects::nonNull)
                            .filter(data -> {
                                String name = getNameFromComponent(data.getProfile());
                                return !Strings.isNullOrEmpty(name) && !isLocalPlayer(name) || playerListPacket.getAction().equals(SPacketPlayerListItem.Action.REMOVE_PLAYER);
                            })
                            .forEach(data -> {
                                String name = getNameFromComponent(data.getProfile());
                                UUID id = data.getProfile().getId();
                                switch(playerListPacket.getAction()) {
                                    case ADD_PLAYER: // if they come back then remove it
                                        for (logoutPos pos : logoutPositions) {
                                            if (pos.id.equals(id))
                                                logoutPositions.remove(pos);
                                        }
                                        break;

                                    case REMOVE_PLAYER: // if they leave and they are in the world save it
                                        if (MC.world.getPlayerEntityByUUID(id) != null) {
                                            EntityPlayer player = MC.world.getPlayerEntityByUUID(id);
                                            AxisAlignedBB BB = player.getEntityBoundingBox();
                                            Vec3d[] pos = {new Vec3d(BB.minX, BB.minY, BB.minZ), new Vec3d(BB.maxX, BB.maxY, BB.maxZ)};
                                            logoutPositions.add(new logoutPos(pos, id, player.getName()));
                                        }
                                        break;
                                }

                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderGameOverlayEvent(RenderGameOverlayEvent.Text event) {
        if (renderPosition.getAsBoolean())
        if (event.getType().equals(RenderGameOverlayEvent.ElementType.TEXT)) {
            for (logoutPos pos : Lists.newArrayList(logoutPositions)) {
                Vec3d topVec = new Vec3d((pos.pos[0].x+pos.pos[1].x)/2, pos.pos[1].y, (pos.pos[0].z+pos.pos[1].z)/2); // position where to place the text in the world
                VectorUtils.ScreenPos textPos = VectorUtils.toScreen(topVec.x, topVec.y, topVec.z); // where to place the text on the screen
                double distance = MC.player.getDistance((pos.pos[0].x+pos.pos[1].x)/2, pos.pos[0].y, (pos.pos[0].z+pos.pos[1].z)/2); // distance from player to logout spot
                if (textPos.isVisible) {
                    String name = pos.name + String.format((" (%.1f)"), distance);
                    SurfaceUtils.drawTextShadow(name, textPos.x - (SurfaceUtils.getTextWidth(name) / 2), textPos.y - (SurfaceUtils.getTextHeight() + 1), Utils.toRGBA(255,0,0,0));
                }
            }
        }
    }

    @SubscribeEvent
    public void onRender(RenderEvent event) { // render box
        if (renderPosition.getAsBoolean()) {
            event.getBuffer().begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

            for (logoutPos position : logoutPositions) {
                GeometryTessellator.drawQuads(event.getBuffer(), // horizontal lines
                        position.pos[0].x,
                        position.pos[0].y,
                        position.pos[0].z,
                        position.pos[1].x,
                        position.pos[1].y,
                        position.pos[1].z,
                        GeometryMasks.Quad.ALL, Utils.Colors.RED);

                GeometryTessellator.drawLines(event.getBuffer(), // vertical lines
                        position.pos[0].x,
                        position.pos[0].y,
                        position.pos[0].z,
                        position.pos[1].x,
                        position.pos[1].y,
                        position.pos[1].z,
                        GeometryMasks.Quad.ALL, Utils.Colors.RED);
            }

            event.getTessellator().draw();
        }
    }



    @SubscribeEvent
    public void onPlayerUpdate(LocalPlayerUpdateEvent event) { // delete cloned player if they're too far
        for (logoutPos pos : logoutPositions) {
            double distance = MC.player.getDistance((pos.pos[0].x+pos.pos[1].x)/2, pos.pos[0].y, (pos.pos[0].z+pos.pos[1].z)/2); // distance from player to entity
            if (distance >= maxDistance.getAsDouble() && distance > 0)
                logoutPositions.remove(pos);
            break; // prevent crash maybe
        }
    }

    /*
    @SubscribeEvent
    public void onWorldUnload (WorldEvent.Unload event) {
        logoutPositions.clear();
    }
    @SubscribeEvent
    public void onWorldLoad (WorldEvent.Load event) {
        logoutPositions.clear();
    }
    */

    private class logoutPos { // keeps track of positions and uuid   should just use Entities
        Vec3d[] pos;
        UUID id;
        String name;
        public logoutPos(Vec3d[] position, UUID uuid, String name) {
            this.pos = position;
            this.id = uuid;
            this.name = name;
        }
    }


    private float getLateralDistanceFromPlayer(Entity entityIn) {
        float f = (float)(entityIn.posX - MC.player.posX);
        float f2 = (float)(entityIn.posZ - MC.player.posZ);
        return MathHelper.sqrt(f*f + f2*f2);
    }
    private String getNameFromComponent(GameProfile profile) {
        return Objects.nonNull(profile) ? profile.getName() : "";
    }
    private boolean isLocalPlayer(String username) {
        return Objects.nonNull(MC.player) && MC.player.getDisplayName().getUnformattedText().equals(username);
    }



    @Override
    public void onLoad() {
        getCommandStub().builders().newCommandBuilder()
                .name("clear")
                .description("Clear cloned players")
                .processor(data -> {
                    data.requiredArguments(0);
                    logoutPositions.clear();
                })
                .build();
    }

}