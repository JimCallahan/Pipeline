package com.theorphanage.pipeline.plugin.ZohanCollection.v1_0_0;

/**
 * Low level path structural component.
 * <p>
 * To change the naming of the directory, simply override the {@link #toString()} method
 * of this enum to return a different name. 
 */
public 
enum SubDir
{
  output, published, QC, work, assembly, verification, takes;

  @Override
  public String toString() {
    switch (this) {
    case assembly:
      return "temerity/assembly";
    case verification:
      return "temerity/verification";
    default:
      return super.toString();
    }
  }
}