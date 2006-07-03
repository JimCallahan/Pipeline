// $Id: SubProcessExecDetails.java,v 1.1 2006/07/03 06:38:42 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*  S U B   P R O C E S S   E X E C   D E T A I L S                                         */
/*------------------------------------------------------------------------------------------*/

/**
 * The full execution details of a subprocess. <P> 
 * 
 * Includes the exact command line and environment the job process is executed under.  
 */
public
class SubProcessExecDetails
  implements Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  public
  SubProcessExecDetails()
  {}

  /**
   * Construct a new exec details. 
   * 
   * @param cmd  
   *   The literal command line arguments of the OS level process. 
   * 
   * @param env  
   *   The environment under which the OS level process is run.  
   */ 
  public
  SubProcessExecDetails
  (  
   String cmd, 
   Map<String,String> env
  )
  {
    pCommand     = cmd; 
    pEnvironment = new TreeMap<String,String>(env);
  }


   
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The literal command line arguments of the OS level process. 
   */
  public String 
  getCommand() 
  {
    return pCommand;
  }

  /**
   * The environment under which the OS level process is run. 
   */ 
  public TreeMap<String,String> 
  getEnvironment()
  {
    return pEnvironment;
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
    encoder.encode("Command", pCommand); 
    encoder.encode("Environment", pEnvironment); 
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    String cmd = (String) decoder.decode("Command"); 
    if(cmd == null) 
      throw new GlueException("The \"Command\" entry is required!");
    pCommand = cmd;

    TreeMap<String,String> env = (TreeMap<String,String>) decoder.decode("Environment"); 
    if(env == null) 
      throw new GlueException("The \"Environment\" entry is required!");
    pEnvironment = env;
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 3558178801875527441L;



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The literal command line arguments of the OS level process. 
   */ 
  private String pCommand; 

  /**
   * The environment under which the OS level process is run. 
   */ 
  private TreeMap<String,String> pEnvironment; 

}
