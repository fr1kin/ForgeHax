package com.matt.forgehax.mods;

import com.github.lunatrius.core.client.renderer.unique.GeometryMasks;
import com.github.lunatrius.core.client.renderer.unique.GeometryTessellator;
import com.google.common.collect.Sets;
import com.matt.forgehax.Helper;
import com.matt.forgehax.asm.ForgeHaxHooks;
import com.matt.forgehax.asm.events.*;
import com.matt.forgehax.asm.events.listeners.BlockModelRenderListener;
import com.matt.forgehax.asm.events.listeners.Listeners;
import com.matt.forgehax.util.Utils;
import com.matt.forgehax.util.blocks.BlockEntry;
import com.matt.forgehax.util.blocks.properties.BoundProperty;
import com.matt.forgehax.util.blocks.properties.ColorProperty;
import com.matt.forgehax.util.command.ExecuteData;
import com.matt.forgehax.util.command.Options;
import com.matt.forgehax.util.command.Setting;
import com.matt.forgehax.util.command.options.BlockEntryProcessor;
import com.matt.forgehax.util.command.options.OptionBuilders;
import com.matt.forgehax.util.command.options.OptionProcessors;
import com.matt.forgehax.util.entity.EntityUtils;
import com.matt.forgehax.util.markers.RenderUploader;
import com.matt.forgehax.util.markers.TessellatorCache;
import com.matt.forgehax.util.markers.Uploaders;
import com.matt.forgehax.util.mod.Category;
import com.matt.forgehax.util.mod.ToggleMod;
import com.matt.forgehax.util.mod.loader.RegisterMod;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.Collection;

import static com.matt.forgehax.Helper.reloadChunks;
import static com.matt.forgehax.Helper.reloadChunksHard;

/**
 * Created on 5/5/2017 by fr1kin
 */
@RegisterMod
public class Markers extends ToggleMod implements BlockModelRenderListener {
    // TODO: Bug when a render chunk is empty according to isEmptyLayer but actually contains tile entities with an invisible render layer type. This will cause them not to be rendered provided they are the only blocks within that region.

    // TODO:
    // there are two bugs currently
    // 1) isChunkEmpty stops chunk from being processed (easy fix - just add or in the statement, but this may slow down chunk loading)
    // 2) sometimes while one chunk is still being processed another thread will start processing it (this is why the exception
    //      RuntimeException("attempted to take a tessellator despite not needing one") is thrown). i still havent figured out a way to
    //      safely terminate the other process, but this will cause chunks to be skipped and old buffer data to be rendered.
    //      One solution is how schematica does it (essentially copies the render code), this would be faster but would be alot of work.
    //      edit: should be fixed now

    private static final int VERTEX_BUFFER_COUNT = 100;
    private static final int VERTEX_BUFFER_SIZE = 0x200;

    @Nullable
    private Uploaders<GeometryTessellator> uploaders;

    private Vec3d renderingOffset = new Vec3d(0, 0, 0);

    public final Options<BlockEntry> options = getCommandStub().builders().<BlockEntry>newOptionsBuilder()
            .name("options")
            .description("Marker block options")
            .supplier(Sets::newConcurrentHashSet)
            .factory(BlockEntry::new)
            .build();

    public final Setting<Boolean> clear_buffer = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("clear_buffer")
            .description("Clear the buffer instead of disabling depth")
            .defaultTo(false)
            .build();

    public final Setting<Boolean> anti_aliasing = getCommandStub().builders().<Boolean>newSettingBuilder()
            .name("antialiasing")
            .description("Enables antialiasing on lines")
            .defaultTo(false)
            .build();

    public final Setting<Integer> anti_aliasing_max = getCommandStub().builders().<Integer>newSettingBuilder()
            .name("antialiasing_max")
            .description("Maximum number of render elements allowed in a render chunk until antialiasing is disabled")
            .defaultTo(0)
            .build();

    public Markers() {
        super(Category.RENDER, "Markers", false, "Renders a box around a block");
    }

