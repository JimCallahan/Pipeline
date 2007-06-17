// $Id: ShakeQtAction.java,v 1.1 2007/06/17 15:34:46 jim Exp $

package us.temerity.pipeline.plugin.ShakeQtAction.v2_0_9;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   S H A K E   Q T   A C T I O N                                                          */
/*------------------------------------------------------------------------------------------*/

/** 
 * Creates a QuickTime movie from a sequence of images using Shake. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Images <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the images used to create the movie. 
 *   </DIV> <BR>
 * 
 *   FPS <BR>
 *   <DIV style="margin-left: 40px;">
 *     The number of image frames per second.
 *   </DIV>
 * </DIV> <P> 
 */
public
class ShakeQtAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  ShakeQtAction() 
  {
    super("ShakeQt", new VersionID("2.0.9"), "Temerity",
	  "Creates a QuickTime movie from a sequence of images using Shake."); 

    {
      ActionParam param = 
	new LinkActionParam
	("Images",
	 "The source which contains the images used to create the movie.", 
	 null);
      addSingleParam(param);
    } 

    {
      ActionParam param = 
	new DoubleActionParam
	("FPS",
	 "The number of image frames per second.",
	 null);
      addSingleParam(param);
    }

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("Images");   
      layout.addEntry("FPS"); 

      setSingleLayout(layout);  
    }

    removeSupport(OsType.Unix);
    addSupport(OsType.MacOS);
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
    Path target = null; 
    Path spath = null;
    FileSeq source = null;
    {
      /* get the input image file sequence to load */
      {
	String sname = (String) getSingleParamValue("Images"); 
	if(sname == null) 
	  throw new PipelineException
	    ("The input Images node was not specified!");
	
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the input Images node (" + sname + ") was not one of the " + 
	     "source nodes!");
	
	NodeID snodeID = new NodeID(nodeID, sname);
	spath = new Path(PackageInfo.sProdPath, snodeID.getWorkingParent()); 
	source = fseq; 
      }

      /* get the target movie file */ 
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if(!fseq.isSingle() || 
	   (suffix == null) || !(suffix.equals("mov"))) 
	  throw new PipelineException
	    ("The ShakeQt Action requires that the primary file sequence (" + fseq + ") " + 
	     "is a QuickTime movie file (.mov)!");
	target = fseq.getPath(0);
      }
    }

    /* create temporary movie and script files */ 
    File movie = createTemp(agenda, 0644, "mov");
    File script = createTemp(agenda, 0755, "bash");
    try {      
      FileWriter out = new FileWriter(script);

      out.write("if shake -v"); 

      Double fps = (Double) getSingleParamValue("FPS");
      if(fps != null)
	out.write(" -fps " + fps);
     
      out.write(" -fi "); 
      if(source.isSingle()) {
	Path path = new Path(spath, source.getPath(0));
	out.write(path.toOsString()); 
      }
      else {
	out.write(spath.toOsString() + "/" + source.getFilePattern() + 
		  " -t " + source.getFrameRange());
      }

      out.write(" -fo " + movie + "\n" + 
		"then\n" + 
		"  cp -f " + movie + " " + target.toOsString() + "\n" + 
		"else\n" + 
		"  echo 'Unable to generate movie!'\n" + 
		"  exit 1\n" + 
		"fi\n");

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }
    
    try {
      ArrayList<String> args = new ArrayList<String>();
      args.add(script.toString());
      
      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "bash", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
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

  private static final long serialVersionUID = 7923488519644774264L;

}

