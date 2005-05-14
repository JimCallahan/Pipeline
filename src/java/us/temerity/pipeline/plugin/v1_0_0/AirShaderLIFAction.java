// $Id: AirShaderLIFAction.java,v 1.1 2005/05/14 08:32:05 jim Exp $

package us.temerity.pipeline.plugin.v1_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   A I R   S H A D E R   L I F   A C T I O N                                              */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates a LIF shader information file used by RhinoMan, PaxRendus and Liquid for 
 * a compiled AIR shader. <P> 
 * 
 * Reads a compiled AIR shader (.slb) or VShade source file (.vsl) which is the single 
 * member of the primary file sequence of one of the source nodes into order to generate
 * a LIF shader information file (.lif) which describes the parameters of the shader.<P> 
 * 
 * See the <A href="http://www.sitexgraphics.com/html/air.html">AIR</A> documentation for 
 * <A href="http://www.sitexgraphics.com/air.pdf"><B>makelif</B></A>(1) for details. <P>
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Shader <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the compiled AIR shader.<BR>
 *   </DIV> 
 * </DIV>
 */
public
class AirShaderLIFAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  AirShaderLIFAction() 
  {
    super("AirShaderLIF", new VersionID("1.0.0"), 
	  "Generates a LIF shader information file used by RhinoMan, PaxRendus and Liquid " + 
	  "for a compiled AIR shader.");
    
    {
      ActionParam param = 
	new LinkActionParam
	("Shader",
	 "The source node which contains the compiled AIR shader or VShade source file.", 
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

    /* file sequence checks */ 
    File source = null; 
    File target = null; 
    String prefix = null;
    {
      {    
	String sname = (String) getSingleParamValue("Shader"); 
	if(sname == null) 
	  throw new PipelineException
	    ("The Shader was not set!");
	
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the Shader (" + sname + ") was not one of the source nodes!");

	FilePattern fpat = fseq.getFilePattern();
	prefix = fpat.getPrefix();
	String suffix = fpat.getSuffix();
	if((suffix == null) || !(suffix.equals("slb") || suffix.equals("vsl")) || 
	   (fseq.numFrames() != 1))
	  throw new PipelineException
	    ("The source primary file sequence (" + fseq + ") must contain a single " + 
	     "compiled AIR shader (.slb) or VShade source (.vsl) file.");

	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	source = new File(PackageInfo.sProdDir, 
			  snodeID.getWorkingParent() + "/" + fseq.getFile(0));
      }
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	FilePattern fpat = fseq.getFilePattern();
	String suffix = fpat.getSuffix();
	if((suffix == null) || !suffix.equals("lif") || (fseq.numFrames() != 1))
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") must contain a single " + 
	     "LIF shader information (.lif) file.");

	target = fseq.getFile(0);

	if(!prefix.equals(fpat.getPrefix())) 
	  throw new PipelineException
	    ("The shader file (" + source + ") and generated shader information file " + 
	     "(" + target + ") must have the same prefix.");
      }
    }


    try {
      ArrayList<String> args = new ArrayList<String>();
      args.add(target.toString());
      args.add(source.toString());
      
      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "makelif", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
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

  private static final long serialVersionUID = 8105241565506088895L;

}

