// $Id: NukeOutputQuicktimeAction.java,v 1.1 2008/06/19 02:57:32 jim Exp $

package com.radarfilm.pipeline.plugin.NukeOutputQuicktimeAction.v2_3_13;

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
 * Uses WindowsNuke to make a Black Magic quicktime from a frame stack.<P>
 * 
*/
public class NukeOutputQuicktimeAction extends NukeActionUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 

public
  NukeOutputQuicktimeAction() 
  {
    super("NukeOutputQuicktime", new VersionID("2.3.13"), "Radar",
	  "Uses WindowsNuke to make a Black Magic quicktime from a frame stack " + 
          "composited images.");
    
    {
	    ActionParam param = new LinkActionParam ("ImageSource","The source node containing the images to convert.", null);
	    addSingleParam(param);
    }
    {
    	ActionParam param = new IntegerActionParam ("FrameRate","Fram rate of the generated movie.",24);
    	addSingleParam(param);
    }    
    {
        ArrayList<String> options = new ArrayList<String>(); 
        options.add("Blackmagic 8 Bit");
        options.add("Blackmagic 8 Bit (2Vuy)");
        options.add("Blackmagic 10 Bit");
        options.add("Blackmagic RGB 10 Bit");
        ActionParam param = new EnumActionParam ("Codec","The animation export method.","Blackmagic 8 Bit", options);
        addSingleParam(param);
     }
    {
        ArrayList<String> options = new ArrayList<String>(); 
        options.add("-");
        options.add("1280 x 720");
        options.add("1920 x 1080");
        ActionParam param = new EnumActionParam ("Resize","Resize the output.","-", options);
        addSingleParam(param);
     }    
      removeSupport(OsType.Unix); // There is no quicktime support on nuke for linux 
      //addSupport(OsType.MacOS); // Should be able to work on OSX but lets just use windows
      addSupport(OsType.Windows);
      
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

    
	String sname = (String) getSingleParamValue("ImageSource");
	FrameRange range = agenda.getPrimarySource(sname).getFrameRange();
	String framerate = String.valueOf(getSingleIntegerParamValue("FrameRate"));
	String codec = getSingleStringParamValue("Codec");
	String resize = getSingleStringParamValue("Resize");
	
	if(sname == null) 
	  throw new PipelineException
	    ("The Image Source was not set!");

		FileSeq fseq = agenda.getPrimarySource(sname);
		NodeID snodeid = new NodeID(nodeID, sname);
		
		Path sourcePath = getWorkingNodeFilePath(snodeid, toNukeFilePattern(fseq.getFilePattern()));
		//Path sourcePath = new Path(new Path(sname).getParentPath(), toNukeFilePattern(fseq.getFilePattern()));
		Path targetPath = getPrimaryTargetPath(agenda, "mov", "Quicktime File");
	
		
	    /* create a temporary MEL script file */ 
	    File script = createTemp(agenda, "nuke");
	    try {      
	      FileWriter out = new FileWriter(script);

	      
	      out.write("Read {\n");
	      out.write("inputs 0\n");
	      out.write("file " + escPath(sourcePath.toString()) + "\n");
	      out.write("last " + range.getEnd() + "\n");
	      out.write("name Read1\n");
	      out.write("}\n");
	      
	      if(resize.equals("1280 x 720")){
	    	out.write("Reformat {\n");
	    	out.write("full_format \"1280 720 0 0 1280 720 1 720p\"\n");
	    	out.write("crop true\n");
	    	out.write("name Reformat1\n");
	    	out.write("selected true\n");
	    	out.write("xpos -300\n");
	    	out.write("ypos -12\n");
	    	out.write("}\n");
	      }

	      
	      if(resize.equals("1920 x 1080")){
	    	out.write("Reformat {\n");
	    	out.write("full_format \"1920 817 0 0 1920 817 1 radarBig\"\n");
	    	out.write("resize distort\n");
	    	out.write("filter Mitchell\n");
	    	out.write("name Reformat2\n");
	    	out.write("selected true\n");
	    	out.write("xpos -345\n");
	    	out.write("ypos -112\n");	    	
	    	out.write("}\n");	    	  
	    	  
	    	out.write("Reformat {\n");
	    	out.write("full_format \"1920 1080 0 0 1920 1080 1 HD\"\n");
	    	out.write("crop true\n");	    	
	    	out.write("name Reformat1\n");
	    	out.write("selected true\n");
	    	out.write("xpos -300\n");
	    	out.write("ypos -12\n");
	    	out.write("}\n");
	      }	      
	      
	      
	      out.write("Write {\n");
	      out.write("file " + escPath(targetPath.toString()) + "\n");
	      out.write("file_type mov\n");
	      out.write("codec \"" + codec +"\"\n");
	      out.write("fps " + framerate + "\n");
	      out.write("quality Lossless\n");
	      out.write("name Write1\n");
	      out.write("}\n\n");
	      
	      
	      out.close();
	    }
	    catch(IOException ex) {
	      throw new PipelineException
		("Unable to write temporary MEL script file (" + script + ") for Job " + 
		 "(" + agenda.getJobID() + ")!\n" +
		 ex.getMessage());
	    }
		
  
  
	/* create the process to run the action */ 
	{
	  ArrayList<String> args = new ArrayList<String>();
	  args.add("-nx"); 
	  args.add(script.getPath());
	  
	  
	  
	  
	  //FrameRange range = agenda.getPrimaryTarget().getFrameRange();
	  
	  if(range != null) {
	    if(range.isSingle()) 
	      args.add(Integer.toString(range.getStart()));
	    else if(range.getBy() == 1) 
	      args.add(range.getStart() + "," + range.getEnd());
	    else 
	      args.add(range.getStart() + "," + range.getEnd() + "," + range.getBy());
	  }
	  
	
	  return createSubProcess(agenda, getNukeProgram(agenda), args, agenda.getEnvironment(), 
	                          agenda.getTargetPath().toFile(), outFile, errFile);
	}
	
}
 
}  

  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

	private static final long serialVersionUID = -5607954507690809590L;

}

