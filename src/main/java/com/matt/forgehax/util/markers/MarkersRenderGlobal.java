package com.matt.forgehax.util.markers;

import com.matt.forgehax.Globals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockRenderLayer;

import javax.annotation.Nullable;

/**
 * Created on 7/26/2017 by fr1kin
 */
public class MarkersRenderGlobal extends RenderGlobal implements Globals {
    private static final MarkersRenderGlobal INSTANCE = new MarkersRenderGlobal(MC);

    public static MarkersRenderGlobal getInstance() {
        return INSTANCE;
    }

    private ChunkRenderDispatcher renderDispatcher = null;

    public MarkersRenderGlobal(Minecraft mcIn) {
        super(mcIn);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        super.onResourceManagerReload(resourceManager);
    }

    @Override
    public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
        super.markBlockRangeForRenderUpdate(x1, y1, z1, x2, y2, z2);
    }

    @Override
    public void renderEntityOutlineFramebuffer() {
        super.renderEntityOutlineFramebuffer();
    }

    @Override
    protected boolean isRenderEntityOutlines() {
        return super.isRenderEntityOutlines();
    }

    @Override
    public void setWorldAndLoadRenderers(@Nullable WorldClient worldClientIn) {
        super.setWorldAndLoadRenderers(worldClientIn);
    }

    @Override
    public void loadRenderers() {
        super.loadRenderers();
    }

    @Override
    protected void stopChunkUpdates() {
        super.stopChunkUpdates();
    }

    @Override
    public void createBindEntityOutlineFbs(int width, int height) {
        super.createBindEntityOutlineFbs(width, height);
    }

    @Override
    public void renderEntities(Entity renderViewEntity, ICamera camera, float partialTicks) {
        super.renderEntities(renderViewEntity, camera, partialTicks);
    }

    @Override
    public String getDebugInfoRenders() {
        return super.getDebugInfoRenders();
    }

    @Override
    protected int getRenderedChunks() {
        return super.getRenderedChunks();
    }

    @Override
    public String getDebugInfoEntities() {
        return super.getDebugInfoEntities();
    }

    @Override
    public void setupTerrain(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator) {
        super.setupTerrain(viewEntity, partialTicks, camera, frameCount, playerSpectator);
    }

    @Override
    public int renderBlockLayer(BlockRenderLayer blockLayerIn, double partialTicks, int pass, Entity entityIn) {
        return super.renderBlockLayer(blockLayerIn, partialTicks, pass, entityIn);
    }

    @Override
    public void updateClouds() {
        super.updateClouds();
    }

    @Override
    public void renderSky(float partialTicks, int pass) {
        super.renderSky(partialTicks, pass);
    }

    @Override
    public void renderClouds(float partialTicks, int pass, double p_180447_3_, double p_180447_5_, double p_180447_7_) {
        super.renderClouds(partialTicks, pass, p_180447_3_, p_180447_5_, p_180447_7_);
    }

    @Override
    public boolean hasCloudFog(double x, double y, double z, float partialTicks) {
        return super.hasCloudFog(x, y, z, partialTicks);
    }
}
