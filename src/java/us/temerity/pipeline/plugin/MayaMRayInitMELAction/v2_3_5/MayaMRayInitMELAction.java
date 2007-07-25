package us.temerity.pipeline.plugin.MayaMRayInitMELAction.v2_3_5;

import java.io.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.CommonActionUtils;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   M R A Y   I N I T   M E L   A C T I O N                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Creates a Mel script which will initialize the mental ray render globals in a
 * scene. 
 */
public 
class MayaMRayInitMELAction
  extends CommonActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  MayaMRayInitMELAction() 
  {
    super("MayaMRayInitMEL", new VersionID("2.3.5"), "Temerity",
          "Creates a MEL script that initializes mental ray in a scene.");
    
    addSupport(OsType.MacOS); 
    addSupport(OsType.Windows); 
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
        ("global proc mentalRayLoad()\n" + 
         "{\n" + 
         "  if (`pluginInfo -q -loaded Mayatomr` == 0) {\n" + 
         "    print \"\\nMentalRay isn\'t loaded, now loading MentalRay\";\n" + 
         "    loadPlugin Mayatomr;\n" + 
         "    miCreateGlobalsNode;\n" + 
         "    miCreateDefaultNodes;\n" + 
         "    miCreateOtherOptionsNodesForURG;\n" + 
         "  }\n" + 
         "  else {\n" + 
         "    print \"\\nMentalRay already loaded, setting as current renderer\";\n" + 
         "    setAttr defaultRenderGlobals.currentRenderer -type \"string\" \"mentalRay\";\n" + 
         "  }\n" + 
         "}\n\n");
      out.write
        ("mentalRayLoad;\n\n");
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

  private static final long serialVersionUID = -3481134444538597292L;

}
