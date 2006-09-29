// $Id: HfsEnvCubeAction.java,v 1.6 2006/09/29 03:03:21 jim Exp $

package us.temerity.pipeline.plugin.v2_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   H F S   E N V   C U B E   A C T I O N                                                  */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates an optimized Houdini cube faced environment map from six directional source 
 * images or a single vertical cross cube format image. <P> 
 * 
 * If the primary file sequence of the node selected by the Image Source parameter contains
 * six images, they will be interpreted as the [+x, -x, +y, -y, +z, -z] directional images.
 * Alternatively, if this sequence contains only a single image, this image should have a 3:4 
 * aspect ratio and contain the six directional images arranged in the following manner: <P> 
 * 
 * <DIV style="margin-left: 40px;">
 *   <IMG alt="" src="../../../../../images/EnvMapCross.gif">
 * </DIV> <P>
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
class HfsEnvCubeAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  HfsEnvCubeAction() 
  {
    super("HfsEnvCube", new VersionID("2.0.0"), "Temerity",
	  "Generates an optimized Houdini cubic faced environment map from six " + 
	  "directional source images or a single vertical cross cube format image.");
    
    {
      ActionParam param = 
	new LinkActionParam
	("ImageSource",
	 "The source node which contains the six image files [+x, -x, +y, -y, +z, -z] " + 
	 "or single vertical cross cube format image to convert.",
	 null);
      addSingleParam(param);
    }
    
    /* layout */ 
    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("ImageSource");
      
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
	
	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);

	switch(fseq.numFrames()) {
	case 1:
	case 6:
	  fromSeq = 
	    new FileSeq(PackageInfo.sProdDir.getPath() + snodeID.getWorkingParent(), 
			fseq);
	  break;

	default:
	  throw new PipelineException
	    ("The source file sequence (" + fseq + ") must contain either exactly six " + 
	     "[+x, -x, +y, -y, +z, -z] images or a single single vertical cross cube " + 
	     "format image!");
	}
      }
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if((suffix == null) || !suffix.equals("rat"))
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") must contain a Houdini " + 
	     "cubic environment map (.rat) file!");
	
	if(fseq.numFrames() != 1)
	  throw new PipelineException
	    ("The target file sequence (" + fseq + ") must be a single environment map!");
	
	target = fseq.getFile(0);
      }
    }


    try {
      ArrayList<String> args = new ArrayList<String>();
      
      switch(fromSeq.numFrames()) {
      case 1:
	args.add(fromSeq.getFile(0).toString());
	break;

      case 6:
	args.add(fromSeq.getFile(5).toString());   //  front: -Z
	args.add(fromSeq.getFile(0).toString());   //  right: +X
	args.add(fromSeq.getFile(4).toString());   //   back: +Z
	args.add(fromSeq.getFile(1).toString());   //   left: -X
	args.add(fromSeq.getFile(2).toString());   //    top: +Y
	args.add(fromSeq.getFile(3).toString());   // bottom: -Y
	break;

      default:
	throw new IllegalStateException(); 
      }

      args.add(target.toString());
      
      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "isixpack", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
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

  private static final long serialVersionUID = -3262838376294654129L;

}

