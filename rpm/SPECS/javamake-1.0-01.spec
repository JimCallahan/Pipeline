Summary: Java Make
Name: javamake
Version: 1.0
Release: 01
Copyright: public domain
Group: Development/Libraries
Source0: javamake-20030628.tgz
BuildRoot: %{_tmppath}/%{name}-%{version}-root
URL: http://www.experimentalstuff.com/Technologies/JavaMake
Packager: Jim Callahan <jim@polaris.net>


%description
This tool's functionality is analogous to the "smart checking" 
feature of Borland JBuilder or the dependency analysis feature 
of IBM Jikes. However, it is a command line tool that is not 
tied to any IDE, can be used with any Java compiler, and can 
run as a task in the popular Ant make system.

%prep


%build


%install
mkdir -p $RPM_BUILD_ROOT/usr/java
cd $RPM_BUILD_ROOT/usr/java
tar -zxvf /usr/src/redhat/SOURCES/javamake-20030628.tgz

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root)
/usr/java/javamake/docs/index.html
/usr/java/javamake/docs/javamake.html
/usr/java/javamake/docs/ant.html
/usr/java/javamake/api/allclasses-frame.html
/usr/java/javamake/api/allclasses-noframe.html
/usr/java/javamake/api/constant-values.html
/usr/java/javamake/api/index-all.html
/usr/java/javamake/api/index.html
/usr/java/javamake/api/overview-tree.html
/usr/java/javamake/api/com/sun/tools/javamake/Main.html
/usr/java/javamake/api/com/sun/tools/javamake/package-frame.html
/usr/java/javamake/api/com/sun/tools/javamake/package-summary.html
/usr/java/javamake/api/com/sun/tools/javamake/package-tree.html
/usr/java/javamake/api/com/sun/tools/javamake/PublicExceptions.ClassFileParseException.html
/usr/java/javamake/api/com/sun/tools/javamake/PublicExceptions.ClassNameMismatchException.html
/usr/java/javamake/api/com/sun/tools/javamake/PublicExceptions.CommandFileReadException.html
/usr/java/javamake/api/com/sun/tools/javamake/PublicExceptions.CompilerInteractionException.html
/usr/java/javamake/api/com/sun/tools/javamake/PublicExceptions.html
/usr/java/javamake/api/com/sun/tools/javamake/PublicExceptions.InternalException.html
/usr/java/javamake/api/com/sun/tools/javamake/PublicExceptions.InvalidCmdOptionException.html
/usr/java/javamake/api/com/sun/tools/javamake/PublicExceptions.InvalidSourceFileExtensionException.html
/usr/java/javamake/api/com/sun/tools/javamake/PublicExceptions.JarDependsOnSourceException.html
/usr/java/javamake/api/com/sun/tools/javamake/PublicExceptions.NoActionRequestedException.html
/usr/java/javamake/api/com/sun/tools/javamake/PublicExceptions.PDBCorruptedException.html
/usr/java/javamake/lib/javamake.jar

%changelog
* Fri Nov  2 2001 Jim Callahan <jim@polaris.net>
- Initial build.


