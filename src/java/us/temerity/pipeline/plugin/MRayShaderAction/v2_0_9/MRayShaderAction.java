// $Id: MRayShaderAction.java,v 1.1 2007/06/17 15:34:43 jim Exp $

package us.temerity.pipeline.plugin.MRayShaderAction.v2_0_9;

import us.temerity.pipeline.*; 

import java.lang.*;
import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   M R A Y   S H A D E R   A C T I O N                                                    */
/*------------------------------------------------------------------------------------------*/

/** 
 * Compiles C source files to generate a Mental Ray shader. <P>
 * 
 * Compiles the shader source file (.c) which is the single member of the primary file 
 * sequence of one of the source nodes into native dynamic library (.so) shader which is 
 * the single member of the primary file sequence of this node. <P> 
 * 
 * See the Mental Ray documentation for details. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Shader Source <BR>
 *   <DIV style="margin-left: 40px;">
 *     The source node which contains the C source for the shader.<BR>
 *   </DIV> <BR>
 * 
 *   Architecture <BR>
 *   <DIV style="margin-left: 40px;">
 *     CPU architecture of target render hosts.
 *     <UL>
 *       <LI> 32-Bit
 *       <LI> 64-Bit
 *     </UL>
 *   </DIV> <BR>
 * 
 *   Debug <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to compile with debugging flags and options.
 *   </DIV> <BR>
 * 
 *   CleanObjects <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to clean up temporary object files after building the DSO.
 *   </DIV> <BR>
 * 
 *   Include Paths <BR>
 *   <DIV style="margin-left: 40px;">
 *     Colon seperated list of additional directories to search for included files.
 *   </DIV> <BR>
 * 
 *   Defined Symbols <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whitespace seperated preprocessor symbol definitions.
 *   </DIV> <BR>
 * </DIV>
 */
