package us.temerity.pipeline.ui;

import java.awt.*;
import java.io.*;

import javax.swing.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

/*------------------------------------------------------------------------------------------*/
/*   E R R O R   P R E F   D I A L O G                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Displays Pipeline error messages and provides a boolean flag to never show the dialog
 * again.
 */ 
public 
class JErrorPrefDialog
  extends JBaseDialog
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
  JErrorPrefDialog
  (
   Frame owner
  )  
  {
    super(owner, "Error");
    initUI();
  }

  /**
   * Construct a new dialog.
   * 
   * @param owner
   *   The parent dialog.
   */ 
  public 
  JErrorPrefDialog
  (
   Dialog owner
  )  
  {
    super(owner, "Error");
    initUI();
  }


  /*----------------------------------------------------------------------------------------*/

  public void 
  initUI() 
  {
    /* create dialog body components */ 
    {
      JPanel body = new JPanel();
      body.setLayout(new BoxLayout(body, BoxLayout.PAGE_AXIS));
      body.setName("MainDialogPanel");

      body.setMinimumSize(new Dimension(300, 180));
      
      {
	JTextArea area = new JTextArea(8, 35); 
	pMessageArea = area;

	area.setName("TextArea");

	area.setLineWrap(true);
	area.setWrapStyleWord(true);
	area.setEditable(false);

	area.setFocusable(true);
      }
      
      {
	JScrollPane scroll = 
          UIFactory.createScrollPane
          (pMessageArea, 
           JScrollPane.HORIZONTAL_SCROLLBAR_NEVER, 
           JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
           null, null, null); 
	
	body.add(scroll);
      }

      body.add(Box.createVerticalStrut(4));
      
      {
        Box hbox = new Box(BoxLayout.LINE_AXIS);
        Component comps[] = UIFactory.createTitledPanels();
        JPanel tpanel = (JPanel) comps[0];
        JPanel vpanel = (JPanel) comps[1];
        
        pShowDialog = UIFactory.createTitledBooleanField
          (tpanel, "Show This Dialog In Future:", 155, vpanel, 45, 
           "Whether to show this error message in future");
        
        pShowDialog.setValue(true);
        
        hbox.add(comps[2]);
        hbox.add(Box.createHorizontalGlue());
        body.add(hbox);
      }


      super.initUI("Error:", body, null, null, null, "Close");
      pack();
    }  
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the title and text of the error message.
   */ 
  public void 
  setMessage
  (
   String title, 
   String msg
  ) 
  {
    pHeaderLabel.setText(title);
    pMessageArea.setText(msg);
  }

  /**
   * Set the title and text of the error message based on an exception.
   */ 
  public void 
  setMessage
  (
   Throwable ex
  ) 
  {
    if(ex instanceof PipelineException) {
      pHeaderLabel.setText("Error:");
      pMessageArea.setText(ex.getMessage());
    }
    else if(ex instanceof IOException) {
      pHeaderLabel.setText("I/O Error:");
      pMessageArea.setText(ex.getMessage());
    }
    else if(ex instanceof HostConfigException) {
      pHeaderLabel.setText("Host Configuration Error:");
      pMessageArea.setText(ex.getMessage());
    }
    else if(ex instanceof GlueException) {
      pHeaderLabel.setText("Glue Error:");
      pMessageArea.setText(Exceptions.getFullMessage(ex));
    }
    else {
      pHeaderLabel.setText("Internal Error:");
      pMessageArea.setText(Exceptions.getFullMessage(ex));
    }
  }

  /**
   * Return a boolean flag which says whether the user wants to see this dialog in the future.
   */
  public boolean
  getShowDialogInFuture()
  {
    return pShowDialog.getValue();
  }

  
  public static void main(
    String[] args)
  {
    UIFactory.initializePipelineUI();
    JFrame frame = new JFrame();
    JErrorPrefDialog dialog = new JErrorPrefDialog(frame);
    dialog.setMessage("This is a test:", "this is my message");
    dialog.setVisible(true);
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -527451032615142495L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The error message text.
   */ 
  private JTextArea pMessageArea;

  /**
   * Whether to show this dialog again in the future.
   */
  private JBooleanField pShowDialog;
}
