// $Id: JavaBuildAction.java,v 1.3 2006/11/23 00:46:59 jim Exp $

package us.temerity.pipeline.plugin.v2_0_15;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.utils.*;

/*------------------------------------------------------------------------------------------*/
/*   J A V A  B U I L D   A C T I O N                                                       */
/*------------------------------------------------------------------------------------------*/

/**
 * Compiles a collection of Java source files and packages them as a JAR.<P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Class Path <BR>
 *   <DIV style="margin-left: 40px;">
 *     Additional JARs and/or directories to add to the Java Compiler's class path.
 *   </DIV> <BR>
 * 
 *   Uses API <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether the append the Pipeline API JAR file to the Java Compiler's class path.
 *   </DIV> <BR>
 *   
 *   <I>Warning Messages</I> <BR>
 *   <DIV style="margin-left: 40px;">
 *     Deprecation <BR>			
 *     <DIV style="margin-left: 40px;">	
 *       Show a description of each use or override of a deprecated member or class.
 *     </DIV> <BR>			
 *     					
 *     Unchecked <BR>			
 *     <DIV style="margin-left: 40px;">	
 *       Give more detail for unchecked conversion warnings that are mandated by the 
 *       Java Language Specification.			
 *     </DIV> <BR>			
 *     					
 *     Fallthrough<BR>			
 *     <DIV style="margin-left: 40px;">	
 *       Check switch blocks for fall-through cases and provide a warning message for 
 *       any that are found.					
 *     </DIV> <BR>			
 *     					
 *     Path <BR>				
 *     <DIV style="margin-left: 40px;">	
 *       Warn about nonexistent path (classpath, sourcepath, etc) directories.	
 *     </DIV> <BR>			
 *     					
 *     Serial <BR>			
 *     <DIV style="margin-left: 40px;">	
 *       Warn about missing serialVersionUID definitions on serializable classes.
 *     </DIV> <BR>			
 *     					
 *     Finally <BR>			
 *     <DIV style="margin-left: 40px;">	
 *       Warn about finally clauses that cannot complete normally.	
 *     </DIV> <BR>
 *   </DIV><P>  
 *   
 *   Extra Options <BR>
 *   <DIV style="margin-left: 40px;">
 *     Additional command-line arguments to pass to the Java Compiler.
 *   </DIV> <BR>
 * </DIV> 
 */
