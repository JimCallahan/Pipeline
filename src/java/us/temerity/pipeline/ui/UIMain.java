// $Id: UIMain.java,v 1.2 2004/04/27 02:20:57 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;
import us.temerity.pipeline.laf.PipelineLookAndFeelLoader;

import java.awt.*;
import java.text.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;
import javax.swing.plaf.synth.*;

/*------------------------------------------------------------------------------------------*/
/*   U I   M A I N                                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * The thread which creates and shows the top-level user interface components 
 * of <B>plui</>(1).
 */ 
public 
class UIMain
  extends Thread
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Construct the user interface.
   * 
   * @param hostname
   *   The hostname running <B>plmaster</B>(1).
   * 
   * @param port
   *   The port number listened to by <B>plmaster</B>(1) for incoming connections.
   */ 
  public 
  UIMain
  ( 
   String hostname, 
   int port
  ) 
  {
    pNodeMgrClient = new NodeMgrClient(hostname, port);
  }


  /*----------------------------------------------------------------------------------------*/
  /*   R U N                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create and show the top-level UI components.
   */ 
  public
  void 
  run() 
  {  
//     {
//       GraphicsEnvironment genv = GraphicsEnvironment.getLocalGraphicsEnvironment();
//       Font[] fonts = genv.getAllFonts();
//       int wk;
//       for(wk=0; wk<fonts.length; wk++) {
// 	System.out.print("Font: " + fonts[wk].getFontName() + "\n");
//       }
//     }

    /* load the Pipeline look-and-feel */ 
    {
      try {
	SynthLookAndFeel synth = new SynthLookAndFeel();
	synth.load(PipelineLookAndFeelLoader.class.getResourceAsStream("synth.xml"),
		   PipelineLookAndFeelLoader.class);
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
      }

      
      JNodeBrowserPanel panel = new JNodeBrowserPanel();
      panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

      JManagerPanel rootPanel = new JManagerPanel(panel);
      rootPanel.setName("RootPanel");

      frame.add(rootPanel);
      
      
//       JPanel rootPanel = null;
//       {
// 	rootPanel = new JPanel();
// 	rootPanel.setName("RootPanel");
// 	rootPanel.setLayout(new BorderLayout());

// 	frame.add(rootPanel);
//       }      

//       JSplitPanePL splitLR = null;
//       { 
// 	JLabel left  = new JLabel("Left");
// 	JLabel right = new JLabel("Right");

// 	splitLR = new JSplitPanePL(JSplitPane.HORIZONTAL_SPLIT, left, right);
//       }
      
//       JSplitPanePL splitUD = null;
//       {
//  	JLabel top = new JLabel("Top");
	
//   	splitUD = new JSplitPanePL(JSplitPane.VERTICAL_SPLIT, top, splitLR);
//  	splitUD.setOneTouchExpandable(true);
//  	splitUD.setResizeWeight(0.5);
//  	splitUD.setDividerSize(10);

//  	rootPanel.add(splitUD);
//       }

      frame.pack();
      frame.setVisible(true);
    }
  }


  





  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The network interface to the <B>plmaster</B>(1) daemon.
   */ 
  private NodeMgrClient  pNodeMgrClient;


}
