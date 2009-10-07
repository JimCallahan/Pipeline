// $Id: JQueueJobsDialog.java,v 1.13 2009/10/07 08:09:50 jim Exp $

package us.temerity.pipeline.ui.core;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   Q U E U E   J O B S   D I A L O G                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * The job submission parameters dialog.
 */ 
public 
class JQueueJobsDialog
  extends JBaseJobReqsDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent frame.
   */ 
  public 
  JQueueJobsDialog
  (
   Frame parent
  )
  {
    super(parent, "Queue Jobs Special", true); 

    updateFrameRanges(new TreeMap<String,FrameRange>(), new MappedSet<String,Integer>());
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Overridden by subclasses to create additional UI components shown above the job
   * requirements drawer.
   * 
   * @param vbox
   *   The parent box.
   */ 
  protected void
  createExtraComponents
  (
   Box vbox
  ) 
  {
    Component comps[] = UIFactory.createTitledPanels();
    {
      JPanel tpanel = (JPanel) comps[0];
      JPanel vpanel = (JPanel) comps[1];

      {
        {
          JBooleanField field = 
            UIFactory.createTitledBooleanField(tpanel, "Override Frames:", sTSize,
                                               vpanel, sVSize);
          pOverrideFramesField = field;
          
          field.addActionListener(this);
          field.setActionCommand("frames-changed");
        }
	    
        UIFactory.addVerticalSpacer(tpanel, vpanel, 3);

        {
          JTextField field = 
            UIFactory.createTitledEditableTextField
             (tpanel, "Frame Ranges:", sTSize,
              vpanel, null, sVSize, 
              "The frames to regenerate specified by one or more single frames or frame " + 
              "ranges separated by spaces.  For example, \"2 5 10-20x1 26-34x2\"."); 

          pFramesField = field;
          
          field.addActionListener(this);
          field.setActionCommand("frames-edited"); 
        }
      }
    }
    
    pFramesDrawer = new JDrawer("Target Files:", (JComponent) comps[2], true);
    vbox.add(pFramesDrawer);

    pFramesBox = vbox;
  }

  /**
   * Update the target frame ranges for the job in question.
   * 
   * @param wholeRange
   *   The entire target frame range indexed by fully resolved node name.  Entry values can be
   *   <CODE>null</CODE> for single frame nodes.
   * 
   * @param targetIndices
   *   The indices into the target frame range of files to regenerate for each node.   
   *   Value can be <CODE>null</CODE> for all frames. 
   */
  public void 
  updateFrameRanges
  ( 
   TreeMap<String,FrameRange> wholeRanges, 
   MappedSet<String,Integer> targetIndices
  ) 
  {
    pWholeRanges = wholeRanges;

    boolean enabled = false;
    if((wholeRanges == null) && (targetIndices == null)) {
      pTargetIndices     = null;
      pOverriddenIndices = null;
    }
    else if((wholeRanges != null) && (targetIndices != null)) {
      pOverrideFramesField.removeActionListener(this);
        pOverrideFramesField.setValue(false); 
      pOverrideFramesField.addActionListener(this);
      
      pFramesField.setEnabled(false); 
      pFramesField.removeActionListener(this);
      {
        FrameRange range = null; 
        TreeSet<Integer> indices = null;
        if(pWholeRanges.size() == 1) {
          Map.Entry<String,FrameRange> entry = pWholeRanges.firstEntry();
          range = entry.getValue();
          indices = targetIndices.get(entry.getKey()); 
        }
        pFramesField.setText(indicesToRangesString(range, indices));
      }
      pFramesField.addActionListener(this);
      
      pTargetIndices     = new MappedSet<String,Integer>();
      pOverriddenIndices = new MappedSet<String,Integer>();
      for(Map.Entry<String,FrameRange> entry : pWholeRanges.entrySet()) {
        String name = entry.getKey();
        FrameRange range = entry.getValue();
        if(range != null) {
          TreeSet<Integer> indices = targetIndices.get(name); 
          pTargetIndices.put(name, indices); 
          pOverriddenIndices.put(name, indices); 
          enabled = true; 
        }
        else {
          pTargetIndices.put(name, (TreeSet<Integer>) null); 
          pOverriddenIndices.put(name, (TreeSet<Integer>) null); 
        }
      }
    }
    
    pOverrideFramesField.setEnabled(enabled);
    pFramesDrawer.setVisible(enabled);
    pFramesBox.revalidate(); 
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
 
  /**
   * Whether to override the target frame indices.
   */ 
  public boolean 
  overrideTargetIndices() 
  {
    return pOverrideFramesField.getValue();
  }
  
  /**
   * The overridden target frame indices into the whole target frame range of files to 
   * regenerate for each node.  Value can be <CODE>null</CODE> for all frames. 
   */ 
  public MappedSet<String,Integer>  
  getTargetIndices()
  {
    return pOverriddenIndices; 
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   L I S T E N E R S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /*-- ACTION LISTENER METHODS -------------------------------------------------------------*/

  /** 
   * Invoked when an action occurs. 
   */ 
  @Override
  public void 
  actionPerformed
  (
   ActionEvent e
  ) 
  {
    String cmd = e.getActionCommand();
    if(cmd.equals("frames-changed")) 
      doFramesChanged();
    else if(cmd.equals("frames-edited"))
      doFramesEdited();
    else 
      super.actionPerformed(e);
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Reset the fields to default values. 
   */ 
  @Override
  public void 
  doApply()
  { 
    pOverrideFramesField.setValue(false);
    super.doApply();
  }

  /**
   * If the frame ranges specified are valid, apply changes and close.
   */ 
  @Override
  public void 
  doConfirm()
  {
    try {
      validateFrames();
      super.doConfirm();
    }
    catch(PipelineException ex) {
      showErrorDialog("Illegal Frame Range:", ex.getMessage()); 
    }
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * The value of the override frames field has changed.
   */ 
  public void 
  doFramesChanged()
  {
    if(pOverrideFramesField.getValue()) {
      pFramesField.setEnabled(true);
    }
    else {
      pFramesField.setEnabled(false);

      pFramesField.removeActionListener(this);
      {
        FrameRange range = null; 
        TreeSet<Integer> indices = null;
        if((pWholeRanges != null) && (pWholeRanges.size() == 1)) {
          Map.Entry<String,FrameRange> entry = pWholeRanges.firstEntry();
          range = entry.getValue();
          indices = pTargetIndices.get(entry.getKey()); 
        }
        pFramesField.setText(indicesToRangesString(range, indices));
      }
      pFramesField.addActionListener(this);
    }
  }
  
  /**
   * The value of the frames field has been edited.
   */ 
  public void 
  doFramesEdited()
  { 
    try {
      validateFrames();
    }
    catch(PipelineException ex) {
      showErrorDialog("Illegal Frame Range:", ex.getMessage()); 
    }
  }

  /**
   * Validate the contents of the frames field and store the resulting indices. 
   */ 
  private void 
  validateFrames()
    throws PipelineException 
  {
    if(pWholeRanges != null) {
      pOverriddenIndices = pTargetIndices; 
      if(pOverrideFramesField.getValue()) 
        pOverriddenIndices = rangesStringToIndices(pFramesField.getText()); 
    }
    else {
      pOverriddenIndices = null;
    }
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Convert a set of target indices into series of space seperated string representations
   * of single frames and/or frame ranges.
   *
   * @param targetIndices
   *   The indexes into the target frame range of files to regenerate or 
   *   <CODE>null</CODE> for all frames. 
   */ 
  private String
  indicesToRangesString
  (
   FrameRange wholeRange,
   TreeSet<Integer> indices
  )
  {
    if(wholeRange != null) {
      ArrayList<FrameRange> ranges = new ArrayList<FrameRange>();
      if(indices != null) {
        Integer start = null;
        Integer by    = null;
        Integer prev  = null;
        for(int idx : indices) {
          try {
            int frame = wholeRange.indexToFrame(idx); 
            if(start == null) {
              start = frame;
            }
            else if(by == null) {
              by = frame - prev;
            }
            else if(by != (frame - prev)) {
              if(by > 1) {
                ranges.add(new FrameRange(start));
                start = prev;
                by    = frame - prev;
              }
              else {
                ranges.add(new FrameRange(start, prev, by)); 
                start = frame;
                by    = null;
              }

            }

            prev = frame;
          }
          catch(IllegalArgumentException ex) {
            /* just ignore invalid indices */ 
          }
        }

        if(by == null) 
          ranges.add(new FrameRange(start)); 
        else if(by > 1) {
            ranges.add(new FrameRange(start));
            ranges.add(new FrameRange(prev));
        }
        else {
          ranges.add(new FrameRange(start, prev, by)); 
        }
      }
      else {
        ranges.add(wholeRange); 
      }

      StringBuilder buf = new StringBuilder(); 
      for(FrameRange range : ranges) 
        buf.append(range.toString() + " ");
     
      return buf.toString();
    }
    
    return null;
  }

  /**
   * Convert a space seperated string representations of single frames and/or frame ranges
   * into a set of target indices for each node.
   * 
   * @param str
   *   The string representation.
   * 
   * @throws PipelineException
   *   If the string is invalid.
   */ 
  private MappedSet<String,Integer> 
  rangesStringToIndices
  (
   String str
  ) 
    throws PipelineException 
  {
    if(str == null) 
      throw new PipelineException
        ("The target frames where overridden but no frame ranges where specified!"); 

    TreeSet<Integer> frames = new TreeSet<Integer>(); 
    for(String part : str.split("\\p{Blank}")) {
      if(part.length() > 0) {
        try {
          FrameRange range = FrameRange.fromString(part);
          for(int idx : range.getFrameNumbers()) 
            frames.add(idx); 
        }
        catch(IllegalArgumentException ex) {
          throw new PipelineException(ex.getMessage());
        }
      }
    }

    MappedSet<String,Integer> indices = new MappedSet<String,Integer>();
    if(!frames.isEmpty()) {
      for(int frame : frames) {
        for(Map.Entry<String,FrameRange> entry : pWholeRanges.entrySet()) {
          String name = entry.getKey();
          FrameRange range = entry.getValue();
          if(range == null) {
            indices.put(name, (TreeSet<Integer>) null); 
          }
          else {
            try {
              int idx = range.frameToIndex(frame); 
              indices.put(name, idx); 
            }
            catch(IllegalArgumentException ex) {
              throw new PipelineException
                ("The given frame (" + frame + ") from the overridden Frame Ranges " + 
                 "(" + str + ") was not valid for node (" + name + ") which has a primary " + 
                 "sequence frame range of (" + range + ")!"); 
            }
          }
        }
      }
    }
    else {
      for(String name : pWholeRanges.keySet()) 
        indices.put(name, (TreeSet<Integer>) null); 
    }

    return indices;
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -8893740941287147895L;

  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The entire target frame range indexed by fully resolved node name. Value can be
   * <CODE>null</CODE> for single frame nodes.
   */ 
  private TreeMap<String,FrameRange>  pWholeRanges; 

  /**
   * The initial indices into the whole target frame range of files to regenerate for each 
   * node.  Value can be <CODE>null</CODE> for all frames. 
   */ 
  private MappedSet<String,Integer>  pTargetIndices; 
  
  /**
   * The overriden indices into the whole target frame range of files to regenerate for 
   * each node.  Value can be <CODE>null</CODE> for all frames. 
   */ 
  private MappedSet<String,Integer>  pOverriddenIndices; 
  
  

  /*----------------------------------------------------------------------------------------*/

  /**
   * The parent containiner of the frames drawer.
   */ 
  private  Box  pFramesBox;

  /**
   * The drawer holding the frame range related components.
   */ 
  private  JDrawer  pFramesDrawer;

  /** 
   * Whether to override the target frames.
   */ 
  private JBooleanField  pOverrideFramesField;
  
  /**
   * The overridden frame ranges.
   */ 
  private JTextField  pFramesField;
  

  
  
}
