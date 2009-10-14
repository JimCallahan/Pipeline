// $Id: JTemplateGlueDialog.java,v 1.1 2009/10/14 18:11:43 jesse Exp $

package us.temerity.pipeline.plugin.TemplateGlueTool.v2_4_12;

import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_12.*;
import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.glue.io.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   G L U E   D I A L O G                                                */
/*------------------------------------------------------------------------------------------*/

public 
class JTemplateGlueDialog
  extends JTopLevelDialog
  implements ActionListener
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T U C T O R                                                                  */
  /*----------------------------------------------------------------------------------------*/

  public
  JTemplateGlueDialog
  (
    String rootNode,
    TemplateGlueInformation oldSettings,
    TreeSet<String> nodesInTemplate,
    MasterMgrClient mclient,
    String author,
    String view, 
    File templateFile
  )
  {
    super("Template Glue Tool");
    
    pTemplateFile = templateFile;
    
    pTopPanel = new JTemplateGluePanel
      (this, rootNode, oldSettings, nodesInTemplate, mclient, author, view);
    
  }
  
  public void
  initUI()
  {
    String header = "Template Glue Tool";
    String cancel = "Cancel";
    String[][] extras = new String[2][2];
    extras[0][0] = "Previous";
    extras[0][1] = "previous";
    extras[1][0] = "Next";
    extras[1][1] = "next";

    JButton buttons[] = super.initUI(header, pTopPanel, null, null, extras, cancel);
    pPreviousButton = buttons[0];
    pNextButton= buttons[1];
    

    this.validate();
    this.pack();
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
    if(cmd.equals("next")) {
      try {
        pTopPanel.doNext();
      }
      catch (PipelineException ex) {
        ex.printStackTrace();
      }
    }
    else if(cmd.equals("previous")) 
      pTopPanel.goBack();
    else if(cmd.equals("cancel"))
      setVisible(false);
  }  
  
  public void
  disableButtons()
  {
    pPreviousButton.setEnabled(false);
    pNextButton.setEnabled(false);
    pCancelButton.setEnabled(false);
  }
  
  public void
  enableButtons()
  {
    pPreviousButton.setEnabled(true);
    pNextButton.setEnabled(true);
    pCancelButton.setEnabled(true);
  }
  
  public void
  doFinal()
  {
    if (isFinished()) {
      try {
        TemplateGlueInformation newSettings = getNewSettings();
        pTemplateFile.delete();
        GlueEncoderImpl.encodeFile(aTemplateGlueInfo, newSettings, pTemplateFile);
      }
      catch (GlueException ex) 
      {
        String error = Exceptions.getFullMessage(ex);
        JErrorDialog dialog = new JErrorDialog(this);
        dialog.setMessage("Error writing the Glue File", error);
        dialog.setVisible(true);
      }
    }
    doCancel();
  }
  
  public boolean
  isFinished()
  {
    return pTopPanel.isFinished();
  }
  
  public TemplateGlueInformation
  getNewSettings()
  {
    return pTopPanel.getNewSettings();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 4710622904879640578L;
  
  public static final String aTemplateGlueInfo = "TemplateGlueInfo";

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  private JButton pPreviousButton;
  private JButton pNextButton;
  
  private File pTemplateFile;
  
  private JTemplateGluePanel pTopPanel;
}
