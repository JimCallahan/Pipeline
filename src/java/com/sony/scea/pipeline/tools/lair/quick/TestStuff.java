package com.sony.scea.pipeline.tools.lair.quick;

import java.util.*;

import us.temerity.pipeline.*;

import com.sony.scea.pipeline.tools.*;
import com.sony.scea.pipeline.tools.lair.*;

public class TestStuff
{

   /**
    * @param args
    */
   public static void main(String[] args)
   {
      String user = "pipeline";
      String view = "default";
      String toolset = "csg_rev15";
      MasterMgrClient mclient = new MasterMgrClient();
      Wrapper w;
      try
      {
	 PluginMgrClient.init();
	 w = new Wrapper(user, view, toolset, mclient);
	 //TreeMap<VersionID, NodeVersion> stuff = mclient.getAllCheckedInVersions("/projects/lr/assets/character/attakai/attakai");
	 //for (VersionID id : stuff.keySet())
	    //System.err.println(id);
	 
	 TreeMap<String, String> assets  = SonyConstants.getAllAssetsMap(w, "lr");
	 for (String longName : assets.values())
	 {
	    SonyAsset as = SonyConstants.stringToAsset(w, longName);
	    System.out.println(as.assetName);
	    {
	       TreeMap<VersionID, LogMessage> hist = mclient.getHistory(as.modScene);
	       int size = hist.size();
	       ArrayList<VersionID> ids = new ArrayList<VersionID>(hist.keySet());
	       for (int i = size -1; i >= 0; i--)
	       {
		  VersionID id = ids.get(i);
	       }
	    }
	 }
	 
	 
	 /*String s = "/projects/lr/production/lair/temple/01/img/";
	  ArrayList<String> children = Globals.getChildrenNodes(w, s);
	  for (String each : children)
	  System.err.println(each);

	  ArrayList<String> allPasses = LairConstants.getAllPasses(w);
	  System.err.println("All Passes");
	  for (String pass : allPasses)
	  System.err.println("\t" + pass);

	  LairShot sh = LairShot.getShot(w, "lair", "temple", "01", null, null);
	  ArrayList<String> passes = LairConstants.getShotPasses(w, sh);
	  System.err.println("This Passes");
	  for (String pass : passes)
	  System.err.println("\t" + pass);
	  sh = LairShot.getShot(w, "lair", "temple", "01", passes, null);
	  TreeMap<String, LinkedList<LairAsset>> assetGroupings = LairConstants
	  .getAssetGroupings(w, sh);
	  for (String group : assetGroupings.keySet())
	  {
	  System.err.println("Group: " + group);
	  LinkedList<LairAsset> assets = assetGroupings.get(group);
	  for (LairAsset as : assets)
	  System.err.println("\tasset: " + as.assetName);
	  }*/

	 //ArrayList<String> values = SonyConstants.getAllMelWithPrefix(w, "lr");
	 //for (String s1 : values)
	 //System.err.println(s1);
	 //NodeStatus status = mclient.status(user, view,
	 //   "/projects/lr/assets/character/attakai/attakai");
	 //System.err.println(Globals.getTreeState(status));

      } catch ( PipelineException e )
      {
	 // TODO Auto-generated catch block
	 e.printStackTrace();
      }
   }
}
