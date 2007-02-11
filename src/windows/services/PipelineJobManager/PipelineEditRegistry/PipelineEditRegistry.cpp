// PipelineEditRegistry.cpp : main project file.

#include "stdafx.h"

using namespace System;
using namespace Microsoft::Win32;

int main(array<System::String ^> ^args)
{
  String^ path = "Path";
  if(args->Length < 1) {
    Console::WriteLine("usage: PipelineEditRegistry path1 [path2 ...]\n");

    Console::WriteLine("Press <ENTER> to continue...");
    Console::ReadLine();

    return 1;
  }

  Console::WriteLine("");
  Console::WriteLine
    ("Checking the ({0}) environmental variable for the required directories:", path);
  for(int ak=0; ak<args->Length; ak++)
    Console::WriteLine("  {0}", args[ak]);
  Console::WriteLine("");

  RegistryKey^ hklm = Registry::LocalMachine;
  RegistryKey^ env = 
    hklm->OpenSubKey("SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment",
		     RegistryKeyPermissionCheck::ReadWriteSubTree);

  String^ nvalue;
  String^ value = (String^) env->GetValue(path);
  if(value == nullptr) {
    Console::WriteLine("Current ({0}) Components =\n  <NONE>\n", path); 

    Console::WriteLine("New ({0}) Components:", path);
    for(int ak=0; ak<args->Length; ak++) 
      Console::WriteLine("  {0}", args[ak]);
    Console::WriteLine("");

    /* since its empty, just add the required directories */ 
    nvalue = String::Join(";", args);
  }
  else {
    /* the current list of directories */ 
    array<String^>^ paths = value->Split(';');

    Console::WriteLine("Current ({0}) Components:", path);
    for(int pk=0; pk<paths->Length; pk++) 
      Console::WriteLine("  {0}", paths[pk]);
    Console::WriteLine("");

    /* count the number of existing directories not in the required list */ 
    int cnt = args->Length;
    for(int pk=0; pk<paths->Length; pk++) {
      bool isAnyPath = false;
      for(int ak=0; ak<args->Length; ak++) { 
	if(paths[pk]->Equals(args[ak])) 
	  isAnyPath = true;
      }

      if(!isAnyPath) 
	cnt++;
    }
	 
    /* build a new list of directories */ 
    array<bool>^ hasPath = gcnew array<bool>(args->Length);
    array<String^>^ npaths = gcnew array<String^>(cnt);
    {
      /* first add the directories not in the included list */ 
      int wk=0;
      for(int pk=0; pk<paths->Length; pk++) {
	bool isAnyPath = false;
	for(int ak=0; ak<args->Length; ak++) { 
	  if(paths[pk]->Equals(args[ak])) {
	    hasPath[ak] = true;
	    isAnyPath = true;
	  }
	} 

	if(!isAnyPath) {
	  npaths[wk] = paths[pk];
	  wk++;
	}
      }

      /* append the required directories */ 
      for(int ak=0; ak<args->Length; ak++) {
	npaths[wk++] = args[ak];
      }
    }

    bool hasAllPaths = true;
    for(int wk=0; wk<hasPath->Length; wk++)
      hasAllPaths &= hasPath[wk];

    if(hasAllPaths) {
      Console::WriteLine
	("The ({0}) environmental variable already contains all required directories!", path);

      //Console::WriteLine("Press <ENTER> to continue...");
      //Console::ReadLine();

      return 0;
    }

    Console::WriteLine("New ({0}) Components:", path);
    for(int pk=0; pk<npaths->Length; pk++) 
      Console::WriteLine("  {0}", npaths[pk]);
    Console::WriteLine("");

    nvalue = String::Join(";", npaths);
  }

  env->SetValue(path, nvalue, RegistryValueKind::ExpandString);

  Console::WriteLine("You must REBOOT your system before the changes take effect!"); 
  Console::WriteLine("Reboot and then reinstall the Pipeline Job Manager...\n");

  Console::WriteLine("Press <ENTER> to continue...");
  Console::ReadLine();

  return 1;
}
