package com.radarfilm.pipeline.plugin.ShakeImgConvert.v2_3_14;

import us.temerity.pipeline.*; 
import us.temerity.pipeline.plugin.*; 

import java.lang.*;
import java.util.*;
import java.io.*;


/*------------------------------------------------------------------------------------------*/
/*   SHAKE IMAGE CONVERT  A C T I O N                                                       */
/*------------------------------------------------------------------------------------------*/

/** 
 */

public class ShakeImgConvert extends CommonActionUtils
{  
/*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 

public
ShakeImgConvert()
{
    super("ShakeImgConvert", new VersionID("2.3.14"), "Radar",
	  "Uses Shake to convert a frame stack " + 
          "composited images.");
    
    {
        ActionParam param = new LinkActionParam ("ImageSource","The source node containing the images to convert.", null);
        addSingleParam(param);
    }
    {
    	ActionParam param = new StringActionParam ("ExtraSourceOptions","Extra options for shake like -blur 10.",null);
        addSingleParam(param);
    }    

    
    // 
    {
        LayoutGroup layout = new LayoutGroup(true);
        layout.addEntry("ImageSource");
        layout.addEntry("ExtraSourceOptions");
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

public String getShakePadding(int padCount){

	int i = 1;
	String padding = "";
	while(i <= padCount ){
		padding = (padding + "@");
		i++;
	}	
	
	return padding;
}

public SubProcessHeavy prep (ActionAgenda agenda, File outFile, File errFile ) throws PipelineException {
  NodeID nodeID = agenda.getNodeID();

  /* sanity checks */ 
  Path fromPath = null;
  FileSeq fromSeq = null;
  FileSeq toSeq = null;
  { 
	  
    ArrayList<String> formats = new ArrayList<String>(); 
    formats.add("yuv");
    formats.add("als");
    formats.add("tdi");
    formats.add("iff");
    formats.add("gif");
    formats.add("jpg");
    formats.add("jpeg");
    formats.add("cin");
    formats.add("lff");
    formats.add("pxb");
    formats.add("ppm");
    formats.add("pri");
    formats.add("qtl");
    formats.add("rgb");
    formats.add("sgi");
    formats.add("bw");
    formats.add("icon");
    formats.add("pic");
    formats.add("tga");
    formats.add("tif");
    formats.add("tiff");
    formats.add("vst");
    formats.add("rla");
    formats.add("exr");
  
	String sname = (String) getSingleParamValue("ImageSource"); 
	String extraSourceOptions = getSingleStringParamValue("ExtraSourceOptions");
	
	/* the target frame range */ 
    FrameRange range = agenda.getPrimaryTarget().getFrameRange();
	
	if(sname == null) 
	  throw new PipelineException
	    ("The Image Source was not set!");

		FileSeq fseq = agenda.getPrimarySource(sname);
				
		NodeID snodeID = new NodeID(nodeID, sname);
		
		
		FilePattern fpattern = fseq.getFilePattern();		
		String ShakeStyleFile = fpattern.getPrefix() + "." + getShakePadding(fpattern.getPadding()) + "." + fpattern.getSuffix();
		Path ShakeStylePath = getWorkingNodeFilePath(snodeID, ShakeStyleFile); 
		
		//Path targetPath = getPrimaryTargetPath(agenda, formats, "Image Sequence");
		FileSeq targetFseq = agenda.getPrimaryTarget();
		FilePattern targetFPattern = targetFseq.getFilePattern();
		String targetShakeStyleFile = targetFPattern.getPrefix() + "." + getShakePadding(targetFPattern.getPadding()) + "." + targetFPattern.getSuffix();
		Path targetShakeStylePath = getWorkingNodeFilePath(snodeID, targetShakeStyleFile);
		
		// ********************************************************************
		// Build the Shake command
		// ********************************************************************
		ArrayList<String> args = new ArrayList<String>();
		/*
		  args.add(ShakeStylePath.toOsString());
		  args.add("-fo " + targetShakeStylePath.toOsString());
		  args.add("-t");
		  args.add(range.toString());
	    */
		  
		  
		  // ********************************************************************
		  // Wirte out temporay wrapper bash script to fix strange mecoder issue
		  // ********************************************************************
		  /* create a temporary MEL script file */ 
		    File wrapscript = createTemp(agenda, "bash");
		   
		    
		    
		    try {      
		      FileWriter out = new FileWriter(wrapscript);
		      	out.write("#!/bin/sh\n");
		      	out.write("shake");
		      	out.write(" ");
		      	out.write(ShakeStylePath.toOsString());
		      	out.write(" ");
		      	
		      	if(extraSourceOptions != null){
		      		out.write(extraSourceOptions);
		      		out.write(" ");
		      	}
		      	
		      	out.write("-t");
		      	out.write(" ");
		      	out.write(range.toString());
		      	out.write(" ");
				out.write("-fo " + targetShakeStylePath.toOsString());
				out.write(" ");
				out.write("-v");
				out.write("\n");
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

	private static final long serialVersionUID = 4907857062421866020L;

}
