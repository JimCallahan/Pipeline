// $Id: Toolsets.java,v 1.1 2004/03/22 03:12:34 jim Exp $
  
package us.temerity.pipeline.core;

import us.temerity.pipeline.*; 

import java.io.*; 
import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T O O L S E T S                                                                        */
/*------------------------------------------------------------------------------------------*/

/**
 * A set of static methods for looking up Toolset environments. <P>
 * 
 * A Toolset is a named set of environmental variables under which a set of interoperable 
 * software tools can be reliably run.  Toolsets are used by Pipeline to execute node
 * Actions and Editors in a consistent manner. <P>
 * 
 * Toolsets are stored on disk as simple shell scripts which contain environmental variable 
 * definitions.  These files are created by evaluating a set of package configuration files
 * for the set of tools which make of the Toolset and then saving the generated 
 * environment. <P>
 * 
 * This class provides a {@link #lookup lookup} method which parses a cooked Toolset shell 
 * script and returns a runtime representation of the Toolset environment.
 */
public
class Toolsets
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  private 
  Toolsets() 
  {}

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Does the given toolset exist?
   */
  public static boolean 
  exists
  (
   String name
  ) 
  {
    synchronized(sEnvironments) {
      if(sEnvironments.containsKey(name)) 
	return true;

      try {
	parseToolset(name);
	return true;
      }
      catch (PipelineException ex) {
	return false;
      }
    }
  }


  
  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Lookup the environmental variable name/value pairs which make up the given
   * named Toolset. <P> 
   *
   * The <CODE>user</CODE> and <CODE>view</CODE> arguments are used to properly initalize
   * the values of the environmental variables: USER, HOME, WORKING.  In order for Pipeline
   * to properly execute node Actions as another user, it is necessary to generate values 
   * for these environmental variables at runtime which override the constant values stored 
   * in the Toolset shell script.
   * 
   * @param name  [<B>in</B>]
   *   The name of the toolset.
   * 
   * @param user  [<B>in</B>]
   *   The name of the user under which this environment will be used.
   * 
   * @param view  [<B>in</B>]
   *   The name of user working area view.
   * 
   * @return 
   *   The toolset environment as a table of name/value pairs.
   * 
   * @throws PipelineException
   *   If unable to find and read a toolset named <CODE>name</CODE>.
   */ 
  public static SortedMap<String,String> 
  lookup
  (
   String name, 
   String user,
   String view
  ) 
    throws PipelineException
  {
    if(name == null) 
      throw new IllegalArgumentException("The name cannot be (null)!");

    if(user == null) 
      throw new IllegalArgumentException("The user cannot be (null)!");

    if(view == null) 
      throw new IllegalArgumentException("The view cannot be (null)!");
    
    TreeMap<String,String> env = null;
    synchronized(sEnvironments) {
      if(!sEnvironments.containsKey(name)) 
	parseToolset(name);
      
      env = new TreeMap<String,String>(sEnvironments.get(name));
    }

    /* set the runtime variables */ 
    env.put("USER", user);
    env.put("HOME", PackageInfo.sHomeDir + "/" + user);
    env.put("WORKING", PackageInfo.sWorkDir + "/" + user + "/" + view);

    return env;
  }


  /**
   * Lookup the environmental variable name/value pairs which make up the given
   * named Toolset for the current user and default working area view. <P> 
   * 
   * @param name  [<B>in</B>]
   *   The name of the toolset.
   * 
   * @return 
   *   The toolset environment as a table of name/value pairs.
   * 
   * @throws PipelineException
   *   If unable to find and read a toolset named <CODE>name</CODE>.
   */ 
  public static SortedMap<String,String> 
  lookup
  (
   String name
  ) 
    throws PipelineException
  {
    return lookup(name, System.getProperty("user.name"), "default");    
  }



  /*----------------------------------------------------------------------------------------*/
  /*   H E L P E R S                                                                        */
  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Parse the shell script containing the environmental variable name/value pairs 
   * which make up the given named Toolset and cache the results.
   */
  private static void 
  parseToolset
  (
   String name
  ) 
    throws PipelineException
  {
    FileReader in = null;
    try {
      File file = new File(PackageInfo.sToolsetDir, name);
      in = new FileReader(file);   
      
      ToolsetEnvParser parser = new ToolsetEnvParser(in);
      TreeMap env = parser.Env();
      
      TreeMap<String,String> cenv = new TreeMap<String,String>();
      for(Object key : env.keySet()) 
	cenv.put((String) key, (String) env.get(key));      
      
      cenv.remove("USER");
      cenv.remove("HOME");
      cenv.remove("WORKING");
      
      synchronized(sEnvironments) {
	sEnvironments.put(name, cenv);
      }
    }
    catch (FileNotFoundException ex) {
      throw new PipelineException
	("Unable to find any toolset named: " + name);
    }
    catch(ParseException ex) {
      throw new PipelineException
	("Unable to parse the environment in toolset: " + name + "\n\n" +
	 ex.getMessage());
    }
    finally {
      if(in != null) {
	try {
	  in.close();  
	}
	catch(IOException ex) {
	  throw new PipelineException(ex);
	}
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * The table previously loaded toolset environments.
   */
  private static TreeMap<String,TreeMap<String,String>>  sEnvironments = 
    new TreeMap<String,TreeMap<String,String>>();
  
}
