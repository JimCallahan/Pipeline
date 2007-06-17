// $Id: HfsEnvMapAction.java,v 1.1 2007/06/17 15:34:40 jim Exp $

package us.temerity.pipeline.plugin.HfsEnvMapAction.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   H F S   E N V   M A P   A C T I O N                                                    */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates an optimized Houdini latitude/longitude format environment map from six 
 * directional source images. <P> 
 * 
 * If the primary file sequence of the node selected by the Image Source parameter contains
 * six images, they will be interpreted as the [+x, -x, +y, -y, +z, -z] directional images.<P>
 * 
 * See the <A href="http://www.sidefx.com">Houdini</A> documentation for details on the
 * usage and behavior of isixpack(1).<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Image Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the images files to convert. <BR> 
 *   </DIV> <BR>
 * </DIV>
 */
public
class HfsEnvMapAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  HfsEnvMapAction() 
  {
    super("HfsEnvMap", new VersionID("2.0.0"), "Temerity", 
	  "Generates an optimized Houdini latitude/longitude format environment map from " + 
	  "six directional source images or a single vertical cross cube format image.");
    
    {
      ActionParam param = 
	new LinkActionParam
	("ImageSource",
	 "The source node which contains the six image files [+x, -x, +y, -y, +z, -z] " + 
	 "to convert.",
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new IntegerActionParam
	("ImageWidth",
	 "The horizontal resolution of the output texture in pixels.", 
	 1024);
      addSingleParam(param);
    }
      
    {
      ActionParam param = 
	new IntegerActionParam
	("ImageHeight",
	 "The vertical resolution of the output texture in pixels.", 
	 512);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	("Antialias",
	 "Whether to antialias the output texels.", 
	 true);
      addSingleParam(param);
    }
    
    /* layout */ 
    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("ImageSource");
      layout.addSeparator(); 
      layout.addEntry("ImageWidth");
      layout.addEntry("ImageHeight");
      layout.addSeparator(); 
      layout.addEntry("Antialias");
      
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

    /* file sequence checks */ 
    FileSeq fromSeq = null;
    File target = null;
    boolean isDirect = false;
    {
      {    
	String sname = (String) getSingleParamValue("ImageSource"); 
	if(sname == null) 
	  throw new PipelineException
	    ("The Image Source was not set!");
	
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the Image Source (" + sname + ") was not one of the source nodes!");
	
	if(fseq.numFrames() != 6) 
	  throw new PipelineException
	    ("The source file sequence (" + fseq + ") must contain exactly six " + 
	     "[+x, -x, +y, -y, +z, -z] images!");

	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	fromSeq = 
	  new FileSeq(PackageInfo.sProdDir.getPath() + snodeID.getWorkingParent(), fseq);
      }
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if(suffix == null) 
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") must contain a Houdini " + 
	     "latitude/longitude environment map file!");
	
	isDirect = !suffix.equals("rat");

	if(fseq.numFrames() != 1)
	  throw new PipelineException
	    ("The target file sequence (" + fseq + ") must be a single environment map!");
	
	target = fseq.getFile(0);
      }
    }
  

    /* lat/long options */ 
    String options = null;
    {
      StringBuilder buf = new StringBuilder();

      {
	Integer width = (Integer) getSingleParamValue("ImageWidth");
	if(width != null) 
	  buf.append(" -u " + width);
      }

      {
	Integer height = (Integer) getSingleParamValue("ImageHeight");
	if(height != null) 
	  buf.append(" -v " + height);
      }
    
      {
	Boolean tf = (Boolean) getSingleParamValue("Antialias");
	if((tf != null) && tf) 
	  buf.append(" -a");
      }

      options = buf.toString();
    }

    /* create the wrapper shell script */ 
    File script = createTemp(agenda, 0755, "bash");
    try {      
      FileWriter out = new FileWriter(script);
      out.write("#!/bin/bash\n\n" +
		"isixpack" + options + " " +
		fromSeq.getFile(5) + " " +   //   front: -Z
		fromSeq.getFile(0) + " " +   //   right: +X
		fromSeq.getFile(4) + " " +   //    back: +Z
		fromSeq.getFile(1) + " " +   //    left: -X
		fromSeq.getFile(2) + " " +   //     top: +Y
		fromSeq.getFile(3) + " ");   //  bottom: -Y

      if(!isDirect) 
	out.write("stdout | iconvert stdin ");
      
      out.write(target + "\n");
      
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

  private static final long serialVersionUID = -3262838376294654128L;

}

