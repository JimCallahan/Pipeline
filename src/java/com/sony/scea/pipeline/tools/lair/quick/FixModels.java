package com.sony.scea.pipeline.tools.lair.quick;

import static com.sony.scea.pipeline.tools.Globals.*;
import static com.sony.scea.pipeline.tools.lair.LairConstants.*;

import java.util.ArrayList;
import java.util.TreeMap;

import us.temerity.pipeline.*;

import com.sony.scea.pipeline.tools.*;
import com.sony.scea.pipeline.tools.lair.LairAsset;
import com.sony.scea.pipeline.tools.lair.LairConstants;
import com.sony.scea.pipeline.tools.lair.LairConstants.AssetType;

public class FixModels
{

   /**
    * @param args
    */
   public static void main(String[] args)
   {
      String user = "pipeline";
      String view = "build";
      String toolset = "csg_rev15";
      MasterMgrClient mclient = new MasterMgrClient();
      Wrapper w;
      try
      {
	 PluginMgrClient.init();
	 w = new Wrapper(user, view, toolset, mclient);
	 TreeMap<String, String> chars = SonyConstants.getAllAssetsMap(w, "lr");
	 int pass = 2;
	 if ( pass == 1 )
	 {
	    for (String charPrefix : chars.keySet())
	    {
	       {
		  System.out.println(charPrefix);
		  String nodeName = chars.get(charPrefix);
		  SonyAsset as = SonyConstants.stringToAsset(w, nodeName);
		  System.out.println("Checking out the models scene");
		  try
		  {
		  getLatest(w, as.modScene, keep, modi);
		  getLatest(w, as.lr_modScene, keep, modi);
		  getLatest(w, as.rigScene, keep, modi);
		  getLatest(w, as.lr_rigScene, keep, modi);
		  getLatest(w, as.matScene, keep, modi);
		  getLatest(w, as.lr_matScene, keep, modi);
		  getLatest(w, as.finalScene, keep, modi);
		  getLatest(w, as.lr_finalScene, keep, modi);
		  getLatest(w, LairConstants.MEL_finalizeCharacter, keep, modi);
		  getLatest(w, as.texGroup, over, froz);
		  //		  getNewest(w, as.shdScene, keep, pFroz);
		  } catch (PipelineException ex)
		  {
		     ex.printStackTrace();
		  }

		  ArrayList<String> crap = new ArrayList<String>();
		  crap.add(as.modScene);
		  crap.add(as.rigScene);
		  crap.add(as.matScene);
		  crap.add(as.finalScene);
		  crap.add(as.lr_modScene);
		  crap.add(as.lr_rigScene);
		  crap.add(as.lr_matScene);
		  crap.add(as.lr_finalScene);
		  //crap.add(as.shdScene);
		  for (String each : crap)
		  {
		     try {
		     NodeMod mod = mclient.getWorkingVersion(user, view, each);
		     mod.setToolset(toolset);
		     mclient.modifyProperties(user, view, mod);
		     } catch (PipelineException ex)
			  {
			     ex.printStackTrace();
			  }
		  }

		  //mclient.link(user, view, as.matScene, as.texGroup, REF, LINKALL, null);
		  

//		  if ( !doesNodeExists(w, as.shdExport) )
//		  {
//		     NodeMod mod = registerNode(w, as.shdExport, "ma", editorMaya());
//		     BaseAction act = actionMayaShaderExport();
//		     mclient
//			.link(user, view, as.shdExport, as.shdScene, DEP, LINKALL, null);
//		     act.setSingleParamValue("SelectionPrefix", as.assetName);
//		     act.setSingleParamValue("MayaScene", as.shdScene);
//		     mod.setAction(act);
//		     mclient.modifyProperties(user, view, mod);
//		  }

		  //		  mclient.submitJobs(user, view, as.shdExport, null);
		  try
		  {
//		     mclient.submitJobs(user, view, as.finalScene, null);
		     mclient.unlink(user, view, as.lr_matScene, as.texGroup);
		     mclient.submitJobs(user, view, as.lr_finalScene, null);
		  } catch ( PipelineException ex )
		  {

		  }

		  System.out.println("Check out done");
	       }
	    }
	 } else if ( pass == 2 )
	 {
	    for (String charPrefix : chars.keySet())
	    {
	       System.out.println(charPrefix);
	       String nodeName = chars.get(charPrefix);
	       SonyAsset as = SonyConstants.stringToAsset(w, nodeName);
	       System.out.println(nodeName);
	       try
	       {
//		  {
//		     NodeID nodeID = new NodeID(user, view, as.finalScene);
//		     mclient.checkIn(nodeID, "Updated all the models with the latest "
//			   + "toolset and the latest finalize-character mel.  "
//			   + "Also linked the textures to the material scene.",
//			VersionID.Level.Minor);
//		  }
		  {
		     NodeID nodeID = new NodeID(user, view, as.lr_finalScene);
		     mclient.checkIn(nodeID,
			"Updated all the low-rez models with the latest "
			      + "toolset and the latest finalize-character mel.  "
			      + "Also linked the textures to the material scene.",
			VersionID.Level.Minor);
		  }
	       } catch ( PipelineException ex )
	       {
		  ex.printStackTrace();
	       }
	    }
	 } else if ( pass == 3 )
	 {
	    for (String charPrefix : chars.keySet())
	    {
	       System.out.println(charPrefix);
	       String nodeName = chars.get(charPrefix);
	       LairAsset as = new LairAsset(new Path(nodeName).getName(),
		  AssetType.CHARACTER);
	       mclient.release(user, view, as.shdExport, true);
	    }
	 }
      } catch ( PipelineException e )
      {
	 // TODO Auto-generated catch block
	 e.printStackTrace();
      }

   }
}
