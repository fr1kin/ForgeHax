package com.matt.forgehax.util.markers;

import com.matt.forgehax.Globals;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

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
  public void setupTerrain(
      Entity viewEntity,
      double partialTicks,
      ICamera camera,
      int frameCount,
      boolean playerSpectator) {
    super.setupTerrain(viewEntity, partialTicks, camera, frameCount, playerSpectator);
  }
  
  @Override
  public int renderBlockLayer(
      BlockRenderLayer blockLayerIn, double partialTicks, int pass, Entity entityIn) {
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
  public void renderClouds(
      float partialTicks, int pass, double p_180447_3_, double p_180447_5_, double p_180447_7_) {
    super.renderClouds(partialTicks, pass, p_180447_3_, p_180447_5_, p_180447_7_);
  }
  
  @Override
  public boolean hasCloudFog(double x, double y, double z, float partialTicks) {
    return super.hasCloudFog(x, y, z, partialTicks);
  }
  
  @Override
  public void updateChunks(long finishTimeNano) {
    super.updateChunks(finishTimeNano);
  }
  
  @Override
  public void renderWorldBorder(Entity entityIn, float partialTicks) {
    super.renderWorldBorder(entityIn, partialTicks);
  }
  
  @Override
  public void drawBlockDamageTexture(
      Tessellator tessellatorIn,
      BufferBuilder worldRendererIn,
      Entity entityIn,
      float partialTicks) {
    super.drawBlockDamageTexture(tessellatorIn, worldRendererIn, entityIn, partialTicks);
  }
  
  @Override
  public void drawSelectionBox(
      EntityPlayer player, RayTraceResult movingObjectPositionIn, int execute, float partialTicks) {
    super.drawSelectionBox(player, movingObjectPositionIn, execute, partialTicks);
  }
  
  @Override
  public void notifyBlockUpdate(
      World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
    super.notifyBlockUpdate(worldIn, pos, oldState, newState, flags);
  }
  
  @Override
  public void notifyLightSet(BlockPos pos) {
    super.notifyLightSet(pos);
  }
  
  public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
  }
  
  @Override
  public void playRecord(@Nullable SoundEvent soundIn, BlockPos pos) {
    super.playRecord(soundIn, pos);
  }
  
  @Override
  public void playSoundToAllNearExcept(
      @Nullable EntityPlayer player,
      SoundEvent soundIn,
      SoundCategory category,
      double x,
      double y,
      double z,
      float volume,
      float pitch) {
    super.playSoundToAllNearExcept(player, soundIn, category, x, y, z, volume, pitch);
  }
  
  @Override
  public void spawnParticle(
      int id,
      boolean ignoreRange,
      boolean p_190570_3_,
      double x,
      double y,
      double z,
      double xSpeed,
      double ySpeed,
      double zSpeed,
      int... parameters) {
    super.spawnParticle(id, ignoreRange, p_190570_3_, x, y, z, xSpeed, ySpeed, zSpeed, parameters);
  }
  
  @Override
  public void onEntityAdded(Entity entityIn) {
    super.onEntityAdded(entityIn);
  }
  
  @Override
  public void onEntityRemoved(Entity entityIn) {
    super.onEntityRemoved(entityIn);
  }
  
  @Override
  public void deleteAllDisplayLists() {
    super.deleteAllDisplayLists();
  }
  
  @Override
  public void broadcastSound(int soundID, BlockPos pos, int data) {
    super.broadcastSound(soundID, pos, data);
  }
  
  @Override
  public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {
    super.playEvent(player, type, blockPosIn, data);
  }
  
  @Override
  public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
    super.sendBlockBreakProgress(breakerId, pos, progress);
  }
  
  @Override
  public boolean hasNoChunkUpdates() {
    return super.hasNoChunkUpdates();
  }
  
  @Override
  public void setDisplayListEntitiesDirty() {
    super.setDisplayListEntitiesDirty();
  }
  
  @Override
  public void updateTileEntities(
      Collection<TileEntity> tileEntitiesToRemove, Collection<TileEntity> tileEntitiesToAdd) {
    super.updateTileEntities(tileEntitiesToRemove, tileEntitiesToAdd);
  }
}
