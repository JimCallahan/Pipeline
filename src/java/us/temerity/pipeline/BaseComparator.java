// $Id: BaseComparator.java,v 1.3 2004/10/28 15:55:23 jim Exp $

package us.temerity.pipeline;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   B A S E   C O M P A R A T O R                                                          */
/*------------------------------------------------------------------------------------------*/

/** 
 * The superclass of all Pipeline plugins for comparing revisions of files. <P>
 * 
 * New kinds of comparators can be written by subclassing this class.  Due to the way plugins
 * are loaded and communicated between applications, any fields added to a subclass will
 * be reinitialized when the action is stored to disk or when it is sent over the network.
 */
public
class BaseComparator
  extends BasePlugin
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  protected
  BaseComparator() 
  {
    super();
  }

  /** 
   * Construct with a name and description. 
   * 
   * @param name 
   *   The short name of the comparator.
   * 
   * @param vid
   *   The comparator plugin revision number.
   * 
   * @param desc 
   *   A short description of the comparator.
   *
   * @param program 
   *   A name of the comparator executable. 
   */ 
  protected
  BaseComparator
  (
   String name, 
   VersionID vid, 
   String desc, 
   String program
  ) 
  {
    super(name, vid, desc);
    
    if(program == null)
      throw new IllegalArgumentException("The program cannot be (null)!");
    pProgram = program;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Gets the name of the comparator executable.
   */
  public String
  getProgram() 
  {
    return pProgram;
  }

   

  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Launch the comparator program (obtained with {@link #getProgram getProgram}) under the 
   * given environmant to compare the two given files. <P> 
   * 
   * The environment <CODE>env</CODE> consists of a table of environmental 
   * variable name/value pairs.  Typically, this environment is corresponds to a Toolset. <P>
   * 
   * Subclasses should override this method if more specialized behavior or different 
   * command line arguments are needed in order to launch the comparator for the given file 
   * sequence.
   * 
   * @param fileA
   *   The absolute path to the first file.
   * 
   * @param fileB
   *   The absolute path to the second file.
   * 
   * @param env  
   *   The environment under which the comparator is run.  
   * 
   * @param dir  
   *   The working directory where the comparator is run.
   *
   * @return 
   *   The controlling <CODE>SubProcessLight</CODE> instance. 
   * 
   * @throws PipelineException
   *   If unable to launch the comparator.
   * 
   * @see SubProcessLight
   */  
  public SubProcessLight
  launch
  (
   File fileA, 
   File fileB,
   Map<String, String> env,      
   File dir        
  ) 
    throws PipelineException
  {
    ArrayList<String> args = new ArrayList<String>();
    args.add(fileA.getPath());
    args.add(fileB.getPath());

    SubProcessLight proc = new SubProcessLight(getName(), getProgram(), args, env, dir);
    proc.start();
    return proc;
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
    if((obj != null) && (obj instanceof BaseComparator)) {
      BaseComparator desc = (BaseComparator) obj;
      return (super.equals(obj) && 
	      pProgram.equals(desc.pProgram));
    }
    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 1045567698449532792L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   *  The name of the comparator executable.
   */     
  private String  pProgram;
  
}



