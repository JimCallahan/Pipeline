Summary: Java Compiler Compiler
Name: javacc
Version: 3.0
Release: 01
Copyright: public domain
Group: Development/Libraries
Source0: javacc-3.0.tar.gz
BuildRoot: %{_tmppath}/%{name}-%{version}-root
URL: https://javacc.dev.java.net/
Packager: Jim Callahan <jim@polaris.net>


%description
Java Compiler Compiler [tm] (JavaCC [tm]) is the most popular 
parser generator for use with Java [tm] applications. A parser 
generator is a tool that reads a grammar specification and 
converts it to a Java program that can recognize matches to 
the grammar. In addition to the parser generator itself, JavaCC 
provides other standard capabilities related to parser generation 
such as tree building (via a tool called JJTree included with 
JavaCC), actions, debugging, etc.

%prep


%build


%install
mkdir -p $RPM_BUILD_ROOT/usr/java
cd $RPM_BUILD_ROOT/usr/java
tar -zxvf /usr/src/redhat/SOURCES/javacc-3.0.tar.gz

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root)
/usr/java/javacc-3.0/bin/javacc.bat
/usr/java/javacc-3.0/bin/javacc
/usr/java/javacc-3.0/bin/jjdoc.bat
/usr/java/javacc-3.0/bin/jjdoc
/usr/java/javacc-3.0/bin/jjtree.bat
/usr/java/javacc-3.0/bin/jjtree
/usr/java/javacc-3.0/bin/lib/javacc.jar
/usr/java/javacc-3.0/doc/CharStream.html
/usr/java/javacc-3.0/doc/JJDoc.html
/usr/java/javacc-3.0/doc/JJTree.html
/usr/java/javacc-3.0/doc/apiroutines.html
/usr/java/javacc-3.0/doc/commandline.html
/usr/java/javacc-3.0/doc/docindex.html
/usr/java/javacc-3.0/doc/errorrecovery.html
/usr/java/javacc-3.0/doc/features.html
/usr/java/javacc-3.0/doc/getstarted.html
/usr/java/javacc-3.0/doc/index.html
/usr/java/javacc-3.0/doc/installhelp.html
/usr/java/javacc-3.0/doc/javaccgrm.html
/usr/java/javacc-3.0/doc/javaccreleasenotes.html
/usr/java/javacc-3.0/doc/jjdocreleasenotes.html
/usr/java/javacc-3.0/doc/jjtreeREADME.html
/usr/java/javacc-3.0/doc/jjtreeintro.html
/usr/java/javacc-3.0/doc/jjtreereleasenotes.html
/usr/java/javacc-3.0/doc/lookahead.html
/usr/java/javacc-3.0/doc/mailinglist.html
/usr/java/javacc-3.0/doc/simpleREADME.html
/usr/java/javacc-3.0/doc/support.html
/usr/java/javacc-3.0/doc/tokenmanager.html
/usr/java/javacc-3.0/doc/usage.html
/usr/java/javacc-3.0/examples/CORBA-IDL/IDL.jj
/usr/java/javacc-3.0/examples/CORBA-IDL/README
/usr/java/javacc-3.0/examples/README
/usr/java/javacc-3.0/examples/GUIParsing/README
/usr/java/javacc-3.0/examples/GUIParsing/ParserVersion/CalcGUI.java
/usr/java/javacc-3.0/examples/GUIParsing/ParserVersion/CalcInput.jj
/usr/java/javacc-3.0/examples/GUIParsing/ParserVersion/Main.java
/usr/java/javacc-3.0/examples/GUIParsing/ParserVersion/ProducerConsumer.java
/usr/java/javacc-3.0/examples/GUIParsing/ParserVersion/README
/usr/java/javacc-3.0/examples/GUIParsing/ParserVersion/TokenCollector.java
/usr/java/javacc-3.0/examples/GUIParsing/TokenMgrVersion/CalcGUI.java
/usr/java/javacc-3.0/examples/GUIParsing/TokenMgrVersion/CalcInput.jj
/usr/java/javacc-3.0/examples/GUIParsing/TokenMgrVersion/CharCollector.java
/usr/java/javacc-3.0/examples/GUIParsing/TokenMgrVersion/Main.java
/usr/java/javacc-3.0/examples/GUIParsing/TokenMgrVersion/MyLexer.java
/usr/java/javacc-3.0/examples/GUIParsing/TokenMgrVersion/README
/usr/java/javacc-3.0/examples/Interpreter/ASTAddNode.java
/usr/java/javacc-3.0/examples/Interpreter/ASTAndNode.java
/usr/java/javacc-3.0/examples/Interpreter/ASTAssignment.java
/usr/java/javacc-3.0/examples/Interpreter/ASTBitwiseAndNode.java
/usr/java/javacc-3.0/examples/Interpreter/ASTBitwiseComplNode.java
/usr/java/javacc-3.0/examples/Interpreter/ASTBitwiseOrNode.java
/usr/java/javacc-3.0/examples/Interpreter/ASTBitwiseXorNode.java
/usr/java/javacc-3.0/examples/Interpreter/ASTBlock.java
/usr/java/javacc-3.0/examples/Interpreter/ASTCompilationUnit.java
/usr/java/javacc-3.0/examples/Interpreter/ASTDivNode.java
/usr/java/javacc-3.0/examples/Interpreter/ASTEQNode.java
/usr/java/javacc-3.0/examples/Interpreter/ASTFalseNode.java
/usr/java/javacc-3.0/examples/Interpreter/ASTGENode.java
/usr/java/javacc-3.0/examples/Interpreter/ASTGTNode.java
/usr/java/javacc-3.0/examples/Interpreter/ASTId.java
/usr/java/javacc-3.0/examples/Interpreter/ASTIfStatement.java
/usr/java/javacc-3.0/examples/Interpreter/ASTIntConstNode.java
/usr/java/javacc-3.0/examples/Interpreter/ASTLENode.java
/usr/java/javacc-3.0/examples/Interpreter/ASTLTNode.java
/usr/java/javacc-3.0/examples/Interpreter/ASTModNode.java
/usr/java/javacc-3.0/examples/Interpreter/ASTMulNode.java
/usr/java/javacc-3.0/examples/Interpreter/ASTNENode.java
/usr/java/javacc-3.0/examples/Interpreter/ASTNotNode.java
/usr/java/javacc-3.0/examples/Interpreter/ASTOrNode.java
/usr/java/javacc-3.0/examples/Interpreter/ASTReadStatement.java
/usr/java/javacc-3.0/examples/Interpreter/ASTStatementExpression.java
/usr/java/javacc-3.0/examples/Interpreter/ASTSubtractNode.java
/usr/java/javacc-3.0/examples/Interpreter/ASTTrueNode.java
/usr/java/javacc-3.0/examples/Interpreter/ASTVarDeclaration.java
/usr/java/javacc-3.0/examples/Interpreter/ASTWhileStatement.java
/usr/java/javacc-3.0/examples/Interpreter/ASTWriteStatement.java
/usr/java/javacc-3.0/examples/Interpreter/Node.java
/usr/java/javacc-3.0/examples/Interpreter/README
/usr/java/javacc-3.0/examples/Interpreter/SPL.java
/usr/java/javacc-3.0/examples/Interpreter/SPL.jjt
/usr/java/javacc-3.0/examples/Interpreter/SimpleNode.java
/usr/java/javacc-3.0/examples/Interpreter/fact.spl
/usr/java/javacc-3.0/examples/Interpreter/odd.spl
/usr/java/javacc-3.0/examples/Interpreter/sqrt.spl
/usr/java/javacc-3.0/examples/JJTreeExamples/ASTMyID.java
/usr/java/javacc-3.0/examples/JJTreeExamples/ASTMyOtherID.java
/usr/java/javacc-3.0/examples/JJTreeExamples/README
/usr/java/javacc-3.0/examples/JJTreeExamples/eg1.jjt
/usr/java/javacc-3.0/examples/JJTreeExamples/eg2.jjt
/usr/java/javacc-3.0/examples/JJTreeExamples/eg3.jjt
/usr/java/javacc-3.0/examples/JJTreeExamples/eg4.jjt
/usr/java/javacc-3.0/examples/JJTreeExamples/eg4DumpVisitor.java
/usr/java/javacc-3.0/examples/JavaCCGrammar/JavaCC.jj
/usr/java/javacc-3.0/examples/JavaGrammars/Java1.0.2.jj
/usr/java/javacc-3.0/examples/JavaGrammars/Java1.0.2LS.jj
/usr/java/javacc-3.0/examples/JavaGrammars/Java1.1.jj
/usr/java/javacc-3.0/examples/JavaGrammars/Java1.1noLA.jj
/usr/java/javacc-3.0/examples/JavaGrammars/OPTIMIZING
/usr/java/javacc-3.0/examples/JavaGrammars/README
/usr/java/javacc-3.0/examples/Lookahead/Example1.jj
/usr/java/javacc-3.0/examples/Lookahead/Example10.jj
/usr/java/javacc-3.0/examples/Lookahead/Example2.jj
/usr/java/javacc-3.0/examples/Lookahead/Example3.jj
/usr/java/javacc-3.0/examples/Lookahead/Example4.jj
/usr/java/javacc-3.0/examples/Lookahead/Example5.jj
/usr/java/javacc-3.0/examples/Lookahead/Example6.jj
/usr/java/javacc-3.0/examples/Lookahead/Example7.jj
/usr/java/javacc-3.0/examples/Lookahead/Example8.jj
/usr/java/javacc-3.0/examples/Lookahead/Example9.jj
/usr/java/javacc-3.0/examples/Lookahead/README
/usr/java/javacc-3.0/examples/MailProcessing/Digest.jj
/usr/java/javacc-3.0/examples/MailProcessing/Faq.jj
/usr/java/javacc-3.0/examples/MailProcessing/README
/usr/java/javacc-3.0/examples/MailProcessing/sampleMailFile
/usr/java/javacc-3.0/examples/Obfuscator/input/package1/Main.java
/usr/java/javacc-3.0/examples/Obfuscator/input/package2/Incr.java
/usr/java/javacc-3.0/examples/Obfuscator/Globals.java
/usr/java/javacc-3.0/examples/Obfuscator/IdsFile.jj
/usr/java/javacc-3.0/examples/Obfuscator/Java1.1.jj
/usr/java/javacc-3.0/examples/Obfuscator/Main.java
/usr/java/javacc-3.0/examples/Obfuscator/MapFile.jj
/usr/java/javacc-3.0/examples/Obfuscator/Obfuscator.java
/usr/java/javacc-3.0/examples/Obfuscator/README
/usr/java/javacc-3.0/examples/Obfuscator/maps
/usr/java/javacc-3.0/examples/Obfuscator/nochangeids
/usr/java/javacc-3.0/examples/Obfuscator/useids
/usr/java/javacc-3.0/examples/SimpleExamples/IdList.jj
/usr/java/javacc-3.0/examples/SimpleExamples/NL_Xlator.jj
/usr/java/javacc-3.0/examples/SimpleExamples/README
/usr/java/javacc-3.0/examples/SimpleExamples/Simple1.jj
/usr/java/javacc-3.0/examples/SimpleExamples/Simple2.jj
/usr/java/javacc-3.0/examples/SimpleExamples/Simple3.jj
/usr/java/javacc-3.0/examples/Transformer/ASTCompilationUnit.java
/usr/java/javacc-3.0/examples/Transformer/ASTSpecialBlock.java
/usr/java/javacc-3.0/examples/Transformer/README
/usr/java/javacc-3.0/examples/Transformer/SimpleNode.java
/usr/java/javacc-3.0/examples/Transformer/ToyJava.jjt
/usr/java/javacc-3.0/examples/Transformer/divide.toy
/usr/java/javacc-3.0/examples/VTransformer/AddAcceptVisitor.java
/usr/java/javacc-3.0/examples/VTransformer/Java1.1.jjt
/usr/java/javacc-3.0/examples/VTransformer/Main.java
/usr/java/javacc-3.0/examples/VTransformer/README
/usr/java/javacc-3.0/examples/VTransformer/SimpleNode.java
/usr/java/javacc-3.0/examples/VTransformer/UnparseVisitor.java

%changelog
* Fri Nov  2 2001 Jim Callahan <jim@polaris.net>
- Initial build.


