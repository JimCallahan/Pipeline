// $Id: BaseEditor.java,v 1.1 2004/02/23 23:50:55 jim Exp $

package us.temerity.pipeline;

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   E D I T O R                                                                  */
/*------------------------------------------------------------------------------------------*/

/** 
 * Superclass of all Pipeline node editor plugins. <P>
 * 
 */
public
class BaseEditor
  extends Named
  implements Glueable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  protected
  BaseEditor() 
  {
    super();
  }

  /** 
   * Construct with a name and description. 
   * 
   * @param name [<B>in</B>]
   *   The short name of the editor.  
   * 
   * @param desc [<B>in</B>]
   *   A short description used in tooltips.
   */ 
  protected
  BaseEditor
  (
   String name,  
   String desc  
  ) 
  {
    super(name);
    
    if(desc == null) 
      throw new IllegalArgumentException("The editor description cannot be (null)!");
    pDescription = desc;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Gets the short name of the editor class without any package prefix.
   */ 
  public String
  getClassName()
  {
    String pname = getClass().getPackage().getName();
    return getClass().getName().substring(pname.length()+1);
  }

  /* 
   * Gets the tooltip description text. 
   */ 
  public String
  getDescription()
  {
    return pDescription;
  }

   

  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Launch the editor program (obtained with {@link #getName getName}) under the given 
   * environmant with all of the files which comprise the given file sequence as 
   * arguments. The environment <CODE>env</CODE> consists of a table of environmental 
   * variable name/value pairs.  Typically, this environment is corresponds to a Toolset. <P>
   * 
   * Subclasses should override this method if more specialized behavior or different 
   * command line arguments are needed in order to launch the editor for the given file 
   * sequence.
   * 
   * @param fseq [<B>in</B>] 
   *   The file sequence to edit.
   * 
   * @param env [<B>in</B>] 
   *   The environment under which the editor is run.  
   * 
   * @param dir [<B>in</B>] 
   *   The working directory where the editor is run.
   *
   * @return 
   *   The controlling <CODE>SubProcess</CODE> instance. 
   * 
   * @see SubProcess
   */  
  public SubProcess
  launch
  (
   FileSeq fseq,      
   Map<String, String> env,      
   File dir        
  ) 
  {
    ArrayList<String> args = new ArrayList<String>();
    for(File file : fseq.getFiles()) 
      args.add(file.getPath());

    SubProcess proc = new SubProcess(getClassName(), getName(), args, env, dir);
    proc.start();
    return proc;
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
    super.toGlue(encoder);
    encoder.encode("Description", pDescription);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);
    
    String description = (String) decoder.decode("Description"); 
    if(description == null) 
      throw new GlueException("The \"Description\" was missing!");
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * A short message which describes the editor.  This messages is used in UI tooltips 
   * where editors are selected.
   */     
  protected String  pDescription;  
  
}



