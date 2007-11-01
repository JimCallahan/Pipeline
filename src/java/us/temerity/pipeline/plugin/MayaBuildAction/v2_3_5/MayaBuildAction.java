// $Id: MayaBuildAction.java,v 1.3 2007/11/01 19:16:55 jesse Exp $

package us.temerity.pipeline.plugin.MayaBuildAction.v2_3_5;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;
import us.temerity.pipeline.math.Range;
import us.temerity.pipeline.plugin.MayaBuildUtils;

/*------------------------------------------------------------------------------------------*/
/*   M A Y A   B U I L D   A C T I O N                                                      */
/*------------------------------------------------------------------------------------------*/

/** 
 * Generates a new Maya scene from component scenes. <P> 
 * 
 * A new empty scene is first created.  The component scenes are either referenced as Maya 
 * references or directly imported depending on a per-source parameter from each source 
 * node who's primary file sequence is a Maya scene file ("ma" or "mb").  
 * 
 * This new version also adds support for Maya's proxy referencing tools.  This allows
 * multiple versions of the same model to be loaded in as a single reference, which a
 * user can then switch between.  To use this feature, a new option has been added to
 * Build Type called Proxy.  When a source is set to Proxy, it will use its Prefix Name
 * parameter to figure out which model it should belong to.  If you set a model to Proxy
 * and give it a Prefix Name that does not match any of the Prefix Names for Reference
 * models, an error will be thrown by the Action.   
 * 
 * You must also specify a Proxy Name for all Proxy models.  These do not have to be 
 * unique for the Action, but must be unique for a given Prefix Name.  The Action 
 * also accepts a Proxy Name for a Reference model.
 * 
 * At each stage in the process, an optional MEL script may be evaluated.  The MEL scripts
 * must be the primary file sequence of one of the source nodes and are assigned to the 
 * appropriate stage using the Intial MEL, Model MEL and Final MEL single valued
 * parameters. <P> 
 * 
 * This action defines the following single valued parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Defer References<BR>
 *   <DIV style="margin-left: 40px;">
 *     Should all the references be unloaded by default.
 *   </DIV> <BR>
 *   
 *   Linear Unit <BR>
 *   <DIV style="margin-left: 40px;">
 *     The linear unit that the generated scene will use. 
 *   </DIV> <BR>
 * 
 *   Angular Unit <BR>
 *   <DIV style="margin-left: 40px;">
 *     The angular unit that the generated scene will use. 
 *   </DIV> <BR>
 *   
 *   Time Unit <BR>
 *   <DIV style="margin-left: 40px;">
 *     The unit of time and frame rate that the generated scene will use. 
 *   </DIV> <BR>
 *   
 *   Start Frame <BR>
 *   <DIV style="margin-left: 40px;">
 *     The start frame of the generated Maya scene.
 *   </DIV> <BR>
 *   
 *   End Frame <BR>
 *   <DIV style="margin-left: 40px;">
 *     The end frame of the generated Maya scene.
 *   </DIV> <BR>
 *   
 *   Initial MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *      The source node containing the MEL script to evaluate just after scene creation
 *      and before importing any models.
 *   </DIV> <BR>
 * 
 *   Model MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *      The source node containing the MEL script to evaluate after importing all models
 *      but before saving the generated Maya scene.
 *   </DIV> <BR>
 * 
 *   Final MEL <BR>
 *   <DIV style="margin-left: 40px;">
 *      The source node containing the MEL script to evaluate after saving the generated 
 *      Maya scene. 
 *   </DIV> 
 * </DIV> <P> 
 * 
 * This action defines the following per-source parameters: <BR>
 * 
 * <DIV style="margin-left: 40px;">
 *   Build Type<BR>
 *   <DIV style="margin-left: 40px;">
 *     The method Maya should use to merge the data from the source scene into the
 *     generated scene.
 *   </DIV> <BR>
 *   
 *   Name Space<BR>
 *   <DIV style="margin-left: 40px;">
 *     Whether Maya should create a namespace for the imported/referenced scene.
 *     This option is highly recommended to avoid name clashes.
 *   </DIV> <BR>
 *   
 *   Num Instances<BR>
 *   <DIV style="margin-left: 40px;">
 *     The number of instances of a model that should be created.  If this is set
 *     to zero, then one instance will still be created, but it will not have
 *     any numbering in the namespace.  For example, if the Prefix Name is 'rock',
 *     setting Num Instances to '0' will result in a namespace of 'rock'.  Setting
 *     it to '1' will result in rock-0001
 *   </DIV> <BR>
 *   
 *   Instance Start<BR>
 *   <DIV style="margin-left: 40px;">
 *     The number for the first instance of a reference.  This allows numbered 
 *     instances to start somewhere besides zero.
 *   </DIV> <BR>
 * 
 *   Prefix Name <BR>
 *   <DIV style="margin-left: 40px;">
 *     The namespace prefix to use for the imported/referenced Maya scene inside the 
 *     generated Maya scene.  If unset, the namespace is based on the filename.
 *   </DIV> <BR>
 *   
 *   Proxy Name <BR>
 *   <DIV style="margin-left: 40px;">
 *     The proxy name to be used for the referenced Maya scene.  If Build Type is set
 *     to Reference, this will be the proxy tag for the reference.  If Build Type is set
 *     to Import than this field will be ignored.
 *   </DIV> <BR>
 * </DIV> <P> 
 */
