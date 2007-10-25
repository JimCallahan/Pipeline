// $Id: NodeBundle.java,v 1.2 2007/10/25 00:09:09 jim Exp $

package us.temerity.pipeline;

import us.temerity.pipeline.toolset.*;
import us.temerity.pipeline.glue.*;

import java.util.*;
import java.io.*;

/*------------------------------------------------------------------------------------------*/
/*   N O D E   B U N D L E                                                                  */
/*------------------------------------------------------------------------------------------*/

/**
 * A node bundle contains the complete set of information required to transfer a tree of 
 * nodes between distinct sites running separate instance of Pipeline. <P> 
 */
public 
class NodeBundle
  implements Glueable, Serializable
{  
  /*----------------------------------------------------------------------------------------*/
  /*   C O N S T R U C T O R                                                                */
  /*----------------------------------------------------------------------------------------*/

  /**
   * This constructor is required by the {@link GlueDecoder} to instantiate the class 
   * when encountered during the reading of GLUE format files and should not be called 
   * from user code.
   */
  public
  NodeBundle() 
  {}

  /**
   * Construct a new node bundle.
   * 
   * @param stamp
   *   The timestamp of when the node bundle was created.
   * 
   * @param rootNodeID
   *   The working root node identifier.
   * 
   * @param versions
   *   The working versions of the nodes in the order they should be unpacked.
   * 
   * @param annotations
   *   The annotations associated with nodes indexed by fully resolved node name and 
   *   annotation name.
   * 
   * @param toolsets
   *   The toolsets used by the nodes indexed by toolset name and operating system type.
   * 
   * @param packages
   *   The toolset packages used by the nodes indexed by package name, operating system type
   *   and package version number.
   */ 
  public
  NodeBundle
  (
   long stamp,
   NodeID rootNodeID, 
   LinkedList<NodeMod> versions, 
   DoubleMap<String,String,BaseAnnotation> annotations, 
   DoubleMap<String,OsType,Toolset> toolsets, 
   TripleMap<String,OsType,VersionID,PackageVersion> packages
  ) 
  {
    pCreatedOn       = stamp; 
    pCreatedBy       = PackageInfo.sUser; 
    pCustomer        = PackageInfo.sCustomer; 
    pCustomerProfile = PackageInfo.sCustomerProfile; 
    pPipelineVersion = PackageInfo.sVersion; 
    pPipelineRelease = PackageInfo.sRelease; 

    if(rootNodeID == null) 
      throw new IllegalArgumentException
        ("The root node identifier cannot be (null)!");
    pRootNodeID = rootNodeID;

    if(versions == null) 
      throw new IllegalArgumentException
        ("The working node versions to bundle cannot be (null)!");
    pVersions = versions; 

    if(annotations == null) 
      throw new IllegalArgumentException
        ("The annotations to bundle cannot be (null)!");
    pAnnotations = annotations; 

    if(toolsets == null) 
      throw new IllegalArgumentException
        ("The toolsets to bundle cannot be (null)!");
    pToolsets = toolsets; 

    if(packages == null) 
      throw new IllegalArgumentException
        ("The toolset packages to bundle cannot be (null)!");
    pPackages = packages; 

    /* sanity checks... */ 
    for(NodeMod mod : pVersions) {
      String tname = mod.getToolset();
      TreeMap<OsType,Toolset> tsets = pToolsets.get(tname);
      if((tsets == null) || tsets.isEmpty()) 
        throw new IllegalArgumentException
          ("The bundled node (" + mod.getName() + ") references a toolset " + 
           "(" + tname + ") not included in the set of toolsets to be bundled!");
      
      for(OsType os : tsets.keySet()) {
        Toolset tset = tsets.get(os);
        if(tset == null) 
          throw new IllegalArgumentException 
            ("No toolset was supplied for (" + tname + ") on the (" + os + ") platform!");

        int wk;
        for(wk=0; wk<tset.getNumPackages(); wk++) {
          String pname = tset.getPackageName(wk);
          VersionID vid = tset.getPackageVersionID(wk); 
          PackageVersion pkg = pPackages.get(pname, os, vid);
          if(pkg == null) 
            throw new IllegalArgumentException 
              ("The bundled toolset (" + tset + ") references a toolset package " + 
               "(" + pname + " v" + vid + ") for the (" + os + ") platform which is not " + 
               "included in the set of to be packages to be bundled!");
        }
      }
    }
  }



  /*----------------------------------------------------------------------------------------*/
  /*   A C C E S S                                                                          */
  /*----------------------------------------------------------------------------------------*/
  
  /**
   * The timestamp of when the node bundle was created.
   */ 
  public long 
  getCreatedOn() 
  {
    return pCreatedOn; 
  }
  
  /**
   * The name of the user which created the node bundle.
   */ 
  public String 
  getCreatedBy() 
  {
    return pCreatedBy; 
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * The name of the studio which created the node bundle.
   */ 
  public String 
  getCustomer() 
  {
    return pCustomer; 
  }

  /**
   * The name of the Pipeline site profile at the studio.
   */ 
  public String 
  getCustomerProfile() 
  {
    return pCustomerProfile;
  }

  /*----------------------------------------------------------------------------------------*/

  /**
   * The version identifier of the Pipeline release.
   */ 
  public String 
  getPipelineVersion()
  {
    return pPipelineVersion; 
  }

  /**
   * The date and time when this version of Pipeline was released.
   */ 
  public String
  getPipelineRelease() 
  {
    return pPipelineRelease; 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the working root node identifier.
   */
  public NodeID
  getRootNodeID() 
  {
    return pRootNodeID; 
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Gets the working versions of the nodes in the order they should be unpacked.
   */
  public List<NodeMod>
  getWorkingVersions() 
  {
    return Collections.unmodifiableList(pVersions);
  }


  /*----------------------------------------------------------------------------------------*/
  
  /** 
   * Get the names of the annotations for the given node.
   * 
   * @param nname 
   *   The fully resolved node name.
   */ 
  public Set<String> 
  getAnnotationNames
  (
   String nname
  ) 
  {
    Set<String> keys = pAnnotations.keySet(nname);
    if(keys != null) 
      Collections.unmodifiableSet(keys);
    return new TreeSet<String>();
  }

  /**
   * Get the a specific annotation for the given node. 
   * 
   * @param nname 
   *   The fully resolved node name.
   * 
   * @param aname 
   *   The name of the annotation. 
   */
  public BaseAnnotation
  getAnnotation
  (
   String nname, 
   String aname
  ) 
  {
    return pAnnotations.get(nname, aname);   
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names of all toolsets for the given operating system in the bundle.
   * 
   * @param os
   *   The operating system type.
   */ 
  public SortedSet<String> 
  getToolsetNames
  (
   OsType os
  ) 
  {
    TreeSet<String> names = new TreeSet<String>();
    for(String tname : pToolsets.keySet()) 
      if(pToolsets.get(tname).containsKey(os)) 
        names.add(tname);
    
    return names;
  }

  /**
   * Gets the names of all toolsets used by the nodes in the bundle.
   */
  public Set<String> 
  getAllToolsetNames() 
  {
    return Collections.unmodifiableSet(pToolsets.keySet());
  }

  /**
   * Get an OS specific toolset with the given name from the bundle.
   * 
   * @param name
   *   The toolset name.
   * 
   * @param os
   *   The operating system type.
   */ 
  public synchronized Toolset
  getToolset
  (
   String name, 
   OsType os
  ) 
  {
    return pToolsets.get(name, os);
  }

  /**
   * Get all OS specific toolsets with the given name from the bundle. 
   * 
   * @param name
   *   The toolset name.
   */ 
  public TreeMap<OsType,Toolset>
  getOsToolsets
  (
   String name
  ) 
  {
    TreeMap<OsType,Toolset> tsets = pToolsets.get(name);
    if(tsets != null) 
      return new TreeMap<OsType,Toolset>(tsets);
    return null;
  }


  /*----------------------------------------------------------------------------------------*/

  /**
   * Get the names and revision numbers of all OS specific toolset packages used by the 
   * toolsets in the bundle.
   * 
   * @param os
   *   The operating system type.
   */ 
  public MappedSet<String,VersionID>
  getToolsetPackageNames
  (
   OsType os
  )  
  {
    MappedSet<String,VersionID> packages = new MappedSet<String,VersionID>();
    for(String pname : pPackages.keySet()) {
      if(pPackages.containsKey(pname, os)) {
        for(VersionID vid : pPackages.keySet(pname, os))
          packages.put(pname, vid);
      }
    }
    
    return packages;
  }

  /**
   * Get the names and revision numbers of all toolset packages for all operating systems 
   * used by the toolsets in the bundle.
   */ 
  public DoubleMap<String,OsType,TreeSet<VersionID>>
  getAllToolsetPackageNames() 
  {
    DoubleMap<String,OsType,TreeSet<VersionID>> packages = 
      new DoubleMap<String,OsType,TreeSet<VersionID>>();

    for(String pname : pPackages.keySet()) {
      for(OsType os : pPackages.keySet(pname)) 
        packages.put(pname, os, new TreeSet<VersionID>(pPackages.keySet(pname, os)));
    }

    return packages;
  }

  /**
   * Get an OS specific toolset package from the bundle. 
   * 
   * @param name
   *   The toolset package name.
   * 
   * @param vid
   *   The revision number of the package.
   * 
   * @param os
   *   The operating system type.
   */ 
  public PackageVersion
  getToolsetPackage
  (
   String name, 
   VersionID vid, 
   OsType os
  )  
  {
    return pPackages.get(name, os, vid);
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S E R I A L I Z A B L E                                                              */
  /*----------------------------------------------------------------------------------------*/

  /**
   * Write the serializable fields to the object stream. <P> 
   * 
   * This enables the class to convert a dynamically loaded annotation plugin instance into a 
   * generic staticly loaded BaseAnnotation instance before serialization.
   */ 
  private void 
  writeObject
  (
   java.io.ObjectOutputStream out
  )
    throws IOException
  {
    out.writeObject(pCreatedOn);
    out.writeObject(pCreatedBy);
    out.writeObject(pCustomer);
    out.writeObject(pCustomerProfile);
    out.writeObject(pPipelineVersion);
    out.writeObject(pPipelineRelease);
    out.writeObject(pRootNodeID);
    out.writeObject(pVersions); 

    {
      DoubleMap<String,String,BaseAnnotation> annotations = 
        new DoubleMap<String,String,BaseAnnotation>(); 

      for(String nname : pAnnotations.keySet()) {
        for(String aname : pAnnotations.keySet(nname)) 
          annotations.put(nname, aname, 
                          new BaseAnnotation(pAnnotations.get(nname, aname)));
      }

      out.writeObject(annotations); 
    }

    out.writeObject(pToolsets);
    out.writeObject(pPackages);
  }

  
  /**
   * Read the serializable fields from the object stream. <P> 
   * 
   * This enables the class to dynamically instantiate an annotation plugin instance and copy
   * its parameters from the generic staticly loaded BaseAnnotation instance in the object 
   * stream. 
   */ 
  private void 
  readObject
  (
   java.io.ObjectInputStream in
  )
    throws IOException, ClassNotFoundException
  {
    pCreatedOn = (Long) in.readObject();
    pCreatedBy = (String) in.readObject();
    pCustomer = (String) in.readObject();
    pCustomerProfile = (String) in.readObject();
    pPipelineVersion = (String) in.readObject();
    pPipelineRelease = (String) in.readObject();
    pRootNodeID = (NodeID) in.readObject();
    pVersions = (LinkedList<NodeMod>) in.readObject();

    {
      pAnnotations = new DoubleMap<String,String,BaseAnnotation>(); 
      
      DoubleMap<String,String,BaseAnnotation> annotations = 
        (DoubleMap<String,String,BaseAnnotation>) in.readObject();

      try {
        PluginMgrClient client = PluginMgrClient.getInstance();
        for(String nname : annotations.keySet()) {
          for(String aname : annotations.keySet(nname)) {
            BaseAnnotation annot = annotations.get(nname, aname);
            pAnnotations.put(nname, aname, 
                             client.newAnnotation(annot.getName(), 
                                                  annot.getVersionID(), 
                                                  annot.getVendor()));
          }
        }
      }  
      catch(PipelineException ex) {
        throw new IOException(ex.getMessage());
      }    
    }

    pToolsets = (DoubleMap<String,OsType,Toolset>) in.readObject();
    pPackages = (TripleMap<String,OsType,VersionID,PackageVersion>) in.readObject();
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
    encoder.encode("CreatedOn", pCreatedOn);
    encoder.encode("CreatedBy", pCreatedBy);
    encoder.encode("Customer", pCustomer);
    encoder.encode("CustomerProfile", pCustomerProfile);
    encoder.encode("PipelineVersion", pPipelineVersion);
    encoder.encode("PipelineRelease", pPipelineRelease);
    encoder.encode("RootNodeID", pRootNodeID);
    encoder.encode("Versions", pVersions); 

    if(!pAnnotations.isEmpty()) {
      DoubleMap<String,String,BaseAnnotation> annotations = 
        new DoubleMap<String,String,BaseAnnotation>(); 

      for(String nname : pAnnotations.keySet()) {
        for(String aname : pAnnotations.keySet(nname)) 
          annotations.put(nname, aname, 
                          new BaseAnnotation(pAnnotations.get(nname, aname)));
      }

      encoder.encode("Annotations", annotations); 
    }

    encoder.encode("Toolsets", pToolsets);
    encoder.encode("Packages", pPackages);
  }

  public void 
  fromGlue
  (
   GlueDecoder decoder 
  ) 
    throws GlueException
  {
    Long createdOn = (Long) decoder.decode("CreatedOn"); 
    if(createdOn == null) 
      throw new GlueException("The \"CreatedOn\" was missing or (null)!");
    pCreatedOn = createdOn;
    
    String createdBy = (String) decoder.decode("CreatedBy"); 
    if(createdBy == null) 
      throw new GlueException("The \"CreatedBy\" was missing or (null)!");
    pCreatedBy = createdBy;
    
    String customer = (String) decoder.decode("Customer"); 
    if(customer == null) 
      throw new GlueException("The \"Customer\" was missing or (null)!");
    pCustomer = customer;

    String profile = (String) decoder.decode("CustomerProfile"); 
    if(profile == null) 
      throw new GlueException("The \"CustomerProfile\" was missing or (null)!");
    pCustomerProfile = profile;
    
    String version = (String) decoder.decode("PipelineVersion"); 
    if(version == null) 
      throw new GlueException("The \"PipelineVersion\" was missing or (null)!");
    pPipelineVersion = version;
  
    String release = (String) decoder.decode("PipelineRelease"); 
    if(release == null) 
      throw new GlueException("The \"PipelineRelease\" was missing or (null)!");
    pPipelineRelease = release;

    NodeID root = (NodeID) decoder.decode("RootNodeID"); 
    if(root == null) 
      throw new GlueException("The \"RootNodeID\" was missing or (null)!");
    pRootNodeID = root;
    
    LinkedList<NodeMod> versions = (LinkedList<NodeMod>) decoder.decode("Versions"); 
    if(versions == null) 
      throw new GlueException("The \"Versions\" was missing or (null)!");
    pVersions = versions;
    
    {
      pAnnotations = new DoubleMap<String,String,BaseAnnotation>(); 
      
      DoubleMap<String,String,BaseAnnotation> annotations = 
        (DoubleMap<String,String,BaseAnnotation>) decoder.decode("Annotations"); 
      if(annotations != null) {
        try {
          PluginMgrClient client = PluginMgrClient.getInstance();
          for(String nname : annotations.keySet()) {
            for(String aname : annotations.keySet(nname)) {
              BaseAnnotation annot = annotations.get(nname, aname);
              pAnnotations.put(nname, aname, 
                               client.newAnnotation(annot.getName(), 
                                                    annot.getVersionID(), 
                                                    annot.getVendor()));
            }
          }
        }  
        catch(PipelineException ex) {
          throw new GlueException(ex.getMessage());
        }    
      }
    }
    
    DoubleMap<String,OsType,Toolset> toolsets = 
      (DoubleMap<String,OsType,Toolset>) decoder.decode("Toolsets"); 
    if(toolsets == null) 
      throw new GlueException("The \"Toolsets\" was missing or (null)!");
    pToolsets = toolsets;
    
    TripleMap<String,OsType,VersionID,PackageVersion> packages = 
      (TripleMap<String,OsType,VersionID,PackageVersion>) decoder.decode("Packages"); 
    if(packages == null) 
      throw new GlueException("The \"Packages\" was missing or (null)!");
    pPackages = packages;
  }



  /*----------------------------------------------------------------------------------------*/
  /*   S T A T I C   I N T E R N A L S                                                      */
  /*----------------------------------------------------------------------------------------*/

  private static final long serialVersionUID = 6713760153223066234L;
                                                


  /*----------------------------------------------------------------------------------------*/
  /*   I N T E R N A L S                                                                    */
  /*----------------------------------------------------------------------------------------*/

  /**
   * The timestamp of when the node bundle was created.
   */ 
  private long  pCreatedOn; 
  
  /**
   * The name of the user which created the node bundle.
   */ 
  private String  pCreatedBy; 

  /**
   * The name of the studio which created the node bundle.
   */ 
  private String  pCustomer; 

  /**
   * The name of the Pipeline site profile at the studio.
   */ 
  private String  pCustomerProfile; 

  /**
   * The version identifier of the Pipeline release.
   */ 
  private String  pPipelineVersion; 

  /**
   * The date and time when this version of Pipeline was released.
   */ 
  private String  pPipelineRelease; 


  /*----------------------------------------------------------------------------------------*/

  /** 
   * The working root node identifier.
   */ 
  private NodeID  pRootNodeID; 

  /**
   * Gets the working versions of the nodes in the order they should be unpacked.
   */
  private LinkedList<NodeMod>  pVersions; 

  /**
   * Gets the annotations associated with nodes indexed by fully resolved node name and 
   * annotation name.
   */
  private DoubleMap<String,String,BaseAnnotation>  pAnnotations; 

  /**
   * Gets the toolsets used by the nodes indexed by toolset name and operating system type.
   */
  private DoubleMap<String,OsType,Toolset> pToolsets; 
 
  /**
   * Gets the toolset packages used by the nodes indexed by package name, operating system 
   * type and package version number.
   */
  private TripleMap<String,OsType,VersionID,PackageVersion>  pPackages;

 
}

