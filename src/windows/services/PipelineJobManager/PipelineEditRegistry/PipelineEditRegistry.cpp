// PipelineEditRegistry.cpp : main project file.

#include "stdafx.h"

using namespace System;
using namespace Microsoft::Win32;

int main(array<System::String ^> ^args)
{
	String^ path = "Path";
	if(args->Length != 1) {
		Console::WriteLine("usage: PipelineEditRegistry JAVAHOME\n");
		return 1;
	}
			
	Console::WriteLine("Checking the (Path) Environmental variable for the Java Runtime DLL...\n");

	String^ jniPath       = String::Format("{0}\\bin", args[0]);
	String^ jniServerPath = String::Format("{0}\\bin\\server", args[0]);

	RegistryKey^ hklm = Registry::LocalMachine;
	RegistryKey^ env = hklm->OpenSubKey("SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment",
										RegistryKeyPermissionCheck::ReadWriteSubTree);

	String^ nvalue;
	String^ value = (String^) env->GetValue(path);
	if(value == nullptr) {
	  Console::WriteLine("Path (current) = <NONE>\n"); 
	  nvalue = String::Format("{0};{1}", jniPath, jniServerPath);
	}
	else {
	    Console::WriteLine("Path (current) = {0}\n", value); 

  		array<String^>^ paths = value->Split(';');

		// DEBUG
		//Console::WriteLine("Path COMPONENTS:");
		//for(int i=0; i<paths->Length; i++) {
		//	Console::WriteLine("  {0}", paths[i]);
		//}
		//Console::WriteLine("");
		// DEBUG

		int cnt = 2;
		for(int i=0; i<paths->Length; i++) {
			if(!paths[i]->Equals(jniPath) && !paths[i]->Equals(jniServerPath))
				cnt++;
		}
	 
		bool hasJni = false;
		bool hasServer = false;
		array<String^>^ npaths = gcnew array<String^>(cnt);
		{
			int wk=0;
			for(int i=0; i<paths->Length; i++) {
				bool isJni = paths[i]->Equals(jniPath);
				hasJni |= isJni;

				bool isServer = paths[i]->Equals(jniServerPath);
				hasServer |= isServer;
				
				if(!isJni && !isServer) {
					npaths[wk] = paths[i];
					wk++;
				}
			}
			npaths[wk++] = jniPath;
			npaths[wk++] = jniServerPath;
		}
	 
		if(hasJni && hasServer) {
			Console::WriteLine("Path is OK!");
			return 0;
		}

		nvalue = String::Join(";", npaths);
	}

	Console::WriteLine("Path (fixed) = {0}\n", nvalue); 

	env->SetValue(path, nvalue, RegistryValueKind::ExpandString);
    //Console::WriteLine("Path (after) = {0}", env->GetValue(path));

	Console::WriteLine("You must REBOOT your system before attempting to install Pipeline Job Manager again!\n");
	Console::WriteLine("Press <ENTER> to continue...");
	Console::ReadLine();

    return 1;
}
