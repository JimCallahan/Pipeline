// $Id: LyxExportAction.java,v 1.1 2007/06/17 15:34:42 jim Exp $

package us.temerity.pipeline.plugin.LyxExportAction.v2_0_9;

import us.temerity.pipeline.*; 

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   L Y X   E X P O R T   A C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/**
 * Exports DVI, PS or PDF format represenations of a Lyx document. 
 * 
 * See the <A href="http://www.lyx.org">Lyx</A> documentation for details.
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Lyx Document <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the Lyx document to export. 
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public
class LyxExportAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  LyxExportAction() 
  {
    super("LyxExport", new VersionID("2.0.9"), "Temerity",
	  "Exports DVI, PS or PDF format represenations of a Lyx document."); 
    
    {
      ActionParam param = 
	new LinkActionParam
	("LyxDocument",
	 "The source node which contains the Lyx document to export.", 
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
    File source = null; 
    File target = null;
    String ext = null;
    {
      /* generate the filename of the Maya scene to load */
      {
	String sname = (String) getSingleParamValue("LyxDocument"); 
	if(sname == null) 
	  throw new PipelineException
	    ("The Lyx Document node was not specified!");
	
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the Lyx Document node (" + sname + ") was not one " + 
	     "of the source nodes!");
	
	String suffix = fseq.getFilePattern().getSuffix();
	if(!fseq.isSingle() || 
	   (suffix == null) || !(suffix.equals("lyx")))
	  throw new PipelineException
	    ("The Lyx Export Action requires that the source node specified by the Lyx " +
	     "Document parameter (" + sname + ") must have a single Lyx document file " + 
	     "(.lyx) as its primary file sequence!");
	
	NodeID snodeID = new NodeID(nodeID, sname);
	Path spath = new Path(PackageInfo.sProdPath,
			      snodeID.getWorkingParent() + "/" + fseq.getPath(0));
	source = spath.toFile(); 
      }

      /* the target file */ 
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix(); 
	if(!fseq.isSingle() || (suffix == null) || 
	   !(suffix.equals("dvi") || suffix.equals("ps") || suffix.equals("pdf")))
	  throw new PipelineException
	    ("The Lyx Export Action requires that the target file sequence is a single " + 
	     "DVI (.dvi), Postscript (.ps) or Portable Document Format (.pdf) file!");

	target = fseq.getPath(0).toFile();
	ext = suffix;
      }
    }

    /* create a temporary script to perform the export */ 
    File script = createTemp(agenda, 0755, "bash");
    try {      
      FileWriter out = new FileWriter(script);

      File input = createTemp(agenda, 0644, "lyx");

      String iname = input.getName();
      File output = new File(input.getParentFile(), 
			     iname.substring(0, iname.length()-3) + ext); 

      out.write("#!/bin/bash\n" + 
		"cp -f " + source + " " + input + "\n" + 
		"lyx --export " + ext + " " + input + "\n" + 
		"mv " + output + " " + target + "\n");

      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary shell script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }
    

    /* create the process to run the action */ 
    try {
      ArrayList<String> args = new ArrayList<String>();
      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 script.toString(), args, agenda.getEnvironment(), agenda.getWorkingDir(), 
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

  private static final long serialVersionUID = -6008166494523853155L;

}

