package com.sony.scea.pipeline.plugins.v1_0_0;

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
      super("RiggerCheckOut", new VersionID("1.0.0"), "SCEA",
	 "Special checkout for the riggers.");

      underDevelopment();
      addSupport(OsType.MacOS);
      addSupport(OsType.Windows);
   }

   public synchronized String collectPhaseInput() throws PipelineException
   {
      if ( pSelected.size() == 0 || pSelected.size() > 1 )
	 throw new PipelineException("Must have a single node selected.");

      if ( pPrimary == null )
	 throw new PipelineException("Must have a single node selected.");

      if ( ( pPrimary.matches(hirezPattern) || pPrimary.matches(lorezPattern) ) )
      {
	 Path p = new Path(pPrimary);
	 ArrayList<String> parts = p.getComponents();
	 int size = parts.size();
	 String assetName = parts.get(size - 2);
	 Path assetStart = p.getParentPath();

	 textureNode = assetStart + "/textures/" + assetName + "_tex";
	 topNode = pPrimary;

      } else if ( ( pPrimary.matches(hirezRigPattern) || pPrimary.matches(lorezRigPattern) ) )
      {
	 Path p = new Path(pPrimary);
	 ArrayList<String> parts = p.getComponents();
	 int size = parts.size();
	 String assetName = parts.get(size - 3);
	 Path assetStart = p.getParentPath().getParentPath();

	 textureNode = assetStart + "/textures/" + assetName + "_tex";
	 if ( pPrimary.matches(hirezRigPattern) )
	    topNode = assetStart + "/" + assetName;
	 else
	    topNode = assetStart + "/" + assetName + "_lr";

      } else
	 throw new PipelineException(
	    "The node selected is not a valid top level node or rig node.");

      return " : Performing Checkout";
   }

   public synchronized boolean executePhase(MasterMgrClient mclient, QueueMgrClient qclient)
      throws PipelineException
   {
      NodeStatus stat = pSelected.get(pPrimary);
      NodeID id = stat.getNodeID();
      String author = id.getAuthor();
      String view = id.getView();
      if ( pPrimary.matches(hirezPattern) || pPrimary.matches(hirezRigPattern) )
	 jcheckOut(mclient, author, view, textureNode, null, CheckOutMode.OverwriteAll,
	    CheckOutMethod.AllFrozen);
      jcheckOut(mclient, author, view, topNode, null, CheckOutMode.KeepModified,
	 CheckOutMethod.PreserveFrozen);
      mclient.removeFiles(author, view, topNode, null);
      return false;
   }

   @Override
   public TreeSet<String> rootsOnExit()
   {
      TreeSet<String> roots = new TreeSet<String>(pRoots);
      roots.remove(pPrimary);
      roots.add(topNode);
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

   private String textureNode = null;
   private String topNode = null;

   private static String hirezPattern = ".*/assets/(character|set|prop)/.*/[a-zA-Z0-9]+";
   private static String lorezPattern = ".*/assets/(character|set|prop)/.*/[a-zA-Z0-9]+_lr";

   private static String hirezRigPattern = ".*/assets/(character|set|prop)/.*/rig/.*_rig";
   private static String lorezRigPattern = ".*/assets/(character|set|prop)/.*/rig/.*_rig_lr";;

   private static final long serialVersionUID = -5609156980458221524L;
}
