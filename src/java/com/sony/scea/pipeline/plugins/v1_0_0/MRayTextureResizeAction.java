// $Id: MRayTextureResizeAction.java,v 1.2 2007/07/10 08:30:25 jim Exp $

package com.sony.scea.pipeline.plugins.v1_0_0;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M R A Y   T E X T U R E   R E S I Z E   A C T I O N                                    */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates optimized Mental Ray memory mappable pyramid textures from source images, 
 * after resizing the image. <P> 
 * 
 * First runs imagemagick to reduce the size of the images.  Then, converts the images 
 * which make up the primary file sequence of one of the source nodes into the texture 
 * maps which make up the primary file sequence of this node. <P>
 * 
 * See the Mental Ray documentation for for details about <B>imf_copy</B>(1) and see the man
 * pages for <b>convert<b> for more information on imagemagick<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Image Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the images files to convert. <BR> 
 *   </DIV> <BR>
 *   <BR>
 * 
 *   Gamma <BR>
 *   <DIV style="margin-left: 40px;">
 *     Gama correction exponent.
 *   </DIV> <BR>
 *   <BR>
 * 
 *   Filter <BR>
 *   <DIV style="margin-left: 40px;">
 *     Overrides the default filter value. 
 *   </DIV> <BR>
 *   <BR>  
 *   
 *   Texel Layout <BR>
 *   <DIV style="margin-left: 40px;">
 *     How to organize output texel data.
 *     <UL>
 *       <LI> Scanlines
 *       <LI> Tiles (faster)
 *     </UL>
 *   </DIV> <BR>
 *   <BR>
 *   
 *   Byte Order <BR>
 *   <DIV style="margin-left: 40px;">
 *     The byte ordering of texel data in the output texture map file.  This must match the
 *     native byte order on the rendering 
 *     <UL>
 *       <LI> Little-Endian (x86)
 *       <LI> Big-Endian (others)
 *     </UL>
 *   </DIV> <BR>
 *   <BR>
 *   ResizeAmount<BR>
 *   <DIV style="margin-left: 40px;">
 *     The new resolution for the image. Selecting None will result in imagemagick not
 *     being run.
 *     <ul>
 *       <li>None
 *       <li>4k
 *       <li>2k
 *       <li>1k
 *       <li>512m
 *       <li>256m
 *       <li>128m
 *   </DIV> <BR>
 *   <BR> 
 * 
 *   Extra Options <BR>
 *   <DIV style="margin-left: 40px;">
 *     Additional command-line arguments. <BR> 
 *   </DIV> <BR>
 * </DIV>
 */
