package us.temerity.pipeline.plugin.EmptyFBXAction.v2_3_3;


import java.io.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.plugin.MayaActionUtils;

/*------------------------------------------------------------------------------------------*/
/*   E M P T Y   F B X   A C T I O N                                                        */
/*------------------------------------------------------------------------------------------*/

public 
class EmptyFBXAction 
  extends MayaActionUtils
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  public
  EmptyFBXAction()
  {
    super("EmptyFBX", new VersionID("2.3.3"), "Temerity",
          "An Action to make an empty FBX file.");
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
    Path targetScene = getPrimaryTargetPath(agenda, "fbx", "The FBX File");

    /* create a temporary MEL script used to export the shaders */ 
    File script = createTemp(agenda, "mel");
    try {
      FileWriter out = new FileWriter(script);
      
      /* load the fbx plugin */ 
      out.write("if (!`pluginInfo -q -l fbxmaya`)\n" +
                "  loadPlugin \"fbxmaya\";\n\n");
      
      out.write("eval FBXExportShowUI -v false;\n" + 
                "eval \"FBXExport -f \\\"" + targetScene + "\\\"\";\n");
      
      out.write("print \"ALL DONE.\\n\";\n");
      
      out.close();
    } 
    catch (IOException ex) {
      throw new PipelineException
        ("Unable to write temporary MEL script file (" + script + ") for Job " + 
         "(" + agenda.getJobID() + ")!\n" +
         ex.getMessage());
    }
    
    /* create the process to run the action */
    return createMayaSubProcess(null, script, true, agenda, outFile, errFile);
  }
  private static final long serialVersionUID = -3590475478155561378L;
}
