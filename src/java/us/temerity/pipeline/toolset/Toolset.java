// $Id: Toolset.java,v 1.10 2007/03/30 23:19:39 jim Exp $

package us.temerity.pipeline.toolset;

import us.temerity.pipeline.*;
import us.temerity.pipeline.glue.*;

import java.util.*;

/*------------------------------------------------------------------------------------------*/
/*   T O O L S E T                                                                          */
/*------------------------------------------------------------------------------------------*/

/**
 * A named shell environment constructed by evaluating a set of toolset packages.
 */
public
class Toolset
  extends Named
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  public 
  Toolset()
  {}

  /**
   * Construct a toolset composed of the given packages.
   * 
   * @param author
   *   The name of the user creating the toolset.
   * 
   * @param name
   *   The name of the toolset.
   * 
   * @param packages
   *   The packages in order of evaluation.
   * 
   * @param desc
   *   The package description.
   * 
   * @param os
   *   The operating system type.
   */ 
  public
  Toolset
  (
   String author, 
   String name, 
   Collection<PackageCommon> packages,
   String desc, 
   OsType os
  ) 
  {
    super(name);

    if(desc == null) 
      throw new IllegalArgumentException("The package description cannot be (null)!");
    pMessage = new SimpleLogMessage(author, desc);

    init(packages, os);
  }

  /**
   * Construct a working toolset composed of the given packages.
   * 
   * @param name
   *   The name of the toolset.
   * 
   * @param packages
   *   The packages in order of evaluation.
   * 
   * @param os
   *   The operating system type.
   */ 
  public
  Toolset
  (
   String name, 
   Collection<PackageCommon> packages, 
   OsType os
  ) 
  {
    super(name);
    init(packages, os);
  }

  /**
   * Construct an empty working toolset.
   * 
   * @param name
   *   The name of the toolset.
   * 
   * @param os
   *   The operating system type.
   */ 
  public
  Toolset
  (
   String name, 
   OsType os
  ) 
  {
    super(name);
    init(new ArrayList<PackageCommon>(), os);
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Construct a toolset composed of the given packages.
   * 
   * @param packages
   *   The packages in order of evaluation.
   * 
   * @param os
   *   The operating system type.
   */ 
  private void 
  init
  (
   Collection<PackageCommon> packages, 
   OsType os
  ) 
  {
    pPackages       = new ArrayList<String>();
    pPackageHistory = new TreeMap<String,LinkedList<Integer>>();
    pVersionIDs     = new ArrayList<VersionID>();  
    pEnvironment    = new TreeMap<String,String>();

    pHasModifiable = false;
    pConflicts     = new TreeMap<String,LinkedList<Integer>>();
    pAnyConflicts  = new TreeSet<Integer>();
    
    String pathSep = PackageInfo.getPathSep(os); 

    int idx = 0;
    for(PackageCommon com : packages) {
      pPackages.add(com.getName());

      if(com instanceof PackageVersion) {
	PackageVersion vsn = (PackageVersion) com;
	pVersionIDs.add(vsn.getVersionID());
      }
      else {
	pHasModifiable = true;
	pVersionIDs.add(null);
      }

      for(String key : com.getEnvNames()) {
	{
	  LinkedList<Integer> pkgs = pPackageHistory.get(key);
	  if(pkgs == null) {
	    pkgs = new LinkedList<Integer>();
	    pPackageHistory.put(key, pkgs);
	  }
	  pkgs.add(idx);
	}

	String value = com.getEnvValue(key);
	if(pEnvironment.containsKey(key)) {
	  String prev = pEnvironment.get(key);
	  switch(com.getMergePolicy(key)) {
	  case Exclusive:
	    {
	      LinkedList<Integer> pkgs = pConflicts.get(key);
	      if(pkgs == null) {
		pkgs = new LinkedList<Integer>();
		pConflicts.put(key, pkgs);
	      }
	      pkgs.add(idx);

	      pAnyConflicts.add(idx);
	    }
	    break;
	    
	  case Override:
	    pEnvironment.put(key, value);
	    break;

	  case Ignore:
	    break;
	    
	  case AppendPath:
	  case PrependPath:
	    if(prev != null) {
	      if(value != null) {
		String first = null;
		String second = null;
		switch(com.getMergePolicy(key)) {
		case AppendPath:
		  first  = prev;
		  second = value;
		  break;
		  
		case PrependPath:
		  first  = value;
		  second = prev;
		}
		
		ArrayList<String> paths = new ArrayList<String>();
		{
		  String dirs[] = first.split(pathSep);  
		  int wk;
		  for(wk=0; wk<dirs.length; wk++) 
		    if((dirs[wk].length() > 0) && !paths.contains(dirs[wk]))
		      paths.add(dirs[wk]);
		}
		
		{
		  String dirs[] = second.split(pathSep);
		  int wk;
		  for(wk=0; wk<dirs.length; wk++) 
		    if((dirs[wk].length() > 0) && !paths.contains(dirs[wk]))
		      paths.add(dirs[wk]);
		}
		
		String nvalue = null;
		{
		  StringBuilder buf = new StringBuilder();
		  for(String path : paths) 
		  buf.append(path + pathSep);
		  String str = buf.toString();
		  
		  if(paths.size() > 1) 
		    nvalue = str.substring(0, str.length()-1);
		  else 
		    nvalue = str;
		}
		
		pEnvironment.put(key, nvalue);
	      }
	      else {
		pEnvironment.put(key, prev);
	      }
	    }
	    else {
	      pEnvironment.put(key, value);
	    }
	  }
	}
	else {
	  pEnvironment.put(key, value);
	}
      }

      idx++;
    }
  }
  


  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * Was the toolset built with any modifiable packages.
   */ 
  public boolean 
  hasModifiablePackages()
  {
    return pHasModifiable;
  }

  /**
   * Is the current toolset ready to be frozen?
   */ 
  public boolean 
  isFreezable() 
  {
    return (hasPackages() && !pHasModifiable && !hasConflicts());
  }

  /**
   * Is the current toolset already frozen?
   */ 
  public boolean 
  isFrozen() 
  {
    return (isFreezable() && (pMessage != null));
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Does this toolset have any environmental variable package conflicts?
   */ 
  public boolean 
  hasConflicts() 
  {
    return (!pAnyConflicts.isEmpty());
  }

  
  /**
   * Does the given environmental variable have any package conflicts?
   * 
   * @param name
   *   The environmental variable name.
   */ 
  public boolean 
  isEnvConflicted
  (
   String name
  ) 
  {
    return pConflicts.containsKey(name);
  }
  
  /**
   * Get the names of the environmental variables which have package conflicts.
   */ 
  public TreeSet<String>
  getConflictedEnvNames() 
  {
    return new TreeSet<String>(pConflicts.keySet());
  }


  /**
   * Does the package for the given index have any environmental variable conflicts?
   * 
   * @param idx
   *   The package index.
   */ 
  public boolean 
  isPackageConflicted
  (
   int idx
  ) 
  {
    return pAnyConflicts.contains(idx);
  }

  /**
   * Does the package for the given index have a conflict for the given environmental 
   * variable name?
   * 
   * @param idx
   *   The package index.
   * 
   * @param name
   *   The environmental variable name.
   */ 
  public boolean 
  isPackageEnvConflicted
  (
   int idx, 
   String name
  )
  {
    return (pConflicts.containsKey(name) && pConflicts.get(name).contains(idx));
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the timestamp (milliseconds since midnight, January 1, 1970 UTC) of when the 
   * toolset was created.
   */ 
  public Long
  getTimeStamp() 
  {
    if(pMessage != null) 
      return pMessage.getTimeStamp();
    return null;
  }

  /**
   * Get the name of the user who created the toolset.
   */ 
  public String
  getAuthor() 
  {
    if(pMessage != null) 
      return pMessage.getAuthor();
    return null;
  }

  /**
   * Get the toolset description.
   */ 
  public String
  getDescription() 
  {
    if(pMessage != null) 
      return pMessage.getMessage();
    return null;
  }


  /*----------------------------------------------------------------------------------------*/

  /** 
   * Does this toolset have any packages?
   */
  public boolean 
  hasPackages() 
  {
    return (!pPackages.isEmpty());
  }

  /**
   * Does the toolset contain a modifiable package with the given name.
   */ 
  public boolean 
  hasModifiablePackage
  (
   String name
  ) 
  {
    int wk;
    for(wk=0; wk<pPackages.size(); wk++) {
      if(pPackages.get(wk).equals(name) && (pVersionIDs.get(wk) == null))
	return true;
    }

    return false;
  }

  /** 
   * Get the number of packages.
   */ 
  public int
  getNumPackages() 
  {
    return (pPackages.size());
  }

  /**
   * Get the name of the package with the given index.
   */ 
  public String
  getPackageName
  (
   int idx
  ) 
  {
    return (pPackages.get(idx));
  }

  /**
   * Get revision number of the package with the given index. <P> 
   * 
   * @return
   *   The revision number or <CODE>null</CODE> if the package is modifiable.
   */ 
  public VersionID
  getPackageVersionID
  (
   int idx
  ) 
  {
    return (pVersionIDs.get(idx));
  }


  /**
   * Get the indices of the packages which provide a value for the given environmental
   * variable name.
   * 
   * @param ename
   *   The environmental variable name.
   */ 
  public LinkedList<Integer> 
  getPackageHistory
  (
   String ename
  ) 
  {
    LinkedList<Integer> indices = new LinkedList<Integer>();
    if(pPackageHistory.containsKey(ename))
      indices.addAll(pPackageHistory.get(ename));
    return indices;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the cooked toolset environment.
   */ 
  public TreeMap<String,String>
  getEnvironment()
  {
    return new TreeMap<String,String>(pEnvironment);
  }
  
  /**
   * Get the cooked toolset environment specialized for a specific user and 
   * operating system.
   * 
   * @param author
   *   The user owning the generated environment.
   * 
   * @param os
   *   The operating system type.
   */ 
  public TreeMap<String,String>
  getEnvironment
  (
   String author, 
   OsType os
  )
  {
    if(author == null) 
      throw new IllegalArgumentException("The author cannot be (null)!");

    TreeMap<String,String> env = getEnvironment();
    Path home = new Path(PackageInfo.getHomePath(os), author);

    switch(os) {
    case Unix:
    case MacOS:
      env.put("USER", author);
      env.put("HOME", home.toOsString(os)); 
      break;

    case Windows:
      {
	env.put("USERNAME", author);
	env.put("USERPROFILE", home.toOsString(os));
	env.put("HOMEPATH", home.toOsString(os));

	Path appdata = new Path(home, "Application Data");
	env.put("APPDATA", appdata.toOsString(os));
      }
    }

    env.put("PIPELINE_OSTYPE", os.toString());

    return env;
  }

  /**
   * Get the cooked toolset environment specialized for a specific user, working area and
   * operating system.
   * 
   * @param author
   *   The user owning the generated environment.
   * 
   * @param view 
   *   The name of the user's working area view. 
   * 
   * @param os
   *   The operating system type.
   */ 
  public TreeMap<String,String>
  getEnvironment
  (
   String author, 
   String view, 
   OsType os
  )
  {
    if(author == null) 
      throw new IllegalArgumentException("The author cannot be (null)!");

    if(view == null) 
      throw new IllegalArgumentException("The view cannot be (null)!");

    TreeMap<String,String> env = getEnvironment(author, os);

    Path working = new Path(PackageInfo.getWorkPath(os), author + "/" + view);
    env.put("WORKING", working.toOsString(os));

    return env;
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
    if(!isFrozen()) 
      throw new GlueException
	("Only frozen Toolsets can be encoded as Glue!");

    super.toGlue(encoder);

    encoder.encode("Packages",       pPackages);
    encoder.encode("PackageHistory", pPackageHistory);
    encoder.encode("Versions",       pVersionIDs);
    encoder.encode("Message",        pMessage);
    encoder.encode("Environment",    pEnvironment);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    ArrayList<String> packages = (ArrayList<String>) decoder.decode("Packages");
    if(packages == null) 
      throw new GlueException("The \"Packages\" was missing or (null)!");
    pPackages = packages;

    TreeMap<String,LinkedList<Integer>> hist = 
      (TreeMap<String,LinkedList<Integer>>) decoder.decode("PackageHistory");
    if(hist == null) 
      throw new GlueException("The \"PackageHistory\" was missing or (null)!");
    pPackageHistory = hist;

    ArrayList<VersionID> versions = (ArrayList<VersionID>) decoder.decode("Versions");
    if(versions == null) 
      throw new GlueException("The \"Versions\" was missing or (null)!");
    pVersionIDs = versions;

    SimpleLogMessage msg = (SimpleLogMessage) decoder.decode("Message");
    if(msg == null) 
      throw new GlueException("The \"Message\" was missing!");
    pMessage = msg;    

    TreeMap<String,String> env = (TreeMap<String,String>) decoder.decode("Environment");
    if(env == null) 
      throw new GlueException("The \"Environment\" was missing or (null)!");
    pEnvironment = env;

    pHasModifiable = false;
    pConflicts     = new TreeMap<String,LinkedList<Integer>>();
    pAnyConflicts  = new TreeSet<Integer>();
  }
 


  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 925603407240870760L;

  
  
  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The names of the packages which make up the toolset in the order they are included.
   */
  private ArrayList<String>  pPackages;

  /**
   * The indices in order of evaluation of the packages which provide a value for an 
   * environmental variable indexed by the environmental variable name.
   */
  private TreeMap<String,LinkedList<Integer>>  pPackageHistory;

  /**
   * The revision numbers of the packages in the order they are included.
   * 
   * If there is no revision number entry for a package, then the package is modifiable.
   */
  private ArrayList<VersionID>  pVersionIDs;


  /**
   * The descriptive message given at the time the toolset was created. <P> 
   * 
   * The timestamp and author of the message are also the timestamp and author of the 
   * toolset. <P> 
   */
  private SimpleLogMessage  pMessage;    

  /**
   * The cooked toolset environment.
   */ 
  private TreeMap<String,String>  pEnvironment;
  
  
  /*----------------------------------------------------------------------------------------*/

  /**
   * Was the toolset built with any modifiable packages.
   */ 
  private boolean  pHasModifiable;

  /**
   * The indices in order of evaluation of the packages which are in conflict indexed by 
   * the environmental variable name.
   */
  private TreeMap<String,LinkedList<Integer>>  pConflicts;

  /**
   * The indices of the packages for which there are one or more environmental 
   * variable conflicts.
   */
  private TreeSet<Integer>  pAnyConflicts;

}
