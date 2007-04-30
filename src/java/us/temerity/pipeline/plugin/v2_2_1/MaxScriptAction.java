// $Id: MaxScriptAction.java,v 1.1 2007/04/30 08:20:58 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M A X   S C R I P T   A C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * Start 3d Studio Max and run a MAXScript.<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Max Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node who's primary file sequence is a single MAXScript. 
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public
class MaxScriptAction
  extends CommonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  MaxScriptAction() 
  {
    super("MaxScript", new VersionID("2.2.1"), "Temerity",
	  "Start 3d Studio Max and run a MAXScript.");
    
    {
      ActionParam param = 
	new LinkActionParam
	(aMaxScript,
	 "The source node who's primary file sequence is a single MAXScript.", 
	 null);
      addSingleParam(param);
    }

    addSupport(OsType.Windows); 
    removeSupport(OsType.Unix); 

    underDevelopment();
  }


  /*----------------------------------------------------------------------------------------*/
  /*   A C T I O N                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a {@link SubProcessHeavy SubProcessHeavy} instance which when executed will 
   * fulfill the given action agenda. <P> 
   * 
   * @param agenda
   *   The agenda to be accomplished by the action.
   * 
   * @param outFile 
   *   The file to which all STDOUT output is redirected.
   * 
   * @param errFile 
   *   The file to which all STDERR output is redirected.
   * 
   * @return 
   *   The SubProcess which will fulfill the agenda.
   * 
   * @throws PipelineException 
   *   If unable to prepare a SubProcess due to illegal, missing or imcompatable 
   *   information in the action agenda or a general failure of the prep method code.
   */
  public SubProcessHeavy
  prep
  (
   ActionAgenda agenda,
   File outFile, 
   File errFile 
  )
    throws PipelineException
  { 
    /* get the MAXScript script path */ 
    Path scriptPath = getPrimarySourcePath(aMaxScript, agenda, "ms", "MAXScript (.ms) file");
    if(scriptPath == null) 
      throw new PipelineException("The MaxScript node was not specified!");

    /* command line arguments */ 
    ArrayList<String> args = new ArrayList<String>();
    args.add("-U");
    args.add("MAXScript");
    args.add(scriptPath.toOsString());
      
    /* create the process to run the action */
    return createSubProcess(agenda, "3dsmax.exe", args, outFile, errFile);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7740936650142327601L;

  public static final String aMaxScript = "MaxScript"; 

}

