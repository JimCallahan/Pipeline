// $Id: ZoomPanBehavior.java,v 1.2 2004/09/01 12:23:35 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import com.sun.j3d.utils.universe.*;

/*------------------------------------------------------------------------------------------*/
/*   Z O O M   P A N   B E H A V I O R                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * A behavior which uses mouse input to zoom (move +/- in Z) and to pan (move parallel to 
 * the XY plane) the camera.  Camera orientation is not affected. <P>                     
 *                                                                                  
 * Zooming is initiated by pressing both the MOUSE1+MOUSE2 buttons and then dragging. The 
 * change in zoom is proportional to distance of the drag in the vertical direction.  In 
 * other words dragging upwards zooms IN and downwards zooms OUT. <P> 
 *                                                                                          
 * Panning is initiated by pressing the MOUSE2 button and then dragging.  The camera is 
 * moved in the opposite direction as the drag and at a speed inversely proportional to 
 * the current zoom factor.                                                          
 */ 
public
class ZoomPanBehavior
  extends Behavior
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a new instance.
   * 
   * @param viewer
   *    The viewer to be controlled.
   * 
   * @param maxFactor
   *    The maximum zoom factor.
   */ 
  public 
  ZoomPanBehavior
  (
   Viewer viewer, 
   double maxFactor
  ) 
  {
    assert(viewer != null);
    pViewer = viewer;

    pPanSpeed  = 0.415;
    pZoomSpeed = 0.1;
    pMaxFactor = maxFactor;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the panning speed factor. 
   */ 
  public double 
  getPanSpeed()
  {
    return pPanSpeed;
  }

  /**
   * Set the panning speed factor.
   */ 
  public void 
  setPanSpeed
  (
   double speed
  ) 
  {
    pPanSpeed = speed;
  }

  

  /*----------------------------------------------------------------------------------------*/
  /*   B E H A V I O R   O V E R R I D E S                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Initialize this behavior. 
   */ 
  public void 
  initialize()
  {
    WakeupCriterion conds[] = {
      new WakeupOnAWTEvent(AWTEvent.MOUSE_EVENT_MASK),
      new WakeupOnAWTEvent(AWTEvent.MOUSE_MOTION_EVENT_MASK)
    };
    pCond = new WakeupOr(conds);

    wakeupOn(pCond);
  }

  /**
   * Process a stimulus meant for this behavior. 
   */ 
  public void 
  processStimulus
  (
   Enumeration criteria
  )
  {
    WakeupCondition cond;
    AWTEvent[] events;

    while(criteria.hasMoreElements()) {
      cond = (WakeupCondition) criteria.nextElement();
      if(!(cond instanceof WakeupOnAWTEvent))
	continue;

      events = ((WakeupOnAWTEvent)cond).getAWTEvent();

      int wk;
      for(wk=0; wk<events.length; wk++) {
	try {
	  if(events[wk] instanceof MouseEvent)
	    mouseEvent((MouseEvent) events[wk]);
	}
	catch(Exception ex) {
	  Logs.ops.severe("Error processing AWT event input: " + ex.getMessage());
	}
      }
    }
    wakeupOn(pCond);
  }

  
  /*----------------------------------------------------------------------------------------*/
  /*   E V E N T S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Process mouse input.
   */ 
  protected void 
  mouseEvent
  (
   MouseEvent e
  )
  {    
    int mods = e.getModifiersEx();
    Canvas3D canvas = pViewer.getCanvas3D();

    switch(e.getID()) {
    case MouseEvent.MOUSE_PRESSED:
      {
	int on1  = (MouseEvent.BUTTON2_DOWN_MASK |
		    MouseEvent.ALT_DOWN_MASK);
      
	int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		    MouseEvent.BUTTON3_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);
	
	
	int on2  = (MouseEvent.BUTTON1_DOWN_MASK |
		    MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.ALT_DOWN_MASK);
	
	int off2 = (MouseEvent.BUTTON3_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);

	/* <BUTTON2+ALT> (pan start) or <BUTTON1+BUTTON2+ALT> (zoom start) */ 
	if(((mods & (on1 | off1)) == on1) || ((mods & (on2 | off2)) == on2)) {

	  /* set start point of the drag */ 
	  pStart = e.getPoint();

	  /* change cursor */ 
	  if((mods & (on1 | off1)) == on1)
	    canvas.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
	  else 
	    canvas.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}
      }
    break;

    case MouseEvent.MOUSE_DRAGGED:
      if(pStart != null) {
	TransformGroup tg = pViewer.getViewingPlatform().getViewPlatformTransform();

	Transform3D xform = new Transform3D();
	tg.getTransform(xform);
	
	Vector3d trans = new Vector3d();
	xform.get(trans);


	int on1  = (MouseEvent.BUTTON2_DOWN_MASK |
		    MouseEvent.ALT_DOWN_MASK);
      
	int off1 = (MouseEvent.BUTTON1_DOWN_MASK | 
		    MouseEvent.BUTTON3_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);
	
	
	int on2  = (MouseEvent.BUTTON1_DOWN_MASK |
		    MouseEvent.BUTTON2_DOWN_MASK | 
		    MouseEvent.ALT_DOWN_MASK);
	
	int off2 = (MouseEvent.BUTTON3_DOWN_MASK | 
		    MouseEvent.SHIFT_DOWN_MASK |
		    MouseEvent.CTRL_DOWN_MASK);
	
	/* <BUTTON2+ALT> (pan) */ 
	if((mods & (on1 | off1)) == on1) {	
	  Point3d startPos = new Point3d();
	  canvas.getPixelLocationInImagePlate(pStart.x, pStart.y, startPos);
	  
	  Point3d endPos = new Point3d();
	  canvas.getPixelLocationInImagePlate(e.getX(), e.getY(), endPos);
	  
	  Transform3D mx = new Transform3D();
	  canvas.getImagePlateToVworld(mx);
	  mx.transform(startPos);
	  mx.transform(endPos);
	  
	  Point3d delta = new Point3d(startPos);
	  delta.sub(endPos);
	  delta.scale(trans.z * pPanSpeed);
	  
	  trans.add(delta);
	  
	  xform.setTranslation(trans);
	  tg.setTransform(xform);
	}

	/* <BUTTON1+BUTTON2+ALT> (zoom) */ 
	else if((mods & (on2 | off2)) == on2) {
	  double dx = ((double) e.getX()) - pStart.x;
	  double dy = ((double) e.getY()) - pStart.y;
	  
	  double zoom = dy;
	  if(Math.abs(dx) > Math.abs(dy)) 
	    zoom = -dx;
	  
	  trans.add(new Vector3d(0.0f, 0.0f, pZoomSpeed*zoom));
	  trans.z = Math.max(((double) canvas.getWidth()) / pMaxFactor, trans.z);
	  
	  xform.setTranslation(trans);	    
	  tg.setTransform(xform);
	}
      }
      pStart = e.getPoint();
      break;
      
    case MouseEvent.MOUSE_RELEASED:
      pViewer.getCanvas3D().setCursor(Cursor.getDefaultCursor());
      break;

    default:
      /* ignore the rest */ 
    }
  }


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The viewer to control. 
   */   
  private Viewer  pViewer;     


  /**
   * The pan speed scale factor. 
   */ 
  private double  pPanSpeed;   

  /**
   * The zoom speed scale factor. 
   */ 
  private double  pZoomSpeed;  

  /**
   * The max zoom factor. 
   */ 
  private double  pMaxFactor; 


  /**
   * The condition passed to {@link #wakeupOn wakeupOn}.
   */ 
  private WakeupCondition  pCond;       

  /**
   * The pixel location of start of mouse drag. 
   */ 
  private Point  pStart;  

}
