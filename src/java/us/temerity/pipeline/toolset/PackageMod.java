// $Id: PackageMod.java,v 1.1 2004/05/21 21:21:31 jim Exp $

package us.temerity.pipeline.toolset;

import us.temerity.pipeline.*;
import us.temerity.pipeline.core.*;

import java.io.*;
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   P A C K A G E   M O D                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A modifiable version of a package. <P> 
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
   * Construct a new modifiable version based on a read-only version of the package.
   * 
   * @param vsn 
   *   The read-only version of the package.
   */ 
  public 
  PackageMod
  (
   PackageVersion vsn
  ) 
  {
    super(vsn);
  }

  /** 
   * Copy constructor. 
   * 
   * @param vsn 
   *   The <CODE>PackageMod</CODE> to copy.
   */ 
  public 
  PackageMod
  (
   PackageMod vsn
  ) 
  {
    super(vsn);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Evaluate the given <B>bash</B>(1) shell script and use the results to initialize the 
   * environmental variable entries of the package.
   * 
   * @param script
   *   The <B>bash</B>(1) shell script to evaluate.
   * 
   * @throws PipelineException
   *   If unable to evaluate the shell script.
   */ 
  public void 
  init
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
      args.add("--ignore-environment");
      args.add(PackageInfo.sBash);
      args.add("--noprofile");
      args.add("-c");
      args.add("source " + path + "; /bin/env");
      
      SubProcess proc = 
	new SubProcess("EvalPackage", new File("/bin/env"), args);
      proc.start();
    
      try {
	proc.join();
      }
      catch(InterruptedException ex) {
	Logs.sub.severe(ex.getMessage());
      }
    
      if(!proc.wasSuccessful()) 
	throw new PipelineException
	  ("Unable to evaluate the shell script (" + script + "):\n\n" + 
	   proc.getStdErr());

      output = proc.getStdOutLines(0);
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

	  Policy policy = Policy.Exclusive;
	  if(name.equals("PATH") || 
	     name.equals("LD_LIBRARY_PATH") || 
	     name.equals("MANPATH") || 
	     name.equals("INFOPATH")) 
	    policy = Policy.PathAppend;

	  pEntries.put(name, new Entry(name, value, policy));
	}
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Set the value and policy of the environmental variable entry with the given name.
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
   Policy policy
  ) 
  {
    pEntries.put(name, new Entry(name, value, policy));
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
  setPolicy
  (
   String name, 
   Policy policy
  ) 
  {
    Entry e = pEntries.get(name);
    if(e == null) 
      throw new IllegalArgumentException
	("No environmental variable entry exist with the name (" + name + ")!");
    
    pEntries.put(name, new Entry(name, e.getValue(), policy));
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

  //private static final long serialVersionUID = 4473685551529032568L;

}
