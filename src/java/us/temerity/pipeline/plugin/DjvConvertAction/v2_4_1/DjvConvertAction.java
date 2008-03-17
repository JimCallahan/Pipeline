// $Id: DjvConvertAction.java,v 1.1 2008/03/17 23:19:15 jim Exp $

package us.temerity.pipeline.plugin.DjvConvertAction.v2_4_1;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   I M G   C V T   A C T I O N                                                            */
/*------------------------------------------------------------------------------------------*/

/** 
 * Convert the images files which make up the primary file sequence of one of the source
 * nodes into the image format of the primary file sequence of this node using
 * <A HREF="http://djv.sourceforge.net/index.html">DJV Imaging</A>.<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Image Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the images to convert. <BR> 
 *   </DIV> 
 * </DIV> <P> 
 * 
 * See the documentation for <A HREF="http://djv.sourceforge.net/djv_convert.html">
 * djv_convert</A> for details on the underlying capabilities of DJV Imaging used by 
 * this action.<P> 
 *
 * This action uses default load and save options, but DJV is capable of a higher level 
 * of control using more specific values for these options.  When this level of control 
 * is desired another action plugin should be created which provides these specific controls 
 * as action parameters.  An example of actions which do this are the DjvQt and DjvUnixQt
 * actions.
 */
public
class DjvConvertAction
  extends DjvActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  DjvConvertAction() 
  {
    super("DjvConvert", new VersionID("2.4.1"), "Temerity",
	  "Converts images files from another format using DJV Imaging.");
    
    {
      ActionParam param = 
	new LinkActionParam
	("ImageSource",
	 "The source node containing the images to convert.",
	 null);
      addSingleParam(param);
    }

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
    NodeID nodeID = agenda.getNodeID();

    /* sanity checks */ 
    String sourceImages = null;
    String targetImages = null;
    {    
      FileSeq sourceSeq = null;
      {
	String sname = (String) getSingleParamValue("ImageSource"); 
	if(sname == null) 
	  throw new PipelineException
	    ("The Image Source was not set!");

	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the Image Source (" + sname + ") was not one of the source " + 
	     "nodes!");

	String suffix = fseq.getFilePattern().getSuffix();
	if((suffix == null) || !getDjvExtensions().contains(suffix)) 
	  throw new PipelineException
	    ("The primary file sequence (" + fseq + ") of the Image Source " + 
	     "(" + sname + ") does not contain a supported image format!");
	  
	sourceSeq = fseq;

	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	Path spath = new Path(PackageInfo.sProdPath, snodeID.getWorkingParent()); 
        Path path = new Path(spath, toDjvFileSeq(fseq));

        sourceImages = path.toOsString();
      }
	
      FileSeq targetSeq = null;
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if((suffix == null) || !getDjvExtensions().contains(suffix))
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") is not a supported " + 
	     "output image format!");

	targetSeq = fseq;
        
	Path tpath = new Path(PackageInfo.sProdPath, agenda.getNodeID().getWorkingParent()); 
        Path path = new Path(tpath, toDjvFileSeq(fseq));

        targetImages = path.toOsString();        
      }

      if((sourceSeq.numFrames() > 1) && (targetSeq.numFrames() > 1) && 
         (sourceSeq.numFrames() != targetSeq.numFrames()))  
	throw new PipelineException 
	  ("The multi-frame source file sequence (" + sourceSeq + ") did not have the " + 
           "same number of frames as the multi-frame target file sequence " + 
           "(" + targetSeq + ")!");
    }
    
    /* create the process to run the action */ 
    try {
      String program = "djv_convert";
      if(PackageInfo.sOsType == OsType.Windows) 
	program = "djv_convert.exe";

      ArrayList<String> args = new ArrayList<String>();
      args.add(sourceImages); 
      args.add(targetImages); 

      return createSubProcess(agenda, program, args, outFile, errFile);
    }
    catch(Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to perform this Action!\n" +
	 ex.getMessage());
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  //private static final long serialVersionUID = 

}

