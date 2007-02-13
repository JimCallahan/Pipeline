package com.sony.scea.pipeline.plugins.v1_1_0;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;

/**
 * Replaces the assets referenced into the selected source node with different 
 * models referenced into the target node, and then applies any linked Syflex Caches or
 * Shave Caches to the models. 
 * <p>
 * This action depends on being able to extract information about the Source node
 * from the ActionInfo on that node.  This requires that all models that need to
 * be replaced have to have source parameters on the Source node or this Action
 * will not be able to find them.  If that happens, then even if the references
 * have the right source suffix, they will not get replaced.  This is a considerable
 * drawback and should be addressed at some point, but it is due to the natural
 * limitations that Pipeline imposes with the ActionInfo class.
 * <p>
 * This Action creates a sed script that will do all the string replacements that
 * are necessary.  It then creates a bash script that runs the sed script, and possible
 * runs maya before and/or after the sed script depending on whether there are 
 * Pre and Post replace MELs.
 * <P>
 * Syflex caches are applied based upon the Cloth Object Action Parameter on the 
 * attached Syflex nodes.  Only caches that have either the SyflexCache or the 
 * SyflexCacheCopy Action will have caches applied.
 * <p>
 * Shave caches are applied using the standard method used by the ShaveApplyCache
 * Action. Note that Shave and a Haircut only allows you one directory as the path for a cache.  
 * If you attempt to link in multiple shave caches in different directories, this Action will
 * throw an error if you attempt to load multiple caches from different directories.  The
 * Action identifies caches from their name, which must start with <code>shaveStatFile_</code>
 * and end with the <code>stat</code> suffix.  Any nodes which do not match this pattern
 * will not be considered cache nodes. 
 * 
 * 
 * <DIV style="margin-left: 40px;">
 *   Source<BR>
 *   <DIV style="margin-left: 40px;">
 *    The maya scene that we are going to run the switch on.
 *   </DIV> <BR>
 *   
 *   Source Suffix<BR>
 *   <DIV style="margin-left: 40px;">
 *    The suffix attached to models in the source scene that is going to be searched
 *    for.  Any models which have this suffix will be replaced if there is a matching model
 *    with the target suffix.
 *   </DIV> <BR>
 *   
 *   Target Suffix<BR>
 *   <DIV style="margin-left: 40px;">
 *    The suffix that is looked for on nodes attached to this node.  If a node has this suffix
 *    and there is a corresponding node with the source suffix, then the node with this suffix
 *    will replace that other node.  Confused yet?
 *   </DIV> <BR>
 *   
 *   Response<BR>
 *   <DIV style="margin-left: 40px;">
 *    What the Action will do with references that it finds that do not have an analog in
 *    the target suffix set. <ul>
 *    	<li> Remove: This will remove any reference that does not have an analog.
 *      <li> Ignore: This just ignores all references without analogs.
 *   </DIV> <BR>
 *   
 *   ApplySyflexCache<BR>
 *   <DIV style="margin-left: 40px;">
 *    Should the syflex caches be applied.
 *   </DIV> <BR>
 *   
 *   ApplyShaveCache<BR>
 *   <DIV style="margin-left: 40px;">
 *    Should the shave caches be applied.
 *   </DIV> <BR>
 *   
 *   PreReplaceMEL<BR>
 *   <DIV style="margin-left: 40px;">
 *    The MEL script that will get run before any of the replacing is done.
 *   </DIV> <BR>
 *   
 *   PostReplaceMEL<BR>
 *   <DIV style="margin-left: 40px;">
 *    The MEL script that will get run after the replacing is done.
 *   </DIV> <BR>
 *   
 *   InitialMEL<BR>
 *   <DIV style="margin-left: 40px;">
 *    The MEL script to run after opening the scene and before applying the cache.
 *   </DIV> <BR>
 *   
 *   CacheMEL<BR>
 *   <DIV style="margin-left: 40px;">
 *    The MEL script to run after applying the cache, but before saving the scene.
 *    
 *   FinalMEL<BR>
 *   <DIV style="margin-left: 40px;">
 *    The MEL script to run after saving the scene.
 *   </DIV> <BR>
 *   
 * </DIV> <P> 
 * 
 * @author Ifedayo O. Ojomo
 * @author Jesse C. Clemens
 * @version 1.1.0
 */