    @Override
    public void onLoad() {
        options.builders().newCommandBuilder()
                .name("add")
                .description("Adds block to block esp")
                .options(OptionBuilders::rgba)
                .processor(OptionProcessors::rgba)
                .options(OptionBuilders::meta)
                .processor(OptionProcessors::meta)
                .options(OptionBuilders::id)
                .options(OptionBuilders::regex)
                .options(OptionBuilders::bounds)
                .processor(BlockEntryProcessor::buildCollection)
                .processor(BlockEntryProcessor::processBounds)
                .processor(BlockEntryProcessor::processColor)
                .processor(data -> {
                    data.requiredArguments(1);
                    data.requiresEntry("entries");

                    Collection<BlockEntry> entries = data.get("entries");

                    final boolean isColorPresent = data.get("isColorPresent", false);

                    final int colorBuffer = data.get("colorBuffer", Utils.Colors.WHITE);

                    entries.forEach(entry -> {
                        // check if there is an existing entry already in the list
                        // if so then append options to it
                        final BlockEntry existing = options.get(entry);
                        if(existing != null) {
                            // set color if a color was specified by the client
                            if(isColorPresent) existing.getWritableProperty(ColorProperty.class).set(colorBuffer);
                            // copy bounds from entry into the existing one
                            entry.getReadableProperty(BoundProperty.class)
                                    .getAll()
                                    .forEach(bound -> existing.getWritableProperty(BoundProperty.class).add(bound.getMin(), bound.getMax()));
                            data.markSuccess();
                        } else if(options.add(entry)) {
                            Helper.printMessage(String.format("Added block \"%s\"", entry.getPrettyName()));
                            data.markSuccess();
                        } else {
                            Helper.printMessage(String.format("Failed to add block \"%s\"", entry.getPrettyName()));
                            data.markFailed(ExecuteData.State.SUCCESS);
                        }
                    });
                })
                .success(cmd -> reloadChunks())
                .build();
        options.builders().newCommandBuilder()
                .name("remove")
                .description("Removes block to block esp")
                .options(OptionBuilders::meta)
                .processor(OptionProcessors::meta)
                .options(OptionBuilders::id)
                .options(OptionBuilders::regex)
                .options(OptionBuilders::bounds)
                .processor(BlockEntryProcessor::buildCollection)
                .processor(BlockEntryProcessor::processBounds)
                .processor(data -> {
                    data.requiredArguments(1);
                    data.requiresEntry("entries");

                    Collection<BlockEntry> entries = data.get("entries");

                    final boolean isBoundPresent = data.has("bounds");

                    entries.forEach(entry -> {
                        final BlockEntry existing = options.get(entry);
                        if(existing != null) {
                            if(isBoundPresent) {
                                // copy bounds from entry into the existing one
                                entry.getReadableProperty(BoundProperty.class)
                                        .getAll()
                                        .forEach(bound -> existing.getWritableProperty(BoundProperty.class).remove(bound.getMin(), bound.getMax()));
                                data.markSuccess();
                            } else if(options.remove(existing)) {
                                Helper.printMessage(String.format("Removed block \"%s\"", entry.getPrettyName()));
                                data.markSuccess();
                            }
                        } else if(entries.size() <= 1) {
                            Helper.printMessage(String.format("Failed to remove block \"%s\"", entry.getPrettyName()));
                            data.markFailed(ExecuteData.State.SUCCESS);
                        }
                    });
                })
                .success(cmd -> reloadChunks())
                .build();
    }

    /**
     * Initialize the VBO uploaders
     */
    private void vboStartup() {
        if(uploaders != null) vboShutdown(); // unload previous if it exists

        try {
            // create new instances
            uploaders = new Uploaders<>(RenderUploader::new, new TessellatorCache<>(VERTEX_BUFFER_COUNT, GeometryTessellator::new));
            uploaders.onShutdown(uploader -> MC.addScheduledTask(() -> {
                uploader.nullifyCurrentThread(); // this will stop anything currently running

                // return the tessellator to cache
                try {
                    uploader.freeTessellator();
                } catch (Throwable t) {
                    // ignore result
                }

                // handle VBO
                try {
                    // attempt to unload the VBO
                    uploader.unload();
                } catch (Throwable t) {
                    // ignore result
                }
            }));
        } catch (Throwable t) {
            // ignore result
        }
    }

    /**
     * Shutdown the uploaders
     */
    private void vboShutdown() {
        try {
            uploaders.unregisterAll();
            uploaders = null;
        } catch (Throwable t) {
            // ignore result
        }
    }

    @Override
    public void onUnload() {
        options.forEach(BlockEntry::cleanupProperties);
        options.serialize();
    }

    @Override
    public void onEnabled() {
        Listeners.BLOCK_MODEL_RENDER_LISTENER.register(this);
        ForgeHaxHooks.SHOULD_DISABLE_CAVE_CULLING.enable(); // need cave culling disabled to parse every block
        reloadChunksHard();
    }