public class 
JavaBuildAction 
  extends BaseAction
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  JavaBuildAction() 
  {
    super("JavaBuild", new VersionID("2.0.15"), "Temerity", 
	  "Compiles a Pipeline utility application JAR file from Java source files."); 

    {
      ActionParam param = 
	new StringActionParam
	(aClassPath, 
	 "Additional JARs and/or directories to add to the Java Compiler's class path.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	(aUsesAPI, 
	 "Whether the append the Pipeline API JAR file to the Java Compiler's class path.", 
	 true);
      addSingleParam(param);   
    }
    
    {
      ActionParam param = 
	new StringActionParam
	(aExtraOptions, 
	 "Additional command-line arguments to pass to the Java Compiler.", 
	 null);
      addSingleParam(param);
    }
    
    /* warning messages */ 
    {
      {
	ActionParam param = 
	  new BooleanActionParam
	  (aDeprecation, 
	   "Show a description of each use or override of a deprecated member or class.", 
	   true);
	addSingleParam(param);   
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aUnchecked, 
	   "Give more detail for unchecked conversion warnings that are mandated by the " + 
	   "Java Language Specification.", 
	   true);
	addSingleParam(param);   
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aFallthrough, 
	   "Check switch blocks for fall-through cases and provide a warning message for " + 
	   "any that are found.", 
	   true);
	addSingleParam(param);   
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aPath, 
	   "Warn about nonexistent path (classpath, sourcepath, etc) directories.", 
	   true);
	addSingleParam(param);   
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aSerial, 
	   "Warn about missing serialVersionUID definitions on serializable classes.", 
	   true);
	addSingleParam(param);   
      }

      {
	ActionParam param = 
	  new BooleanActionParam
	  (aFinally, 
	   "Warn about finally clauses that cannot complete normally.", 
	   true);
	addSingleParam(param);   
      }
    }

    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aClassPath);
      layout.addEntry(aUsesAPI); 
      layout.addSeparator();
      layout.addEntry(aExtraOptions);

      {
	LayoutGroup warn = new LayoutGroup
	  ("Warning Messages", "Controls over the types of warning messages.", true);
	warn.addEntry(aDeprecation);
	warn.addEntry(aUnchecked);
	warn.addEntry(aFallthrough);
	warn.addEntry(aPath);
	warn.addEntry(aSerial);
	warn.addEntry(aFinally);

	layout.addSubGroup(warn);
      }

      setSingleLayout(layout);
    }

    addSupport(OsType.MacOS);
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
    /* sanity checks */
    TreeSet<Path> sourceClasses = new TreeSet<Path>();
    Path targetJAR = null;
    {
      /* the target JAR file */ 
      {
	NodeID nodeID = agenda.getNodeID();
	FileSeq fseq = agenda.getPrimaryTarget();
	String suffix = fseq.getFilePattern().getSuffix();
	if(!fseq.isSingle() || (suffix == null) || !suffix.equals("jar")) 
	  throw new PipelineException
	    ("The JavaBuild Action requires that the target primary file sequence " + 
	     "must be a single JAR file (.jar)!"); 
	
	targetJAR = new Path(PackageInfo.sProdPath,
			     nodeID.getWorkingParent() + "/" + fseq.getPath(0)); 
      }

      /* the Java source files */ 
      {
	Path npath = new Path(agenda.getNodeID().getName());
	String parent = npath.getParentPath().toString();

	for(String sname : agenda.getSourceNames()) {
	  FileSeq fseq = agenda.getPrimarySource(sname);

	  FilePattern fpat = fseq.getFilePattern();
	  String suffix = fpat.getSuffix();
	  if(!fseq.isSingle() || (suffix == null) || !suffix.equals("java")) 
	    throw new PipelineException
	      ("The JavaBuild Action requires that the source node (" + sname + ") " +
	       "must have a single Java source file (.java) as its primary file sequence!");
	  
	  if(!sname.startsWith(parent))
	    throw new PipelineException 
	      ("The Java source file node (" + sname + ") must live in a subdirectory of " + 
	       "the generated JAR file (" + targetJAR + ")!");
	  
	  Path spath = new Path(sname.substring(parent.length() + 1));
	  sourceClasses.add(new Path(spath.getParentPath(), fpat.getPrefix()));
	}
      }
    }
    
    /* create a temporary shell script file */ 
    File script = createTemp(agenda, 0755, "bash");
    try {      
      FileWriter out = new FileWriter(script);

      out.write("#!/bin/bash\n\n");
      
      {
	TreeSet<Path> dirs = new TreeSet<Path>();
	for(Path cname : sourceClasses) {
	  Path parent = cname.getParentPath(); 
	  while(parent != null) {
	    dirs.add(parent); 
	    parent = parent.getParentPath();
	  }
	}
	
	out.write("echo 'Creating Build Dirs...'\n"); 
	for(Path dir : dirs) {
	  Path path = new Path(getTempPath(agenda), dir);
	  out.write("echo 'mkdir -m 777 " + path.toOsString() + "'\n" + 
		    "if ! mkdir -m 777 " + path.toOsString() + "\n" + 
		    "then\n" +
		    "  echo 'FAILED.'\n" + 
		    "  exit 1\n" + 
		    "fi\n");	
	}

	out.write("echo 'Done.'\n" + 
		  "echo\n\n");
      }

      {
	StringBuilder buf = new StringBuilder();
	buf.append("javac -source 1.5"); 
	 
	String extra = (String) getSingleParamValue(aExtraOptions);
	if(extra != null) 
	  buf.append(" " + extra);
	
	buf.append(" -d " + getTempPath(agenda).toOsString());

	{
	  buf.append(" -Xlint:");

	  {
	    Boolean tf = (Boolean) getSingleParamValue(aDeprecation);
	    if((tf == null) || !tf) 
	      buf.append("-");
	    buf.append("deprecation,");
	  }

	  {
	    Boolean tf = (Boolean) getSingleParamValue(aUnchecked);
	    if((tf == null) || !tf) 
	      buf.append("-");
	    buf.append("unchecked,");
	  }

	  {
	    Boolean tf = (Boolean) getSingleParamValue(aFallthrough);
	    if((tf == null) || !tf) 
	      buf.append("-");
	    buf.append("fallthrough,");
	  }

	  {
	    Boolean tf = (Boolean) getSingleParamValue(aPath);
	    if((tf == null) || !tf) 
	      buf.append("-");
	    buf.append("path,");
	  }

	  {
	    Boolean tf = (Boolean) getSingleParamValue(aSerial);
	    if((tf == null) || !tf) 
	      buf.append("-");
	    buf.append("serial,");
	  }

	  {
	    Boolean tf = (Boolean) getSingleParamValue(aFinally);
	    if((tf == null) || !tf) 
	      buf.append("-");
	    buf.append("finally");
	  }
	}

	buf.append(" -classpath ");
	{
	  Path api = null;
	  {
	    Boolean useAPI = (Boolean) getSingleParamValue(aUsesAPI);
	    if((useAPI != null) && useAPI)
	      api = new Path(PackageInfo.getInstPath(PackageInfo.sOsType), "lib/api.jar");
	  }

	  String classPath = (String) getSingleParamValue(aClassPath); 
	  if((classPath != null) && (classPath.length() > 0)) {
	    if(api != null) 
	      buf.append(classPath + ":" + api.toOsString()); 
	    else 
	      buf.append(classPath);
	  }
	  else if(api != null) {
	    buf.append(api.toOsString()); 
	  }
	}
	
	for(Path cname : sourceClasses) 
	  buf.append(" " + cname.toOsString() + ".java");
	
	String compile = buf.toString();
	out.write("echo 'Compiling...'\n" +
		  "echo '" + compile + "'\n" +
		  "if " + compile + "\n" + 
		  "then\n" +
		  "  echo 'Done.'\n" + 
		  "  echo\n" + 
		  "else\n" + 
		  "  echo 'FAILED.'\n" + 
		  "  exit 1\n" + 
		  "fi\n\n");
      }

      out.write("cd " + getTempPath(agenda).toOsString() + "\n");

      {
	StringBuilder buf = new StringBuilder();
	buf.append("jar cvf " + targetJAR.toOsString());

	for(Path cname : sourceClasses) 
	  buf.append(" " + cname.toOsString() + ".class");
	
	String archive = buf.toString();
	out.write("echo 'Building JAR...'\n" +
		  "echo '" + archive + "'\n" +
		  "if " + archive + "\n" + 
		  "then\n" +
		  "  echo 'Done.'\n" + 
		  "  echo\n" + 
		  "else\n" + 
		  "  echo 'FAILED.'\n" + 
		  "  exit 1\n" + 
		  "fi\n\n");
      }

      out.write("echo 'ALL DONE.'");

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
      ArrayList<String> args = new ArrayList<String>();
      args.add(script.toString());

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

  private static final long serialVersionUID = 5477525606063361613L;

  private static final String aClassPath    = "ClassPath";
  private static final String aUsesAPI      = "UsesAPI";
  private static final String aExtraOptions = "ExtraOptions";
  private static final String aDeprecation  = "Deprecation"; 
  private static final String aUnchecked    = "Unchecked";  
  private static final String aFallthrough  = "Fallthrough"; 
  private static final String aPath         = "Path";        
  private static final String aSerial       = "Serial";      
  private static final String aFinally      = "Finally";     

}