public
class MRayShaderAction
  extends BaseAction
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public
  MRayShaderAction() 
  {
    super("MRayShader", new VersionID("2.0.9"), "Temerity",
	  "Compiles C source files to generate a Mental Ray shader."); 
    
    {
      ActionParam param = 
	new LinkActionParam
	("ShaderSource",
	 "The source node which contains the C source for the shader.",
	 null);
      addSingleParam(param);
    }

    {
      ArrayList<String> choices = new ArrayList<String>();
      choices.add("32-Bit");
      choices.add("64-Bit"); 

      ActionParam param = 
	new EnumActionParam
	("Architecture", 
	 "CPU architecture of target render hosts.", 
	 "32-Bit", choices);
      addSingleParam(param);
    } 
      
    {
      ActionParam param = 
	new BooleanActionParam
	("CleanObjects",
	 "Whether clean up temporary object files after building the DSO.", 
	 true);
      addSingleParam(param);
    }
       
    {
      ActionParam param = 
	new BooleanActionParam
	("Debug",
	 "Whether to compile with debugging flags and options.", 
	 false);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	("IncludePaths",
	 "Colon seperated list of additional directories to search for included files.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new StringActionParam
	("DefinedSymbols",
	 "Whitespace seperated preprocessor symbol definitions.", 
	 null);
      addSingleParam(param);
    }

    {  
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry("ShaderSource");
      layout.addSeparator();
      layout.addEntry("Architecture");
      layout.addEntry("CleanObjects");
      layout.addEntry("Debug");
      layout.addSeparator();
      layout.addEntry("IncludePaths");
      layout.addEntry("DefinedSymbols");

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
    /* file sequence checks */ 
    Path source = null; 
    Path target = null; 
    String prefix = null;
    {
      {    
	String sname = (String) getSingleParamValue("ShaderSource"); 
	if(sname == null) 
	  throw new PipelineException
	    ("The Shader Source was not set!");
	
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow the Shader Source (" + sname + ") was not one of the source nodes!");

	FilePattern fpat = fseq.getFilePattern();
	prefix = fpat.getPrefix();
	String suffix = fpat.getSuffix();
	if((suffix == null) || !suffix.equals("c") || (fseq.numFrames() != 1))
	  throw new PipelineException
	    ("The source primary file sequence (" + fseq + ") must contain a single " + 
	     "shader C source (.c) file.");

	NodeID snodeID = new NodeID(agenda.getNodeID(), sname);
	source = new Path(PackageInfo.sProdPath, 
			  snodeID.getWorkingParent() + "/" + fseq.getPath(0));
      }
      
      {
	FileSeq fseq = agenda.getPrimaryTarget();
	FilePattern fpat = fseq.getFilePattern();
	String suffix = fpat.getSuffix();
	if((suffix == null) || !suffix.equals("so") || (fseq.numFrames() != 1))
	  throw new PipelineException
	    ("The target primary file sequence (" + fseq + ") must contain a single " + 
	     "native dynamic shared library (.so) file.");

	target = fseq.getPath(0);

	if(!prefix.equals(fpat.getPrefix())) 
	  throw new PipelineException
	    ("The shader C source file (" + source + ") and compiled shader DSO " + 
	     "(" + target + ") must have the same prefix.");
      }
    }

    boolean bit64 = false;    
    {
      EnumActionParam param = (EnumActionParam) getSingleParam("Architecture");
      bit64 = (param.getIndex() == 1);
      
      if(bit64 && (PackageInfo.sOsType == OsType.MacOS))
	throw new PipelineException
	  ("Sorry, 64-Bit shader compiling is not supported on MacOS yet.");
    }

    boolean debug = false;
    {
      Boolean value = (Boolean) getSingleParamValue("Debug");
      debug = (value != null) && value;
    }

    boolean cleanObj = false;
    {
      Boolean value = (Boolean) getSingleParamValue("CleanObjects");
      cleanObj = (value != null) && value;
    }
    
    /* create a shell script file */ 
    File script = createTemp(agenda, 0644, "bash");
    try {      
      FileWriter out = new FileWriter(script);

      out.write("rm -f " + prefix + ".o\n");

      if(PackageInfo.sOsType == OsType.Unix) {
	out.write("gcc -c -fPIC -Bsymbolic ");
	if(debug) 
	  out.write("-g -DDEBUG -DCVD_DEBUG ");
	else
	  out.write("-O3 ");
      }      
      else if(PackageInfo.sOsType == OsType.MacOS) {
	out.write("cc -c ");

	if(debug) 
	  out.write("-g -DDEBUG -DCVD_DEBUG");
	else 
	  out.write("-O3");

	out.write("-fPIC -dynamic -fno-common -DMACOSX -D_REENTRANT ");
      }

      {
	String value = (String) getSingleParamValue("DefinedSymbols");
	if(value != null) {
	  String[] symbols = value.split("\\p{Space}");
	  int wk;
	  for(wk=0; wk<symbols.length; wk++) {
	    if((symbols[wk] != null) && (symbols[wk].length() > 0))
	      out.write("-D" + symbols[wk] + " ");
	  }
	}
      }

      {
	String miRoot = agenda.getEnvironment().get("MI_ROOT");
	if(miRoot == null) 
	  throw new PipelineException
	    ("No MI_ROOT environmental variable defined!");
	out.write("-I" + miRoot + "/devkit ");
      }

      {
	String value = (String) getSingleParamValue("IncludePaths");
	if(value != null) {
	  String[] paths = value.split(":");
	  int wk;
	  for(wk=0; wk<paths.length; wk++) {
	    if((paths[wk] != null) && (paths[wk].length() > 0))
	      out.write("-I" + paths[wk] + " ");
	  }
	}
      }

      out.write(source.toOsString() + "\n");

      if(PackageInfo.sOsType == OsType.Unix) {
	out.write("ld -export-dynamic -shared " +
		  "-o " + target.toOsString() + " " + prefix + ".o");
      }
      else if(PackageInfo.sOsType == OsType.MacOS) {
	out.write("libtool -flat_namespace -undefined suppress -dynamic " + 
		  "/usr/local/mi/lib/macosx-fpsave.o " + 
		  "-o " + target.toOsString() + " " + prefix + ".o"); 
      }

      out.write("\n");
      
      if(cleanObj) 
        out.write("rm -f " + prefix + ".o\n");
      
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
      args.add(script.getPath());
      
      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 "bash", args, agenda.getEnvironment(), agenda.getWorkingDir(), 
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

  private static final long serialVersionUID = -5712631014359798425L;

}

