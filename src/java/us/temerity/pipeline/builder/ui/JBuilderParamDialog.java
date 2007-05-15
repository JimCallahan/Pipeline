/**
 * 
 */
package us.temerity.pipeline.builder.ui;

import javax.swing.JButton;

import us.temerity.pipeline.builder.BaseBuilder;
import us.temerity.pipeline.ui.JTopLevelDialog;

/**
 *
 */
public 
class JBuilderParamDialog
  extends JTopLevelDialog
{
  /**
   * 
   */
  public 
  JBuilderParamDialog
  (
    BaseBuilder builder
  )
  {
    super("Builder Parameters");
    
    pBuilder = builder;
  }
  
  public void
  initUI()
  {
    String header = pBuilder.getName();
    String cancel = "quit";
    String[][] extras = new String[3][2];
    extras[0][0] = "Next";
    extras[0][1] = "next-pass";
    extras[1][0] = "Run Next";
    extras[1][1] = "run-next-pass";
    extras[2][0] = "Run All";
    extras[2][1] = "run-all-passes";
    
    JBuilderTopPanel topPanel = new JBuilderTopPanel(pBuilder);
    JButton buttons[] = super.initUI(pBuilder.getName(), topPanel, null, null, extras, cancel);
    pNextButton = buttons[0];
    pRunNextButton = buttons[1];
    pRunAllButton = buttons[2];
    
    this.validate();
    this.pack();
  }

  
  private JButton pNextButton;
  private JButton pRunNextButton;
  private JButton pRunAllButton;
  
  private BaseBuilder pBuilder;
  
  private static final long serialVersionUID = 1418949118885334316L;
}
