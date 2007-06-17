// $Id: CompositeAction.java,v 1.1 2007/06/17 15:34:38 jim Exp $

package us.temerity.pipeline.plugin.CompositeAction.v1_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   C O M P O S E   A C T I O N                                                            */
/*------------------------------------------------------------------------------------------*/

/** 
 * Performs a series of simple image compositing operations. <P> 
 * 
 * The target primary file sequence should contain image files.  Each of these target
 * files will be generated by starting with the corresponding primary file sequence image of 
 * the Background source node and applying the compositing Operation for each source node in 
 * ascending Order.  Only source nodes which set the Order and Operation per-source parameters
 * will be processed.  If the primary file sequence of any of these sources do not contain
 * a corresponing image file to the target, the compositing operation will be skipped for 
 * that frame.  <P> 
 * 
 * The background, source and target images are assumed to have the same image resultion.   
 * However, no checking is done to insure this is the case. <P> 
 * 
 * All compositing operations are performed using the ImageMagick composite(1) command line 
 * utility.  See the man pages for composite(1) and ImageMagick(1) for details of the 
 * supported image formats and compositing operations. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Background <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the images to use as the bottom layer of the 
 *     compositing operations. <BR> 
 *   </DIV> 
 * </DIV> <P> 
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Order <BR>
 *   <DIV style="margin-left: 40px;">
 *     Each source node which sets this parameter should have a primary file sequence 
 *     containing image files.  This parameter determines the order that the compositing
 *     operation specified by Operation is applied.  If this parameter is not set for a 
 *     source node, it will be ignored.
 *   </DIV> <BR> 
 * 
 *   Operation <BR>
 *   <DIV style="margin-left: 40px;">
 *     The type of compositing operation to use when combining the background (or result
 *     of the previous operation) and the image file of the source node.  
 *   </DIV> <BR> 
 * </DIV> <P> 
 */
public
class CompositeAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  CompositeAction() 
  {
    super("Composite", new VersionID("1.0.0"), "Temerity", 
	  "Performs a series of simple image compositing operations.");
    
    {
      ActionParam param = 
	new LinkActionParam
	("Background",
	 "The source node containing the background images.",
	 null);
      addSingleParam(param);
    }

    {
      LinkedList<String> layout = new LinkedList<String>();
      layout.add("Order");
      layout.add("Operation"); 

      setSourceLayout(layout);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   B A S E   A C T I O N   O V E R R I D E S                                            */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Does this action support per-source parameters?  
   */ 
  public boolean 
  supportsSourceParams()
  {
    return true;
  }
  
  /**
   * Get an initial set of action parameters associated with an upstream node. 
   */ 
  public TreeMap<String,ActionParam>
  getInitialSourceParams()
  {
    TreeMap<String,ActionParam> params = new TreeMap<String,ActionParam>();
    
    {
      ActionParam param = 
	new IntegerActionParam
	("Order", 
	 "Apply the compositing operations in this order.",
	 100);
      params.put(param.getName(), param);
    }

    {
      ArrayList<String> ops = new ArrayList<String>();
      ops.add("Over");
      ops.add("In");
      ops.add("Out");
      ops.add("Atop");
      ops.add("Xor");
      ops.add("Plus");
      ops.add("Minus");
      ops.add("Add");
      ops.add("Subtract");
      ops.add("Difference");
      ops.add("Multiply");
      ops.add("Copy");
      ops.add("CopyRed");
      ops.add("CopyGreen");
      ops.add("CopyBlue");
      ops.add("CopyOpacity");

      ActionParam param = 
	new EnumActionParam
	("Operation", 
	 "The compositing operation to apply",
	 "Over", ops);
      params.put(param.getName(), param);
    }

    return params;
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
    FileSeq background = null;
    TreeMap<Integer,LinkedList<String>> layers = new TreeMap<Integer,LinkedList<String>>();
    TreeMap<String,FileSeq> images = new TreeMap<String,FileSeq>();
    TreeMap<String,String> ops = new TreeMap<String,String>();
    String ext = null;
    {    
      {
	String sname = (String) getSingleParamValue("Background"); 
	if(sname == null) 
	  throw new PipelineException
	    ("The Background image source was not set!");

	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the Background node (" + sname + ") was not one of the source " + 
	     "nodes!");

	if(fseq.getFilePattern().getSuffix() == null) 
	  throw new PipelineException
	    ("The primary file sequence (" + fseq + ") of Background node (" + sname + ") " +
	     "does not have a file suffix!");

	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	background = 
	  new FileSeq(PackageInfo.sProdDir + "/" + snodeID.getWorkingParent(), fseq);
      }
      
      for(String sname : getSourceNames()) {
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq.getFilePattern().getSuffix() == null) 
	  throw new PipelineException
	    ("The primary file sequence (" + fseq + ") of source node (" + sname + ") " +
	     "does not have a file suffix!");

	Integer order = (Integer) getSourceParamValue(sname, "Order");
	String op = (String) getSourceParamValue(sname, "Operation");
	if((order != null) && (op != null)) {
	  LinkedList<String> snames = layers.get(order);
	  if(snames == null) {
	    snames = new LinkedList<String>();
	    layers .put(order, snames);
	  }
	  
	  snames.add(sname);

	  NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	  FileSeq imgs =
	    new FileSeq(PackageInfo.sProdDir + "/" + snodeID.getWorkingParent(), fseq);
	  images.put(sname, imgs);
	  
	  ops.put(sname, op.toLowerCase());
	}
      }

      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if(suffix == null)
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") does not have a file suffix!");
	ext = suffix;
      }
    }

    /* create a temporary script file */ 
    File script = createTemp(agenda, 0755, "bash");
    try {      
      FileWriter out = new FileWriter(script);

      out.write("#!/bin/bash\n\n");
      
      int idx = 0;
      for(File target : agenda.getPrimaryTarget().getFiles()) {
	out.write("rm -f " + target + "\n");

	if(idx < background.numFrames()) {
	  File bg = background.getFile(idx);
	  File comp = null;

	  for(LinkedList<String> snames : layers.values()) {
	    for(String sname : snames) {
	      FileSeq fseq = images.get(sname);
	      if(idx < fseq.numFrames()) {
		File fg = fseq.getFile(idx);

		comp = createTemp(agenda, 0644, ext);
		comp.delete();
		
		out.write("composite -compose " + ops.get(sname) + 
			  " " + fg + " " + bg + " " + comp + "\n");

		bg = comp;
	      }
	    }
	  }

	  if(comp != null) 
	    out.write("mv " + comp + " " + target + "\n\n");
	  else
	    out.write("cp " + bg + " " + target + "\n\n");
	}

	idx++;
      }
      
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create the process to run the action */ 
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

  private static final long serialVersionUID = -5413081014233805151L;

}

