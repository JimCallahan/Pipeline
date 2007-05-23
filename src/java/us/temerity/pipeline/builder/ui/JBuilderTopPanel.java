package us.temerity.pipeline.builder.ui;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import us.temerity.pipeline.LogMgr;
import us.temerity.pipeline.PipelineException;
import us.temerity.pipeline.builder.BaseBuilder;
import us.temerity.pipeline.builder.BaseBuilder.SetupPass;
import us.temerity.pipeline.builder.HasBuilderParams.PrefixedName;
import us.temerity.pipeline.ui.*;


public 
class JBuilderTopPanel
  extends JPanel
  implements ComponentListener
{
  
  public JBuilderTopPanel
  (
    BaseBuilder builder
  ) 
    throws PipelineException
  {
    super();
    pBuilder = builder;
    
    BoxLayout layout = new BoxLayout(this, BoxLayout.X_AXIS);
    this.setLayout(layout);
    
    jSplitPane = new JHorzSplitPanel();
    jSplitPane.setDividerLocation(100);
    
    jSecondSplitPane = new JVertSplitPanel();
    jSecondSplitPane.setDividerLocation(.5);
    jSecondSplitPane.setResizeWeight(.5);
    
    {
      {
	pLogArea = new JTextArea(0, 0);
	pLogArea.setWrapStyleWord(true);
	pLogArea.setEditable(false);
	pLogArea.setLineWrap(true);
	LogMgr.getInstance().logToTextArea(pLogArea);
      }
      JScrollPane scroll = new JScrollPane(pLogArea);
      scroll.addComponentListener(this);
      jSecondSplitPane.setBottomComponent(scroll);
    }
    {
      pTreeCardPanel = new JPanel();
      pTreeCardLayout = new CardLayout();
      pTreeCardPanel.setLayout(pTreeCardLayout);
      
      jSplitPane.setLeftComponent(pTreeCardPanel);
    }
    {
      pFirstPassPanel = new JPanel();
      pFirstPassLayouts = new CardLayout();
      pFirstPassPanel.setLayout(pFirstPassLayouts);
      pFirstPassPanel.setAlignmentX(LEFT_ALIGNMENT);
      pFirstPassPanel.setAlignmentY(TOP_ALIGNMENT);
    }
    {  
      jSecondSplitPane.setTopComponent(pFirstPassPanel);
    }
    {
      jSplitPane.setRightComponent(jSecondSplitPane);
    }
    {
      BaseBuilder theBuilder = pBuilder.getCurrentBuilder();
      SetupPass pass = pBuilder.getCurrentSetupPass();
      PrefixedName prefixName = new PrefixedName(theBuilder.getPrefixedName(), pass.getName());
      int passNum = theBuilder.getCurrentPass();
      
      JBuilderParamPanel paramPanel = new JBuilderParamPanel(theBuilder, passNum);
      pFirstPassPanel.add(paramPanel, prefixName.toString());
      pFirstPassLayouts.show(pFirstPassPanel, prefixName.toString());
    }
    this.add(jSplitPane);
  }
  
  public void
  addNextSetupPass() 
    throws PipelineException
  {
    BaseBuilder theBuilder = pBuilder.getCurrentBuilder();
    SetupPass pass = pBuilder.getCurrentSetupPass();
    PrefixedName prefixName = new PrefixedName(theBuilder.getPrefixedName(), pass.getName());
    int passNum = theBuilder.getCurrentPass();
    JBuilderParamPanel paramPanel = new JBuilderParamPanel(theBuilder, passNum);
    pFirstPassPanel.add(paramPanel, prefixName.toString());
    pFirstPassLayouts.show(pFirstPassPanel, prefixName.toString());
  }
  
  public JBuilderParamPanel
  getCurrentBuilderParamPanel()
  {
    return (JBuilderParamPanel) pFirstPassPanel.getComponent(0);
  }
  
  public void 
  listPanels()
  {
    for (Component c : pFirstPassPanel.getComponents())
    {
      System.out.println(c);
    }
  }
  
  public void
  setLeftSplitDivider
  (
    double percentage 
  )
  {
    jSecondSplitPane.setDividerLocation(percentage);
  }
  
  public void 
  componentHidden
  (
    ComponentEvent e
  )
  {}

  public void 
  componentMoved
  (
    ComponentEvent e
  )
  {}

  public void 
  componentResized
  (
    ComponentEvent e
  )
  {
    Component comp = e.getComponent();
    JScrollPane scroll = (JScrollPane) comp;
    Dimension size = scroll.getSize();
    Dimension logDim = pLogArea.getSize();
    Dimension newSize = new Dimension(size.width - 25, logDim.height);
    pLogArea.setSize(newSize);
    scroll.validate();
  }
  
  public void 
  componentShown
  (
    ComponentEvent e
  )
  {}

  
  private BaseBuilder pBuilder;
  private JPanel pTreeCardPanel;
  private JPanel pFirstPassPanel;
  private JTextArea pLogArea;
  private JSplitPane jSplitPane;
  private JSplitPane jSecondSplitPane;
  
  private CardLayout pTreeCardLayout;
  
  private JFancyTree pTree;
  
  private CardLayout pFirstPassLayouts;
  
  private static final long serialVersionUID = 634240817965602649L;
}
