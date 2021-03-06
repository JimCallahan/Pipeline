// $Id: PackageCommon.java,v 1.12 2007/06/19 20:16:06 jim Exp $

package us.temerity.pipeline.toolset;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   P A C K A G E   C O M M O N                                                            */
/*------------------------------------------------------------------------------------------*/

/**
 * The superclass of <CODE>PackageVersion</CODE> and <CODE>PackageMode</CODE> which provides 
 * the common fields and methods needed by both classes. <P>
 */
public
class PackageCommon
  extends Named
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  protected 
  PackageCommon() 
  {
    pEntries = new TreeMap<String,PackageEntry>();
  }

  /**
   * Internal constructor used by subclasses to create an empty package.
   * 
   * @param name 
   *   The name of the package.
   */ 
  protected
  PackageCommon
  (
   String name 
  ) 
  {
    super(name);
    pEntries = new TreeMap<String,PackageEntry>();
  }
  
  /** 
   * Internal copy constructor used by both <CODE>PackageMod</CODE> and 
   * <CODE>PackageVersion</CODE> when constructing instances based off an instance of 
   * the other subclass.
   */
  protected 
  PackageCommon
  (
   PackageCommon com
  ) 
  {
    super(com.getName());
    pEntries = (TreeMap<String,PackageEntry>) com.pEntries.clone();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   P R E D I C A T E S                                                                  */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Whether this package has no environmental variable definitions.
   */ 
  public boolean
  isEmpty()
  {
    return pEntries.isEmpty();
  }
  

  /** 
   * Whether the given package has the same name as this package.
   * 
   * @param com 
   *   The package
   */
  public boolean
  similarTo
  (
   PackageCommon com
  )
  {
    if(com != null) 
      return (pName.equals(com.getName()));

    return false;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the environmental variables defined for this package.
   */ 
  public Set<String>
  getEnvNames() 
  {
    return Collections.unmodifiableSet(pEntries.keySet());
  }

  /**
   * Get the value of the environmental variable with the given name.
   * 
   * @return 
   *   The value or <CODE>null</CODE> if the variable is undefined or has no value.
   */ 
  public String
  getEnvValue
  (
   String name
  ) 
  {
    PackageEntry e = pEntries.get(name);
    if(e != null)
      return e.getValue();
    return null;
  }
  
  /**
   * Get the package combine policy for the environmental variable with the given name.
   * 
   * @return 
   *   The policy or <CODE>null</CODE> if the variable is undefined.
   */ 
  public MergePolicy
  getMergePolicy
  (
   String name
  ) 
  {
    PackageEntry e = pEntries.get(name);
    if(e != null)
      return e.getMergePolicy();
    return null;
  }

  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the package environment.
   */ 
  public TreeMap<String,String>
  getEnvironment()
  {
    TreeMap<String,String> env = new TreeMap<String,String>();
    
    for(String name : pEntries.keySet())
      env.put(name, pEntries.get(name).getValue());

    return env;
  }

  /**
   * Get the package environment specific to the given user.
   * 
   * @param author
   *   The user owning the generated environment.
   */ 
  public TreeMap<String,String>
  getEnvironment
  (
   String author
  )
  {
    if(author == null) 
      throw new IllegalArgumentException("The author cannot be (null)!");

    TreeMap<String,String> env = getEnvironment();

    switch(PackageInfo.sOsType) {
    case Unix:
    case MacOS:
      {
        env.put("USER", author);

        Path home = new Path(PackageInfo.sHomePath, author);
        env.put("HOME", home.toOsString()); 
      }
      break;

    case Windows:
      {
	env.put("USERNAME", author);

        Path profile = PackageInfo.getUserProfilePath(author);
	if(profile != null) 
          env.put("USERPROFILE", profile.toOsString());

        Path appdata = PackageInfo.getAppDataPath(author);
        if(appdata != null) 
          env.put("APPDATA", appdata.toOsString());
      }
    }

    env.put("PIPELINE_OSTYPE", PackageInfo.sOsType.toString());

    return env;
  }

  /**
   * Get the package environment specific to the given user and working area.
   * 
   * @param author
   *   The user owning the generated environment.
   * 
   * @param view 
   *   The name of the user's working area view. 
   */ 
  public TreeMap<String,String>
  getEnvironment
  (
   String author, 
   String view
  )
  {
    if(author == null) 
      throw new IllegalArgumentException("The author cannot be (null)!");

    if(view == null) 
      throw new IllegalArgumentException("The view cannot be (null)!");

    TreeMap<String,String> env = getEnvironment(author);

    Path working = new Path(PackageInfo.sWorkPath, author + "/" + view);
    env.put("WORKING", working.toOsString());

    return env;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   O B J E C T   O V E R R I D E S                                                      */
  /*----------------------------------------------------------------------------------------*/

  /** 
   * Indicates whether some other object is "equal to" this one.
   * 
   * @param obj 
   *   The reference object with which to compare.
   */
  public boolean
  equals
  (
   Object obj   
  )
  {
    if((obj != null) && (obj instanceof PackageCommon)) {
      PackageCommon com = (PackageCommon) obj;
      return pEntries.equals(com.pEntries);
    }
    return false;
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
    return super.clone();
  }



  /*----------------------------------------------------------------------------------------*/
  /*   G L U E A B L E                                                                      */
  /*----------------------------------------------------------------------------------------*/
  
  public void 
  toGlue
  ( 
   GlueEncoder encoder  
  ) 
    throws GlueException
  {
    super.toGlue(encoder);

    if(!pEntries.isEmpty()) 
      encoder.encode("Entries", pEntries);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    TreeMap<String,PackageEntry> entries = 
      (TreeMap<String,PackageEntry>) decoder.decode("Entries"); 
    if(entries != null) 
      pEntries = entries;
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = -6890472432252789088L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The table of environmental variable entries.
   */
  protected TreeMap<String,PackageEntry>  pEntries; 

}
