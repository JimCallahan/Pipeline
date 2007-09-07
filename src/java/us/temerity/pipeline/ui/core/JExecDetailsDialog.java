// $Id: JExecDetailsDialog.java,v 1.6 2007/09/07 18:52:38 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.*;
import us.temerity.pipeline.ui.*; 

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.concurrent.atomic.*;
import javax.swing.*;
import javax.swing.event.*;

/*------------------------------------------------------------------------------------------*/
/*   E X E C   D E T A I L S   D I A L O G                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays the command-line, working directory and environment used to execute a job.
 */ 
public  
class JExecDetailsDialog
  extends JTopLevelDialog
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Construct a new dialog.
   * 
   */ 
  public 
  JExecDetailsDialog()
  {
    super("Execution Details");

    /* create dialog body components */ 
    {
      JPanel body = new JPanel();
      body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
      
      {
	JPanel panel = new JPanel();
	panel.setName("MainDialogPanel");
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

	/* working directory */ 
	{
	  panel.add(UIFactory.createPanelLabel("Working Directory:"));
	  
	  panel.add(Box.createRigidArea(new Dimension(0, 4)));
	  
	  JTextField field = UIFactory.createTextField(null, 100, JLabel.LEFT);
	  pWorkingDirField = field;
	  
	  panel.add(field);
	}

	body.add(panel);
      }

      {
	JPanel panel = new JPanel();
	panel.setName("HorizontalBar");

	Dimension size = new Dimension(100, 7);       
	panel.setPreferredSize(size);
	panel.setMinimumSize(size);
	panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 7));
	
	body.add(panel);
      }

      /* command line */ 
      JPanel above = new JPanel();
      {
	above.setName("MainDialogPanel");
	above.setLayout(new BoxLayout(above, BoxLayout.Y_AXIS));

	{
	  Box hbox = new Box(BoxLayout.X_AXIS);
	  
	  hbox.add(Box.createRigidArea(new Dimension(4, 0)));
	  
	  {
	    JLabel label = new JLabel("X");
	    pCommandLineLabel = label;
	    
	    label.setName("PanelLabel");
	    
	    hbox.add(label);
	  }
	  
	  hbox.add(Box.createHorizontalGlue());
	  
	  above.add(hbox);
	}
	
	above.add(Box.createRigidArea(new Dimension(0, 4)));
	
	{
	  JTextArea area = new JTextArea(null, 5, 70);
	  pCommandLineArea = area; 
	  
	  area.setName("CodeTextArea");
	  area.setLineWrap(true);
	  area.setWrapStyleWord(true);
	  area.setEditable(false);
	}
	
	{
	  JScrollPane scroll = 
            UIFactory.createScrollPane
            (pCommandLineArea, 
             ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER, 
             ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
             new Dimension(100, 27), null, null);

	  above.add(scroll);
	}
      }
        
      /* environment */ 
      JPanel below = new JPanel();
      {
	below.setName("MainDialogPanel");
	below.setLayout(new BoxLayout(below, BoxLayout.Y_AXIS));

	{
	  Box hbox = new Box(BoxLayout.X_AXIS);
	  
	  hbox.add(Box.createRigidArea(new Dimension(4, 0)));
	  
	  {
	    JLabel label = new JLabel("X");
	    pEnvLabel = label;

	    label.setName("PanelLabel");

	    hbox.add(label);
	  }
	  
	  hbox.add(Box.createHorizontalGlue());
	  
	  below.add(hbox);
	}

	below.add(Box.createRigidArea(new Dimension(0, 4)));

	Component comps[] = UIFactory.createTitledPanels();
	{
	  JPanel tpanel = (JPanel) comps[0];
	  JPanel vpanel = (JPanel) comps[1];
	
	  tpanel.add(Box.createRigidArea(new Dimension(sTSize, 0)));
	  vpanel.add(Box.createHorizontalGlue());
	}

	{
          pEnvScroll = 
            UIFactory.createScrollPane
            (comps[2], 
             ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER, 
             ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, 
             new Dimension(100, 50), new Dimension(100, 300), null);
          
	  below.add(pEnvScroll);
	}
      }

      {
	JVertSplitPanel split = new JVertSplitPanel(above, below);
	split.setResizeWeight(0.0);
	split.setAlignmentX(0.5f);

	body.add(split);
      }
               
      super.initUI("X", body, null, null, null, "Close");
    }
  }
  
  

  /*----------------------------------------------------------------------------------------*/
  /*   U S E R   I N T E R F A C E                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Update the dialog contents.
   * 
   * @param jheader
   *   The job portion of the dialog header.
   * 
   * @param job
   *   The queue job.
   * 
   * @param info
   *   The current job status information. 
   */ 
  public void 
  updateContents
  (
   String jheader,
   QueueJob job,
   QueueJobInfo info, 
   SubProcessExecDetails details
  ) 
  {
    ActionAgenda agenda = job.getActionAgenda();
    QueueJobResults results = info.getResults();
    
    String dir = "-";
    if((agenda != null) && (info.getOsType() != null))
      dir = agenda.getTargetPath(info.getOsType()).toString();
    
    String hostname = "";
    if(info.getHostname() != null)
      hostname = ("    [" + info.getHostname() + "]");

    String command = "-";
    if(details != null) 
      command = details.getCommand();

    TreeMap<String,String> env = new TreeMap<String,String>();
    if(details != null) 
      env = details.getEnvironment();
    
    

    pHeaderLabel.setText("Execution Details -" + jheader + hostname);
 
    pWorkingDirField.setText(dir);
     
    BaseAction action = job.getAction();
    pCommandLineLabel.setText("Action Command:  " + 
			      action.getName() + " (v" + action.getVersionID() + ")");
    pCommandLineArea.setText(command);
      
    {
      Component comps[] = UIFactory.createTitledPanels();
      {
	JPanel tpanel = (JPanel) comps[0];
	JPanel vpanel = (JPanel) comps[1];
	
	if(!env.isEmpty()) {
	  String last = env.lastKey();
	  for(String key : env.keySet()) {
	    String value = env.get(key);
	      
	    JTextField field = 
	      UIFactory.createTitledTextField(tpanel, key + ":", sTSize, 
					     vpanel, value, sVSize);
	    field.setHorizontalAlignment(JLabel.LEFT);
	    
	    if(!key.equals(last))
	      UIFactory.addVerticalSpacer(tpanel, vpanel, 3);
	  }
	}
	else {
	  tpanel.add(Box.createRigidArea(new Dimension(sTSize, 0)));
	  vpanel.add(Box.createHorizontalGlue());
	}
      }

      pEnvLabel.setText("Toolset Environment:  " + agenda.getToolset());
      pEnvScroll.setViewportView(comps[2]);
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -1000045463020428620L;

  private static final int  sTSize = 200;
  private static final int  sVSize = 400;


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The command line header.
   */ 
  private JLabel pCommandLineLabel; 
  
  /**
   * The command line text area.
   */ 
  private JTextArea pCommandLineArea; 
  

  /**
   * The working directory field.
   */ 
  private JTextField pWorkingDirField; 


  /**
   * The environment header.
   */ 
  private JLabel pEnvLabel; 
  
  /**
   * The environment scroll pane. 
   */ 
  private JScrollPane  pEnvScroll; 

}