public
class MRayTextureResizeAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
public
  MRayTextureResizeAction() 
  {
    super("MRayTextureResize", new VersionID("1.0.0"), "SCEA", 
	  "Generates optimized Mental Ray memory mappable pyramid textures from " + 
	  "source images, after a resize.");
    
    {
      ActionParam param = 
	new LinkActionParam
	(aImageSource,
	 "The source node which contains the image files to convert.",
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new DoubleActionParam
	(aGamma, 
	 "Gamma correction exponent.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new DoubleActionParam
	(aFilter, 
	 "Overrides the default filter value.", 
	 null);
      addSingleParam(param);
    }


    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Scanlines");
      choices.add("Tiles");

      {
	ActionParam param = 
	  new EnumActionParam
	  (aTexelLayout, 
	   "How to organize output texel data.",
	   "Tiles", choices);
	addSingleParam(param);
      }
    }   

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("Little-Endian");
      choices.add("Big-Endian");

      {
	ActionParam param = 
	  new EnumActionParam
	  (aByteOrder, 
	   "The byte ordering of texel data in the output texture map file.", 
	   "Little-Endian", choices);
	addSingleParam(param);
      }
    }
    
    {
       ArrayList<String> choices = new ArrayList<String>();
       choices.add("None");
       choices.add("4k");
       choices.add("2k");
       choices.add("1k");
       choices.add("512m");
       choices.add("256m");
       choices.add("128m");
       {
	  ActionParam param = 
	     new EnumActionParam
	     (aResizeAmount, 
		"The final size of the texture before the map conversion", 
		"1k", 
		choices);
	  addSingleParam(param);
       }
    }


    {
      ActionParam param = 
	new StringActionParam
	(aExtraOptions,
	 "Additional command-line arguments.", 
	 null);
      addSingleParam(param);
    }


    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aImageSource);
      layout.addSeparator();      
      layout.addEntry(aResizeAmount);
      layout.addSeparator();
      layout.addEntry(aGamma);
      layout.addEntry(aFilter);
      layout.addEntry(aTexelLayout);
      layout.addEntry(aByteOrder);
      layout.addSeparator();
      layout.addEntry(aExtraOptions);
      

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

    /* file sequence checks */ 
    Path fromPath = null;
    FileSeq fromSeq = null;
    FileSeq toSeq = null;
    {
      {    
	String sname = (String) getSingleParamValue(aImageSource); 
	if(sname == null) 
	  throw new PipelineException
	    ("The Image Source was not set!");
	
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the Image Source (" + sname + ") was not one of the source nodes!");
	
	fromSeq = fseq;
	
	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	fromPath = new Path(PackageInfo.sProdPath, snodeID.getWorkingParent());
      }
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if((suffix == null) || !suffix.equals("map"))
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") must contain Mental Ray" + 
	     "memory mappable pyramid textures (.map)!");
	
	toSeq = fseq;
      }

      if(fromSeq.numFrames() != toSeq.numFrames()) 
	throw new PipelineException 
	  ("The source primary file sequence (" + fromSeq + ") does not have the same" + 
	   "number of frames as the target primary file sequence (" + toSeq + ")!");
    }

    ArrayList<String> args = new ArrayList<String>();    
    args.add("-p");

    Double filter = (Double) getSingleParamValue(aFilter);
    if(filter != null) {
      if(filter < 0.0) 
	throw new PipelineException
	  ("The Filter value (" + filter + ") cannot be negative!");

      args.add("-f");
      args.add(filter.toString());
    }

    Double gamma = (Double) getSingleParamValue(aGamma);
    if(gamma != null) {
      if(gamma < 0.0) 
	throw new PipelineException
	  ("The Gamma value (" + gamma + ") cannot be negative!");

      args.add("-g");
      args.add(gamma.toString());
    }

    {
      EnumActionParam param = (EnumActionParam) getSingleParam(aTexelLayout);
      switch(param.getIndex()) {
      case 0:
	break;

      case 1:
	args.add("-r");
	break;

      default:
	throw new PipelineException
	  ("Illegal Texel Layout value!");	
      }
    }

    {
      EnumActionParam param = (EnumActionParam) getSingleParam(aByteOrder);
      switch(param.getIndex()) {
      case 0:
	args.add("-L");
	break;

      case 1:
	args.add("-B");
	break;

      default:
	throw new PipelineException
	  ("Illegal Byte Order value!");	
      }
    }
    
    File image = createTemp(agenda, 0666, fromSeq.getFilePattern().getSuffix());

    addExtraOptions(args);

    File script = createTemp(agenda, 0755, "bash");
    try {      
       FileWriter out = new FileWriter(script);

       String cmdopts = null;
       {
	  StringBuffer buf = new StringBuffer();
	  buf.append("imf_copy");
	  for(String arg : args) 
	     buf.append(" " + arg);
	  cmdopts = buf.toString();
       }

       out.write("#!/bin/bash\n\n");

       int convert = ((EnumParam) getSingleParam(aResizeAmount)).getIndex();
       {
	  ArrayList<Path> fromPaths = fromSeq.getPaths();
	  ArrayList<Path> toPaths   = toSeq.getPaths();
	  int wk;
	  for(wk=0; wk<fromPaths.size(); wk++) {
	     Path fpath = new Path(fromPath, fromPaths.get(wk));
	     if (convert == 0)
	     {
		out.write(cmdopts + " " + fpath.toOsString() + " " + 
		   toPaths.get(wk).toOsString() + "\n");
	     } else
	     {
		String convertString = "convert -resize ";
		switch (convert)
		{
		   case 1: 
		      convertString += "4096x4096";
		      break;
		   case 2: 
		      convertString += "2048x2048";
		      break;
		   case 3: 
		      convertString += "1024x1024";
		      break;
		   case 4: 
		      convertString += "512x512";
		      break;
		   case 5: 
		      convertString += "256x256";
		      break;
		   case 6: 
		      convertString += "128x128";
		      break;
		}
		
		out.write(convertString + " " + fpath.toOsString() + 
		   " " + image.getAbsolutePath() + "\n");
		out.write(cmdopts + " " + image.getAbsolutePath() + " " + 
		   toPaths.get(wk).toOsString() + "\n");
	     }
	     out.close();
	  }
       }
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
	   script.getPath(),  new ArrayList<String>(), 
	   agenda.getEnvironment(), agenda.getWorkingDir(), 
	   outFile, errFile); 
      }
      catch(Exception ex1) {
	throw new PipelineException
	  ("Unable to generate the SubProcess to perform this Action!\n" +
	   ex1.getMessage());
      } 
  }

  /**
   * Append any additional command-line arguments.
   */ 
  private void 
  addExtraOptions
  (
   ArrayList<String> args
  ) 
    throws PipelineException
  {
    String extra = (String) getSingleParamValue(aExtraOptions);
    if(extra == null) 
      return;

    String parts[] = extra.split("\\p{Space}");
    int wk;
    for(wk=0; wk<parts.length; wk++) {
      if(parts[wk].length() > 0) 
	args.add(parts[wk]);
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/
  private static final long serialVersionUID = 7970775894564478263L;
  private static final String aExtraOptions = "ExtraOptions";
  private static final String aByteOrder = "ByteOrder";
  private static final String aTexelLayout = "TexelLayout";
  private static final String aFilter = "Filter";
  private static final String aGamma = "Gamma";
  private static final String aImageSource = "ImageSource";
  private static final String aResizeAmount = "ResizeAmount";


}

