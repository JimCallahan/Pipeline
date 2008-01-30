package com.sony.scea.pipeline.tools.lair;

import java.util.*;

import us.temerity.pipeline.*;

import com.sony.scea.pipeline.tools.Globals;
import com.sony.scea.pipeline.tools.Wrapper;
import com.sony.scea.pipeline.tools.Globals.NodeLocation;

public class LairShot
{
   /**
    * @param movieName
    * @param seqName
    * @param shotName
    * @param length
    * @param models
    */
   @SuppressWarnings("hiding")
   public LairShot(String movieName, String seqName, String shotName, int length,
	 ArrayList<LairAsset> assets, ArrayList<String> passes,
	 TreeMap<String, LinkedList<LairAsset>> assetGroupings)
   {
      this.movieName = movieName;
      this.seqName = seqName;
      this.shotName = shotName;
      this.length = length;
      this.assets = assets;
      this.passes = passes;
      this.assetGroupings = assetGroupings;

      shotStart = "/projects/lr/production/" + movieName + "/" + seqName + "/" + shotName
	    + "/";

      String shotPrefix = seqName + "_" + shotName + "_";

      animScene = shotStart + "anim/" + shotPrefix + "anim";
      animExportGroup = shotStart + "anim/data/" + shotPrefix + "data";
      lightScene = shotStart + "lgt/" + shotPrefix + "lgt";
      preLightScene = shotStart + "lgt/" + shotPrefix + "preLgt";
      lightShaderDefsMI = shotStart + "lgt/shaders/mi_shader_defs";

      String miStart = shotStart + "mi/";
      String imgStart = shotStart + "img/";

      cameraMI = miStart + "com/cam";
      geoTopGroup = miStart + "modTop";

      {
	 TreeMap<String, FileSeq> temp = new TreeMap<String, FileSeq>();
	 TreeMap<String, String> miTemp = new TreeMap<String, String>();
	 TreeMap<String, String> colTemp = new TreeMap<String, String>();
	 TreeMap<String, String> miInstTemp = new TreeMap<String, String>();
	 if ( assets != null )
	 {
	    for (LairAsset as : assets)
	    {
	       temp.put(as.assetName, new FileSeq(as.assetName, "anim"));
	       String geoMi = miStart + as.assetName + "/com/mod";
	       miTemp.put(as.assetName, geoMi);
	       String collate = shotStart + "anim/data/" + as.assetName + "_collate";
	       colTemp.put(as.assetName, collate);
	       String miGeoInst = miStart + as.assetName + "/com/modInst";
	       miInstTemp.put(as.assetName, miGeoInst);
	    }
	 }
	 animExportGroupSecSeqs = new TreeMap<String, FileSeq>(temp);
	 assetGeoMI = new TreeMap<String, String>(miTemp);
	 assetCollateScenes = new TreeMap<String, String>(colTemp);
	 assetGeoInstMI = new TreeMap<String, String>(miInstTemp);
      }
      {
	 TreeMap<String, String> tempLight = new TreeMap<String, String>();
	 TreeMap<String, String> tempOptions = new TreeMap<String, String>();
	 TreeMap<String, String> tempBaseOptions = new TreeMap<String, String>();
	 TreeMap<String, String> tempOverride = new TreeMap<String, String>();
	 DoubleMap<String, String, String> tempShade = new DoubleMap<String, String, String>();
	 DoubleMap<String, String, String> tempDImage = new DoubleMap<String, String, String>();
	 DoubleMap<String, String, String> tempShadeDef = new DoubleMap<String, String, String>();
	 TreeMap<String, String> tempImage = new TreeMap<String, String>();
	 if ( passes != null )
	 {
	    for (String pass : passes)
	    {
	       String lightMI = miStart + "com/" + pass + "/lgt";
	       tempLight.put(pass, lightMI);

	       String optMI = miStart + "com/" + pass + "/opt";
	       tempOptions.put(pass, optMI);

	       String baseOptMi = "/projects/lr/assets/options/" + pass;
	       tempBaseOptions.put(pass, baseOptMi);

	       String overMI = miStart + "com/" + pass + "/camOpt";
	       tempOverride.put(pass, overMI);

	       String imageMI = imgStart + "com/" + pass + "/" + shotPrefix + pass;
	       tempImage.put(pass, imageMI);

	       if ( assets != null )
	       {
		  for (LairAsset as : assets)
		  {
		     String shadeMI = miStart + as.assetName + "/" + pass + "/shd";
		     tempShade.put(as.assetName, pass, shadeMI);
		     String shaderDefMI = miStart + as.assetName + "/" + pass
			   + "/shaders/mi_shader_defs";
		     tempShadeDef.put(as.assetName, pass, shaderDefMI);
		     String dImageMI = imgStart + as.assetName + "/" + pass + "/"
			   + shotPrefix + as.assetName + "_" + pass;
		     tempDImage.put(as.assetName, pass, dImageMI);
		  }
	       }
	       if ( assetGroupings != null )
	       {
		  for (String assetGroup : assetGroupings.keySet())
		  {
		     String dImageMI = imgStart + assetGroup + "/" + pass + "/"
			   + shotPrefix + assetGroup + "_" + pass;
		     tempDImage.put(assetGroup, pass, dImageMI);
		  }
	       }
	    }
	 }
	 passLightMI = new TreeMap<String, String>(tempLight);
	 passOptionMI = new TreeMap<String, String>(tempOptions);
	 passBaseOptionMI = new TreeMap<String, String>(tempBaseOptions);
	 passCamOverMI = new TreeMap<String, String>(tempOverride);
	 assetPassShadeMI = new DoubleMap<String, String, String>(tempShade);
	 assetPassImages = new DoubleMap<String, String, String>(tempDImage);
	 assetPassShaderDefsMI = new DoubleMap<String, String, String>(tempShadeDef);
	 passImages = new TreeMap<String, String>(tempImage);
      }
   }

