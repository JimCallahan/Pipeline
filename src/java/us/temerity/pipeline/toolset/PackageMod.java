// $Id: PackageMod.java,v 1.8 2005/01/22 06:10:10 jim Exp $

package us.temerity.pipeline.toolset;

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P A C K A G E   M O D                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A modifiable version of a toolset package. <P> 
 */
public
class PackageMod
  extends PackageCommon
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  PackageMod() 
  {
    super();
  }

  /**
   * Construct an empty modifiable package.
   * 
   * @param name 
   *   The name of the package
   */ 
  public
  PackageMod
  (
   String name
  ) 
  {
    super(name);
  }
  
  /** 
   * Construct a new modifiable version based another package.
   * 
   * @param com 
   *   The package to copy.
   */ 
  public 
  PackageMod
  (
   PackageCommon com 
  ) 
  {
    super(com);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Create anenvironmental variable entry with the given name. <P> 
   * 
   * The value of the created environmental variable will be <CODE>null</CODE> and the 
   * policy will be the default policy for the given variable name (see 
   * {@link MergePolicy#getDefaultPolicy getDefaultPolicy} for details).
   * 
   * @param name
   *   The name of the environmental variable.
   */ 
  public void 
  createEntry
  (
   String name
  ) 
  {
    setEntry(name, null, MergePolicy.getDefaultPolicy(name));
  }

  /**
   * Set the value and policy of the environmental variable entry with the given name. <P>
   * 
   * If no entry exists for the given name, a new entry will be created.
   * 
   * @param name
   *   The name of the environmental variable.
   * 
   * @param value
   *   The value of the environmental variable.
   * 
   * @param policy
   *   The package combine policy for this entry.
   */ 
  public void 
  setEntry
  (
   String name, 
   String value, 
   MergePolicy policy
  ) 
  {
    pEntries.put(name, new PackageEntry(name, value, policy));
  }

  /**
   * Set the value of an existing environmental variable entry with the given name.
   * 
   * @param name
   *   The name of the environmental variable.
   * 
   * @param value
   *   The value of the environmental variable.
   */ 
  public void 
  setValue
  (
   String name, 
   String value
  ) 
  {
    PackageEntry e = pEntries.get(name);
    if(e == null) 
      throw new IllegalArgumentException
	("No environmental variable entry exist with the name (" + name + ")!");

    pEntries.put(name, new PackageEntry(name, value, e.getMergePolicy()));
  }

  /**
   * Set the policy of an existing environmental variable entry with the given name.
   * 
   * @param name
   *   The name of the environmental variable.
   * 
   * @param policy
   *   The new package combine policy for this entry.
   */ 
  public void 
  setMergePolicy
  (
   String name, 
   MergePolicy policy
  ) 
  {
    PackageEntry e = pEntries.get(name);
    if(e == null) 
      throw new IllegalArgumentException
	("No environmental variable entry exist with the name (" + name + ")!");
    
    pEntries.put(name, new PackageEntry(name, e.getValue(), policy));
  }


  /**
   * Remove the environmental variable entry with the given name.
   * 
   * @param name
   *   The name of the environmental variable.
   */ 
  public void 
  removeEntry
  (
   String name
  ) 
  {
    pEntries.remove(name);
  }


  /**
   * Remove all of the environmental variable entries.
   */ 
  public void 
  removeAllEntries()
  {
    pEntries.clear();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O P S                                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Evaluate the given <B>bash</B>(1) shell script and use the results to set the 
   * environmental variable entries of the package. <P> 
   * 
   * The policies of the created variables will be set to the default policy for each 
   * variable name (see {@link MergePolicy#getDefaultPolicy getDefaultPolicy} for 
   * details).
   * 
   * @param script
   *   The <B>bash</B>(1) shell script to evaluate.
   * 
   * @throws PipelineException
   *   If unable to evaluate the shell script.
   */ 
  public void 
  loadShellScript
  (
   File script
  ) 
    throws PipelineException
  {
    String path = null;
    try {
      path = script.getCanonicalPath();
    }
    catch(IOException ex) {
      throw new PipelineException(ex);
    }

    /* evaluate the shell script and collect the output */ 
    String output[] = null;
    {
      ArrayList<String> args = new ArrayList<String>();
      args.add("--noprofile");
      args.add("-c");
      args.add("if source " + path + "; then /bin/env; else exit 1; fi");
      
      TreeMap<String,String> env = new TreeMap<String,String>();

      SubProcessLight proc = 
	new SubProcessLight("EvalPackage", PackageInfo.sBash, args, 
			    env, PackageInfo.sTempDir);
      proc.start();
    
      try {
	proc.join();
      }
      catch(InterruptedException ex) {
	LogMgr.getInstance().log
	  (LogMgr.Kind.Sub, LogMgr.Level.Severe,
	   ex.getMessage());
      }
    
      if(!proc.wasSuccessful()) 
	throw new PipelineException
	  ("Unable to evaluate the shell script (" + script + "):\n\n" + 
	   proc.getStdErr());

      output = proc.getStdOut().split("\\n");
    }
    
    /* parse the environmental variable output */ 
    {
      int wk;
      for(wk=0; wk<output.length; wk++) {
	int idx = output[wk].indexOf('=');
	if(idx == -1) 
	  throw new PipelineException 
	    ("Unable to parse line (" + wk + ") of the shell output (" + output[wk] + ")!");

	String name = output[wk].substring(0, idx);
	if(!name.equals("HOME") && 
	   !name.equals("PWD") && 
	   !name.equals("SHLVL") && 
	   !name.equals("USER") && 
	   !name.equals("WORKING") && 
	   !name.equals("_")) {

	  String value = null;
	  if(idx < (output[wk].length()-1)) 
	    value = output[wk].substring(idx+1);

	  setEntry(name, value, MergePolicy.getDefaultPolicy(name));
	}
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   C L O N E A B L E                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Return a deep copy of this object.
   */
  public Object 
  clone()
  {
    return new PackageMod(this);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 8739270879338857579L;

}
