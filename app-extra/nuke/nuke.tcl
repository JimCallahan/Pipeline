#---------------------------------------------------------------------------------------------
# This overrides the way Nuke mangles filenames before passing them to the operating system 
# to support Temerity Pipeline's WORKING environmental variable. The WORKING variable includes
# the OS specific path to current root working area directory.  This makes it possible to 
# create OS and artist working area independent Nuke scripts.
#---------------------------------------------------------------------------------------------

if $WIN32 {
    proc filename_fix {arg} {
	if {[string range $arg 0 6]=="WORKING"} {
            set WORKING [getenv WORKING ""]
	    return [join [split $WORKING "\\"] "/"][string range $arg 7 [string length $arg]]
	} else {
	    return $arg
	}
    }
} else {
    proc filename_fix {arg} {
	if {[string range $arg 0 6]=="WORKING"} {
            set WORKING [getenv WORKING ""]
	    return $WORKING[string range $arg 7 [string length $arg]]
	} else {
	    return $arg
	}
    }
}





#            puts "WINDOWS MANGLING..."
#            puts $arg
#            puts [join [split $WORKING "\\"] "/"][string range $arg 7 [string length $arg]]

#            puts "UNIX MANGLING..."
#            puts $arg
#            puts $WORKING[string range $arg 7 [string length $arg]]
