// $Id: NodeNoteTool.java,v 1.1 2007/11/03 22:07:36 jesse Exp $

package us.temerity.pipeline.plugin.NodeNoteTool.v2_3_14;

import java.awt.Component;
import java.awt.Dimension;
import java.util.TreeMap;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   N O T E   T O O L                                                            */
/*------------------------------------------------------------------------------------------*/

public 
class NodeNoteTool
  extends BaseTool
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public 
  NodeNoteTool()
  {
    super("NodeNote", new VersionID("2.3.14"), "Temerity",
	  "Views or edits the notes on a node.");
    
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
    
    addPhase(new FirstPhase());
    addPhase(new SecondPhase());
  }

  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  @Override
  public boolean 
  updateOnExit()
  {
    return false;
  }
  
  /*----------------------------------------------------------------------------------------*/
  /*   P H A S E S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  private 
  class FirstPhase
    extends ToolPhase
  {
    @Override
    public String 
    collectInput()
      throws PipelineException
    {
      if (pPrimary == null || pSelected.size() > 1) 
	throw new PipelineException("Please only select one node to edit notes.");

      return " : Finding Note";
    }

    @Override
    public NextPhase 
    execute
    (
      MasterMgrClient mclient,
      @SuppressWarnings("unused")
      QueueMgrClient qclient
    )
      throws PipelineException
    {
      TreeMap<String, BaseAnnotation> annots = mclient.getAnnotations(pPrimary);
      pAnnotation = null;
      for (BaseAnnotation an : annots.values()) {
	if (an.getName().equals("NodeNote")) {
	  pAnnotation = an;
	  break;
	}
      }
      if (pAnnotation == null) {
	pAnnotation = PluginMgrClient.getInstance().
	  newAnnotation(aNodeNote, new VersionID("2.3.14"), "Temerity");
	mclient.addAnnotation(pPrimary, pAnnotation.getName(), pAnnotation);
      }
      return NextPhase.Continue;
    }
  }
  
  private 
  class SecondPhase
    extends ToolPhase
  {
    @SuppressWarnings("unused")
    @Override
    public String 
    collectInput()
      throws PipelineException
    {
      Box vbox = new Box(BoxLayout.Y_AXIS);
      TextAreaAnnotationParam param = 
	(TextAreaAnnotationParam) pAnnotation.getParam(aNodeNote);
      {
	Component comps[] = UIFactory.createTitledPanels();
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];
	
	pTextArea = 
	  UIFactory.createTitledEditableTextArea
	  (tpanel, "Node Note", sTSize, vpanel, 
	   param.getStringValue(), sVSize2, param.getRows(), 
	   true, "The node note");
	
	JDrawer draw = new JDrawer("Node Notes", (JComponent) comps[2], true);
	vbox.add(draw);
	vbox.add(UIFactory.createFiller(sTSize+sVSize2));
      }
      
      
      
      JScrollPane scroll = new JScrollPane(vbox);
      Dimension size = new Dimension(sTSize + sVSize2 + 52, 300);
      scroll.setMinimumSize(size);
      scroll.setPreferredSize(size);

      scroll.getViewport().setScrollMode(JViewport.BACKINGSTORE_SCROLL_MODE);
      JToolDialog diag = new JToolDialog("Node Notes", scroll, "Confirm");

      diag.setVisible(true);

      if (diag.wasConfirmed()) 
	return " : Editing Note";

      return null;
    }
    
    @Override
    public NextPhase 
    execute
    (
      MasterMgrClient mclient,
      @SuppressWarnings("unused")
      QueueMgrClient qclient
    )
      throws PipelineException
    {
      String value = pTextArea.getText();
      pAnnotation.setParamValue(aNodeNote, value);
      mclient.addAnnotation(pPrimary, pAnnotation.getName(), pAnnotation);
      return NextPhase.Finish;
    }
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2173660970735599054L;

  public static final String aNodeNote = "NodeNote";

  private static final int sTSize = 120;
  private static final int sVSize2 = 400;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  private BaseAnnotation pAnnotation;
  private JTextArea pTextArea;
}
