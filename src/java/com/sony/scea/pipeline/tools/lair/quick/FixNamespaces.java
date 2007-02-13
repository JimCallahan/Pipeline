package com.sony.scea.pipeline.tools.lair.quick;

import java.util.ArrayList;
import java.util.TreeMap;

import sun.security.action.GetLongAction;
import us.temerity.pipeline.*;

import com.sony.scea.pipeline.tools.*;
import com.sony.scea.pipeline.tools.lair.LairAsset;

public class FixNamespaces
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
      Wrapper w = null;
      try
      {
	 w = new Wrapper(user, view, toolset, mclient);
	 PluginMgrClient.init();
      } catch ( PipelineException e1 )
      {
	 e1.printStackTrace();
      }

      ArrayList<String> seqs = new ArrayList<String>();
      //seqs.add("seq031");
      seqs.add("seq034");

      String project = "lr";
      String movie = "lair";

      TreeMap<String, String> spaces = SonyConstants.getCustomNamespaces(project);
      for (String seq : seqs)
      {
	 try
	 {
	    ArrayList<String> shots = SonyConstants.getShotList(w, project, movie, seq);
	    for (String shot : shots)
	    {
	       SonyShot sh = SonyShot.getShot(w, project, movie, seq, shot, null, null);
	       System.err.println(sh.animScene);
	       Globals.getLatest(w, sh.animScene, CheckOutMode.KeepModified,
		  CheckOutMethod.FrozenUpstream);
	       NodeMod mod = mclient.getWorkingVersion(user, view, sh.animScene);
	       BaseAction act = mod.getAction();
	       for (String source : mod.getSourceNames())
	       {
		  if ( source
		     .equals("/projects/lr/assets/prop/rohnCircleArt/rohnCircleArt") )
		  {
		     mclient.unlink(user, view, sh.animScene, source);
		     mclient.link(user, view, sh.animScene,
			"/projects/lr/assets/prop/rohnCircleArt/rohnCircleArt_lr",
			LinkPolicy.Reference, LinkRelationship.All, null);
		     act
			.initSourceParams("/projects/lr/assets/prop/rohnCircleArt/rohnCircleArt_lr");
		  }
	       }
	       for (SonyAsset as : sh.assets)
	       {
		  System.err.println("\t" + as.assetName);
		  String space = spaces.get(as.assetName);
		  System.err.println("\t" + space);
		  if ( space != null )
		  {
		     act.setSourceParamValue(as.lr_finalScene, "PrefixName", space);
		  }
	       }
	       act.setSingleParamValue("TimeUnits", "NTSC (30 fps)");
	       mod.setAction(act);
	       mclient.modifyProperties(user, view, mod);
	       Globals.enableAction(w, sh.animScene);
	       mclient.removeFiles(user, view, sh.animScene, null);
	       try
	       {
		  mclient.submitJobs(user, view, sh.animScene, null);
	       } catch ( PipelineException ex1 )
	       {
		  ex1.printStackTrace();

	       }
	    }
	 } catch ( PipelineException e )
	 {
	    e.printStackTrace();
	 }
      }

   }
}
