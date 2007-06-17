// $Id: DLShadowAction.java,v 1.1 2007/06/17 15:34:39 jim Exp $

package us.temerity.pipeline.plugin.DLShadowAction.v2_2_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   D L   S H A D O W   A C T I O N                                                        */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates optimized 3Delight shadow maps from Z-depth source images. <P> 
 * 
 * Converts the Z-depth images (.z) which make up the primary file sequence of one of the 
 * source nodes into the shadow maps (.shd) which make up the primary file sequence of this 
 * node. <P>
 * 
 * See the <A href="http://www.3delight.com">3Delight</A> documentation for 
 * <A href="http://www.3delight.com/ZDoc/3delight_12.html"><B>tdlmake</B></A>(1) for 
 * details. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   ZFile Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the Z-depth images files to convert. <BR> 
 *   </DIV> <BR>
 * </DIV><P> 
 * 
 * By default, the "python" program is used by this action to run the "tdlmake" commands.  
 * An alternative program can be specified by setting PYTHON_BINARY in the Toolset 
 * environment to the name of the Python interpertor this Action should use.  When naming an 
 * alternative Python interpretor under Windows, make sure to include the ".exe" extension 
 * in the program name.
 */
public
class DLShadowAction
  extends PythonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  DLShadowAction() 
  {
    super("DLShadow", new VersionID("2.2.1"), "Temerity", 
	  "Generates optimized 3Delight shadow maps from Z-depth source images.");
    
    {
      ActionParam param = 
	new LinkActionParam
	(aZFileSource,
	 "The source node which contains the Z-depth image files to convert.",
	 null);
      addSingleParam(param);
    }
    
    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);

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
    /* file sequence checks */ 
    Path sourcePath = null;
    FileSeq sourceSeq = null;
    FileSeq targetSeq = null;
    {
      {    
	String sname = getSingleStringParamValue(aZFileSource); 
	if(sname == null) 
	  throw new PipelineException
	    ("The ZFile Source was not set!");
	
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the " + aZFileSource + " (" + sname + ") was not one of the " + 
             "source nodes!");
	
	String suffix = fseq.getFilePattern().getSuffix();
	if((suffix == null) || !suffix.equals("z")) 
	  throw new PipelineException
	    ("The source primary file sequence (" + fseq + ") must contain depth maps (.z)!");

	sourceSeq = fseq;
	
	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	sourcePath = new Path(PackageInfo.sProdPath, snodeID.getWorkingParent());
      }
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if((suffix == null) || !suffix.equals("shd"))
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") must contain 3Delight " + 
	     "shadow maps (.shd)!");
	
	targetSeq = fseq;
      }

      if(sourceSeq.numFrames() != targetSeq.numFrames()) 
	throw new PipelineException 
	  ("The source primary file sequence (" + sourceSeq + ") does not have the same" + 
	   "number of frames as the target primary file sequence (" + targetSeq + ")!");
    }

    /* build command-line arguments */ 
    ArrayList<String> args = new ArrayList<String>();
    args.add("-shadow"); 

    /* texture conversion program */ 
    String program = "tdlmake";
    if(PackageInfo.sOsType == OsType.Windows) 
      program = "tdlmake.exe";
    
    /* create the process to run the action */ 
    return createPythonSubProcess(agenda, program, args, sourcePath, sourceSeq, targetSeq, 
                                  null, outFile, errFile); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6610858775395423913L;

  public static final String aZFileSource  = "ZFileSource";

}

