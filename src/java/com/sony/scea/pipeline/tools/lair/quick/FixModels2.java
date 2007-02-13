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

public class FixModels2
{

   /**
    * @param args
    */
   public static void main(String[] args)
   {
      String user = "pipeline";
      String view = "build";
      String toolset = "csg_rev18";
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
		  //System.out.println("Checking out the models scene");
		  try
		  {
		     if ( Globals.doesNodeExists(w, as.texGroup) )
			getLatest(w, as.texGroup, over, froz);
		     if ( Globals.doesNodeExists(w, as.modScene) )
			getLatest(w, as.modScene, keep, modi);
		     if ( Globals.doesNodeExists(w, as.lr_modScene) )
			getLatest(w, as.lr_modScene, keep, modi);
		     if ( Globals.doesNodeExists(w, as.rigScene) )
			getLatest(w, as.rigScene, keep, modi);
		     if ( Globals.doesNodeExists(w, as.lr_rigScene) )
			getLatest(w, as.lr_rigScene, keep, modi);
		     if ( Globals.doesNodeExists(w, as.matScene) )
			getLatest(w, as.matScene, keep, modi);
		     if ( Globals.doesNodeExists(w, as.lr_matScene) )
			getLatest(w, as.lr_matScene, keep, modi);
		     if ( Globals.doesNodeExists(w, as.finalScene) )
			getLatest(w, as.finalScene, keep, modi);
		     if ( Globals.doesNodeExists(w, as.lr_finalScene) )
			getLatest(w, as.lr_finalScene, keep, modi);
		     getNewest(w, LairConstants.MEL_finalizeCharacter, keep, modi);

		  } catch ( PipelineException ex )
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
		  try
		  {
		     for (String each : crap)
		     {
			if ( Globals.doesNodeExists(w, each) )
			{
			   NodeMod mod = mclient.getWorkingVersion(user, view, each);
			   mod.setToolset(toolset);
			   mclient.modifyProperties(user, view, mod);
			}
		     }
		  } catch ( PipelineException ex )
		  {
		     ex.printStackTrace();
		  }

		  if ( Globals.doesNodeExists(w, as.modScene)
			&& Globals.doesNodeExists(w, as.rigScene) )
		     mclient.link(user, view, as.rigScene, as.modScene, DEP, LINKALL, null);
		  if ( Globals.doesNodeExists(w, as.lr_modScene)
			&& Globals.doesNodeExists(w, as.lr_rigScene) )
		     mclient.link(user, view, as.lr_rigScene, as.lr_modScene, DEP, LINKALL,
			null);

		  try
		  {
		     mclient.submitJobs(user, view, as.finalScene, null);
		     mclient.submitJobs(user, view, as.lr_finalScene, null);
		  } catch ( PipelineException ex )
		  {
		     ex.printStackTrace();
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
		  {
		     NodeID nodeID = new NodeID(user, view, as.finalScene);
		     mclient.checkIn(nodeID, "Updated all the models with the latest "
			   + "toolset and the latest finalize-character mel.  "
			   + "Fixed any REF and DEP issues that might have existed",
			VersionID.Level.Minor);
		  }
		  {
		     NodeID nodeID = new NodeID(user, view, as.lr_finalScene);
		     mclient.checkIn(nodeID,
			"Updated all the low-rez models with the latest "
			      + "toolset and the latest finalize-character mel.  "
			      + "Fixed any REF and DEP issues that might have existed",
			VersionID.Level.Minor);
		  }
	       } catch ( PipelineException ex )
	       {
		  ex.printStackTrace();
	       }
	    }
	 }
      } catch ( PipelineException e )
      {
	 // TODO Auto-generated catch block
	 e.printStackTrace();
      }

   }
}
