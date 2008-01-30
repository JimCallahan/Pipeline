package com.sony.scea.pipeline.tools;

import us.temerity.pipeline.FileSeq;
import us.temerity.pipeline.PipelineException;

/**
 * The full naming convention for an asset, generated from a few tokens.
 * <p>
 * This class should represent most, if not all, of the name of the nodes
 * associated with a single asset.  All the names a public, final values
 * which are set through the constructor and cannot be changed after that,
 * @author jesse
 *
 */
public class SonyAsset implements Comparable<SonyAsset>
{
  /**
   * Constructs all the names for an asset
   * @param proj The project the asset is in
   * @param name The name of the asset
   * @param type THe asset type
   */
  @SuppressWarnings("hiding")
  public SonyAsset(String proj, String name, AssetType type)
  {
    this.project = proj;
    this.assetName = name;
    this.assetType = type;
    String assetStart = "/projects/" + proj + "/assets/" + type.toString() + "/"
    + name;

    modScene = assetStart + "/model/" + name + "_mod";
    blendShapeScene = assetStart + "/model/" + name + "_mod_head_BS";
    headModScene = assetStart + "/model/" + name + "_mod_head";
    rigScene = assetStart + "/rig/" + name + "_rig";
    syflexScene = assetStart + "/rig/" + name + "_syf";
    matScene = assetStart + "/material/" + name + "_mat";
    matExpScene = assetStart + "/material/" + name + "_matExp";
    finalScene = assetStart + "/" + name;

    lr_modScene = assetStart + "/model/" + name + "_mod_lr";
    lr_rigScene = assetStart + "/rig/" + name + "_rig_lr";
    lr_matScene = assetStart + "/material/" + name + "_mat_lr";
    lr_finalScene = assetStart + "/" + name + "_lr";

    shdScene = assetStart + "/shader/" + name + "_shd";
    shdExport = assetStart + "/shader/" + name + "_shdexp";
    shdIncGroup = assetStart + "/shader/shaders/" + name + "_shdinc";
    shdIncGroupSecSeq = new FileSeq("mi_shader_defs", "mi");

    texGroup = assetStart + "/textures/" + name + "_tex";
  }

  public final String project;
  public final String assetName;
  public final AssetType assetType;

  // Hi Rez Models
  public final String modScene;
  public final String headModScene;
  public final String blendShapeScene;
  public final String rigScene;
  public final String syflexScene;
  public final String matScene;
  public final String matExpScene;
  public final String finalScene;

  // Low Rez Models
  public final String lr_modScene;
  public final String lr_rigScene;
  public final String lr_matScene;
  public final String lr_finalScene;

  // Shader Nodes
  public final String shdScene;
  public final String shdIncGroup;
  public final FileSeq shdIncGroupSecSeq;
  public final String shdExport;

  // Texture Nodes
  public final String texGroup;

  public int compareTo(SonyAsset that)
  {
    return ( this.assetName.compareTo(that.assetName) );
  }

  @Override
  public boolean equals(Object obj)
  {
    if ( obj == null )
      return false;
    if ( !( obj instanceof SonyAsset ) )
    {
      return false;
    }
    SonyAsset that = (SonyAsset) obj;
    if ( this.assetName.equals(that.assetName) && this.assetType.equals(that.assetType) )
      return true;
    return false;
  }

  /**
   * represents the three different types of assets.
   * 
   * @author Jesse Clemens
   */
  public static enum AssetType
  {
    CHARACTER, PROP, SET;

    @Override
    public String toString()
    {
      String toReturn = null;
      switch (this)
      {
      case CHARACTER:
	toReturn = "character";
	break;
      case PROP:
	toReturn = "prop";
	break;
      case SET:
	toReturn = "set";
	break;
      }
      return toReturn;
    }

    public static String[] stringValues()
    {
      AssetType v[] = values();
      String toReturn[] = new String[v.length];
      for (int i = 0; i < v.length; i++)
	toReturn[i] = v[i].toString();
      return toReturn;
    }

    /**
     * A method for converting a String into an AssetType. Throws a
     * {@link PipelineException PipelineException} when an invalid string is
     * passed in.
     * 
     * <P>
     * 
     * @param s
     *           The String to convert to an AssetType
     * @return The AssetType that corresponds to <code>s</code>.
     * @throws PipelineException
     * 
     */
    public static AssetType fromString(String s) throws PipelineException
    {
      AssetType toReturn = null;
      s = s.toLowerCase();
      if ( s.equals("character") )
	toReturn = AssetType.CHARACTER;
      else if ( s.equals("prop") )
	toReturn = AssetType.PROP;
      else if ( s.equals("set") )
	toReturn = AssetType.SET;
      else
	throw new PipelineException("The given type (" + s + ") does "
	  + "not match any of the known asset types");
      return toReturn;
    }
  }
}
