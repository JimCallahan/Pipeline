// $Id: HfsReadCmdAction.java,v 1.1 2008/03/20 21:25:13 jim Exp $

package us.temerity.pipeline.plugin.HfsReadCmdAction.v2_4_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.PythonActionUtils; 
import us.temerity.pipeline.plugin.HfsActionUtils; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*    H F S   R E A D   C M D   A C T I O N                                                 */
/*------------------------------------------------------------------------------------------*/

/**
 * Generates a Houdini command script which sets a filename parameter of an operator in a 
 * Houdini scene so that it will read the files associated with a given source node.<P> 
 * 
 * Often it can be useful to have a master Houdini scene file used in a number of contexts
 * where the only difference is the names of the input files being read by the scene.  This
 * action provides a way of setting these file names within the Houdini scene by generating 
 * a small Houdini command script which uses "opparm" to set the 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   File Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the files to be read by the given Houdini operator.
 *   </DIV> <BR>
 * 
 *   Operator Name <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the Houdini operator who's parameter which will be set by the generated
 *     command file.<P> 
 *   </DIV> <BR>
 * 
 *   Parameter Name <BR> 
 *   <DIV style="margin-left: 40px;">
 *     The name of the Houdini operator's parameter which will be set by the generated
 *     command file.<P> 
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public
class HfsReadCmdAction
  extends HfsActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  HfsReadCmdAction() 
  {
    super("HfsReadCmd", new VersionID("2.4.1"), "Temerity",
	  "Generates a Houdini command script which sets a filename parameter of an " + 
          "operator in a Houdini scene so that it will read the files associated with " + 
          "a given source node."); 

    {
      ActionParam param = 
	new LinkActionParam
	(aFileSource,
	 "The source node which contains the files to be read by the given Houdini " + 
         "operator.", 
	 null);
      addSingleParam(param);
    } 

    {
      ActionParam param = 
	new StringActionParam
	(aOperatorName, 
	 "The name of the Houdini operator who's parameter which will be set by the  " +
         "generated command file.", 
	 null);
      addSingleParam(param);
    } 

    {
      ActionParam param = 
	new StringActionParam
	(aParameterName, 
	 "The name of the Houdini operator's parameter which will be set by the generated " + 
         "command file.", 
	 null);
      addSingleParam(param);
    } 

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aFileSource);
      layout.addEntry(aOperatorName);
      layout.addEntry(aParameterName);

      setSingleLayout(layout);
    }

    //addSupport(OsType.Windows);

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
    /* target files */ 
    Path target = getHoudiniCommandTargetPath(agenda); 

    /* source images */ 
    Path sourcePath = null;
    FileSeq sourceSeq = null;
    {
      String sname = getSingleStringParamValue(aFileSource); 
      if(sname != null) {
        FileSeq fseq = agenda.getPrimarySource(sname);
        if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the " + aFileSource + " (" + sname + ") was not one of the " + 
             "source nodes!");
        
        sourceSeq = fseq;
        
        Path spath = new Path(sname);
        sourcePath = new Path("$WORKING" + spath.getParent());
      }
    }

    /* create the temporary Houdini command script */ 
    File hscript = createTemp(agenda, "cmd");
    try {      
      FileWriter out = new FileWriter(hscript);

      String opname = getSingleStringParamValue(aOperatorName, false);
      String pname  = getSingleStringParamValue(aParameterName, false);      
      String expr = (sourcePath + "/" + toHoudiniFilePattern(sourceSeq.getFilePattern()));
      writeStringOpparm(opname, pname, expr, out); 

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary script file (" + hscript + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* just copy the temporary command file to the target location */ 
    return createTempCopySubProcess(agenda, hscript, target, outFile, errFile);
  }
    


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 2736950469873087724L;

  public static final String aFileSource    = "FileSource";
  public static final String aOperatorName  = "OperatorName";
  public static final String aParameterName = "ParameterName"; 

}

