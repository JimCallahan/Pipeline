package com.sony.scea.pipeline.plugins.v1_0_0;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/**
 * Used to renumber a group of nodes, all having the same frame range to a new frame range.
 * <P>
 * All of the nodes must be selected and must have the exact same frame range. 
 */
public class MultipleRenumberTool extends BaseTool
{
  
   public MultipleRenumberTool()
   {
      super("MultipleRenumber", new VersionID("1.0.0"), "SCEA",
	 "Renumbers a bunch of nodes with the same frame range..");

      underDevelopment();

      addSupport(OsType.MacOS);
      addSupport(OsType.Windows);
   }
   
   @SuppressWarnings("static-access")
public synchronized String collectPhaseInput() throws PipelineException
   {
      for (NodeStatus stat : pSelected.values())
      {
	 NodeMod mod = stat.getDetails().getWorkingVersion();
	 if (mod == null)
	    throw new PipelineException("All selected nodes must have a working version");
	 FileSeq seq = mod.getPrimarySequence();
	 if (!seq.hasFrameNumbers())
	    throw new PipelineException("All selected nodes must have frame numbers");
	 FrameRange range = seq.getFrameRange();
	 if (range == null)
	    throw new PipelineException("All selected nodes must be sequences.");
	 if (pOrigFrameRange == null)
	    pOrigFrameRange = range;
	 else if (!pOrigFrameRange.equals(range))
	    throw new PipelineException("All selected nodes must have the same frame range.");
	 
      }
      
      /* create dialog components */
      JComponent body = null;
      {
        Component comps[] = UIFactory.createTitledPanels();
        JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
        body = (JComponent) comps[2];

        UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

        {
  	tpanel.add(UIFactory.createFixedLabel("Frame Range:", sTSize, JLabel.RIGHT));
  	
  	{
  	  Box hbox = new Box(BoxLayout.X_AXIS);
  	  
  	  {
  	    JIntegerField field = 
  	      UIFactory.createIntegerField(null, sVSize1, JLabel.CENTER);
  	    pStartFrameField = field;
  	    
  	    hbox.add(field);
  	  }
  	  
  	  hbox.add(Box.createHorizontalGlue());
  	  hbox.add(Box.createRigidArea(new Dimension(8, 0)));
  	  
  	  {
  	    JLabel label = new JLabel("to");
  	    hbox.add(label);
  	  }
  	  
  	  hbox.add(Box.createRigidArea(new Dimension(8, 0)));
  	  hbox.add(Box.createHorizontalGlue());
  	  
  	  {
  	    JIntegerField field = 
  	      UIFactory.createIntegerField(null, sVSize1, JLabel.CENTER);
  	    pEndFrameField = field;
  	    
  	    hbox.add(field);
  	  }
  	  
  	  hbox.add(Box.createHorizontalGlue());
  	  hbox.add(Box.createRigidArea(new Dimension(8, 0)));
  	  
  	  {
  	    JLabel label = new JLabel("by");
  	    hbox.add(label);
  	  }
  	  
  	  hbox.add(Box.createRigidArea(new Dimension(8, 0)));
  	  hbox.add(Box.createHorizontalGlue());
  	  
  	  {
  	    JIntegerField field = 
  	      UIFactory.createIntegerField(null, sVSize1, JLabel.CENTER);
  	    pByFrameField = field;
  	    
  	    hbox.add(field);
  	  }
  	  
  	  Dimension size = new Dimension(sVSize+1, 19);
  	  hbox.setMinimumSize(size);
  	  hbox.setMaximumSize(size);
  	  hbox.setPreferredSize(size);
  	  
  	  vpanel.add(hbox);
  	}
        }
        
        UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

        {
  	JBooleanField field = 
  	  UIFactory.createTitledBooleanField(tpanel, "Remove Obsolete Files:", sTSize, 
  					     vpanel, sVSize);
  	pRemoveFilesField = field;
        }

        UIFactory.addVerticalGlue(tpanel, vpanel);
      }

      /* query the user */
      JToolDialog diag = new JToolDialog("Renumber:", body, "Continue");

      diag.setVisible(true);
      if(diag.wasConfirmed()) {
        /* collect new frame range info */
        {
  	{
  	  Integer frame = pStartFrameField.getValue();
  	  if(frame == null)
  	    throw new PipelineException
  	      ("Unable to renumber node with an unspecified start frame!");
  	  pStartFrame = frame;
  	}
  	
  	{
  	  Integer frame = pEndFrameField.getValue();
  	  if(frame == null)
  	    throw new PipelineException
  	      ("Unable to renumber node with an unspecified end frame!");
  	  pEndFrame = frame;
  	}
  	
  	if(pStartFrame > pEndFrame) {
  	  Integer tmp = pEndFrame;
  	  pEndFrame = pStartFrame;
  	  pStartFrame = tmp;
  	  
  	  pStartFrameField.setValue(pStartFrame);
  	  pEndFrameField.setValue(pEndFrame);
  	}
  	
  	{
  	  Integer frame = pByFrameField.getValue();
  	  if(frame == null)
  	    throw new PipelineException
  	      ("Unable to renumber node with an unspecified frame increment!");
  	  pByFrame = frame;
  	}
  	
  	pIsByEnabled = pByFrameField.isEnabled();
  	
  	{
  	  Boolean tf = pRemoveFilesField.getValue();
  	  pRemoveFiles = (tf != null) && tf;
  	}
        }
      }
      
      return " : you can count on me";
   }
   
   public synchronized boolean executePhase(MasterMgrClient mclient, QueueMgrClient qclient)
      throws PipelineException
   {
      /* validate the new frame range */
      {
	pTargetFrameRange = null;

	if(!pIsByEnabled
	    && ((((pOrigFrameRange.getStart() - pStartFrame) % pByFrame) != 0) || 
		(((pOrigFrameRange.getStart() - pEndFrame) % pByFrame) != 0)))
	  throw new PipelineException
	    ("Unable to renumber node due to misalignment of the new frame range ("
	     + pStartFrame + "-" + pEndFrame + "x" + pByFrame + ") with the original " + 
	     "frame range (" + pOrigFrameRange + ")!");

	try {
	  pTargetFrameRange = new FrameRange(pStartFrame, pEndFrame, pByFrame);
	} 
	catch (IllegalArgumentException ex) {
	  throw new PipelineException("Unable to renumber node. " + ex.getMessage());
	}

	if(pOrigFrameRange.equals(pTargetFrameRange))
	  throw new PipelineException
	    ("Unecessary to renumber node when the frame range (" +
	     pOrigFrameRange + " " + "has not been altered!");
      }

  
      for(NodeStatus stat: pSelected.values())
      {
	 NodeID id = stat.getNodeID();
	 mclient.renumber(id, pTargetFrameRange, pRemoveFiles);
      }
      return false;
   }
   
   private static final int sTSize  = 150;
   private static final int sVSize  = 300;
   private static final int sVSize1 = 60;
   
   /**
    * New frame range info.
    */ 
   private JIntegerField pStartFrameField;
   private int pStartFrame;

   private JIntegerField pEndFrameField;
   private int pEndFrame;

   private JIntegerField pByFrameField;
   private int pByFrame;
   private boolean pIsByEnabled;

   private JBooleanField pRemoveFilesField;
   private boolean pRemoveFiles;

   private FrameRange pOrigFrameRange = null;
   
   
   
   /**
    * New frame range.
    */
   private FrameRange pTargetFrameRange;
   private static final long serialVersionUID = -3930924991536008911L;
}
