// $Id: SeqRapportGeneratorAction.java,v 1.1 2008/06/19 02:57:32 jim Exp $

package com.radarfilm.pipeline.plugin.SeqRapportGeneratorAction.v2_3_13;

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
 * Generates humanreadable rapport from finalCutProXmlFile.<P>
 * 
*/
public class SeqRapportGeneratorAction extends CommonActionUtils
{  
/*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 


public
SeqRapportGeneratorAction() 
  {
    super("SeqRapportGeneratorAction", new VersionID("2.3.13"), "Radar",
	  "Uses mencoder to make a movie from a frame stack ");
    
    {
        ActionParam param = new LinkActionParam ("XmlSource","The source node containing the images to convert.", null);
        addSingleParam(param);
    }

    

    
    // 
    {
        LayoutGroup layout = new LayoutGroup(true);
        layout.addEntry("XmlSource");

        
        setSingleLayout(layout);
      }    
    
    
      //addSupport(OsType.MacOS); // Should be able to work on OSX but lets just use Linux
      
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

  
	String sname = (String) getSingleParamValue("XmlSource"); 
	
	if(sname == null) 
	  throw new PipelineException
	    ("The Image Source was not set!");

		
		String xmlpath = null;
		if (sname != null){
			FileSeq fxml = agenda.getPrimarySource(sname);
			xmlpath = fxml.toString();
		}
		
		
		NodeID snodeID = new NodeID(nodeID, sname);
		
		
		
		
		
		
		ArrayList<String> suffixes = new ArrayList<String>();
		suffixes.add("html");
		suffixes.add("htm");
		Path targetPath = getPrimaryTargetPath(agenda, suffixes, "Html fil");
	
		// ********************************************************************
		// Build the mencoder command
		// ********************************************************************
		ArrayList<String> args = new ArrayList<String>();
		
		  args.add("/intranet/jpt/phpCliPrograms/Edit2pipe.php");
		  args.add("none");
		  args.add(xmlpath);
		  args.add(xmlpath);
		
		  args.add(targetPath.toOsString());
		  

		  
	
	  //Create sub process
	  return createSubProcess(agenda, "php5", args, agenda.getEnvironment(), 
	                          agenda.getTargetPath().toFile(), outFile, errFile);
	
	
}
 
}  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

	private static final long serialVersionUID = -5489411338371782414L;
}

