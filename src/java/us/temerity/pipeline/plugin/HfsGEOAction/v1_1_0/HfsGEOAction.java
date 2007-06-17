// $Id: HfsGEOAction.java,v 1.1 2007/06/17 15:34:41 jim Exp $

package us.temerity.pipeline.plugin.HfsGEOAction.v1_1_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   H F S   G E O   A C T I O N                                                            */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates a sequence of cooked geometry files by evaluating a SOP. <P> 
 * 
 * This action provides a convienent method for evaluating a Geometry output operator 
 * contained in the source Houdini scene using hscript(1).  The target primary file 
 * sequences should contain Houdini geometry files (.geo/.bgeo) which will be generated by 
 * this operator.  The frame range (trange f1 f2 f3) and output filename (sopoutput) 
 * parameters of this operator will be overridden by the Action to correspond to the files
 * to be regenerated by the job. <P> 
 * 
 * See the <A href="http://www.sidefx.com">Houdini</A> documentation for details on the
 * usage and behavior of the Geometry output operator and hscript(1).<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Houdini Scene <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the Houdini scene file to load.  This scene should
 *     contain the Geometry output operator which will be evaluated.
 *   </DIV> <BR>
 * 
 *   Output Operator <BR>
 *   <DIV style="margin-left: 40px;">
 *     The name of the Geometry output operator to evaluate in order to generate the target
 *     geometry files.  Only the last component of the operator name without a (/out) prefix
 *     is required.
 *   </DIV> <BR> 
 * 
 *   Use Graphical License<BR> 
 *   <DIV style="margin-left: 40px;">
 *     Whether to use an interactive graphical Houdini license when running hscript(1).  
 *     Normally, hscript(1) is run using a non-graphical license (-R option).  A graphical 
 *     license may be required if the site has not obtained any non-graphical licenses.
 *   </DIV> 
 * </DIV> <P> 
 */
public
class HfsGEOAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  HfsGEOAction() 
  {
    super("HfsGEO", new VersionID("1.1.0"), "Temerity", 
	  "Generates a sequence of cooked geometry files by evaluating a SOP.");
    
    {
      ActionParam param = 
	new LinkActionParam
	("HoudiniScene",
	 "The source Houdini scene node.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	("OutputOperator",
	 "The name of the Geometry output operator", 
	 "geometry1");
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	("UseGraphicalLicense",
	 "Whether to use an interactive graphical Houdini license when running hscript(1).",
	 false);
      addSingleParam(param);
    }

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("HoudiniScene");
      layout.addEntry("OutputOperator");
      layout.addSeparator();
      layout.addEntry("UseGraphicalLicense");

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
    File source = null;
    String opname = null;
    {
      /* generate the filename of the Houdini scene to load */ 
      {
	String sname = (String) getSingleParamValue("HoudiniScene"); 
	if(sname != null) {
	  FileSeq fseq = agenda.getPrimarySource(sname);
	  if(fseq == null) 
	    throw new PipelineException
	      ("Somehow the Houdini Scene node (" + sname + ") was not one of the source " + 
	       "nodes!");
	  
	  String suffix = fseq.getFilePattern().getSuffix();
	  if(!fseq.isSingle() || (suffix == null) || !suffix.equals("hip"))
	    throw new PipelineException
	      ("The HfsGEO Action requires that the source node specified by the Houdini " +
	       "Scene parameter (" + sname + ") must have a single Houdini scene file as " + 
	       "its primary file sequence!");

	  NodeID snodeID = new NodeID(nodeID, sname);
	  source = new File(PackageInfo.sProdDir,
			    snodeID.getWorkingParent() + "/" + fseq.getFile(0));
	}
      }

      /* the full name of the geometry output operator */ 
      {
	String name = (String) getSingleParamValue("OutputOperator"); 
	if((name == null) || (name.length() == 0))
	  throw new PipelineException
	    ("The HfsGEO Action requires a valid Output Operator name!");

	opname = ("/out/" + name);
      }

      /* validate the target file sequence */ 
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if((suffix == null) || !(suffix.equals("geo") || suffix.equals("bgeo"))) 
	  throw new PipelineException
	    ("The HfsGEO Action requires that the primary target file sequence must " + 
	     "contain Houdini geometry (.geo/.bgeo) files!");
      }
    }

    /* create the temporary Houdini command script */ 
    File hscript = createTemp(agenda, 0644, "cmd");
    try {      
      FileWriter out = new FileWriter(hscript);
    
      FileSeq fseq = new FileSeq(PackageInfo.sProdDir.getPath() + nodeID.getWorkingParent(), 
				 agenda.getPrimaryTarget());
      if(fseq.hasFrameNumbers()) {
	FilePattern fpat = fseq.getFilePattern();
	FrameRange frange = fseq.getFrameRange();
	out.write("opparm " + opname + " trange on\n" +
		  "opparm " + opname + " f1 " + frange.getStart() + "\n" +
		  "opparm " + opname + " f2 " + frange.getEnd() + "\n" +
		  "opparm " + opname + " f3 " + frange.getBy() + "\n" +
		  "opparm " + opname + " sopoutput '" + fpat.getPrefix() + ".$F");

	if(fpat.getPadding() > 1) 
	  out.write(String.valueOf(fpat.getPadding()));
	
	out.write("." + fpat.getSuffix() + "'\n");
      }
      else {
	out.write("opparm " + opname + " trange off\n" +
		  "opparm " + opname + " " + fseq + "\n");
      }

      out.write("opparm -c " + opname + " execute\n");

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary script file (" + hscript + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* license type */ 
    String licopt = " -R";
    {
      Boolean tf = (Boolean) getSingleParamValue("UseGraphicalLicense"); 
      if((tf != null) && tf)
	licopt = "";
    }

    /* create the wrapper shell script */ 
    File script = createTemp(agenda, 0755, "bash");
    try {      
      FileWriter out = new FileWriter(script);
      out.write("#!/bin/bash\n\n" +
		"cat " + hscript + " | hscript" + licopt + " -v " + source);      
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

  private static final long serialVersionUID = 609492265479189679L;

}