   public static LairShot getShot(Wrapper w, String movie, String seq, String shot,
	 ArrayList<String> passes, TreeMap<String, LinkedList<LairAsset>> assetGroupings)
      throws PipelineException
   {
      LairShot sh = new LairShot(movie, seq, shot, 0, null, null, null);
      if ( Globals.doesNodeExists(w, sh.animScene) )
      {
	 NodeID nodeID = new NodeID(w.user, w.view, sh.animScene);
	 NodeLocation loc = Globals.getNodeLocation(w, sh.animScene);
	 NodeCommon com = null;
	 switch (loc)
	 {
	    case LOCAL:
	       com = w.mclient.getWorkingVersion(nodeID);
	       break;
	    case REP:
	       TreeMap<VersionID, NodeVersion> versions = w.mclient
		  .getAllCheckedInVersions(sh.animScene);
	       com = versions.get(versions.lastKey());
	       break;
	    default:
	       throw new PipelineException("The animation node doesn't exist "
		     + "anywhere you can get at it.");
	 }
	 ArrayList<LairAsset> assets = new ArrayList<LairAsset>();
	 int length;
	 for (String source : com.getSourceNames())
	 {
	    if ( !( ( source.matches(LairConstants.cameraPatternStart + ".*") ) || ( source
	       .matches(LairConstants.lightsPatternStart + ".*") ) ) )
	       assets.add(LairConstants.stringToAsset(w, source));
	 }
	 if ( Globals.doesNodeExists(w, sh.animExportGroup) )
	 {
	    nodeID = new NodeID(w.user, w.view, sh.animExportGroup);
	    loc = Globals.getNodeLocation(w, sh.animExportGroup);
	    com = null;
	    switch (loc)
	    {
	       case LOCAL:
		  com = w.mclient.getWorkingVersion(nodeID);
		  break;
	       case REP:
		  TreeMap<VersionID, NodeVersion> versions = w.mclient
		     .getAllCheckedInVersions(nodeID.getName());
		  com = versions.get(versions.lastKey());
		  break;
	       default:
		  throw new PipelineException("The anim export node doesn't exist "
			+ "anywhere you can get at it.");
	    }
	    //System.err.println(com.getName());
	    BaseAction act = com.getAction();
	    //System.err.println(act.getName());
	    int first = (Integer) act.getSingleParamValue("FirstFrame");
	    int last = (Integer) act.getSingleParamValue("LastFrame");
	    //System.out.println("GetShot-length:" + first + "\t" + last);
	    length = last - first + 1;
	 } else
	    throw new PipelineException("Cannot parse render passes for shot ("
		  + sh.lightScene + ") as the tree either does not exist or isn't built"
		  + " correctly.\n  If the tree does not exist, please run "
		  + "buildLairAnimation first and then rerun this tool."
		  + " \nAnim Export Scene Error");

	 if ( passes == null )
	    passes = LairConstants.getShotPasses(w, sh);
	 if ( assetGroupings == null )
	 {
	    sh = new LairShot(movie, seq, shot, 0, assets, passes, null);
	    assetGroupings = LairConstants.getAssetGroupings(w, sh);
	 }

	 return new LairShot(movie, seq, shot, length, assets, passes, assetGroupings);
      } else
	 throw new PipelineException("Cannot parse render passes for shot ("
	       + sh.lightScene + ") as the tree either does not exist or isn't built"
	       + " correctly.\n  If the tree does not exist, please run "
	       + "buildLairAnimation first and then rerun this tool. "
	       + "\nAnim Scene Error");
   }

   public final String movieName;
   public final String seqName;
   public final String shotName;
   public final String shotStart;
   public final int length;
   public final ArrayList<LairAsset> assets;
   public final ArrayList<String> passes;
   public final TreeMap<String, LinkedList<LairAsset>> assetGroupings;

   public final String animScene;
   public final String animExportGroup;
   public final TreeMap<String, FileSeq> animExportGroupSecSeqs;
   public final TreeMap<String, String> assetGeoMI;
   public final TreeMap<String, String> assetGeoInstMI;
   public final TreeMap<String, String> assetCollateScenes;
   public final String geoTopGroup;

   public final String cameraMI;
   public final TreeMap<String, String> passLightMI;
   public final TreeMap<String, String> passOptionMI;
   public final TreeMap<String, String> passBaseOptionMI;
   public final TreeMap<String, String> passCamOverMI;
   public final DoubleMap<String, String, String> assetPassShadeMI;

   /**
    * miStart + as.assetName + "/" + pass + "/shaders/mi_shader_defs";
    */
   public final DoubleMap<String, String, String> assetPassShaderDefsMI;
   public final DoubleMap<String, String, String> assetPassImages;
   public final TreeMap<String, String> passImages;

   public final String preLightScene;
   public final String lightScene;
   /**
    * shotStart + "lgt/shaders/mi_shader_defs";
    */
   public final String lightShaderDefsMI;

}
