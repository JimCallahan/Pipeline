// $Id: JSimpleNodeDetailPanel.java,v 1.1 2008/07/21 17:31:10 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;
import us.temerity.pipeline.glue.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*------------------------------------------------------------------------------------------*/
/*   S I M P L E   N O D E   D E T A I L   P A N E L                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Common base class for all panels which display detailed node status information which 
 * does not require per-file level handling.
 */ 
public  
class JSimpleNodeDetailPanel
  extends JBaseNodeDetailPanel
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new panel with the default working area view.
   */
  protected 
  JSimpleNodeDetailPanel() 
  {
    super();
  }

  /**
   * Construct a new panel with a working area view identical to the given panel.
   */
  protected
  JSimpleNodeDetailPanel
  (
   JTopLevelPanel panel
  )
  {
    super(panel);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Helper for looking up the job IDs for the panel's current node which match a given 
   * QueueState (heavyweight status) and/or the node IDs (lightweight status).
   */ 
  protected void
  lookupNodeJobsWithState
  (
   TreeSet<NodeID> nodes,
   TreeSet<Long> jobs, 
   QueueState state
  ) 
  {
    if(pStatus != null) {
      if(pStatus.hasHeavyDetails()) {
        NodeDetailsHeavy details = pStatus.getHeavyDetails();
        
        Long[] jobIDs   = details.getJobIDs();
        QueueState[] qs = details.getQueueState();
        assert(jobIDs.length == qs.length);
        
        int wk;
        for(wk=0; wk<jobIDs.length; wk++) {
          if(qs[wk] == state) {
            assert(jobIDs[wk] != null);
            jobs.add(jobIDs[wk]);
          }
        }
      }
      else if(pStatus.hasLightDetails()) {
        nodes.add(pStatus.getNodeID());
      }
    }
  }

  /**
   * Helper for looking up the job IDs for panel's current node which have a pending 
   * QueueState (heavyweight status) and/or the node IDs (lightweight status).
   */ 
  protected void
  lookupNodeJobsPending
  (
   TreeSet<NodeID> nodes,
   TreeSet<Long> jobs
  ) 
  {
    if(pStatus != null) {
      if(pStatus.hasHeavyDetails()) {
        NodeDetailsHeavy details = pStatus.getHeavyDetails();
        
        Long[] jobIDs   = details.getJobIDs();
        QueueState[] qs = details.getQueueState();
        assert(jobIDs.length == qs.length);
        
        int wk;
        for(wk=0; wk<jobIDs.length; wk++) {
          switch(qs[wk]) {
          case Queued:
          case Paused:
          case Running:
            assert(jobIDs[wk] != null);
            jobs.add(jobIDs[wk]);
          }
        }
      }
      else if(pStatus.hasLightDetails()) {
        nodes.add(pStatus.getNodeID());
      }
    }
  }

   

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 4871182576575873185L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  

}