public class ModelReplaceCacheAction extends BaseAction{

	/*----------------------------------------------------------------------------------------*/
	/*   S T A T I C   I N T E R N A L S                                                      */
	/*----------------------------------------------------------------------------------------*/
	private static final String aPreReplace = "PreReplaceMEL";
	private static final String aSource = "Source";
	private static final String aPostReplace = "PostReplaceMEL";
	private static final String aSrcSuff = "SourceSuffix";
	private static final String aTrgtSuff = "TargetSuffix";
	private static final String aResponse = "Response";
	private static final String aRemove = "Remove";
	private static final String aIgnore = "Ignore";
	private static final String aDefaultSwitch = "DefaultSwitch";

	private static final String aInitialMEL = "InitialMEL";
	private static final String aCacheMEL = "CacheMEL";
	private static final String aFinalMEL = "FinalMEL";
	private static final String aApplySyflexCache = "ApplySyflexCache";
	private static final String aApplyShaveCache = "ApplyShaveCache";
	
	private static final long serialVersionUID = 4542176392420524437L;



	/*----------------------------------------------------------------------------------------*/
	/*   C O N S T R U C T O R                                                                */
	/*----------------------------------------------------------------------------------------*/

	public ModelReplaceCacheAction() {
		super("ModelReplaceCache", new VersionID("1.1.0"), "SCEA",
			"Gives the ability to get the sources of selected source node and reference " +
			"other versions of those sources such as cloth, hr models to a target node. " +
			"The user can choose the response should a source node not have any matching " +
			"nodes.");
		
		underDevelopment();
		
		{
		  ActionParam param = new LinkActionParam
		  (aPreReplace, "The mel script to be run before replacing references.", null);
		  addSingleParam(param);
		}
		{
		  ActionParam param = new LinkActionParam
		  (aSource, "The source node which contains the files to replace.", null);
		  addSingleParam(param);
		}
		{
		  ActionParam param = new LinkActionParam
		  (aPostReplace, "The mel script to be run after replacing references.", null);
		  addSingleParam(param);
		}

		{
		  ActionParam param = new StringActionParam
		  (aSrcSuff,"The suffix of files to replace, e.g lr, syf.","lr");
		  addSingleParam(param);
		}
		{
		  ActionParam param = new StringActionParam
		  (aTrgtSuff,"The suffix of files to be used as replacement","");
		  addSingleParam(param);
		}
		{
		  ArrayList<String> values = new ArrayList<String>();
		  values.add(aIgnore);
		  values.add(aRemove);
		  ActionParam param = new EnumActionParam
		  (aResponse,"Action to take if a matching replacement cannot be found",
		    aIgnore, values);
		  addSingleParam(param);
		}
		{
		  ActionParam param = 
		    new LinkActionParam
		    (aInitialMEL,
		      "The MEL script to evaluate after scene creation and before importing models.",
		      null);
		  addSingleParam(param);
		}

		{
		  ActionParam param = 
		    new LinkActionParam
		    (aCacheMEL,
		      "The MEL script to evaluate after loading the caches.", 
		      null);
		  addSingleParam(param);
		}

		{
		  ActionParam param = 
		    new LinkActionParam
		    (aFinalMEL,
		      "The MEL script to evaluate after saving the scene.", 
		      null);
		  addSingleParam(param);
		}
		{
		  ActionParam param = 
		    new BooleanActionParam
		    (aApplySyflexCache,
		      "Whether or not to apply the syflex caches.", 
		      false);
		  addSingleParam(param);
		}
		
		{
		  ActionParam param = 
		    new BooleanActionParam
		    (aApplyShaveCache,
		      "Whether or not to apply the shave cache.", 
		      false);
		  addSingleParam(param);
		}
		
		{
		  ActionParam param = 
		    new BooleanActionParam
		    (aDefaultSwitch,
		      "Whether or not to switch the default suffix (which is nothing) as well as an inputted suffix.", 
		      true);
		  addSingleParam(param);
		}

		{  
			LayoutGroup layout = new LayoutGroup(true);
			layout.addEntry(aSource);
			layout.addSeparator();   
			layout.addEntry(aPreReplace);
			layout.addEntry(aPostReplace);
			layout.addSeparator();
			layout.addEntry(aSrcSuff);
			layout.addEntry(aTrgtSuff);
			layout.addEntry(aDefaultSwitch);
			layout.addSeparator();
			layout.addEntry(aResponse);  
			layout.addSeparator();
			layout.addSeparator();
			layout.addEntry(aApplySyflexCache);
			layout.addEntry(aApplyShaveCache);
			layout.addSeparator();
			layout.addEntry(aInitialMEL);
			layout.addEntry(aCacheMEL);
			layout.addEntry(aFinalMEL);


			setSingleLayout(layout);   
		}
		
//		underDevelopment();

	}//end constructor

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
	@Override
	public SubProcessHeavy prep(ActionAgenda agenda, File outFile, File errFile) 
	throws PipelineException {
		Path sourceNodePath = null;
		String fileName = null;
		String targetFileName = null;

		NodeID nodeID = agenda.getNodeID();
		Path initialMel = null;
		Path cacheMel = null;
		Path finalMel = null;
		Path finalScene;

		{
			/* MEL script filenames */
			initialMel = getMelPath(aInitialMEL, "Initial MEL", agenda);
			finalMel = getMelPath(aFinalMEL, "Final MEL", agenda);
			cacheMel = getMelPath(aCacheMEL, "Cache MEL", agenda);
		}


		File debug = createTemp(agenda, 0644, "err");
		File tempMaya = createTemp(agenda,0777,"ma");

		PrintWriter err = null;
		try {
			err = new PrintWriter (new BufferedWriter (new FileWriter(debug)));
		} catch (IOException e) {
			throw new PipelineException(e);
		}

		FileSeq target = agenda.getPrimaryTarget();
		String replacePattern = ".*/assets/(character|set|prop)/";
		String modelPattern = replacePattern + ".*";

		String sname = null;
		sname = (String) getSingleParamValue(aSource);
		if(sname == null) 
			throw new PipelineException ("The Source node was not set!");

		err.println("Source is "+sname);		

		String preMel = (String) getSingleParamValue(aPreReplace);
		String postMel = (String) getSingleParamValue(aPostReplace);
		String srcSuff = (String) getSingleParamValue(aSrcSuff);
		String trgtSuffTemp = (String) getSingleParamValue(aTrgtSuff);
		TreeSet<String> trgtSuff = new TreeSet<String>(Arrays.asList(trgtSuffTemp.split(" ")));
		if ((Boolean) getSingleParamValue(aDefaultSwitch))
		  trgtSuff.add("");
		String resp = (String) ((EnumActionParam) getSingleParam(aResponse)).getValue();
		
		boolean syflexCache = (Boolean) getSingleParamValue(aApplySyflexCache);
		boolean shaveCache = (Boolean) getSingleParamValue(aApplyShaveCache);
		
		err.println("Source suffix is: "+srcSuff);
		err.println("Target suffix is: "+trgtSuff+"\n");

		if(srcSuff.equals(trgtSuff))		 		 
			throw new PipelineException("The source suffix is identical to the target suffix");

		/*--The source fileName--*/
		{
			NodeID snodeID = new NodeID(agenda.getNodeID(),sname);
			//Path nodePath = new Path(PackageInfo.sProdPath,
			//		snodeID.getWorkingParent() + "/" + agenda.getPrimarySource(sname).getPath(0));

			FileSeq fseq = agenda.getPrimarySource(sname); 
			if (fseq == null)
				throw new PipelineException("The value of the Maya Scene paramter cannot be null");
			String suffix = fseq.getFilePattern().getSuffix();
			if(!fseq.isSingle() || (suffix == null) || 
					!(suffix.equals("ma") || suffix.equals("mb"))) 
				throw new PipelineException
				("The SyflexCacheAction Action requires that the Maya Scene" + 
				"must be a single Maya scene file."); 

			sourceNodePath = new Path(PackageInfo.sProdPath, 
					snodeID.getWorkingParent() + "/" + fseq.getPath(0));
			fileName = sourceNodePath.toOsString();
		}



		/*---the target fileName---*/
		{
			FileSeq fseq = agenda.getPrimaryTarget();
			String suffix = fseq.getFilePattern().getSuffix();
			if(!fseq.isSingle() || 
					(suffix == null) || !(suffix.equals("ma") || suffix.equals("mb"))) 
				throw new PipelineException
				("The MayaShaderCopy Action requires that the primary target file sequence must " + 
				"be a single Maya scene file."); 

			finalScene = new Path(PackageInfo.sProdPath,
					agenda.getNodeID().getWorkingParent() + "/" + fseq.getFile(0));
			//Path trgtNode = new Path(PackageInfo.sProdPath, 
			//		agenda.getNodeID().getWorkingParent() + "/" + target.getPath(0));
			targetFileName = finalScene.toOsString();

		}


		if(!agenda.getSourceNames().contains(sname))
			throw new PipelineException 
			("The Source node (" + sname + ") is not linked to the target node " + 
					"(" + target + ")!");

		TreeSet<String> nomatchRef = new TreeSet<String>();

		
		TreeSet<String> syflexSources = new TreeSet<String>();
		TreeSet<String> shaveSources = new TreeSet<String>();
		Path shaveCachePath = null;
		err.println(agenda.getSourceNames());
		for (String sourceName : agenda.getSourceNames())
		{
		  { // Syflex
		    ActionInfo info = agenda.getSourceActionInfo(sourceName);
		    err.println(sourceName+"\t"+info);

		    if (info != null)
		    {
		      String name = info.getName();
		      err.println(name);
		      if (name.equals("SyflexCache") || name.equals("SyflexCacheCopy"))
		      {
			syflexSources.add(sourceName);
			err.println("Syflex Cache source: "+sourceName);
			continue;
		      }
		    }
		  } // end syflex
		  { // shave 
		    Path sourcePath = new Path(sourceName);
		    String objectPart = sourcePath.getName();
		    Path pathPart = sourcePath.getParentPath();

		    if(objectPart.startsWith("shaveStatFile_")) {
		      if (shaveCachePath == null)
			shaveCachePath = pathPart;
		      else {
			if(!shaveCachePath.equals(pathPart)) {
			  throw new PipelineException
			  ("Shave and a Haircut does not allow more than one cache path.\n" +
			    "This node has linked cache nodes with the paths\n (" + shaveCachePath + ")\n" +
			    "and (" + pathPart + ")");
			}
		      }
		      objectPart = objectPart.replaceFirst("shaveStatFile_", "");
		      String buffer[] = objectPart.split("_");
		      int size = buffer.length;
		      String shaveName = "";
		      for(int i = 0; i < size; i++) {
			String part = buffer[i];
			shaveName = part;
			if(i != (size - 1))
			  shaveName += ":";
		      }
		      shaveSources.add(shaveName);
		    }
		  } // end shave
		}
		err.println("\n"+syflexSources);
		if(syflexSources.isEmpty())
		  syflexCache = false;
		if (shaveSources.isEmpty() || shaveCachePath == null)
		  shaveCache = false;

		
		/*--get models connected to the source----------------*/
		ActionInfo srcInfo = agenda.getSourceActionInfo(sname);
		if(srcInfo==null)
			throw new PipelineException("This action's source node "+sname+" has no sources of its own!");

		TreeSet<String> srcLinks = new TreeSet<String>(srcInfo.getSourceNames());
		err.println(srcLinks);		

		for(String link : srcInfo.getSourceNames()){
			err.println(link);
			if(!(link.endsWith(srcSuff) && link.matches(modelPattern))){
				err.println(link+" is not the appropriate type: "+srcSuff);
				srcLinks.remove(link);
				nomatchRef.add(link);
				err.println("Planning to remove "+link);
			}//end if
			if((srcSuff.length()==0)&&(link.indexOf('_')>0)){
				srcLinks.remove(link);
				nomatchRef.add(link);
			}
		}//end for

		err.println("Source Models are: ");
		for (String s : srcLinks)
			err.println("\t" + s);
		err.println("");

		/*--get models connected to the target---------------------*/
		TreeSet<String> trgtLinks = new TreeSet<String>(agenda.getSourceNames());
		for(String link: agenda.getSourceNames()) {
		  if(!link.matches(modelPattern)){
		    err.println(link+" is not the appropriate type");
		    trgtLinks.remove(link);
		    for (String suffix : trgtSuff)
		      if (link.endsWith(suffix))
			continue;
		    err.println(link+" does not match suffix types");
		    trgtLinks.remove(link);
		    
		  }//end if
		}//end for
		err.println("Target Models are: ");
		for (String s : trgtLinks)
			err.println("\t" + s);
		err.println("");


		/*--Check for the models linked to both nodes---------------------*/

		TreeMap<String,String> actualLinks = new TreeMap<String,String>();
		for(String src: srcLinks){

			String check = src.replaceFirst(replacePattern, "");		 		 		 
			int end = check.indexOf('/');
			if(end<0)
				end = check.length();

			check = check.substring(0, end);
			err.println("Pattern is "+check);

			for(String trgt: trgtLinks){
				err.println("The matched string was \" " + (replacePattern+check+"/.*\"") );
				err.println("The trgt string was: " + trgt);
				if(trgt.matches(replacePattern+check+"/.*")) {
					actualLinks.put(src,trgt);
					nomatchRef.remove(src);
				} else {		 		 		 		 		 
					if(resp.equals(aRemove) && (!actualLinks.keySet().contains(src)))
						nomatchRef.add(src);
				}
			}
		}

		err.println("The common ones are:\n"+actualLinks+"\n");
		err.println("The ones to remove or skip are\n"+nomatchRef);		 		 

		/*--Write the sed file--------------*/
		File sedFile = createTemp(agenda, 0755, "sed");
		try {
			FileWriter out = new FileWriter(sedFile);

			out.write("#!/bin/sed -f\n\n");
			String trgtRef = null;
			for(String srcRef: actualLinks.keySet()){
				trgtRef = actualLinks.get(srcRef);
				out.write("s:"+srcRef+":"+trgtRef+":g\n");		 		 		 
			}//end for
			out.close();
		} catch(IOException ex) {
			throw new PipelineException
			("Unable to write temporary script file (" + sedFile + ") for Job " + 
					"(" + agenda.getJobID() + ")!\n" +
					ex.getMessage());
		}//end catch


		/*--Write the mel script----*/
		File saveMaya = null;
		File melScript = null;
		if(syflexCache)
			err.println("DARNIT");
		
		if(preMel!=null)
			err.println("Premel ugh");
		
		if(postMel!=null)
			err.println("Postmel ugh");
		
		if((preMel!=null) || (postMel != null) || (syflexCache) || ((!nomatchRef.isEmpty()) && (resp.equals(aRemove)))){
			err.println("This should work");
		}
		
		if((preMel!=null) || (postMel != null) || (syflexCache) || 
				((!nomatchRef.isEmpty()) && (resp.equals(aRemove)))){
			melScript = createTemp(agenda, 0755, "mel");
			saveMaya = createTemp(agenda, 0777, "ma");
			try {
				PrintWriter out = new PrintWriter(new BufferedWriter(new 
						FileWriter(melScript)));		     

				if(preMel!=null) {
					NodeID melID = new NodeID(agenda.getNodeID(),preMel);
					Path melPath = new Path(PackageInfo.sProdPath,
							melID.getWorkingParent() + "/" + agenda.getPrimarySource(preMel).getPath(0));
					out.println("source \""+melPath.toOsString()+"\";");
				}

				if(resp.equals(aRemove)){
					for(String asset : nomatchRef) {
						out.println("print (\"dereferencing file: $WORKING"+asset+".ma\\n\");");
						out.println("catchQuiet(`file -rr \"$WORKING"+asset+".ma\"`);");
					}//end for
				}

				out.println("string $refs[] = `file -q -r`;");
				out.println("string $ref;");
				out.println("for ($ref in $refs) {");
				out.println("		 string $nmSpc = `file -q -rfn $ref`;");
				out.println("		 file -lr $nmSpc $ref;");
				out.println("}");

				if(postMel!=null) {
					NodeID melID = new NodeID(agenda.getNodeID(),postMel);
					Path melPath = new Path(PackageInfo.sProdPath,
							melID.getWorkingParent() + "/" + agenda.getPrimarySource(postMel).getPath(0));		 		 		 
					out.println("source \""+melPath.toOsString()+"\";");
				}

				out.println("// SAVE");
				out.println("file -rn \""+saveMaya.getPath()+"\";");
				out.println("file -f -save -options \"v=0\" -type \"mayaAscii\";\n");
				
				if(syflexCache || shaveCache){
					err.println("Ohhhhh - we cache");
					
					out.println("if (!`pluginInfo -q -l EnvPathConvert`)");
					out.write("     loadPlugin \"EnvPathConvert\";\n\n");

					if(initialMel!=null)		 		 		 
						out.println("source \""+initialMel.toOsString()+"\";\n");
					
					if (syflexCache)
					{
					  for (String source : syflexSources)
					  {
					    //NodeID sNodeId = new NodeID(nodeID, source);
					    FileSeq fseq = agenda.getPrimarySource(source);
					    FrameRange range = fseq.getFrameRange();
					    int startFrame = range.getStart();
					    //int endFrame = range.getEnd();
					    Path p = new Path(source);
					    String cachePath = ("$WORKING" + p.getParent() + "/" + fseq.getFilePattern().getPrefix() + "." );
					    ActionInfo info = agenda.getSourceActionInfo(source);
					    String clothName = (String) info.getSingleParamValue("ClothObject");
					    out.println("{");
					    out.println("string $childs[] = `listRelatives -shapes "+ clothName +"`;");
					    out.println("string $child = $childs[0];");
					    out.println("setAttr ($child + \".startFrame\") "+(startFrame-1)+";");
					    out.println("string $envNode = `createNode envPathConvert`;");
					    out.println("setAttr -type \"string\" ($envNode + \".envPath\") \""+ cachePath+"\";");
					    out.println("connectAttr -f ($envNode + \".absPath\") ($child + \".cacheName\");");
					    out.println("catch(`setAttr ($child + \".active\") on`);");
					    out.println("}\n");   
					  }
					}
					if (shaveCache)
					{
					  out.println("string $envNode = `createNode envPathConvert`;");
					  String cacheString =  "$WORKING" + shaveCachePath.toString();
					  out.println("setAttr -type \"string\" ($envNode + \".envPath\") \""+ cacheString+"\";");
					  out.println("connectAttr -f ($envNode + \".absPath\") shaveGlobals.tmpDir;");

					  for(String shaveObject : shaveSources) {
					    String objAndAttr = shaveObject + ".rd";
					    out.println("if (`objExists " + shaveObject + "`)");
					    out.println("     catch(`setAttr " + objAndAttr + " 2`);");
					  }

					}
					
					if (cacheMel != null)
					  out.println("source \""+cacheMel.toOsString()+"\";");
					
					out.println("file -f -save;");

					if(finalMel!=null)
						out.println("source \""+finalMel.toOsString()+"\";");
				}
				out.close();
			}//end try
			catch(IOException ex) {
				throw new PipelineException("Unable to write the temporary MEL script(" 
						+ melScript + ") used add the references!");
			}//end catch

		}		 

		/*--Write the bash script----*/
		File script = createTemp(agenda, 0755, "bash");

		try {      
			FileWriter out = new FileWriter(script);
			out.write("#!/bin/bash\n\n");
			out.write("cat "+fileName+
					" | sed -f "+sedFile.getPath()+ " "+ fileName+
					" > "+ tempMaya.getPath() +"\n");
			if(syflexCache || (preMel!=null) || (postMel != null) || 
					(!nomatchRef.isEmpty() && (resp.equals(aRemove)))){
				File cleanMaya = createTemp(agenda, 0777, "ma");
				//NodeID nodeID = agenda.getNodeID();
				Path wdir = new Path(PackageInfo.sProdPath.toOsString());
				String workingArea = nodeID.getWorkingPath().toString().replaceAll(agenda.getNodeID().getName(),"");
				workingArea = wdir.toOsString()+workingArea;
				err.println("The working area is "+ workingArea);

				out.write("maya -batch -script "+melScript.getPath()+" ");
				out.write("-command \"file -lnr -open \\\""+tempMaya.getPath()+"\\\"\"\n");
				out.write("cat "+saveMaya.getPath()+" | sed \"s:"+ workingArea + ":\\$WORKING:g\" > "+cleanMaya.getPath()+"\n"); 
				out.write("cp "+cleanMaya.getPath()+" "+targetFileName);
			} else {
				out.write("cp "+tempMaya.getPath()+" "+targetFileName);
			}
			out.close();
		} catch(IOException ex) {
			throw new PipelineException
			("Unable to write temporary script file (" + script + ") for Job " + 
					"(" + agenda.getJobID() + ")!\n" +
					ex.getMessage());
		} //end catch

		err.close();

		try {
			return new SubProcessHeavy
			(agenda.getNodeID().getAuthor(), getName() + "-" + agenda.getJobID(), 
					script.getPath(), new ArrayList<String>(), 
					agenda.getEnvironment(), agenda.getWorkingDir(), 
					outFile, errFile);
		}//end try
		catch(Exception ex) {
			throw new PipelineException
			("Unable to generate the SubProcess to perform this Action!\n" 
					+ ex.getMessage());
		}//end catch   

	}//end prep

