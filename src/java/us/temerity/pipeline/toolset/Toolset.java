// $Id: Toolset.java,v 1.1 2004/05/29 06:36:26 jim Exp $

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
   */ 
  public
  Toolset
  (
   String author, 
   String name, 
   Collection<PackageCommon> packages,
   String desc
  ) 
  {
    if(desc == null) 
      throw new IllegalArgumentException("The package description cannot be (null)!");
    pMessage = new LogMessage(author, desc);

    pPackages    = new LinkedList<String>();
    pVersionIDs  = new TreeMap<String,VersionID>();  
    pEnvironment = new TreeMap<String,String>();

    pHasModifiable = false;
    pConflicts     = new TreeMap<String,LinkedList<String>>();
    
    for(PackageCommon com : packages) {
      pPackages.add(com.getName());

      if(com instanceof PackageVersion) {
	PackageVersion vsn = (PackageVersion) com;
	pVersionIDs.put(vsn.getName(), vsn.getVersionID());
      }
      else {
	pHasModifiable = true;
      }

      for(String key : com.getEnvNames()) {
	String value = com.getEnvValue(key);

	if(pEnvironment.containsKey(key)) {
	  String prev = pEnvironment.get(key);

	  switch(com.getMergePolicy(key)) {
	  case Exclusive:
	    {
	      LinkedList<String> pkgs = pConflicts.get(key);
	      if(pkgs == null) {
		pkgs = new LinkedList<String>();
		pConflicts.put(key, pkgs);
	      }
	      pkgs.add(com.getName());
	    }
	    break;
	    
	  case Override:
	    pEnvironment.put(key, value);
	    break;

	  case Ignore:
	    break;

	  case AppendPath:
	    if((prev != null) && (prev.length() > 0)) 
	      pEnvironment.put(key, prev + ":" + value);
	    else 
	      pEnvironment.put(key, value);
	    break;

	  case PrependPath:
	    if((prev != null) && (prev.length() > 0)) 
	      pEnvironment.put(key, value + ":" + prev);
	    else 
	      pEnvironment.put(key, value);
	    break;
	  }
	}
	else {
	  pEnvironment.put(key, value);
	}
      }
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
   * Does this toolset have any package conflicts?
   */ 
  public boolean 
  hasConflicts() 
  {
    return (!pConflicts.isEmpty());
  }
  
  /**
   * Get the names of the environmental variables which have package conflicts.
   */ 
  public Set<String>
  getConflictedNames() 
  {
    return Collections.unmodifiableSet(pConflicts.keySet());
  }

  /**
   * Get the names of the packages which have conflicts for the given environmental variable.
   * 
   * @param name
   *   The name of the environmental variable.
   * 
   * @return
   *   The package names in order of evaluation or <CODE>null</CODE> if no
   *   conflict exists.
   */ 
  public Collection<String>
  getConflictingPackages
  (
   String name
  ) 
  {
    LinkedList<String> packages = pConflicts.get(name);
    if(packages != null) 
      return Collections.unmodifiableCollection(packages);
    return null;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get when the toolset was created.
   */ 
  public Date
  getTimeStamp() 
  {
    return pMessage.getTimeStamp();
  }

  /**
   * Get the name of the user who created the toolset.
   */ 
  public String
  getAuthor() 
  {
    return pMessage.getAuthor();
  }

  /**
   * Get the toolset description.
   */ 
  public String
  getDescription() 
  {
    return pMessage.getMessage();
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of the packages which make up the toolset in evaluation order.
   */ 
  public Collection<String>
  getPackages() 
  {
    return Collections.unmodifiableCollection(pPackages);
  }

  /**
   * Get revision number of the given package. <P> 
   * 
   * @param name
   *   The name of the package.
   * 
   * @return
   *   The revision number or <CODE>null</CODE> if the package is modifiable.
   */ 
  public VersionID
  getPackageVersionID
  (
   String name
  ) 
  {
    return pVersionIDs.get(name);
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
    if(hasConflicts() || pHasModifiable) 
      throw new GlueException
	("Cannot encode Toolsets with conflicts or modifiable packages as Glue!");

    super.toGlue(encoder);

    encoder.encode("Packages",    pPackages);
    encoder.encode("Versions",    pVersionIDs);
    encoder.encode("Message",     pMessage);
    encoder.encode("Environment", pEnvironment);
  }
  
  public void 
  fromGlue
  (
   GlueDecoder decoder  
  ) 
    throws GlueException
  {
    super.fromGlue(decoder);

    LinkedList<String> packages = (LinkedList<String>) decoder.decode("Packages");
    if(packages == null) 
      throw new GlueException("The \"Packages\" was missing or (null)!");
    pPackages = packages;

    TreeMap<String,VersionID> versions = 
      (TreeMap<String,VersionID>) decoder.decode("Versions");
    if(versions == null) 
      throw new GlueException("The \"Versions\" was missing or (null)!");
    pVersionIDs = versions;

    LogMessage msg = (LogMessage) decoder.decode("Message");
    if(msg == null) 
      throw new GlueException("The \"Message\" was missing!");
    pMessage = msg;    

    TreeMap<String,String> env = (TreeMap<String,String>) decoder.decode("Environment");
    if(env == null) 
      throw new GlueException("The \"Environment\" was missing or (null)!");
    pEnvironment = env;

    pHasModifiable = false;
    pConflicts     = new TreeMap<String,LinkedList<String>>();
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
  private LinkedList<String>  pPackages;

  /**
   * The revision numbers of the packages indexed by package name. <P> 
   * 
   * If there is no revision number entry for a package, then the package is modifiable.
   */
  private TreeMap<String,VersionID>  pVersionIDs;

  /**
   * The descriptive message given at the time the toolset was created. <P> 
   * 
   * The timestamp and author of the message are also the timestamp and author of the 
   * toolset. <P> 
   */
  private LogMessage  pMessage;    

  /**
   * The cooked toolset environment.
   */ 
  private TreeMap<String,String>  pEnvironment;
  

  /**
   * Was the toolset built with any modifiable packages.
   */ 
  private boolean  pHasModifiable;

  /**
   * The names of the packages who's entries cannot be combined with the values defined by
   * a previous package indexed by the conflicting entry name. <P> 
   * 
   * If this table is empty, then the toolset is suitable for production use.
   */
  private TreeMap<String,LinkedList<String>>  pConflicts;

}
