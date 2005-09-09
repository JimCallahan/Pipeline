// $Id: SuffixEditor.java,v 1.4 2005/09/09 21:22:27 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   S U F F I X   E D I T O R                                                              */
/*------------------------------------------------------------------------------------------*/

/**
 * A specification of the default editor to be used for files with a particular suffix. <P> 
 */
public
class SuffixEditor
  implements Comparable, Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  SuffixEditor() 
  {}

  /**
   * Construct a new suffix/editor specification.
   * 
   * @param suffix
   *   The filename suffix
   */ 
  public 
  SuffixEditor
  (
   String suffix
  ) 
  {
    if(suffix == null) 
      throw new IllegalArgumentException("The suffix cannot be (null)!");
    pSuffix = suffix;
  }

  /**
   * Construct a new suffix/editor specification.
   * 
   * @param suffix
   *   The filename suffix.
   * 
   * @param desc
   *   The description text.
   * 
   * @param editor
   *   The default editor plugin to use for files with the given suffix. 
   */ 
  public 
  SuffixEditor
  (
   String suffix, 
   String desc,
   BaseEditor editor
  ) 
  {
    if(suffix == null) 
      throw new IllegalArgumentException("The suffix cannot be (null)!");
    pSuffix = suffix;

    pDescription = desc;
    pEditor      = editor;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the filename suffix.
   */ 
  public String
  getSuffix() 
  {
    assert(pSuffix != null);
    return pSuffix;
  }

  
  /**
   * Get the short description of the file format.
   * 
   * @return 
   *   The description text or <CODE>null</CODE> if none exists.
   */ 
  public String
  getDescription()
  {
    return pDescription;
  }
  
  /**
   * Set the short description of the file format.
   * 
   * @param desc
   *   The description text.
   */ 
  public void
  setDescription
  (
   String desc
  )
  {
    pDescription = desc;
  }
  

  /** 
   * Gets the editor plugin instance used to as the default editor for files having 
   * this filename suffix.
   * 
   * @return 
   *   The editor or <CODE>null</CODE> if undefined.
   */ 
  public BaseEditor
  getEditor() 
  {
    return pEditor;
  }

  /**
   * Sets the editor plugin instance used to as the default editor for files having 
   * this filename suffix.
   * 
   * @param editor
   *   The default editor plugin or <CODE>null</CODE> for no editor.
   */ 
  public void
  setEditor
  (
   BaseEditor editor
  ) 
  {
    pEditor = editor;
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
    if((obj != null) && (obj instanceof SuffixEditor)) {
      SuffixEditor se = (SuffixEditor) obj;
      return pSuffix.equals(se.pSuffix);
    }
    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C O M P A R A B L E                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Compares this object with the specified object for order.
   * 
   * @param obj 
   *   The <CODE>Object</CODE> to be compared.
   */
  public int
  compareTo
  (
   Object obj
  )
  {
    if(obj == null) 
      throw new NullPointerException();
    
    if(!(obj instanceof SuffixEditor))
      throw new IllegalArgumentException("The object to compare was NOT a SuffixEditor!");

    return compareTo((SuffixEditor) obj);
  }


  /**
   * Compares this <CODE>SuffixEditor</CODE> with the given <CODE>SuffixEditor</CODE> for 
   * order.
   * 
   * @param se 
   *   The <CODE>SuffixEditor</CODE> to be compared.
   */
  public int
  compareTo
  (
   SuffixEditor se
  )
  {
    return pSuffix.compareTo(se.pSuffix);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S E R I A L I Z A B L E                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the serializable fields to the object stream. <P> 
   * 
   * This enables the node to convert a dynamically loaded action plugin instance into a 
   * generic staticly loaded BaseAction instance before serialization.
   */ 
  private void 
  writeObject
  (
   java.io.ObjectOutputStream out
  )
    throws IOException
  {
    out.writeObject(pSuffix);
    out.writeObject(pDescription);
    
    BaseEditor editor = null;
    if(pEditor != null) 
      editor = new BaseEditor(pEditor);
    out.writeObject(editor);
  }

  /**
   * Read the serializable fields from the object stream. <P> 
   * 
   * This enables the node to dynamically instantiate an action plugin instance and copy
   * its parameters from the generic staticly loaded BaseAction instance in the object 
   * stream. 
   */ 
  private void 
  readObject
  (
    java.io.ObjectInputStream in
  )
    throws IOException, ClassNotFoundException
  {
    pSuffix = (String) in.readObject();
    pDescription = (String) in.readObject();

    BaseEditor editor = (BaseEditor) in.readObject();
    if(editor != null) {
      try {
	PluginMgrClient client = PluginMgrClient.getInstance();
	pEditor = client.newEditor(editor.getName(), editor.getVersionID(), editor.getVendor());
      }
      catch(PipelineException ex) {
	throw new IOException(ex.getMessage());
      }
    }
    else {
      pEditor = null;
    }
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
    encoder.encode("Suffix", pSuffix);
    
    if(pDescription != null) 
      encoder.encode("Description", pDescription);

    if(pEditor != null) 
      encoder.encode("Editor", new BaseEditor(pEditor));		     
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    String suffix = (String) decoder.decode("Suffix"); 
    if(suffix == null) 
      throw new GlueException("The \"Suffix\" was missing!");
    pSuffix = suffix;

    pDescription = (String) decoder.decode("Description");     

    BaseEditor editor = (BaseEditor) decoder.decode("Editor");     
    if(editor != null) {
      try {
	PluginMgrClient client = PluginMgrClient.getInstance();
	pEditor = client.newEditor(editor.getName(), editor.getVersionID(), editor.getVendor());
      }
      catch(PipelineException ex) {
	throw new GlueException(ex.getMessage());
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -4174194640105458661L;

  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The filename suffix.
   */
  private String  pSuffix;        

  /**
   * The short description of the file format.
   */
  private String  pDescription;        

  /** 
   * The editor plugin instance used to as the default editor for files having this
   * filename suffix.
   */
  private BaseEditor  pEditor; 

}
