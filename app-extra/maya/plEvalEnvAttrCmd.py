# import maya
# maya.cmds.loadPlugin("plEvalEnvAttrCmd.py")
# maya.cmds.plEvalEnvAttr("plEvalEnvNodeName", "destinationNode.attribute")

import sys
import maya.cmds as cmds
import maya.OpenMaya as OpenMaya
import maya.OpenMayaMPx as OpenMayaMPx

kPluginCmdName    = "plEvalEnvAttr"
kPluginCmdVendor  = "TemeritySoftware"
kPluginCmdVersion = "1.0.0"

# command
class scriptedCommand(OpenMayaMPx.MPxCommand):
	def __init__(self):
		OpenMayaMPx.MPxCommand.__init__(self)
	def doIt(self,arglist):
                if not cmds.pluginInfo("plEvalEnvNode.py", query=True, loaded=True):
                        cmds.loadPlugin("plEvalEnvNode.py")
                nodeName = cmds.createNode("plEvalEnvNode", name= arglist.asString(0))
                cmds.setAttr(nodeName + ".out", "", type="string")
                cmds.connectAttr(nodeName + ".out", arglist.asString(1))
                return nodeName

# Creator
def cmdCreator():
	return OpenMayaMPx.asMPxPtr( scriptedCommand() )
	
# Initialize the script plug-in
def initializePlugin(mobject):
	mplugin = OpenMayaMPx.MFnPlugin(mobject, kPluginCmdVendor, kPluginCmdVersion)
	try:
		mplugin.registerCommand( kPluginCmdName, cmdCreator )
	except:
		sys.stderr.write( "Failed to register command: %s\n" % kPluginCmdName )
		raise

# Uninitialize the script plug-in
def uninitializePlugin(mobject):
	mplugin = OpenMayaMPx.MFnPlugin(mobject, kPluginCmdVendor, kPluginCmdVersion)
	try:
		mplugin.deregisterCommand( kPluginCmdName )
	except:
		sys.stderr.write( "Failed to unregister command: %s\n" % kPluginCmdName )
		raise

	
