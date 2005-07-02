// $Id: HfsIConvertAction.java,v 1.1 2005/07/02 00:50:28 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   H F S   I C O N V E R T   A C T I O N                                                  */
/*------------------------------------------------------------------------------------------*/

/** 
 * Convert the images files which make up the primary file sequence of one of the source
 * nodes into the image format of the primary file sequence of this node. <P> 
 * 
 * See the <A href="http://www.sidefx.com">Houdini</A> documentation for details on the
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
 *     <DIV style="margin-left: 80px;">
 *       Natural - Use the natural bit depth of the target image format. <BR>
 *       8-Bit (byte) - Integer 8-bits per channel. <BR>
 *       16-Bit (short) - Integer 16-bits per channel. <BR>
 *       16-Bit (half) - Half precision floating point. <BR> 
 *       32-Bit (float) - Full precision floating point. <BR>
 *     </DIV> <BR>
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public
class HfsIConvertAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  HfsIConvertAction() 
  {
    super("HfsIConvert", new VersionID("1.0.0"), 
	  "Converts images files from another format.");
    
    {
      ActionParam param = 
	new LinkActionParam
	("ImageSource",
	 "The source node containing the images to convert.",
	 null);
      addSingleParam(param);
    }

    /* color depth */ 
    {
      ArrayList<String> depth = new ArrayList<String>();
      depth.add("Natural");
      depth.add("8-Bit (byte)");
      depth.add("16-Bit (short)");
      depth.add("16-Bit (half)");
      depth.add("32-Bit (float)");
      
      ActionParam param = 
	new EnumActionParam
	("ColorDepth",
	 "Specifies the bit-depth of pixels in the output image.", 
	 "Natural", depth);
      addSingleParam(param);
    }

    /* layout */ 
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("ImageSource");
      layout.addEntry("ColorDepth");
      
      setSingleLayout(layout);
    }
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
    FileSeq fromSeq = null;
    FileSeq toSeq = null;
    {    
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
	if(suffix == null)
	  throw new PipelineException
	    ("The primary file sequence (" + fseq + ") of the Image Source " + 
	     "(" + sname + ") does not have a filename suffix!");
	  
	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	fromSeq = 
	  new FileSeq(PackageInfo.sProdDir.getPath() + snodeID.getWorkingParent(), fseq);
      }
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if(suffix == null)
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") does not have a " + 
	     "filename suffix!");
	
	toSeq = fseq;
      }
      
      if(fromSeq.numFrames() != toSeq.numFrames()) 
	throw new PipelineException 
	  ("The source file sequence (" + fromSeq + ") did not have the same number of " +
	   "frames as the target file sequence (" + toSeq + ")!");
    }
    
    String options = null;
    {
      StringBuffer buf = new StringBuffer();

      {
	EnumActionParam param = (EnumActionParam) getSingleParam("ColorDepth");
	switch(param.getIndex()) {
	case 0:
	  break;

	case 1:
	  buf.append(" -d byte");
	  break;

	case 2:
	  buf.append(" -d short");
	  break;

	case 3:
	  buf.append(" -d half");
	  break;

	case 4:
	  buf.append(" -d float");
	  break;

	default:
	throw new PipelineException
	  ("Illegal ColorDepth value!");
	}
      }

      options = buf.toString();
    }

    /* create the wrapper shell script */ 
    File script = createTemp(agenda, 0755, "bash");
    try {      
      FileWriter out = new FileWriter(script);
      out.write("#!/bin/bash\n\n");

      int wk;
      for(wk=0; wk<fromSeq.numFrames(); wk++) {
	File fromImage = fromSeq.getFile(wk);
	File toImage   = toSeq.getFile(wk);
	
	out.write("iconvert" + options + " " + fromImage + " " + toImage + "\n");
      }	

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }
    
    try {
      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 script.getPath(), new ArrayList<String>(), 
	 agenda.getEnvironment(), agenda.getWorkingDir(), 
	 outFile, errFile); 
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

  private static final long serialVersionUID = 6683912580476857434L;

}