    @Override
    public void onDisabled() {
        vboShutdown();
        Listeners.BLOCK_MODEL_RENDER_LISTENER.unregister(this);
        ForgeHaxHooks.SHOULD_DISABLE_CAVE_CULLING.disable();
    }

    /*
    @Override
    public String getDisplayText() {
        int cacheSize = uploaders != null ? uploaders.cache().size() : 0;
        int cacheCapacity = uploaders != null ? uploaders.cache().capacity() : 0;
        return super.getDisplayText() + String.format("[C:%d/%d]", cacheSize, cacheCapacity);
    }
    */

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Load event) {
        try {
            // shutdown vbos
            vboShutdown();
        } catch (Throwable e) {
            handleException(e);
        }
    }

    @SubscribeEvent
    public void onLoadRenderers(LoadRenderersEvent event) {
        try {
            // create new instances of everything
            vboStartup();
            // allocate all space needed
            for (RenderChunk renderChunk : event.getViewFrustum().renderChunks) uploaders.register(renderChunk);
        } catch (Throwable t) {
            handleException(t);
        }
    }

    @SubscribeEvent
    public void onWorldRendererDeallocated(WorldRendererDeallocatedEvent event) {
        if(uploaders != null) try {
            uploaders.get(event.getRenderChunk()).ifPresent(uploader -> {
                uploader.lock().lock();
                try {
                    uploader.freeTessellator();
                } catch (Throwable t) {
                    handleException(event.getRenderChunk(), t);
                } finally {
                    uploader.lock().unlock();
                }
            });
        } catch (Throwable t) {
            // ignore
        }
    }

    @SubscribeEvent
    public void onPreBuildChunk(BuildChunkEvent.Pre event) {
        if(uploaders != null) try {
            uploaders.get(event.getRenderChunk()).ifPresent(uploader -> {
                uploader.lock().lock();
                try {
                    uploader.setCurrentThread(); // set this to the current thread, stopping other threads processing this same chunk from continuing

                    // check if a tessellator already exists, if so then this chunk is being processed on another thread and we should stop it
                    if(uploader.getTessellator() != null) uploader.freeTessellator();
                    // now take a new tessellator
                    uploader.takeTessellator();

                    // begin drawing
                    uploader.getTessellator().beginLines();
                    // reset render count
                    uploader.resetRenderCount();
                    // translate buffer
                    BlockPos renderPos = event.getRenderChunk().getPosition();
                    uploader.getTessellator().setTranslation(-renderPos.getX(), -renderPos.getY(), -renderPos.getZ());
                } catch (Throwable t) {
                    handleException(event.getRenderChunk(), t);
                } finally {
                    uploader.lock().unlock();
                }
            });
        } catch (Throwable t) {
            // ignore
        }
    }

    @SubscribeEvent
    public void onPostBuildChunk(BuildChunkEvent.Post event) {
        if(uploaders != null) try {
            uploaders.get(event.getRenderChunk()).ifPresent(uploader -> {
                uploader.lock().lock();
                try {
                    // ensure we are in the right thread
                    uploader.validateCurrentThread();
                    // finish drawing
                    if(uploader.isTessellatorDrawing()) uploader.getBufferBuilder().finishDrawing();
                } catch (Throwable t) {
                    handleException(event.getRenderChunk(), t);
                } finally {
                    uploader.lock().unlock();
                }
            });
        } catch (Throwable t) {
            // ignore
        }
    }

    @Override
    public void onBlockRenderInLoop(final RenderChunk renderChunk, final Block block, final IBlockState state, final BlockPos pos) {
        if(uploaders != null) try {
            uploaders.get(renderChunk).ifPresent(uploader -> {
                uploader.lock().lock();
                try {
                    uploader.validateCurrentThread();
                    if(uploader.isTessellatorDrawing()) {
                        BlockEntry blockEntry = options.get(state);
                        if(blockEntry != null
                                && blockEntry.getReadableProperty(BoundProperty.class).isWithinBoundaries(pos.getY())) {
                            AxisAlignedBB bb = state.getSelectedBoundingBox(Helper.getWorld(), pos);
                            GeometryTessellator.drawLines(
                                    uploader.getBufferBuilder(),
                                    bb.minX, bb.minY, bb.minZ,
                                    bb.maxX, bb.maxY, bb.maxZ,
                                    GeometryMasks.Line.ALL,
                                    blockEntry.getReadableProperty(ColorProperty.class).getAsBuffer()
                            );
                            uploader.incrementRenderCount();
                        }
                    }
                } catch (Throwable t) {
                    handleException(renderChunk, t);
                } finally {
                    uploader.lock().unlock();
                }
            });
        } catch (Throwable t) {
            // ignore
        }
    }

    @SubscribeEvent
    public void onChunkUploaded(ChunkUploadedEvent event) {
        if(uploaders != null) try {
            uploaders.get(event.getRenderChunk()).ifPresent(uploader -> {
                try {
                    uploader.upload();
                } catch (Throwable t) {
                    handleException(event.getRenderChunk(), t);
                }
            });
        } catch (Throwable t) {
            // ignore
        }
    }

    @SubscribeEvent
    public void onChunkDeleted(DeleteGlResourcesEvent event) {
        if(uploaders != null) try {
            uploaders.unregister(event.getRenderChunk());
        } catch (Throwable t) {
            // ignore
        }
    }

    @SubscribeEvent
    public void onSetupTerrain(SetupTerrainEvent event) {
        // doesn't have to be here, I just conveniently had this hook
        renderingOffset = EntityUtils.getInterpolatedPos(event.getRenderEntity(), MC.getRenderPartialTicks());
    }

    @SubscribeEvent
    public void onRenderChunkAdded(AddRenderChunkEvent event) {
        if(uploaders != null) try {
            uploaders.get(event.getRenderChunk()).ifPresent(uploader -> uploader.setRendering(true));
        } catch (Throwable t) {
            // ignore
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRenderWorld(RenderWorldLastEvent event) {
        if(uploaders != null) try {
            GlStateManager.pushMatrix();
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.disableAlpha();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            if(!clear_buffer.get())
                GlStateManager.disableDepth();
            else {
                GlStateManager.clearDepth(1.f);
                GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
            }

            final boolean aa_enabled = anti_aliasing.get();
            final int aa_max = anti_aliasing_max.get();

            GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            GlStateManager.glEnableClientState(GL11.GL_COLOR_ARRAY);

            uploaders.forEach((k, v) -> {
                if (v.isRendering()) {
                    if(aa_enabled && (aa_max == 0 || v.getRenderCount() <= aa_max))
                        GL11.glEnable(GL11.GL_LINE_SMOOTH);

                    GlStateManager.pushMatrix();

                    BlockPos pos = k.getPosition();
                    GlStateManager.translate(
                            (double) pos.getX() - renderingOffset.x,
                            (double) pos.getY() - renderingOffset.y,
                            (double) pos.getZ() - renderingOffset.z
                    );

                    k.multModelviewMatrix();

                    v.getVertexBuffer().bindBuffer();

                    GlStateManager.glVertexPointer(
                            DefaultVertexFormats.POSITION_3F.getElementCount(),
                            DefaultVertexFormats.POSITION_3F.getType().getGlConstant(),
                            DefaultVertexFormats.POSITION_3F.getSize() + DefaultVertexFormats.COLOR_4UB.getSize(),
                            0
                    );
                    GlStateManager.glColorPointer(
                            DefaultVertexFormats.COLOR_4UB.getElementCount(),
                            DefaultVertexFormats.COLOR_4UB.getType().getGlConstant(),
                            DefaultVertexFormats.POSITION_3F.getSize() + DefaultVertexFormats.COLOR_4UB.getSize(),
                            DefaultVertexFormats.POSITION_3F.getSize()
                    );

                    v.getVertexBuffer().drawArrays(GL11.GL_LINES);

                    GlStateManager.popMatrix();

                    GL11.glDisable(GL11.GL_LINE_SMOOTH);

                    v.setRendering(false);
                }
            });

            GL11.glDisable(GL11.GL_LINE_SMOOTH);

            GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
            GlStateManager.glDisableClientState(GL11.GL_COLOR_ARRAY);

            OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);

            GlStateManager.shadeModel(GL11.GL_FLAT);
            GlStateManager.disableBlend();
            GlStateManager.enableAlpha();
            GlStateManager.enableTexture2D();
            GlStateManager.enableDepth();
            GlStateManager.enableCull();
            GlStateManager.popMatrix();
        } catch (Throwable t) {
            // ignore
        }
    }

    private static void handleException(RenderChunk renderChunk, Throwable t) {
        //throwable.printStackTrace();
        Helper.getLog().error(t.toString());
    }
    private static void handleException(Throwable t) {
        handleException(null, t);
    }
}
