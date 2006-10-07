// $Id: JavaUtilityAction.java,v 1.2 2006/10/07 12:54:24 jim Exp $

package us.temerity.pipeline.plugin.v2_0_15;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.utils.*;
import us.temerity.pipeline.glue.GlueException;
import us.temerity.pipeline.glue.io.GlueEncoderImpl;

/*------------------------------------------------------------------------------------------*/
/*   J A V A   U T I L I T Y   A C T I O N                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * Run an external Pipeline utility application. <P> 
 * 
 * The application must be packaged as JAR file and is typically compiled from Java sources
 * using the {@link JavaBuildAction}.  The class specified by Main Class must be derived 
 * from {@link BaseUtilityApp}.
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Main Class <BR>
 *   <DIV style="margin-left: 40px;">
 *     The full name of the entry point class including Java package path.
 *   </DIV> <BR>
 * 
 *   Class Path <BR>
 *   <DIV style="margin-left: 40px;">
 *     Additional JARs and/or directories to append to the Java Runtime's class path.<BR>
 *     The Pipeline API JAR will always be appended to any paths specified here.
 *   </DIV> <BR>
 * 
 *   Parallel GC <BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether to use the Parallel garbage collector.
 *   </DIV> <BR>
 * 
 *   Initial Heap Size <BR>
 *   <DIV style="margin-left: 40px;">
 *     Ihe initial size, in bytes, of the memory allocation pool. <BR> 
 *     This value must be a multiple of 1024 greater than 1MB.
 *   </DIV> <BR>
 * 
 *   Maximum Heap Size <BR>
 *   <DIV style="margin-left: 40px;">
 *     Ihe maximum size, in bytes, of the memory allocation pool. <BR> 
 *     This value must be a multiple of 1024 greater than 2MB.
 *   </DIV> <BR>
 * 
 *   Extra Options <BR>
 *   <DIV style="margin-left: 40px;">
 *     Additional command-line arguments. <BR> 
 *   </DIV> <BR>
 * </DIV> <P> 
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Order <BR>
 *   <DIV style="margin-left: 40px;">
 *     Each source node which sets this parameter should have a JAR file its primary
 *     file sequence which will be added to the path searched for classes by the Java Runtime.
 *     This parameter determines the order in which the JARs are searched.  If this parameter 
 *     is not set for a source node, it will be ignored.
 *   </DIV> 
 * </DIV> <P> 
 */
