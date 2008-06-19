// $Id: WavGeneratorAction.java,v 1.1 2008/06/19 02:57:32 jim Exp $

package com.radarfilm.pipeline.plugin.WavGeneratorAction.v2_3_13;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.math.Range;
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   SeqRapportGeneratorAction
/*------------------------------------------------------------------------------------------*/

/**  
 * Generates wav shot files and a HTML rapport from a finalCut quicktime and final cut xml.<P>
 * 
*/
public class WavGeneratorAction extends CommonActionUtils
{  
/*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 

public
WavGeneratorAction() 
  {
    super("WavGeneratorAction", new VersionID("2.3.13"), "Radar",
	  "Uses Mencoder and sox to make wav files from a quicktime and a finalcut xml ");

    {
    	ActionParam param = new StringActionParam ("ProjectName","The Project name.","sunshineBarry");
        addSingleParam(param);
    }        
    {
        ActionParam param = new LinkActionParam ("XmlSource","The source node containing the xml data.", null);
        addSingleParam(param);
    }
    {
        ActionParam param = new LinkActionParam ("MovSource","The source node containing the images to convert.", null);
        addSingleParam(param);
    }
    

    
    // 
    {
        LayoutGroup layout = new LayoutGroup(true);
        layout.addEntry("ProjectName");
        layout.addEntry("XmlSource");
        layout.addEntry("MovSource");

        
        setSingleLayout(layout);
      }    
    
    	//removeSupport(OsType.Unix);
    	//addSupport(OsType.Windows);
    	
      
      //underDevelopment();
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
public SubProcessHeavy prep (ActionAgenda agenda, File outFile, File errFile ) throws PipelineException {
  NodeID nodeID = agenda.getNodeID();

  /* sanity checks */ 
  Path fromPath = null;
  FileSeq fromSeq = null;
  FileSeq toSeq = null;
  { 
	  
    ArrayList<String> formats = new ArrayList<String>(); 
    formats.add("xml");
    formats.add("mov");

  
	String sname = (String) getSingleParamValue("XmlSource");
	String mname = (String) getSingleParamValue("MovSource");
	String projectName = getSingleStringParamValue("ProjectName");
	
	if(sname == null) 
	  throw new PipelineException
	    ("The Xml Source was not set!");

		NodeID snodeid = new NodeID(nodeID, sname);
		NodeID mnodeid = new NodeID(nodeID, mname);
	

		FileSeq fxml = agenda.getPrimarySource(sname);
		Path xmlPath = getWorkingNodeFilePath(snodeid,(fxml.toString()));


		FileSeq mxml = agenda.getPrimarySource(mname);
		Path movPath = getWorkingNodeFilePath(mnodeid,(mxml.toString()));

		ArrayList<String> suffixes = new ArrayList<String>();
		suffixes.add("html");
		suffixes.add("htm");
		Path targetPath = getPrimaryTargetPath(agenda, suffixes, "Html fil");		
		
		// ********************************************************************
		// Build the command
		// ********************************************************************
		ArrayList<String> args = new ArrayList<String>();
		
		  args.add("/intranet/jpt/phpCliPrograms/Edit2pipe.php");
		  args.add(projectName);
		  args.add(escPath(xmlPath.toString()));
		  args.add(escPath(movPath.toString()));
		  
		  args.add(targetPath.toOsString());
		  
		  args.add("both");
		
	  //Create sub process
	  return createSubProcess(agenda, "php5", args, agenda.getEnvironment(), 
	                          agenda.getTargetPath().toFile(), outFile, errFile);
	
	
}
 
}  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

	private static final long serialVersionUID = 4003946660228239416L;

}

