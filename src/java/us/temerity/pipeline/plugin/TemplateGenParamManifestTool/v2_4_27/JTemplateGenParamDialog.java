package us.temerity.pipeline.plugin.TemplateGenParamManifestTool.v2_4_27;

import java.awt.event.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.builder.v2_4_12.*;
import us.temerity.pipeline.ui.*;


/*------------------------------------------------------------------------------------------*/
/*   T E M P L A T E   G E N   P A R A M   D I A L O G                                      */
/*------------------------------------------------------------------------------------------*/

public 
class JTemplateGenParamDialog
  extends JTopLevelDialog
  implements ActionListener
{

  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T U C T O R                                                                  */
  /*----------------------------------------------------------------------------------------*/

  public
  JTemplateGenParamDialog
  (
    TemplateGlueInformation glueInfo,
    TemplateParamManifest oldManifest
  )
  {
    super("Template Gen Param Manifest Tool");
    
    pTopPanel = new JTemplateGenParamPanel(this, glueInfo, oldManifest);
    
  }

  
  public void
  initUI()
  {
    String header = "Template Gen Param Manifest Tool";
    String cancel = "Cancel";
    String[][] extras = new String[2][2];
    extras[0][0] = "Previous";
    extras[0][1] = "previous";
    extras[1][0] = "Next";
    extras[1][1] = "next";

    JButton buttons[] = super.initUI(header, pTopPanel, null, null, extras, cancel, null);
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
  
  public boolean
  isFinished()
  {
    return pTopPanel.isFinished();
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -905919575853440923L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private JButton pPreviousButton;
  private JButton pNextButton;
  
  private JTemplateGenParamPanel pTopPanel;
}
