// $Id: InsertNodeTool.java,v 1.2 2009/09/16 15:56:45 jesse Exp $

package us.temerity.pipeline.plugin.InsertNodeTool.v2_4_5;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   I N S E R T   N O D E   T O O L                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * Inserts the node version contained in a JAR archive previously created by extract site 
 * version operation.
 */
public 
class InsertNodeTool
  extends BaseTool
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  InsertNodeTool()
  {
    super("InsertNode", new VersionID("2.4.5"), "Temerity",
          "Inserts the node version contained in a JAR archive previously created by " + 
          "extract site version operation.");
    
    addPhase(new PhaseOne());
    addPhase(new PhaseTwo());
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   P H A S E   O N E                                                                    */
  /*----------------------------------------------------------------------------------------*/
  private 
  class PhaseOne
    extends BaseTool.ToolPhase
  {
    public 
    PhaseOne() 
    {
      super();
    }
    
    /**
     * Select the JAR archive.
     * 
     * @return 
     *   The phase progress message or <CODE>null</CODE> to abort early.
     * 
     * @throws PipelineException
     *   If unable to validate the given user input.
     */
    @Override
    public String
    collectInput() 
      throws PipelineException 
    {
      JToolDialog toolDialog = new JToolDialog("Insert Node Version", null, "Continue");
      
      JFileSelectDialog dialog = 
        new JFileSelectDialog(toolDialog, "Select File", "Select JAR Archive:", 
                              "JAR File:", 40, "Select"); 

      dialog.updateTargetFile(new File("/"));
      dialog.setVisible(true);
      if(!dialog.wasConfirmed())
        return null;

      File file = dialog.getSelectedFile();
      if(file == null) 
        return null;
      pJarPath = new Path(file);
      
      return ": Looking up Node Version Information.";
    }
    
    /**
     * Collect information from the JAR archive.
     */
    @Override
    public NextPhase 
    execute
    (
      MasterMgrClient mclient,
      QueueMgrClient qclient
    )
      throws PipelineException
    {
      pNodeVersion = mclient.lookupSiteVersion(pJarPath);
      pIsInserted  = mclient.isSiteVersionInserted(pJarPath);
      pMissing     = mclient.getMissingSiteVersionRefs(pJarPath);
      
      return NextPhase.Continue;
    }
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   P H A S E   T W O                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private 
  class PhaseTwo
    extends BaseTool.ToolPhase
  {
    public 
    PhaseTwo() 
    {
      super();
    }
    
    /**
     * Display information about the node version from the JAR archive.
     * 
     * @return 
     *   The phase progress message or <CODE>null</CODE> to abort early.
     * 
     * @throws PipelineException
     *   If unable to validate the given user input.
     */
    @Override
    public String
    collectInput() 
      throws PipelineException 
    {
      FileSeq fseq = pNodeVersion.getPrimarySequence();
      FrameRange range = fseq.getFrameRange();
      FilePattern fpat = fseq.getFilePattern();

      /* Make an equivalent of the register dialog. */
      JToolDialog toolDialog = null;
      {
        Box body = null;
        {
          Component comps[] = UIFactory.createTitledPanels();
          JPanel tpanel = (JPanel) comps[0];
          JPanel vpanel = (JPanel) comps[1];
          body = (Box) comps[2];
          
          JTextField source = 
            UIFactory.createTitledTextField(tpanel, "Filename Prefix:", sTSize, 
                                            vpanel, pNodeVersion.getName(), sVSize);
          source.setEditable(false);
          
          UIFactory.addVerticalSpacer(tpanel, vpanel, 12);
          
          {
            tpanel.add(UIFactory.createFixedLabel("Frame Range:", sTSize, JLabel.RIGHT));

            {
              Box hbox = new Box(BoxLayout.X_AXIS);
              
              {
                Integer startFrame = null;
                if(range != null) 
                  startFrame = range.getStart();

                JIntegerField field = 
                  UIFactory.createIntegerField(startFrame, sVSize1, JLabel.CENTER);
                field.setEnabled(false);

                hbox.add(field);
              }
              
              hbox.add(Box.createHorizontalGlue());
              hbox.add(Box.createRigidArea(new Dimension(8, 0)));

              {
                JLabel label = new JLabel("to");
                label.setName("DisableLabel");

                hbox.add(label);
              }

              hbox.add(Box.createRigidArea(new Dimension(8, 0)));
              hbox.add(Box.createHorizontalGlue());

              {
                Integer endFrame = null;
                if(!fseq.isSingle() && (range != null)) 
                  endFrame = range.getEnd();

                JIntegerField field = 
                  UIFactory.createIntegerField(endFrame, sVSize1, JLabel.CENTER);
                field.setEnabled(false);

                hbox.add(field);
              }

              hbox.add(Box.createHorizontalGlue());
              hbox.add(Box.createRigidArea(new Dimension(8, 0)));

              {
                JLabel label = new JLabel("by");
                label.setName("DisableLabel");

                hbox.add(label);
              }

              hbox.add(Box.createRigidArea(new Dimension(8, 0)));
              hbox.add(Box.createHorizontalGlue());

              {
                Integer byFrame = null;
                if(!fseq.isSingle() && (range != null)) 
                  byFrame = range.getBy();

                JIntegerField field = 
                  UIFactory.createIntegerField(byFrame, sVSize1, JLabel.CENTER);
                field.setEnabled(false);                

                hbox.add(field);
              }
              
              Dimension size = new Dimension(sVSize+1, 19);
              hbox.setMinimumSize(size);
              hbox.setMaximumSize(size);
              hbox.setPreferredSize(size);

              vpanel.add(hbox);
            }
          }

          UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
          
          {
            Integer pad = null;
            if(!fseq.isSingle()) 
              pad = fpat.getPadding();
            JIntegerField field = 
              UIFactory.createTitledIntegerField
              (tpanel, "Frame Padding:", sTSize, 
               vpanel, pad, sVSize);
            field.setEnabled(false);
          }
            
          UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

          {
            JTextField field = 
              UIFactory.createTitledTextField
                (tpanel, "Filename Suffix:", sTSize, 
                 vpanel, fpat.getSuffix(), sVSize);
          }

          UIFactory.addVerticalSpacer(tpanel, vpanel, 12);

          {
            JTextField field = 
              UIFactory.createTitledTextField
                (tpanel, "Toolset:", sTSize, 
                 vpanel, pNodeVersion.getToolset(), sVSize);
          }

          UIFactory.addVerticalGlue(tpanel, vpanel);
        }
        
        toolDialog = new JToolDialog("Insert Node Version", body, "Insert");
      }
      
      toolDialog.setVisible(true);
      if(!toolDialog.wasConfirmed())
        return null;

      return (": Inserting the node: " + pNodeVersion.getName() + 
              " (v" + pNodeVersion.getVersionID() + ")."); 
    }
    
    @Override
    public NextPhase 
    execute
    (
      MasterMgrClient mclient,
      QueueMgrClient qclient
    )
      throws PipelineException
    {
      mclient.insertSiteVersion(pJarPath);
      
      return NextPhase.Finish;
    }
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 5989778606742200709L;

  private static final int sTSize  = 150;
  
  private static final int sVSize  = 300;
  private static final int sVSize1 = 80;
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The path to the JAR archive containing the node version.
   */ 
  private Path pJarPath; 
  
  /**
   * The node version.
   */ 
  private NodeVersion pNodeVersion; 

  /**
   * Whether the node version already has been inserted.
   */ 
  private boolean pIsInserted;

  /**
   * The names and revision numbers of the missing dependencies required to be inserted 
   * before this node can be inserted.
   */ 
  private TreeMap<String,VersionID>  pMissing; 

}
