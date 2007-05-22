package us.temerity.pipeline.builder.ui;

import java.awt.CardLayout;

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
      Box logBox = new Box(BoxLayout.X_AXIS);
      
      pLogArea = new JTextArea(4, 0);
      pLogArea.setWrapStyleWord(true);
      pLogArea.setEditable(false);
      pLogArea.setLineWrap(true);
      logBox.add(pLogArea);
      JScrollPane scroll = new JScrollPane(logBox);
      LogMgr.getInstance().logToTextArea(pLogArea);
      jSecondSplitPane.setBottomComponent(scroll);
    }
    {
      pTreeCardPanel = new JPanel();
      pTreeCardLayout = new CardLayout();
      pTreeCardPanel.setLayout(pTreeCardLayout);
      
      jSplitPane.setLeftComponent(pTreeCardPanel);
    }
    {
      Box hBox = new Box(BoxLayout.X_AXIS);
      {
	pFirstPassPanel = new JPanel();
	pFirstPassLayouts = new CardLayout();
        pFirstPassPanel.setLayout(pFirstPassLayouts);
        pFirstPassPanel.setAlignmentX(LEFT_ALIGNMENT);
        hBox.add(pFirstPassPanel);
        hBox.add(UIFactory.createFiller(5));
        hBox.setAlignmentX(LEFT_ALIGNMENT);
      }
      
      JScrollPane scroll = new JScrollPane(hBox);
      scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      jSecondSplitPane.setTopComponent(scroll);
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
  
  public void
  setLeftSplitDivider
  (
    double percentage 
  )
  {
    jSecondSplitPane.setDividerLocation(percentage);
  }
  
  private BaseBuilder pBuilder;
  private JPanel pTreeCardPanel;
  private JPanel pFirstPassPanel;
  private JTextArea pLogArea;
  private JSplitPane jSplitPane;
  private JSplitPane jSecondSplitPane;
  
  private CardLayout pTreeCardLayout;
  
  private JFancyTree pTree;
  
  private CardLayout pFirstPassLayouts;
  
  private JPanel pFirstPasses;
  private JPanel pSecondPasses;
  
  private static final long serialVersionUID = 634240817965602649L;

}
