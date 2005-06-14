// $Id: JTabbedPanel.java,v 1.2 2005/06/14 13:38:33 jim Exp $

package us.temerity.pipeline.ui.core;

import us.temerity.pipeline.glue.*;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.util.*;
import javax.swing.*;

/*------------------------------------------------------------------------------------------*/
/*   T A B B E D   P A N N E L                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A <CODE>JTabbedPane</CODE> which implements {@link Glueable Glueable}.
 */ 
public 
class JTabbedPanel
  extends JTabbedPane
  implements Glueable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  JTabbedPanel()
  {
    super();
  }

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Add the given manager panel as a tab.
   */ 
  public void 
  addTab
  (
   JManagerPanel mgr
  ) 
  {
    addTab(null, sTabIcon, mgr);
  }

  /**
   * Add the given panel as a tab.
   */ 
  public void 
  addTab
  (
   JPanel panel
  ) 
  {
    addTab(null, sTabIcon, panel);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/

  public void 
  toGlue
  ( 
   GlueEncoder encoder   
  ) 
    throws GlueException
  {
    LinkedList<JManagerPanel> tabs = new LinkedList<JManagerPanel>();
    int wk;
    for(wk=0; wk<getTabCount(); wk++) 
      tabs.add((JManagerPanel) getComponentAt(wk));
    encoder.encode("Tabs", tabs);

    encoder.encode("SelectedIndex", getSelectedIndex());
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    LinkedList<JManagerPanel> tabs = (LinkedList<JManagerPanel>) decoder.decode("Tabs");
    if(tabs == null) 
      throw new GlueException("The \"Tabs\" was missing or (null)!");
    for(JManagerPanel mgr : tabs) 
      addTab(mgr);

    Integer idx = (Integer) decoder.decode("SelectedIndex");
    if(idx == null) 
      throw new GlueException("The \"SelectedIndex\" was missing or (null)!");
    setSelectedIndex(idx);    
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = 1073333511291732408L;


  private static final Icon sTabIcon = 
    new ImageIcon(LookAndFeelLoader.class.getResource("TabIcon.png"));

}
