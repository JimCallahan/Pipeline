// $Id: MencoderEncodeAction.java,v 1.1 2008/06/19 02:57:32 jim Exp $

package com.radarfilm.pipeline.plugin.MencoderEncodeAction.v2_3_10;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.math.Range;
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   N U K E OUTPUT QUICKTIME A C T I O N                                                   */
/*------------------------------------------------------------------------------------------*/

/**  
 * Uses Mencoder to make a movie from a frame stack.<P>
 * 
*/
public class MencoderEncodeAction extends CommonActionUtils
{  
/*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 

public
  MencoderEncodeAction() 
  {
    super("MencoderEncodeAction", new VersionID("2.3.10"), "Radar",
	  "Uses mencoder to make a movie from a frame stack " + 
          "composited images.");
    
    {
        ActionParam param = new LinkActionParam ("ImageSource","The source node containing the images to convert.", null);
        addSingleParam(param);
    }
    {
	    ActionParam param = new LinkActionParam ("WaveSource","The source node containing the wave file to add.", null);
	    addSingleParam(param);
    }
    
    {
    	ActionParam param = new IntegerActionParam ("FrameRate","Fram rate of the generated movie.",24);
    	addSingleParam(param);
    }
    
    {
    	ActionParam param = new StringActionParam ("OptionsVideo","extra command line flags.","-noskip -ovc lavc -mpegopts format=mpeg2:tsaf -lavcopts vcodec=mpeg2video:vrc_buf_size=1835:vrc_maxrate=9800:vbitrate=9500:keyint=15:vstrict=0");
        addSingleParam(param);
    }    
    {
    	ActionParam param = new StringActionParam ("OptionsAudio","extra command line flags.","-oac copy");
        addSingleParam(param);
    }    

    
    // 
    {
        LayoutGroup layout = new LayoutGroup(true);
        layout.addEntry("ImageSource");
        layout.addEntry("WaveSource");
        layout.addSeparator();
        layout.addEntry("FrameRate");
        layout.addEntry("OptionsVideo");
        layout.addEntry("OptionsAudio");

        
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
    formats.add("yuv");
    formats.add("jpg");
    formats.add("jpeg");
    formats.add("sgi");
    formats.add("tga");
    formats.add("tif");
    formats.add("tiff");
  
	String sname = (String) getSingleParamValue("ImageSource"); 
	String wavname = (String) getSingleParamValue("WaveSource");
	String framerate = String.valueOf(getSingleIntegerParamValue("FrameRate"));
	String optionsVideo = getSingleStringParamValue("OptionsVideo");
	String optionsAudio = getSingleStringParamValue("OptionsAudio");
	
	if(sname == null) 
	  throw new PipelineException
	    ("The Image Source was not set!");

		FileSeq fseq = agenda.getPrimarySource(sname);
		
		String wavepath = null;
		if (wavname != null){
			FileSeq fwav = agenda.getPrimarySource(wavname);
			wavepath = fwav.toString();
		}
		
		
		NodeID snodeID = new NodeID(nodeID, sname);
		
		
		FilePattern fpattern = fseq.getFilePattern();
		String MencoderStyleFile = fpattern.getPrefix() + ".*." + fpattern.getSuffix();
		Path MencoderStylePath = getWorkingNodeFilePath(snodeID, MencoderStyleFile); 
		
		
		
		
		ArrayList<String> suffixes = new ArrayList<String>();
		suffixes.add("avi");
		suffixes.add("mpg");
		suffixes.add("mpeg");
		Path targetPath = getPrimaryTargetPath(agenda, suffixes, "Avi og mpg file");
	
		// ********************************************************************
		// Build the mencoder command
		// ********************************************************************
		ArrayList<String> args = new ArrayList<String>();
		
		  args.add("mf://" + MencoderStylePath.toOsString());
		  args.add("-fps " + framerate);
		
		  if (wavepath != null ){
			  args.add(optionsAudio);
			  args.add("-audiofile " + wavepath);
		  }
		  
		  
		  args.add(optionsVideo);
		  args.add("-o " + targetPath.toOsString());
		  
		  
		  // ********************************************************************
		  // Wirte out temporay wrapper bash script to fix strange mecoder issue
		  // ********************************************************************
		  /* create a temporary MEL script file */ 
		    File wrapscript = createTemp(agenda, "bash");
		   
		    
		    
		    try {      
		      FileWriter out = new FileWriter(wrapscript);

		      	out.write("Read {\n");
		      	out.write("#!/bin/sh\n" + 
		      			  "moreparams=\"\"\n" + 
		      			  "until [ -z \"$1\" ]  # Until all parameters used up...\n" + 
		      			  "do\n" + 
		      			  "          moreparams=\"$moreparams $1 \"\n" + 
		      			  "            shift\n" + 
		      			  "    done\n" + 
		      			  "\n" + 
		      			  "\n" + 
		      			  "echo $moreparams\n" + 
		      			  "mencoder $moreparams\n" + 
		      	          "");
		      
		      out.close();
		    }
		    catch(IOException ex) {
		      throw new PipelineException
			("Unable to write temporary BASH script file (" + wrapscript + ") for Job " + 
			 "(" + agenda.getJobID() + ")!\n" +
			 ex.getMessage());
		    }			  
	
		    args.add(0, wrapscript.getPath());
	
	  //Create sub process
	  return createSubProcess(agenda, "bash", args, agenda.getEnvironment(), 
	                          agenda.getTargetPath().toFile(), outFile, errFile);
	
	
}
 
}  


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

	private static final long serialVersionUID = 403048409446060315L;

}

