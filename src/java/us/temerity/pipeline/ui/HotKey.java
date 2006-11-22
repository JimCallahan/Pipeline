// $Id: HotKey.java,v 1.4 2006/11/22 09:08:01 jim Exp $

package us.temerity.pipeline.ui;

import us.temerity.pipeline.glue.*;

import java.awt.event.*;

/*------------------------------------------------------------------------------------------*/
/*   H O T   K E Y                                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A keyboard event which triggers the execution of an action. <P> 
 */
public 
class HotKey
  implements Glueable
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  HotKey() 
  {
    pKeyCode = KeyEvent.VK_UNDEFINED;
  }

  /**
   * Constuct a new hot key.
   */ 
  public 
  HotKey
  (
   boolean shiftDown, 
   boolean altDown, 
   boolean ctrlDown, 
   int keyCode
  ) 
  {
    pShiftDown = shiftDown;
    pAltDown   = altDown;
    pCtrlDown  = ctrlDown;

    pKeyCode = keyCode;
  }

  /**
   * Construct a hot key from a key event. <P> 
   * 
   * @param e
   *   The key event passed as the parameter of the {@link KeyListener#keyPressed keyPressed}
   *   method.
   */ 
  public 
  HotKey
  (
   KeyEvent e
  ) 
  {
    int code = e.getKeyCode();
    switch(code) {
    case KeyEvent.VK_SHIFT:
    case KeyEvent.VK_ALT:
    case KeyEvent.VK_CONTROL:
      throw new IllegalArgumentException
	("The " + KeyEvent.getKeyText(code) + " key cannot be used as a hot key!");
    }
    
    int mods = e.getModifiersEx();    
    pShiftDown = ((mods & KeyEvent.SHIFT_DOWN_MASK) == KeyEvent.SHIFT_DOWN_MASK);
    pAltDown   = ((mods & KeyEvent.ALT_DOWN_MASK)   == KeyEvent.ALT_DOWN_MASK);
    pCtrlDown  = ((mods & KeyEvent.CTRL_DOWN_MASK)  == KeyEvent.CTRL_DOWN_MASK);
    
    pKeyCode = code;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Whether the SHIFT key must be down when the key is pressed.
   */ 
  public boolean 
  withShiftDown() 
  {
    return pShiftDown;
  }

  /**
   * Whether the ALT key must be down when the key is pressed.
   */ 
  public boolean 
  withAltDown() 
  {
    return pAltDown;
  }

  /**
   * Whether the CTRL key must be down when the key is pressed.
   */ 
  public boolean 
  withCtrlDown() 
  {
    return pCtrlDown;
  }
  
  /**
   * Get the key code as defined by {@link KeyEvent KeyEvent}.
   */ 
  public int
  getKeyCode() 
  {
    return pKeyCode;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R I S O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Does the given key event correspond to the hot key? <P> 
   * 
   * The <CODE>e</CODE> argument must be the parameter of the 
   * {@link KeyListener#keyPressed keyPressed} method.
   */ 
  public boolean 
  wasPressed
  (
   KeyEvent e
  ) 
  {
    int on  = 0;
    int off = 0;

    if(pShiftDown) 
      on |= KeyEvent.SHIFT_DOWN_MASK;
    else 
      off |= KeyEvent.SHIFT_DOWN_MASK;
    
    if(pAltDown) 
      on |= KeyEvent.ALT_DOWN_MASK;
    else 
      off |= KeyEvent.ALT_DOWN_MASK;
    
    if(pCtrlDown) 
      on |= KeyEvent.CTRL_DOWN_MASK;
    else 
      off |= KeyEvent.CTRL_DOWN_MASK;
    
    if((e.getModifiersEx() & (on | off)) != on) 
      return false;
    
    return (pKeyCode == e.getKeyCode());
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O N V E R S I O N                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Convert the hot key into a human readable form.
   */ 
  public String
  toString() 
  {
    StringBuilder buf = new StringBuilder();

    if(pShiftDown) 
      buf.append("SHIFT+");

    if(pAltDown) 
      buf.append("ALT+");

    if(pCtrlDown) 
      buf.append("CTRL+");

    buf.append(KeyEvent.getKeyText(pKeyCode));

    return (buf.toString());
  }
  

  
  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  public boolean
  equals
  (
   Object obj   
  )
  {
    if((obj != null) && (obj instanceof HotKey)) {
      HotKey key = (HotKey) obj; 
      return ((pShiftDown == key.pShiftDown) && 
	      (pAltDown == key.pAltDown) && 
	      (pCtrlDown == key.pCtrlDown) && 
	      (pKeyCode == key.pKeyCode));
    }
    return false;
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
    encoder.encode("ShiftDown", pShiftDown);
    encoder.encode("AltDown", pAltDown);
    encoder.encode("CtrlDown", pCtrlDown);
    encoder.encode("KeyCode", pKeyCode);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    Boolean shift = (Boolean) decoder.decode("ShiftDown");
    if(shift == null) 
      throw new GlueException("The \"ShiftDown\" was missing or (null)!");
    pShiftDown = shift;

    Boolean alt = (Boolean) decoder.decode("AltDown");
    if(alt == null) 
      throw new GlueException("The \"AltDown\" was missing or (null)!");
    pAltDown = alt;

    Boolean ctrl = (Boolean) decoder.decode("CtrlDown");
    if(ctrl == null) 
      throw new GlueException("The \"CtrlDown\" was missing or (null)!");
    pCtrlDown = ctrl;

    Integer keyCode = (Integer) decoder.decode("KeyCode");
    if(keyCode == null) 
      throw new GlueException("The \"KeyCode\" was missing or (null)!");
    pKeyCode = keyCode;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether the SHIFT key must be down when the key is pressed.
   */ 
  private boolean  pShiftDown;

  /**
   * Whether the ALT key must be down when the key is pressed.
   */ 
  private boolean  pAltDown;

  /**
   * Whether the CTRL key must be down when the key is pressed.
   */ 
  private boolean  pCtrlDown;

  /**
   * The key code as defined by {@link KeyEvent KeyEvent}.
   */ 
  private int  pKeyCode;

}
