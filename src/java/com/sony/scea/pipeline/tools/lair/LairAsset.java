package com.sony.scea.pipeline.tools.lair;

import us.temerity.pipeline.FileSeq;

import com.sony.scea.pipeline.tools.lair.LairConstants.AssetType;

public class LairAsset implements Comparable<LairAsset>
{
   @SuppressWarnings("hiding")
   public LairAsset(String assetName, AssetType assetType)
   {
      this.assetName = assetName;
      this.assetType = assetType;
      String assetStart = "/projects/lr/assets/" + assetType.toString() + "/" + assetName;

      modScene = assetStart + "/model/" + assetName + "_mod";
      rigScene = assetStart + "/rig/" + assetName + "_rig";
      matScene = assetStart + "/material/" + assetName + "_mat";
      finalScene = assetStart + "/" + assetName;

      lr_modScene = assetStart + "/model/" + assetName + "_mod_lr";
      lr_rigScene = assetStart + "/rig/" + assetName + "_rig_lr";
      lr_matScene = assetStart + "/material/" + assetName + "_mat_lr";
      lr_finalScene = assetStart + "/" + assetName + "_lr";

      shdScene = assetStart + "/shader/" + assetName + "_shd";
      shdExport = assetStart + "/shader/" + assetName + "_shdexp";
      shdIncGroup = assetStart + "/shader/shaders/" + assetName + "_shdinc";
      shdIncGroupSecSeq = new FileSeq("mi_shader_defs", "mi");

      texGroup = assetStart + "/textures/" + assetName + "_tex";
   }

   public final String assetName;
   public final AssetType assetType;

   // Hi Rez Models
   public final String modScene;
   public final String rigScene;
   public final String matScene;
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

   public int compareTo(LairAsset that)
   {
      return ( this.assetName.compareTo(that.assetName) );
   }

   @Override
   public boolean equals(Object obj)
   {
      if ( obj == null )
	 return false;
      if ( !( obj instanceof LairAsset ) )
      {
	 return false;
      }
      LairAsset that = (LairAsset) obj;
      if ( this.assetName.equals(that.assetName) && this.assetType.equals(that.assetType) )
	 return true;
      return false;

   }
}
