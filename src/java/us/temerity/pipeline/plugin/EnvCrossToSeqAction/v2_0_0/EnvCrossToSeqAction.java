// $Id: EnvCrossToSeqAction.java,v 1.1 2007/06/17 15:34:39 jim Exp $

package us.temerity.pipeline.plugin.EnvCrossToSeqAction.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   E N V   C R O S S   T O   S E Q   A C T I O N                                          */
/*------------------------------------------------------------------------------------------*/

/** 
 * Uses Shake to convert a single vertical cross environment cube image into a sequence of 
 * seperate images for each of the cube faces. <P> 
 * 
 * The primary file sequence of the source node specified by the Image Source parameter should
 * contain a single vertical cross environment cube image.  This image should have a 3:4 
 * aspect ratio and contain the six directional images with the following layout: <BR> 
 * 
 * <DIV style="margin-left: 40px;">
 *   <IMG alt="" src="../../../../../images/EnvCross.jpg">
 * </DIV> <P>
 * 
 * This action will split this vertical cross images into a sequence of six cube face images
 * with the following order and orientation: <BR> 
 * 
 * <DIV style="margin-left: 40px;">
 *   <IMG alt="" src="../../../../../images/EnvCube.0001.jpg">
 *   <IMG alt="" src="../../../../../images/EnvCube.0002.jpg">
 *   <IMG alt="" src="../../../../../images/EnvCube.0003.jpg">
 *   <IMG alt="" src="../../../../../images/EnvCube.0004.jpg">
 *   <IMG alt="" src="../../../../../images/EnvCube.0005.jpg">
 *   <IMG alt="" src="../../../../../images/EnvCube.0006.jpg">
 * </DIV> <P>
 * 
 * This sequence of images can then be converted into RenderMan environment cube textures 
 * using the {@link PREnvCubeAction PREnvCube}, {@link AirEnvCubeAction AirEnvCube} or 
 * {@link DLEnvCubeAction DLEnvCube} actions. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Image Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the vertical cross image to convert. <BR> 
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public
class EnvCrossToSeqAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  EnvCrossToSeqAction() 
  {
    super("EnvCrossToSeq", new VersionID("2.0.0"), "Temerity",
	  "Uses Shake to convert a single vertical cross environment cube image into a " +
	  "sequence of seperate images for each of the cube faces.");
    
    {
      ActionParam param = 
	new LinkActionParam
	("ImageSource",
	 "The source node which contains the vertical cross image to convert.", 
	 null);
      addSingleParam(param);
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

    /* sanity check */ 
    File cross = null; 
    FileSeq target = null;
    {
      /* generate the filename of the source envmap */
      {
	String sname = (String) getSingleParamValue("ImageSource"); 
	if(sname == null) 
	  throw new PipelineException
	    ("The Image Source node was not specified!");
	
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the Image Source node (" + sname + ") was not one of the source nodes!");
	
	if(!fseq.isSingle())
	  throw new PipelineException
	    ("The EnvCrossToSeq Action requires that the source node specified by the Image " +
	     "Source parameter (" + sname + ") must have a single vertical cross environment " + 
	     "map as its primary file sequence!");
	
	NodeID snodeID = new NodeID(nodeID, sname);
	cross = new File(PackageInfo.sProdDir,
			  snodeID.getWorkingParent() + "/" + fseq.getFile(0));
      }

      /* the target image files */ 
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	if(fseq.numFrames() != 6)
	  throw new PipelineException
	    ("The target primary file sequence must contain (6) images!");
	target = new FileSeq(PackageInfo.sProdDir + "/" + nodeID.getWorkingParent(), fseq);
      }
    }
      
    /* create the temporary Shake script */ 
    File script = createTemp(agenda, 0644, "shk");
    try {      
      FileWriter out = new FileWriter(script);

      out.write
	("cross = SFileIn(\"" + cross + "\", \"Auto\", 0);\n" + 
	 "CCrop1 = CCrop(cross, " + 
	 "(cross.width/3)*2, (cross.height/4)*2, (cross.width/3)*3, (cross.height/4)*3);\n" + 
	 "CCrop2 = CCrop(cross, " + 
	 "0, (cross.height/4)*2, (cross.width/3), (cross.height/4)*3);\n" + 
	 "CCrop3 = CCrop(cross, " + 
	 "(cross.width/3), (cross.height/4)*3, (cross.width/3)*2, (cross.height/4)*4);\n" + 
	 "CCrop4 = CCrop(cross, " + 
	 "(cross.width/3), (cross.height/4), (cross.width/3)*2, (cross.height/4)*2);\n" + 
	 "CCrop5 = CCrop(cross, " + 
	 "(cross.width/3), (cross.height/4)*2, (cross.width/3)*2, (cross.height/4)*3);\n" + 
	 "CCrop6 = CCrop(cross, " + 
	 "(cross.width/3), 0, (cross.width/3)*2, (cross.height/4));\n" + 
	 "Orient6 = Orient(CCrop6, 2, 0, 0);\n");

      int wk = 1;
      for(File file : target.getFiles()) {
	out.write("out" + wk + " = FileOut(" + (wk==6 ? "Orient" : "CCrop") + wk + 
		  ", \"" + file + "\");\n");
	wk++;
      }

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary Shake script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create the process to run the action */ 
    try {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-exec");
      args.add(script.getPath());

      args.add("-t");
      args.add("1-6x1"); 

      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "shake", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
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

  private static final long serialVersionUID = -459594474533374805L;

}

