package us.temerity.pipeline.builder.ui;

import java.awt.CardLayout;

import javax.swing.*;

import us.temerity.pipeline.LogMgr;
import us.temerity.pipeline.builder.BaseBuilder;


public 
class JBuilderTopPanel
  extends JPanel
{
  
  public JBuilderTopPanel
  (
    BaseBuilder builder
  )
  {
    super();
    pBuilder = builder;
    
    jSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, false);
    jSplitPane.setDividerLocation(100);
    
    {
      pLogPanel = new JPanel();
      Box logBox = new Box(BoxLayout.X_AXIS);
      pLogArea = new JTextArea(8, 0);
      LogMgr.getInstance().logToTextArea(pLogArea);
      jSplitPane.setTopComponent(logBox);
    }
    {
      JPanel cardPanel = new JPanel();
      pCardLayout = new CardLayout();
      cardPanel.setLayout(pCardLayout);
      
      jSplitPane.setBottomComponent(cardPanel);
    }
  }
  
  private BaseBuilder pBuilder;
  private JPanel pLogPanel;
  private JTextArea pLogArea;
  private JSplitPane jSplitPane;
  
  private CardLayout pCardLayout;
  
  private CardLayout pFirstPassLayouts;
  
  private JPanel pFirstPasses;
  private JPanel pSecondPasses;
}
