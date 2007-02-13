package com.sony.scea.pipeline.plugins.v1_1_0;

import java.util.ArrayList;
import java.util.TreeSet;

import us.temerity.pipeline.*;

/**
 * Tool to check out asset trees for riggers.  
 * 
 * The tool first checkouts any necessary texture nodes and then 
 * checkouts the top level node.
 * 
 * @author jesse clemens
 *
 */
public class RiggerCheckOutTool extends BaseTool
{
    public RiggerCheckOutTool()
   {
      super("RiggerCheckOut", new VersionID("1.1.0"), "SCEA",
	 "Special checkout for the riggers and modelers.");

      underDevelopment();
      addSupport(OsType.MacOS);
      addSupport(OsType.Windows);
      topNodes = new TreeSet<String>();
   }

   public synchronized String collectPhaseInput() throws PipelineException
   {
      if ( pSelected.size() == 0 || pSelected.size() > 1 )
	 throw new PipelineException("Must have a single node selected.");

      if ( pPrimary == null )
	 throw new PipelineException("Must have a single node selected.");

      if ( pPrimary.matches(lorezPattern)  )
      {
	 topNodes.add(pPrimary);
      } else if ( ( pPrimary.matches(hirezRigPattern) || pPrimary.matches(hirezModPattern) ) )
      {
	 Path p = new Path(pPrimary);
	 ArrayList<String> parts = p.getComponents();
	 int size = parts.size();
	 String assetName = parts.get(size - 3);
	 Path assetStart = p.getParentPath().getParentPath();
	 
	 advancedSearch = true;
	 rigName = assetStart + "/rig/" + assetName + "_rig";

      } else
	 throw new PipelineException("The node selected is not a valid top "
	       + "level node or rig node or model node.");

      return " : Performing Checkout";
   }

   public synchronized boolean executePhase(MasterMgrClient mclient, QueueMgrClient qclient)
      throws PipelineException
   {
      NodeStatus stat = pSelected.get(pPrimary);
      NodeID id = stat.getNodeID();
      String author = id.getAuthor();
      String view = id.getView();

      if (advancedSearch)
      {
	 NodeStatus status = mclient.status(author, view, rigName);
	 if (true)
	    throw new PipelineException(status.getTargetNames().toString());
	 for (String target : status.getTargetNames())
	 {
	    if (target.matches(lorezPattern))
	       topNodes.add(target);
	    
	 }
	 
      }
      
      for (String topNode : topNodes)
      {
	 jcheckOut(mclient, author, view, topNode, null, CheckOutMode.KeepModified,
	    CheckOutMethod.PreserveFrozen);
	 mclient.removeFiles(author, view, topNode, null);
	 mclient.submitJobs(author, view, topNode, null);
      }
      
      return false;
   }

   @Override
   public TreeSet<String> rootsOnExit()
   {
      TreeSet<String> roots = new TreeSet<String>(pRoots);
      roots.remove(pPrimary);
      roots.addAll(topNodes);
      return roots;
   }
   
   private void jcheckOut(MasterMgrClient mclient, String user, String view, String name,
       VersionID id, CheckOutMode mode, CheckOutMethod method) throws PipelineException
   {
     if (id == null)
       id = mclient.getCheckedInVersionIDs(name).last();
     if (id == null)
       throw new PipelineException("BAD BAD BAD");
     mclient.checkOut(user, view, name, id, mode, method);
   }

   private TreeSet<String> topNodes = null;
   
   private boolean advancedSearch = false;
   private String rigName = null;

   private static String lorezPattern = ".*/assets/(character|set|prop)/.*/[a-zA-Z0-9]+_lr";
   private static String hirezRigPattern = ".*/assets/(character|set|prop)/.*/rig/.*_rig";
   private static String hirezModPattern = ".*/assets/(character|set|prop)/.*/model/.*_mod";

   private static final long serialVersionUID = -8170804827441263773L;
}
