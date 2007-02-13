package com.sony.scea.pipeline.plugins.v1_0_0;

import java.util.TreeSet;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;
/**
 * One-off tool that runs on a bunch of map files and sets their ImageSource
 * param to what ever file is linked to them.  <p>
 * Useful when lots of map files are having new images hooked up to them.
 * 
 * @author Jesse Clemens
 *
 */
public class SetMapNameTool extends BaseTool
{
   public SetMapNameTool()
   {
      super("SetMapName", new VersionID("1.0.0"), "SCEA",
	 "Utility to quickly set the Image Source on map files.");

      underDevelopment();

      addSupport(OsType.MacOS);
      addSupport(OsType.Windows);
   }

   public synchronized String collectPhaseInput() throws PipelineException
   {
      if ( pPrimary == null )
	 throw new PipelineException("Youse got to select something!");

      return ": Making the world a better place, one day at a time.";
   }

   public synchronized boolean executePhase(MasterMgrClient mclient, QueueMgrClient qclient)
      throws PipelineException
   {
      for (String nodeName : pSelected.keySet())
      {
	 NodeStatus status = pSelected.get(nodeName);
	 NodeID id = status.getNodeID();
	 NodeMod mod = status.getDetails().getWorkingVersion();
	 BaseAction act = mod.getAction();
	 TreeSet<String> sources = new TreeSet<String>(mod.getSourceNames());
	 if ( sources.size() == 1 )
	 {
	    act.setSingleParamValue("ImageSource", sources.first());
	    act.setSingleParamValue("TexelLayout", "Scanlines");
	    mod.setAction(act);
	    mclient.modifyProperties(id.getAuthor(), id.getView(), mod);
	 }

      }
      return false;
   }

   private static final long serialVersionUID = 1005349780693470772L;

}
