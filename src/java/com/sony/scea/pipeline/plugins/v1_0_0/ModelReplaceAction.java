package com.sony.scea.pipeline.plugins.v1_0_0;

import java.io.*;
import java.util.*;
import us.temerity.pipeline.*;

/**
 * Replaces the lo-res models linked to the selected source node to hi-res ones
 * if those hi-res models are also linked to the target node.
 * <p>
 * I'm not bothering to comment this one, since it's obsolute.  See the version 1.2.0 docs
 * for an explanation of how the Action now works.
 * 
 * @author Ifedayo O. Ojomo
 * @version 1.0.0
 */
public class ModelReplaceAction 
extends BaseAction {
	
	/*----------------------------------------------------------------------------------------*/
	/*   S T A T I C   I N T E R N A L S                                                      */
	/*----------------------------------------------------------------------------------------*/
	private static final long serialVersionUID = -1452064964040176587L;
	
	
	/*----------------------------------------------------------------------------------------*/
	/*   C O N S T R U C T O R                                                                */
	/*----------------------------------------------------------------------------------------*/

	public ModelReplaceAction() {
		super("ModelReplace", new VersionID("1.0.0"), "SCEA",
			"Replaces the lo-res models linked to the selected source node to hi-res ones " +
			"if those hi-res models are also linked to the target node.");
	    {
	    	ActionParam param = new LinkActionParam
	    		("Source", "The source node which contains the files to replace.", null);
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
			
		File debug = createTemp(agenda, 0644, "temp");
		PrintWriter err = null;
		try {
			err = new PrintWriter (new BufferedWriter (new FileWriter(debug)));
		} catch (IOException e) {
			throw new PipelineException(e);
		}
		
		FileSeq target = agenda.getPrimaryTarget();

	   	String modelPattern = ".*/assets/(character|set|prop)/.*";
	   	String sname = (String) getSingleParamValue("Source");
	    	
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
		
		for(String link : lrInfo.getSourceNames()){
			if(!(link.endsWith("_lr") && link.matches(modelPattern))){
				System.err.println(link+" is not the appropriate type");
				srcLinks.remove(link);
			}//end if
		}//end for
		err.println("Lo-res models are:\n"+srcLinks);
		
			
		/*--get Hi-Res models connected to the target---------------------*/
		TreeSet<String> trgtLinks = new TreeSet<String>(agenda.getSourceNames());
		
		for(String link: agenda.getSourceNames()) {
			if(!link.matches(modelPattern)){
				trgtLinks.remove(link);
			}//end if
		}//end for
		
		err.println("Hi-res models are:\n"+trgtLinks);
			
			
		/*--Check for the models linked to both nodes---------------------*/
		TreeMap<String,String> actualLinks = new TreeMap<String,String>();
		for(String hrLink: trgtLinks){
			if(srcLinks.contains(hrLink+"_lr")){
				actualLinks.put(hrLink+"_lr", hrLink);
			}
		}
		System.err.println("The common ones are:\n"+actualLinks);
			
			
		/*--Write the sed file--------------*/
		File sedFile = createTemp(agenda, 0755, "sed");
		try {
			FileWriter out = new FileWriter(sedFile);
			
			out.write("#!/bin/sed -f\n\n");
			String hiRes = null;
			for(String loRes: actualLinks.keySet()){
				hiRes = actualLinks.get(loRes);
				out.write("s:"+loRes+":"+hiRes+":g\n");			
			}//end for
			out.close();
		} catch(IOException ex) {
			throw new PipelineException
		  		("Unable to write temporary script file (" + sedFile + ") for Job " + 
		  		"(" + agenda.getJobID() + ")!\n" +
		  		ex.getMessage());
		}//end catch
			
		
		err.close();
			
		/*--Write the bash script----*/
		File script = createTemp(agenda, 0755, "bash");
		
		try {      
		    FileWriter out = new FileWriter(script);
		    out.write("#!/bin/bash\n\n");
		    out.write("cat "+fileName+
	    		" | sed -f "+sedFile.getPath()+ " "+ fileName+
	    		" > "+ targetFileName +"\n");
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