public class 
JavaUtilityAction 
  extends BaseAction
{
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
  
  public
  JavaUtilityAction() 
  {
    super("JavaUtility", new VersionID("2.0.15"), "Temerity", 
	  "Exectutes a Java based external Pipeline utiltiy application.");

    {
      ActionParam param = 
	new StringActionParam
	(aMainClass, 
	 "The full name of the entry point class including Java package path.",
	 null);
      addSingleParam(param);   
    }
    
    {
      ActionParam param = 
	new StringActionParam
	(aClassPath, 
	 "Additional JARs and/or directories to add to the Java Runtime's class path.", 
	 null);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new BooleanActionParam
	(aParallelGC, 
	 "Whether to use the Parallel garbage collector.", 
	 true);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new ByteSizeActionParam
	(aInitialHeapSize, 
	 "Ihe initial size, in bytes, of the memory allocation pool. This value must be " + 
	 "a multiple of 1024 greater than 1MB.",
	 16777216L);
      addSingleParam(param);
    }

    {
      ActionParam param = 
	new ByteSizeActionParam
	(aMaximumHeapSize, 
	 "Ihe maximum size, in bytes, of the memory allocation pool. This value must be " + 
	 "a multiple of 1024 greater than 2MB.",
	 67108864L);
      addSingleParam(param);
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
      layout.addEntry(aMainClass); 
      layout.addEntry(aClassPath);
      layout.addSeparator();
      layout.addEntry(aParallelGC);
      layout.addEntry(aInitialHeapSize);
      layout.addEntry(aMaximumHeapSize);      
      layout.addSeparator();
      layout.addEntry(aExtraOptions);

      setSingleLayout(layout);
    }

    addSupport(OsType.MacOS);
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
	(aOrder, 
	 "Search the JAR files in this order.",
	 100);
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
    /* sanity checks */
    String mainClass = null;
    TreeMap<Integer,LinkedList<Path>> jars = new TreeMap<Integer,LinkedList<Path>>();
    {
      /* entry point class */ 
      {
	mainClass = (String) getSingleParamValue(aMainClass); 
	if((mainClass == null) || (mainClass.length() == 0))
	  throw new PipelineException 
	    ("A Main Class must be specified!");
      }

      /* generate the table of JAR files to search */ 
      NodeID nodeID = agenda.getNodeID();
      for(String sname : getSourceNames()) {
	Integer order = (Integer) getSourceParamValue(sname, aOrder);
	FileSeq fseq = agenda.getPrimarySource(sname);
	if(fseq == null) 
	  throw new PipelineException
	    ("Somehow an per-source Order parameter exists for a node (" + sname + ") " + 
	     "which was not one of the source nodes!");
	
	String suffix = fseq.getFilePattern().getSuffix();
	if(!fseq.isSingle() || 
	   (suffix == null) || !suffix.equals("jar"))
	  throw new PipelineException
	    ("The JavaUtiltiy Action requires that the source node (" + sname + ") with " + 
	     "per-source Order parameter must have a single JAR file as its primary file " + 
	     "sequence!");
	
	NodeID snodeID = new NodeID(nodeID, sname);
	Path archive = new Path(PackageInfo.sProdPath,
				snodeID.getWorkingParent() + "/" + fseq.getPath(0));

	LinkedList<Path> archives = jars.get(order);
	if(archives == null) {
	  archives = new LinkedList<Path>();
	  jars.put(order, archives);
	}
	
	archives.add(archive);
      }
    }

    /* create the input GLUE file for the utility application */ 
    File glueFile = createTemp(agenda, 0644, "glue");
    try {
      TreeMap<String, Object> table = new TreeMap<String, Object>();
      table.put("Agenda", agenda);
      table.put("Action", new BaseAction(this));
      
      GlueEncoderImpl encode = new GlueEncoderImpl("agenda", table);
      FileWriter out = new FileWriter(glueFile);
      out.write(encode.getText());
      out.close();
    } 
    catch (GlueException e) {
      throw new PipelineException
	("Unable to encode the GLUE input file for Job (" + agenda.getJobID() + ")!\n" + 
	 e.getMessage());
    } 
    catch (IOException e) {
      throw new PipelineException
	("Unable to write the intput GLUE file (" + glueFile + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 e.getMessage());
    }

    try {
      ArrayList<String> args = new ArrayList<String>();
      args.add("-enableassertions"); 
      args.add("-server"); 

      {
	Boolean tf = (Boolean) getSingleParamValue(aParallelGC); 
	if((tf != null) && tf)
	  args.add("-XX:+UseParallelGC");
      }

      
      Long initialSize = 16777216L;
      {
	Long size = (Long) getSingleParamValue(aInitialHeapSize); 
	if(size != null) {
	  if(size < 1048576L) 
	    throw new PipelineException
	      ("The Initial Heap Size must be at least 1MB!");

	  if((size % 1024) != 0)
	    throw new PipelineException
	      ("The Initial Heap Size must be a muliple of 1024 bytes!"); 

	  initialSize = size;
	}
      }

      Long maximumSize = 67108864L; 
      {
	Long size = (Long) getSingleParamValue(aMaximumHeapSize); 
	if(size != null) {
	  if(size < 2097152L) 
	    throw new PipelineException
	      ("The Maximum Heap Size must be at least 2MB!");

	  if((size % 1024) != 0)
	    throw new PipelineException
	      ("The Maximum Heap Size must be a muliple of 1024 bytes!"); 

	  maximumSize = size;
	}
      }

      if(initialSize > maximumSize) 
	throw new PipelineException
	  ("The Initial Heap Size must be smaller than the Maximum Heap Size!");
      args.add("-Xms" + initialSize); 
      args.add("-Xmx" + maximumSize); 


      addExtraOptions(args);


      args.add("-cp");
      {
	boolean first = true;
	StringBuffer buf = new StringBuffer();
	for(LinkedList<Path> archives : jars.values()) {
	  for(Path apath : archives) {
	    if(!first) 
	      buf.append(":");
	    first = false;

	    buf.append(apath.toOsString()); 	    
	  }
	}
	
	String classPath = (String) getSingleParamValue(aClassPath); 
	if((classPath != null) && (classPath.length() > 0)) {
	  if(!first) 
	    buf.append(":");
	  first = false;

	  buf.append(classPath);
	}
	
	if(!first) 
	  buf.append(":");
	first = false;
	
	Path api = new Path(PackageInfo.getInstPath(PackageInfo.sOsType), "lib/api.jar");
	buf.append(api.toOsString()); 

	args.add(buf.toString());
      }

      args.add(mainClass);
      args.add(glueFile.toString());

      String java = PackageInfo.getJavaRuntime(PackageInfo.sOsType).toOsString();
      return new SubProcessHeavy
	(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
	 java, args, agenda.getEnvironment(), agenda.getWorkingDir(), 
	 outFile, errFile);
    } 
    catch (Exception ex) {
      throw new PipelineException
	("Unable to generate the SubProcess to perform this Action!\n" +
	 ex.getMessage());
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
    String extra = (String) getSingleParamValue("ExtraOptions");
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

  private static final long serialVersionUID = -1571885861406221728L;

  private static final String aMainClass       = "MainClass";
  private static final String aClassPath       = "ClassPath";
  private static final String aParallelGC      = "ParallelGC";
  private static final String aInitialHeapSize = "InitialHeapSize";
  private static final String aMaximumHeapSize = "MaximumHeapSize";
  private static final String aExtraOptions    = "ExtraOptions";
  private static final String aOrder           = "Order"; 

}
