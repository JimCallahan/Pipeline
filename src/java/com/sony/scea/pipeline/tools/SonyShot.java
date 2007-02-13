package com.sony.scea.pipeline.tools;

import java.util.*;

import us.temerity.pipeline.*;

import com.sony.scea.pipeline.tools.Globals;
import com.sony.scea.pipeline.tools.Wrapper;
import com.sony.scea.pipeline.tools.Globals.NodeLocation;

public class SonyShot
{
   /**
    * @param movieName
    * @param seqName
    * @param shotName
    * @param length
    * @param models
    */
   @SuppressWarnings("hiding")
   public SonyShot(String project, String movieName, String seqName, String shotName,
	 int length, ArrayList<SonyAsset> assets, ArrayList<String> passes,
	 TreeMap<String, LinkedList<SonyAsset>> assetGroupings)
   {
      this.project = project;
      this.movieName = movieName;
      this.seqName = seqName;
      this.shotName = shotName;
      this.length = length;
      this.assets = assets;
      this.passes = passes;
      this.assetGroupings = assetGroupings;

      shotStart = "/projects/" + project + "/production/" + movieName + "/" + seqName + "/"
	    + shotName + "/";

      String shotPrefix = seqName + "_" + shotName + "_";

      animScene = shotStart + "anim/" + shotPrefix + "anim";
      animExportGroup = shotStart + "anim/data/" + shotPrefix + "data";
      lightScene = shotStart + "lgt/" + shotPrefix + "lgt";
      testLightScene = shotStart + "lgt/" + shotPrefix + "testLgt";
      preLightScene = shotStart + "lgt/" + shotPrefix + "preLgt";
      switchLightScene = shotStart + "lgt/" + shotPrefix + "switchLgt";
      lightShaderDefsMI = shotStart + "lgt/shaders/mi_shader_defs";

      String miStart = shotStart + "mi/";
      String imgStart = shotStart + "img/";

      initialImages = imgStart + "test/" + seqName + "_" + shotName;
      cameraMI = miStart + "com/cam";
      geoTopGroup = miStart + "modTop";

      {
	 TreeMap<String, FileSeq> temp = new TreeMap<String, FileSeq>();
	 TreeMap<String, String> miTemp = new TreeMap<String, String>();
	 TreeMap<String, String> colTemp = new TreeMap<String, String>();
	 TreeMap<String, String> miInstTemp = new TreeMap<String, String>();
	 if ( assets != null )
	 {
	    for (SonyAsset as : assets)
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
		  for (SonyAsset as : assets)
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

   public static SonyShot getShot(Wrapper w, String project, String movie, String seq,
	 String shot, ArrayList<String> passes,
	 TreeMap<String, LinkedList<SonyAsset>> assetGroupings) throws PipelineException
   {
      SonyShot sh = new SonyShot(project, movie, seq, shot, 0, null, null, null);
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
	 ArrayList<SonyAsset> assets = new ArrayList<SonyAsset>();
	 int length;
	 for (String source : com.getSourceNames())
	 {
	    if ( !( ( source.matches(SonyConstants.cameraStartPattern(project) + ".*") ) || ( source
	       .matches(SonyConstants.lightStartPattern(project) + ".*") ) ) )
	       try
	       {
		  assets.add(SonyConstants.stringToAsset(w, source));
	       } catch ( PipelineException ex )
	       {}
	 }
	 BaseAction act = com.getAction();
	 if ( act.getName().equals("MayaBuild") )
	 {
	    int first = (Integer) act.getSingleParamValue("StartFrame");
	    int last = (Integer) act.getSingleParamValue("EndFrame");
	    length = last - first + 1;
	 } else
	    length = 1;
	 //System.out.println("GetShot-length:" + first + "\t" + last);
	 

	 /*if ( passes == null )
	  passes = SonyConstants.getShotPasses(w, sh);
	  if ( assetGroupings == null )
	  {
	  sh = new SonyShot(project, movie, seq, shot, 0, assets, passes, null);
	  assetGroupings = SonyConstants.getAssetGroupings(w, sh);
	  }*/

	 return new SonyShot(project, movie, seq, shot, length, assets, passes,
	    assetGroupings);
      }
      throw new PipelineException("Cannot parse render passes for shot (" + sh.lightScene
	    + ") as the tree either does not exist or isn't built"
	    + " correctly.\n  If the tree does not exist, please run "
	    + "buildLairAnimation first and then rerun this tool. " + "\nAnim Scene Error");
   }

   public final String project;
   public final String movieName;
   public final String seqName;
   public final String shotName;
   public final String shotStart;
   public final int length;
   public final ArrayList<SonyAsset> assets;
   public final ArrayList<String> passes;
   public final TreeMap<String, LinkedList<SonyAsset>> assetGroupings;

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

   public final String switchLightScene;
   public final String preLightScene;
   public final String testLightScene;
   public final String lightScene;
   public final String initialImages;
   /**
    * shotStart + "lgt/shaders/mi_shader_defs";
    */
   public final String lightShaderDefsMI;

}
