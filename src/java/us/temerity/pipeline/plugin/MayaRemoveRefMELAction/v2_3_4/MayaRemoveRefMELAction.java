// $Id: MayaRemoveRefMELAction.java,v 1.1 2007/07/19 04:53:27 jesse Exp $

package us.temerity.pipeline.plugin.MayaRemoveRefMELAction.v2_3_4;

import java.io.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.CommonActionUtils;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   R E M O V E   R E F   M E L   A C T I O N                                    */
/*------------------------------------------------------------------------------------------*/


/**
 * Creates a MEL script which, when run, removes all references from a scene.
 */
public 
class MayaRemoveRefMELAction
  extends CommonActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  MayaRemoveRefMELAction() 
  {
    super("MayaRemoveRefMEL", new VersionID("2.3.4"), "Temerity",
          "Creates a MEL script that removes all references from a scene.");
    
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
    /* target MEL script */ 
    Path target = getPrimaryTargetPath(agenda, "mel", "MEL script");

    /* create a temporary file which will be copied to the target */ 
    File temp = createTemp(agenda, "mel");
    try {      
      FileWriter out = new FileWriter(temp);
      
      out.write
        ("{\n" + 
	 "  string $files[] = `file -q -r`;\n" + 
	 "  while (size($files) > 0)\n" + 
	 "  {\n" + 
	 "    $file = $files[0];\n" + 
	 "    file -rr $file;\n" + 
	 "    $files = `file -q -r`;\n" + 
	 "  }\n" + 
         "}");
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
        ("Unable to write the target MEL script file (" + temp + ") for Job " + 
         "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    }

    /* create the process to run the action */ 
    return createTempCopySubProcess(agenda, temp, target, outFile, errFile);
  }
  
  
  
  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  private static final long serialVersionUID = -4664702227016367430L;
}
