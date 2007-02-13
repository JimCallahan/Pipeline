package com.sony.scea.pipeline.plugins.v1_0_0;

import us.temerity.pipeline.*;

/**
 * Allows a super-user to delete multiple nodes from Pipeline without having to check them out.
 * <p>
 * Useful for doing large scale node clean-up.  Does not do any sort of order sorting, so it can
 * fail if you select multiple nodes in a tree and it tried to delete one that is upstream of
 * another.
 *  
 * @author Jesse Clemens
 *
 */
public class DeleteManyTool extends BaseTool
{
   public DeleteManyTool()
   {
      super("DeleteMany", new VersionID("1.0.0"), "SCEA", "Deletes lots of nodes at once.");

      underDevelopment();
      addSupport(OsType.MacOS);
      addSupport(OsType.Windows);
   }

   public synchronized String collectPhaseInput() throws PipelineException
   {
      return " : Deleting stuff";
   }

   public synchronized boolean executePhase(MasterMgrClient mclient, QueueMgrClient qclient)
      throws PipelineException
   {
      for (String s : pSelected.keySet())
      {
	 mclient.delete(s, false);
      }
      return false;
   }

   private static final long serialVersionUID = -8765286337358281519L;
}
