package com.sony.scea.pipeline.plugins.v1_1_0;

import java.io.*;
import java.util.*;

import us.temerity.pipeline.*;

/**
 * Replaces the assets referenced into the selected source node with different 
 * models referenced into the target node. The replacement is done based on search 
 * suffixes for the source and target assets, e.g lr, syf, etc 
 *  <p>
 *   I'm not bothering to comment this one, since it's obsolute.  See the version 1.2.0 docs
 * for an explanation of how the Action now works.
 * 
 * @author Ifedayo O. Ojomo
 * @version 1.1.0
 */
public class ModelReplaceAction  
extends BaseAction {
	
	/*----------------------------------------------------------------------------------------*/
	/*   S T A T I C   I N T E R N A L S                                                      */
	/*----------------------------------------------------------------------------------------*/
	private static final long serialVersionUID = -837925606216866798L;	
	
	/*----------------------------------------------------------------------------------------*/
	/*   C O N S T R U C T O R                                                                */
	/*----------------------------------------------------------------------------------------*/

	public ModelReplaceAction() {
		super("ModelReplace", new VersionID("1.1.0"), "SCEA",
			"Gives the ability to get the sources of selected source node and reference " +
			"other versions of those sources such as cloth, hr models to a target node. " +
			"The user can choose the response should a source node not have any matching " +
			"nodes.");
	    {
	    	ActionParam param = new LinkActionParam
	    		("Source", "The source node which contains the files to replace.", null);
	    	addSingleParam(param);
	    }
	    {
	    	ActionParam param = new StringActionParam
	    		("SourceSuffix","The suffix of files to replace, e.g lr, syf.","lr");
	    	addSingleParam(param);
	    }
	    {
	    	ActionParam param = new StringActionParam
	    		("TargetSuffix","The suffix of files to be used as replacement","");
	    	addSingleParam(param);
	    }
	    {
	    	ArrayList<String> values = new ArrayList<String>();
	    	values.add("Ignore");
	    	values.add("Remove");
	    	ActionParam param = new EnumActionParam
	    		("Response","Action to take if a matching replacement cannot be found",
	    				"Ignore", values);
	    	addSingleParam(param);
	    }
	    //underDevelopment();

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
			
		File debug = createTemp(agenda, 0644, "err");
		PrintWriter err = null;
		try {
			err = new PrintWriter (new BufferedWriter (new FileWriter(debug)));
		} catch (IOException e) {
			throw new PipelineException(e);
		}
		
		FileSeq target = agenda.getPrimaryTarget();
	   	String modelPattern = ".*/assets/(character|set|prop)/.*";
	   	String sname = (String) getSingleParamValue("Source");
	   	String srcSuff = (String) getSingleParamValue("SourceSuffix");
	   	String trgtSuff = (String) getSingleParamValue("TargetSuffix");
	   	String resp = (String) ((EnumActionParam) getSingleParam("Response")).getValue();
	   	
	   	err.println("Source suffix is: "+srcSuff);
	   	err.println("Target suffix is: "+trgtSuff);
	   	
	   	
	   	if(srcSuff.equals(trgtSuff))		
	   		throw new PipelineException("The source suffix is identical to the target suffix");
	    	
	    /*--The source fileName--*/
	   	NodeID snodeID = new NodeID(agenda.getNodeID(),sname);
		Path nodePath = new Path(PackageInfo.sProdPath,
				snodeID.getWorkingParent() + "/" + agenda.getPrimarySource(sname).getPath(0));
		String fileName = nodePath.toOsString();
		
		
		/*---the target fileName---*/
		Path trgtNode = new Path(PackageInfo.sProdPath, 
				agenda.getNodeID().getWorkingParent() + "/" + target.getPath(0));
		String targetFileName = trgtNode.toOsString();

	    		
    	if(sname == null) 
    		throw new PipelineException ("The Source node was not set!");
		
    	if(!agenda.getSourceNames().contains(sname))
    		throw new PipelineException 
    			("The Source node (" + sname + ") is not linked to the target node " + 
    			"(" + target + ")!");

    	/*--get Lo-Res model names connected to the source----------------*/
		ActionInfo lrInfo = agenda.getSourceActionInfo(sname);
		TreeSet<String> srcLinks = new TreeSet<String>(lrInfo.getSourceNames());
		for (String s : srcLinks)
			err.println("\t" + s);
		err.println("The original sources are "+lrInfo.getSourceNames().toString());
		
		for(String link : lrInfo.getSourceNames()){
			if(!(link.endsWith(srcSuff) && link.matches(modelPattern))){
				err.println(link+" is not the appropriate type: "+srcSuff);
				srcLinks.remove(link);
			}//end if
			if((srcSuff.length()==0)&&(link.indexOf('_')>0))
				srcLinks.remove(link);
				
		}//end for
		err.println("Source models are:\n"+srcLinks);
		
			
		/*--get Hi-Res models connected to the target---------------------*/
		TreeSet<String> trgtLinks = new TreeSet<String>(agenda.getSourceNames());
		
		for(String link: agenda.getSourceNames()) {
			if(!(link.endsWith(trgtSuff) && link.matches(modelPattern))){
				err.println(link+" is not the appropriate type");
				trgtLinks.remove(link);
			}//end if
		}//end for
		err.println("Target models are:\n"+trgtLinks);
			
			
		/*--Check for the models linked to both nodes---------------------*/
		TreeSet<String> nomatchRef = new TreeSet<String>();
		
		TreeMap<String,String> actualLinks = new TreeMap<String,String>();
		for(String src: srcLinks){
			int end = src.indexOf('_');
			if(end<0)
				end = src.length();
			String check = src.substring(0, end);
			err.println("Pattern is "+check);
			
			if(trgtSuff.length()>0)
				check+="_";
			
			if(trgtLinks.contains(check+trgtSuff)){
				actualLinks.put(src, check+trgtSuff);
			} else {
				if(resp.equals("Remove"))
					nomatchRef.add(src);
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
			
		
		err.close();
		
		/*--Write the mel script----*/
		File melScript = null;
		if(!nomatchRef.isEmpty()){
			try {
				melScript =
					File.createTempFile("ModelReplaceAction.", ".mel", 
							PackageInfo.sTempPath.toFile());
				FileCleaner.add(melScript);
			}//end try
			catch(IOException ex) {
				throw new PipelineException(
						"Unable to create the temporary MEL script used to collect "
						+ "texture information from the Maya scene!");
			}//end catch

			try {
				PrintWriter out = new PrintWriter(new BufferedWriter(new 
						FileWriter(melScript)));	    

				for(String asset : nomatchRef) {

					out.println("print (\"dereferencing file: $WORKING" + asset + 
							".ma\");");
					out.println("file -rr \"$WORKING"+asset+".ma\";");
				}//end for

				out.println("// SAVE");
				out.println("file -save;");

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
	    		" > "+ targetFileName +"\n");
		    if(!nomatchRef.isEmpty()){
		    	out.write("maya -batch -script "+melScript.getPath()+" ");
		    	out.write("-command \"file -lnr -open \\\""+targetFileName+"\\\"\"\n");
		    }
		    out.close();
		} catch(IOException ex) {
			throw new PipelineException
	  		("Unable to write temporary script file (" + script + ") for Job " + 
	  		"(" + agenda.getJobID() + ")!\n" +
	  		ex.getMessage());
		} //end catch
			
    
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
}//end class ModelReplaceAction
