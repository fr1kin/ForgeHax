package com.matt.forgehax.asm.events.replacementhooks;

import com.matt.forgehax.util.event.Cancelable;
import com.matt.forgehax.util.event.Event;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

public class PlaySoundAtEntityEvent extends Event implements Cancelable
{
  private SoundEvent name;
  private SoundCategory category;
  private final float volume;
  private final float pitch;

  // avoid getting cucked by verifier
  public PlaySoundAtEntityEvent(EntityPlayerSP entity, SoundEvent name, SoundCategory category, float volume, float pitch)
  {
    this((Entity)entity, name, category, volume, pitch);
  }


  public PlaySoundAtEntityEvent(Entity entity, SoundEvent name, SoundCategory category, float volume, float pitch)
  {
    this.name = name;
    this.category = category;
    this.volume = volume;
    this.pitch = pitch;
  }

  public SoundEvent getSound() { return this.name; }
  public SoundCategory getCategory() { return this.category; }
  //public float getDefaultVolume() { return this.volume; }
  //public float getDefaultPitch() { return this.pitch; }
  public float getVolume() { return this.volume; }
  public float getPitch() { return this.pitch; }
  //public void setSound(SoundEvent value) { this.name = value; }
  //public void setCategory(SoundCategory category) { this.category = category; }
  //public void setVolume(float value) { this.newVolume = value; }
  //public void setPitch(float value) { this.newPitch = value; }
}