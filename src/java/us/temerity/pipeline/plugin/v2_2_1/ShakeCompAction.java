// $Id: ShakeCompAction.java,v 1.1 2007/03/26 23:23:55 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   S H A K E   C O M P   A C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * Executes a Shake script evaluating all FileOut nodes. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Shake Script <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the Shake script to execute.
 *   </DIV> <BR>
 * 
 *   Extra Options <BR>
 *   <DIV style="margin-left: 40px;">
 *     Additional command-line arguments. <BR> 
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public
class ShakeCompAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  ShakeCompAction() 
  {
    super("ShakeComp", new VersionID("2.2.1"), "Temerity",
	  "Executes a Shake script evaluating all FileOut nodes.");
    
    {
      ActionParam param = 
	new LinkActionParam
	(aShakeScript,
	 "The source Shake script node.", 
	 null);
      addSingleParam(param);
    } 

    addExtraOptionsParam(); 

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aShakeScript);   
      layout.addSeparator();
      addExtraOptionsParamToLayout(layout); 

      setSingleLayout(layout);  
    }

    addSupport(OsType.MacOS);
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
    /* the Shake script to evaluate */ 
    Path sourceScript = getPrimarySourcePath(aShakeScript, agenda, "shk", "Shake script");

    /* the target frame range */ 
    FrameRange range = agenda.getPrimaryTarget().getFrameRange();

    /* create the process to run the action */ 
    {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-exec");
      args.add(sourceScript.toOsString());
      
      if(range != null) {
	args.add("-t");
	args.add(range.toString()); 
      }

      args.addAll(getExtraOptionsArgs());

      return createSubProcess(agenda, "shake", args, outFile, errFile);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -7932374861023736683L;

  public static final String aShakeScript = "ShakeScript";

}