	/**
	 * Get the abstract path to the MEL file specified by the given parameter.
	 * 
	 * @param pname
	 *   The name of the single valued MEL parameter.
	 * 
	 * @param title
	 *   The title of the parameter in exception messages.
	 * 
	 * @param agenda
	 *   The agenda to be accomplished by the action.
	 * 
	 * @return 
	 *   The MEL file or <CODE>null</CODE> if none was specified.
	 */ 
	private Path getMelPath	(String pname, String title, ActionAgenda agenda)
	throws PipelineException 
	{
		Path script = null; 
		String mname = (String) getSingleParamValue(pname); 
		if(mname != null) {
			FileSeq fseq = agenda.getPrimarySource(mname);
			if(fseq == null) 
				throw new PipelineException
				("Somehow the " + title + " node (" + mname + ") was not one of the " + 
				"source nodes!");

			String suffix = fseq.getFilePattern().getSuffix();
			if(!fseq.isSingle() || (suffix == null) || !suffix.equals("mel")) 
				throw new PipelineException
				("The SyflexApplyCache Action requires that the source node specified by the " + 
						title + " parameter (" + mname + ") must have a single MEL file as its " + 
				"primary file sequence!");

			NodeID mnodeID = new NodeID(agenda.getNodeID(), mname);
			script = new Path(PackageInfo.sProdPath, 
					mnodeID.getWorkingParent() + "/" + fseq.getFile(0)); 
		}

		return script;		       
	}




}
