// $Id: ShakeDiffComparator.java,v 1.1 2005/09/17 18:49:44 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   S H A K E   C O M P A R A T O R                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * Image comparison using Shake.
 */
public
class ShakeDiffComparator
  extends BaseComparator
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  ShakeDiffComparator()
  {
    super("ShakeDiff", new VersionID("2.0.0"), "Temerity", 
	  "Image comparison using Shake.", 
	  "shake");
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Launch GIMP with a script which will compare the given two images using
   * layers. <P> 
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
    args.add(fileA.toString());
    args.add("-compare");
    args.add(fileB.toString());

    SubProcessLight proc = new SubProcessLight(getName(), getProgram(), args, env, dir);
    proc.start();

    return proc;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -8871230815112080837L;

}


