// $Id: JoglTestApp.java,v 1.1 2004/12/11 13:41:32 jim Exp $

package us.temerity.pipeline.core;

import us.temerity.pipeline.*;

import java.awt.*;
import java.awt.event.*; 
import javax.swing.*;

import net.java.games.jogl.*;

/*------------------------------------------------------------------------------------------*/
/*   J O G L   T E S T   A P P                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A bare bones JOGL application.
 */
public
class JoglTestApp
  extends BaseApp
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct and run the application with the given command-line arguments.
   */
  public
  JoglTestApp() 
  {
    super("jogltest");
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   R U N                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Run the application with the given command-line arguments.
   * 
   * @param args 
   *   The command-line arguments.
   */ 
  public
  void 
  run
  (
   String[] args
  )
  {
    try {
      JFrame frame = new JFrame("jogltest");
      frame.setSize(512, 512);
      
      {
	JPanel panel = new JPanel();
	panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));   

// 	{
// 	  JButton btn = new JButton("Push Me");
// 	  panel.add(btn);
// 	}

	{
	  GLCapabilities caps = new GLCapabilities();  
	  caps.setDoubleBuffered(true);

	  GLCanvas canvas = GLDrawableFactory.getFactory().createGLCanvas(caps);
	  
	  panel.add(canvas);

	  JoglTestRenderer renderer = new JoglTestRenderer(canvas);
	  canvas.addGLEventListener(renderer);
	  canvas.addMouseListener(renderer);	
	}

	frame.setContentPane(panel);
      }
      
      frame.setVisible(true);
    }
    catch(Exception ex) {
      Logs.net.severe(getFullMessage(ex));
      System.exit(1);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P T I O N S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The implementation of the <CODE>--help</CODE> command-line option.
   */ 
  public void
  help()
  {
    Logs.ops.info(
      "USAGE:\n" +
      "  jogltest\n");
  }

}