public
class MayaBuildAction
  extends MayaBuildUtils
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/
 
  public
  MayaBuildAction() 
  {
    super("MayaBuild", new VersionID("2.3.5"), "Temerity",
	  "Builds a Maya scene from component scenes.");
    
    addUnitsParams();
    addStartFrameParam();
    addEndFrameParam();
    addInitalMELParam();
    addModelMELParam();
    addFinalMELParam();
    
    {
      ActionParam param = 
	new BooleanActionParam
	(aDeferReferences,
	 "Should all created references be in the unloaded state.", 
	 false);
      addSingleParam(param);
    }
    
    {
      LayoutGroup layout = new LayoutGroup(true);
      layout.addEntry(aDeferReferences);
      layout.addSeparator();
      addUnitsParamsToLayout(layout); 
      layout.addSeparator();
      layout.addEntry(aStartFrame);
      layout.addEntry(aEndFrame);
      layout.addSeparator();
      layout.addEntry(aInitialMEL);
      layout.addEntry(aModelMEL);
      layout.addEntry(aFinalMEL);
      
      setSingleLayout(layout);
    }

    {
      LinkedList<String> layout = new LinkedList<String>();
      layout.add(aBuildType);
      layout.add(aNameSpace);
      layout.add(aPrefixName);
      layout.add(aProxyName);
      layout.add(aNumInstances);
      layout.add(aInstanceStart);

      setSourceLayout(layout);
    }

    addSupport(OsType.MacOS);
    addSupport(OsType.Windows);
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

    addBuildTypeSourceParam(params);
    addNamespaceSourceParam(params);
    addPrefixNameSourceParam(params);
    addProxyNameSourceParam(params);
    addNumInstancesSourceParam(params);
    addInstanceStartSourceParam(params);

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
    /* MEL script paths */ 
    //Path initialMEL = getMelScriptSourcePath(aInitialMEL, agenda);
    //Path modelMEL   = getMelScriptSourcePath(aModelMEL, agenda);
    //Path finalMEL   = getMelScriptSourcePath(aFinalMEL, agenda);
    
    boolean deferReferences = getSingleBooleanParamValue(aDeferReferences);

    /* model filenames */ 
    TreeMap<String,Path> modelPaths = new TreeMap<String,Path>();
    DoubleMap<String, String, Path> proxyPaths = new DoubleMap<String, String, Path>();
    TreeMap<String, String> nameSpaces = new TreeMap<String, String>();
    TreeMap<String, String> reverseNameSpaces = new TreeMap<String, String>();
    TreeMap<String, Boolean> usesNameSpace = new TreeMap<String, Boolean>();
    TreeMap<String, String> buildTypes = new TreeMap<String, String>();
    TreeMap<String, String> proxyNames = new TreeMap<String, String>();
    
    for(String sname : agenda.getSourceNames()) {
      if(hasSourceParams(sname)) {
        FileSeq fseq = agenda.getPrimarySource(sname);
        String suffix = fseq.getFilePattern().getSuffix();
        if(fseq.isSingle() && (suffix != null)) {
          if(suffix.equals("ma") || suffix.equals("mb")) {
            Path npath = new Path(sname);
            
            String prefixName = getSourceStringParamValue(sname, aPrefixName);
            if(prefixName == null) {
              prefixName = npath.getName();
            }
            
            String proxyName = getSourceStringParamValue(sname, aProxyName);
            
            boolean useNSpace = false;
            {
              Boolean tf = (Boolean) getSourceParamValue(sname, aNameSpace);
              useNSpace = ((tf != null) && tf);
            }
            
            String buildType = getSourceStringParamValue(sname, aBuildType, false);
            if (buildType.equals(aProxy)) {
              if (proxyName == null)
        	throw new PipelineException
        	  ("The source (" + sname + ") is specified as a Proxy model, " +
        	   "but no Proxy Name is set.  A Valid Proxy Name is required " +
        	   "for all Proxy models.");
              if (proxyPaths.containsKey(prefixName, proxyName))
        	throw new PipelineException
        	  ("Two models specify the same Prefix Name (" + prefixName + ") " +
        	   "and Proxy Name (" + proxyName + ")");
              proxyPaths.put(prefixName, proxyName, new Path(npath.getParentPath(), fseq.getPath(0)));
            }
            else {
              if (!(buildType.equals(aReference) || buildType.equals(aImport)))
        	throw new PipelineException
        	  ("Invalid build type (" + buildType + ") specified!");
              modelPaths.put(sname, new Path(npath.getParentPath(), fseq.getPath(0)));
              nameSpaces.put(sname, prefixName);
              reverseNameSpaces.put(prefixName, sname);
              buildTypes.put(sname, buildType);
              usesNameSpace.put(sname, useNSpace);
              if (proxyName != null)
        	proxyNames.put(sname, proxyName);
            }
          }
        }
      }
    }
    
    /* extra proxy error checking */
    for (String nspace : proxyPaths.keySet()) {
      String model = reverseNameSpaces.get(nspace);
      if (model == null)
	throw new PipelineException
	  ("There is a proxy model assigned to a namespace (" + nspace + ") that does not " +
	   "exist for a Reference model.");
      if (!buildTypes.get(model).equals(aReference))
	throw new PipelineException
	  ("There is a proxy model assigned to a namespace (" + nspace + ") that does not " +
	   "correspond to a Reference model.");
      Set<String> proxys = proxyPaths.keySet(nspace);
      String refProxy = proxyNames.get(model); 
      if (refProxy != null && proxys.contains(refProxy))
	throw new PipelineException
	  ("The same Proxy Name cannot be specified for a Reference and a Proxy in the same " +
	   "namespace (" + nspace + ")");
    }

    /* the target Maya scene */
    Path targetScene = getMayaSceneTargetPath(agenda);
    String sceneType = getMayaSceneType(agenda);

    /* create a temporary MEL script file */ 
    File script = createTemp(agenda, "mel");
    try {      
      FileWriter out = new FileWriter(script);

      /* a workaround needed in "maya -batch" mode */ 
      out.write("// WORK AROUNDS:\n" + 
		"lightlink -q;\n\n");
      
      out.write("source \"proxyUtils.mel\";\n\n");
      
      /* rename the current scene as the output scene */ 
      out.write("// SCENE SETUP\n" + 
		"file -rename \"" + targetScene + "\";\n" + 
                "file -type \"" + sceneType + "\";\n\n");

      out.write(genUnitsMEL());

      /* the initial MEL script */ 
      writeInitialMEL(agenda, out);
      
      /* the model file reference imports */ 
      for(String sname : modelPaths.keySet()) {
	Path mpath = modelPaths.get(sname);

        String buildType = buildTypes.get(sname);
        
        int numInstances = 
          getSourceIntegerParamValue(sname, aNumInstances, new Range<Integer>(0, null));
        int instanceStart =
          getSourceIntegerParamValue(sname, aInstanceStart, new Range<Integer>(0, null));
        
        int number = numInstances;
        if (numInstances == 0) {
          number = 1;
          instanceStart = -1;
        }
        
        for (int each = 0; each < number; each++) {
          out.write
            ("// MODEL: " + sname + "\n" + 
             "print \"" + buildType + " Model: " + mpath + "\\n\";\n" +
             "{" + 
             "string $actualFile = `file\n");
          
          if (deferReferences)
            out.write
              ("-loadReferenceDepth \"none\"\n");

          String origSpace = nameSpaces.get(sname);
          String nspace = nameSpaces.get(sname);
          
          if (instanceStart != -1) {
            int current = instanceStart + each;
            String padded = pad(current);
            nspace = nspace + padded;
          }

          boolean useNSpace = false;
          {
            Boolean tf = (Boolean) getSourceParamValue(sname, aNameSpace);
            useNSpace = ((tf != null) && tf);
          }

          if(buildType.equals(aImport)) {
            out.write("  -import\n");

            if(useNSpace) 
              out.write("  -namespace \"" + nspace + "\"\n");
          }
          else if(buildType.equals(aReference)) {
            out.write("  -reference\n");

            if(useNSpace)
              out.write("  -namespace \"" + nspace + "\"\n");
            else
              out.write("  -renamingPrefix \"" + nspace + "\"\n");
          }
            
          {
            String fname = mpath.getName();
            if(fname.endsWith("ma")) 
              out.write("  -type \"mayaAscii\"\n");
            else if(fname.endsWith("mb")) 
              out.write("  -type \"mayaBinary\"\n");
            else 
              throw new PipelineException
                ("Unknown Maya scene format for source file (" + mpath + ")!");
          }

          out.write
          ("  -options \"v=0\"\n" + 
           "  \"$WORKING" + mpath + "\"`;\n");
        
        if (buildType.equals(aReference)) {
          out.write
            ("string $refNode = `file -q -rfn $actualFile`;\n");

          String proxy = proxyNames.get(sname);
          if (proxy != null) {
            out.write
              ("if( isValidReference( $refNode ) )\n" + 
      	       "  setAttr ($refNode + \".proxyTag\") -type \"string\" \"" + proxy + "\";\n");
          }
        }
          TreeMap<String, Path> proxies = proxyPaths.get(origSpace);
          if (proxies != null && proxies.size() > 0) {
            for (String proxyName : proxies.keySet()) {
              Path ppath = proxies.get(proxyName);
              String finalPath = "$WORKING" + ppath;
              out.write("optionVar -intValue proxyOptionsSharedEdits true;\n");
              out.write("proxyAdd $refNode \"" + finalPath + "\" \"" + proxyName +"\";\n");  
            }
            
          }
          out.write("}\n\n");
        }
      }
      
      /* the model MEL script */ 
      writeModelMEL(agenda, out);
      
      /* set the time options */ 
      {	
        out.write("// TIME RANGE\n");

        Integer start = (Integer) getSingleParamValue(aStartFrame);
        if(start != null) {
          out.write("playbackOptions -e -min " + start + ";\n");
          out.write("playbackOptions -e -ast " + start + ";\n");
        }
        
        Integer end = (Integer) getSingleParamValue(aEndFrame);
        if(end != null) {
          out.write("playbackOptions -e -max " + end + ";\n");
          out.write("playbackOptions -e -aet " + end + ";\n");
        }

        out.write("\n"); 
      }

      /* save the file */ 
      out.write("// SAVE\n" + 
		"print \"Saving Generated Scene: " + targetScene + "\\n\";\n" + 
		"file -save;\n\n");

      /* the final MEL script */ 
      writeFinalMEL(agenda, out);

      out.write("print \"ALL DONE.\\n\";\n");
      out.close();
    }
    catch(IOException ex) {
      throw new PipelineException
	("Unable to write temporary MEL script file (" + script + ") for Job " + 
	 "(" + agenda.getJobID() + ")!\n" +
	 ex.getMessage());
    }

    /* create the process to run the action */
    return createMayaSubProcess(null, script, true, agenda, outFile, errFile);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create a four digit padding string for a reference number.
   */ 
  private String 
  pad
  (
   int num
  )
    throws PipelineException
  {
    String parse = String.valueOf(num);
    int length = parse.length();
    if (length > 4)
      throw new PipelineException("Cannot create references numbers over 9999");
    for (int i = length; i < 4; i++) 
      parse = "0" + parse;
    return parse;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -3616929088803763448L;

  
}
