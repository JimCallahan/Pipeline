// $Id: UIMaster.java,v 1.1 2004/04/30 11:21:54 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.laf.LookAndFeelLoader;

import java.awt.*;
import java.util.*;
import java.text.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.synth.*;

/*------------------------------------------------------------------------------------------*/
/*   U I   M A S T E R                                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Provides centralized management of the user interface components and network 
 * communication of <B>plui</B>(1).
 */ 
public 
class UIMaster
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Construct the sole instance.
   * 
   * @param hostname
   *   The hostname running <B>plmaster</B>(1).
   * 
   * @param port
   *   The port number listened to by <B>plmaster</B>(1) for incoming connections.
   */ 
  private 
  UIMaster
  (
   String hostname, 
   int port
  ) 
  {
    pNodeMgrClient = new NodeMgrClient(hostname, port);

    pManagerPanels = new LinkedList<JManagerPanel>();

    InitUITask task = new InitUITask();
    SwingUtilities.invokeLater(task);
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N I T I A L I Z A T I O N                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Initialize the user interface and connection to <B>plmaster<B>(1).
   * 
   * @param hostname
   *   The hostname running <B>plmaster</B>(1).
   * 
   * @param port
   *   The port number listened to by <B>plmaster</B>(1) for incoming connections.
   */ 
  public static void 
  init
  (
   String hostname, 
   int port
  ) 
  {
    assert(sMaster == null);
    sMaster = new UIMaster(hostname, port);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the UIMaster instance.
   */ 
  public static UIMaster
  getInstance() 
  {
    return sMaster;
  }


  /**
   * Add a new manager panel.
   */ 
  public void 
  addManager
  (
   JManagerPanel mgr
  ) 
  {
    synchronized(pManagerPanels) {
      assert(!pManagerPanels.contains(mgr));
      pManagerPanels.add(mgr);

      System.out.print("ManagerPanels = " + pManagerPanels.size() + "\n");
    }
  }
  
  /**
   * Recursively remove the subtree of manager panels.
   */ 
  public void 
  removeManager
  (
   JManagerPanel mgr
  ) 
  {
    synchronized(pManagerPanels) {
      assert(pManagerPanels.contains(mgr));
      pManagerPanels.remove(mgr);

      System.out.print("ManagerPanels = " + pManagerPanels.size() + "\n");
    }
  }
  
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L   C L A S S E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Initialize the user interface components. 
   */ 
  private
  class InitUITask
    extends Thread
  { 
    public void 
    run() 
    {  
      /* load the look-and-feel */ 
      {
	try {
	  SynthLookAndFeel synth = new SynthLookAndFeel();
	  synth.load(LookAndFeelLoader.class.getResourceAsStream("synth.xml"),
		     LookAndFeelLoader.class);
	  UIManager.setLookAndFeel(synth);
	}
	catch(ParseException ex) {
	  Logs.ops.severe("Unable to parse the look-and-feel XML file (synth.xml):\n" + 
			  "  " + ex.getMessage());
	  System.exit(1);
	}
	catch(UnsupportedLookAndFeelException ex) {
	  Logs.ops.severe("Unable to load the Pipeline look-and-feel:\n" + 
			  "  " + ex.getMessage());
	  System.exit(1);
	}
      }
      
      /* application wide UI settings */ 
      {
	JPopupMenu.setDefaultLightWeightPopupEnabled(false);
      }
      
      /* create and show the top-level components */ 
      {
	JFrame frame = null;
	{
	  frame = new JFrame("plui");
	  
	  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	  
	  frame.setLocationRelativeTo(null);
	  Point p = frame.getLocation();
	  frame.setBounds(p.x-250, p.y-200, 500, 400);
	}
	
	{
	  JPanel root = new JPanel();
	  
	  root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));   
	  
	  {
	    JPanel panel = new JPanel(new BorderLayout());
	    panel.setName("RootPanel");
	    
	    JManagerPanel mgr = null;
	    {
	      mgr = new JManagerPanel();
	      mgr.setContents(new JEmptyPanel());
	      
	      panel.add(mgr);
	    }
	    
	    root.add(panel);
	  }
	  
	  root.add(Box.createRigidArea(new Dimension(0, 2)));
	  
	  {
	    JPanel panel = new JPanel(); 
	    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS)); 
	    
	    panel.add(Box.createRigidArea(new Dimension(2, 0)));
	    
	    {
	      JToggleButton btn = new JToggleButton();
	      btn.setName("StopLight");
	      
	      Dimension size = new Dimension(15, 19);
	      btn.setMinimumSize(size);
	      btn.setMaximumSize(size);
	      btn.setPreferredSize(size);
	      
	      panel.add(btn);
	    }
	    
	    {
	      JTextField field = new JTextField();
	      pProgressField = field;
	      
	      field.setMinimumSize(new Dimension(200, 19));
	      field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 19));
	      field.setPreferredSize(new Dimension(200, 19));
	      
	      panel.add(field);
	    }
	    
	    panel.add(Box.createRigidArea(new Dimension(3, 0)));
	    panel.add(Box.createHorizontalGlue());
	    
	    root.add(panel);
	  }
	  
	  root.add(Box.createRigidArea(new Dimension(0, 4)));
	  
	  frame.setContentPane(root);
	}
	
	frame.setVisible(true);
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The sole instance.
   */ 
  private static UIMaster  sMaster;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The network interface to the <B>plmaster</B>(1) daemon.
   */ 
  private NodeMgrClient  pNodeMgrClient;


  /**
   * The light which warns users that a network operation is in progress.
   */ 
  private JLabel  pProgressLight;

  /**
   * The progress message field.
   */ 
  private JTextField  pProgressField;

 
  /**
   * The current set of manager panels.
   */ 
  private LinkedList<JManagerPanel>  pManagerPanels;

}
