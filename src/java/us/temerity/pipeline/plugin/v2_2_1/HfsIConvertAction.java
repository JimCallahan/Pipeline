// $Id: HfsIConvertAction.java,v 1.1 2007/04/07 01:11:24 jim Exp $

package us.temerity.pipeline.plugin.v2_2_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   H F S   I C O N V E R T   A C T I O N                                                  */
/*------------------------------------------------------------------------------------------*/

/** 
 * Houdini image conversion utility. <P> 
 * 
 * Convert the images files which make up the primary file sequence of one of the source
 * nodes into the image format of the primary file sequence of this node. <P> 
 * 
 * See the <A href="http://www.sidefx.com"><B>Houdini</B></A> documentation for details on the
 * usage and behavior of iconvert(1).<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Image Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the images to convert. <BR> 
 *   </DIV> 
 * 
 *   Color Depth <BR>
 *   <DIV style="margin-left: 40px;">
 *     Specifies the bit-depth of pixels in the output image:<BR>
 *     <DIV style="margin-left: 40px;">
 *       Natural - Use the natural bit depth of the target image format. <BR>
 *       8-Bit (byte) - Integer 8-bits per channel. <BR>
 *       16-Bit (short) - Integer 16-bits per channel. <BR>
 *       16-Bit (half) - Half precision floating point. <BR> 
 *       32-Bit (float) - Full precision floating point. <BR>
 *     </DIV> <BR>
 *   </DIV> <BR>
 * </DIV> <P> 
 * 
 * By default, the "python" program is used by this action to run the "iconvert" commands.  
 * An alternative program can be specified by setting PYTHON_BINARY in the Toolset 
 * environment to the name of the Python interpertor this Action should use.  When naming an 
 * alternative Python interpretor under Windows, make sure to include the ".exe" extension 
 * in the program name.
 */
public
class HfsIConvertAction
  extends PythonActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  HfsIConvertAction() 
  {
    super("HfsIConvert", new VersionID("2.2.1"), "Temerity",
	  "Converts images files from another format.");
    
    {
      ActionParam param = 
	new LinkActionParam
	(aImageSource,
	 "The source node containing the images to convert.",
	 null);
      addSingleParam(param);
    }

    {
      ArrayList<String> depth = new ArrayList<String>();
      depth.add("Natural");
      depth.add("8-Bit (byte)");
      depth.add("16-Bit (short)");
      depth.add("16-Bit (half)");
      depth.add("32-Bit (float)");
      
      ActionParam param = 
	new EnumActionParam
	(aColorDepth,
	 "Specifies the bit-depth of pixels in the output image.", 
	 "Natural", depth);
      addSingleParam(param);
    }

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aImageSource);
      layout.addEntry(aColorDepth);
      
      setSingleLayout(layout);
    }

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
    /* file sequence checks */ 
    Path sourcePath = null;
    FileSeq sourceSeq = null;
    FileSeq targetSeq = null;
    {
      {    
	String sname = getSingleStringParamValue(aImageSource); 
	if(sname == null) 
	  throw new PipelineException
	    ("The Image Source was not set!");
	
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the " + aImageSource + " (" + sname + ") was not one of the " + 
             "source nodes!");
	
	sourceSeq = fseq;
	
	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	sourcePath = new Path(PackageInfo.sProdPath, snodeID.getWorkingParent());
      }
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if(suffix == null)
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") does not have a " + 
	     "filename suffix!");
	
	targetSeq = fseq;
      }

      if(sourceSeq.numFrames() != targetSeq.numFrames()) 
	throw new PipelineException 
	  ("The source primary file sequence (" + sourceSeq + ") does not have the same" + 
	   "number of frames as the target primary file sequence (" + targetSeq + ")!");
    }

    /* build common command-line arguments */ 
    ArrayList<String> args = new ArrayList<String>();
    {
      switch(getSingleEnumParamIndex(aColorDepth)) {
      case 0:
        break;
        
      case 1:
        args.add("-d");
        args.add("byte");
        break;
        
      case 2:
        args.add("-d");
        args.add("short");
        break;
        
      case 3:
        args.add("-d");
        args.add("half");
        break;
        
      case 4:
        args.add("-d");
        args.add("float");
        break;
        
      default:
	throw new PipelineException
	  ("Illegal ColorDepth value!");
      }
    }

    /* image conversion program */ 
    String program = "iconvert";
    if(PackageInfo.sOsType == OsType.Windows) 
      program = "iconvert.exe";
    
    /* create the process to run the action */ 
    return createPythonSubProcess(agenda, program, args, sourcePath, sourceSeq, targetSeq, 
                                  null, outFile, errFile); 
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8440271782178625699L;

  public static final String aImageSource = "ImageSource";
  public static final String aColorDepth  = "ColorDepth";

}

