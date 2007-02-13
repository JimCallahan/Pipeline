package com.sony.scea.pipeline.tools.lair.quick;

import java.util.ArrayList;
import java.util.TreeMap;

import us.temerity.pipeline.*;

import com.sony.scea.pipeline.tools.*;

public class ListUserShots
{
  public static void main(String[] args)
  {
    String user = "pipeline";
    String view = "build";
    String toolset = "csg_rev15";
    MasterMgrClient mclient = new MasterMgrClient();
    Wrapper w = null;
    try
    {
	 PluginMgrClient.init();
	 w = new Wrapper(user, view, toolset, mclient);
    } catch (PipelineException ex)
    {
      ex.printStackTrace();
      System.exit(1);
    }
    try
    {
      String proj = "lr";
      String movie = "lair";
      ArrayList<String> sequences = SonyConstants.getSequenceList(w, proj, movie);
      
      for (String seq : sequences)
      {
	ArrayList<String> shots = SonyConstants.getShotList(w, proj, movie, seq);
	for (String shot : shots)
	{
	  SonyShot s = new SonyShot(proj, movie, seq, shot, 10, null, null, null);
	  String animScene = s.animScene;
	  TreeMap<VersionID, NodeVersion> versions =  mclient.getAllCheckedInVersions(animScene);
	  for (VersionID id : versions.keySet())
	  {
	    NodeVersion ver = versions.get(id);
	    String auth = ver.getAuthor();
	    if (auth.equals("tdickens"))
	    {
	      System.out.println(s.animScene + "\t" + id.toString());
	      System.out.println(ver.getLogMessage().getMessage());
	      System.out.println();
	    }
	  }
	}
      }
    } catch ( PipelineException e )
    {
      e.printStackTrace();
    }
    
  }
  
}
