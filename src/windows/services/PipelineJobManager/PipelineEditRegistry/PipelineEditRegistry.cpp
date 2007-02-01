// PipelineEditRegistry.cpp : main project file.

#include "stdafx.h"

using namespace System;
using namespace Microsoft::Win32;

int main(array<System::String ^> ^args)
{
	if(args->Length != 1) {
		Console::WriteLine("usage: PipelineEditRegistry jre-bin\n");
		return 1;
	}
			
	String^ jniPath = args[0];
	String^ jniServerPath = String::Format("{0}\\server", jniPath);

	RegistryKey^ hklm = Registry::LocalMachine;
	RegistryKey^ env = hklm->OpenSubKey("SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment",
										RegistryKeyPermissionCheck::ReadWriteSubTree);

	String^ value = (String^) env->GetValue("Path");
	Console::WriteLine("Path (before) = {0}", value); 

	array<String^>^ paths = value->Split(';');

	// DEBUG
	Console::WriteLine("Path COMPONENTS:");
	for(int i=0; i<paths->Length; i++) {
		Console::WriteLine("  {0}", paths[i]);
	}
	// DEBUG

	int cnt = 2;
    for(int i=0; i<paths->Length; i++) {
		if(!paths[i]->Equals(jniPath) && !paths[i]->Equals(jniServerPath))
			cnt++;
	}
 
	array<String^>^ npaths = gcnew array<String^>(cnt);
	int wk=0;
    for(int i=0; i<paths->Length; i++) {
		if(!paths[i]->Equals(jniPath) && !paths[i]->Equals(jniServerPath)) {
			npaths[wk] = paths[i];
			wk++;
		}
	}
	npaths[wk++] = jniPath;
	npaths[wk++] = jniServerPath;
 
	String^ nvalue = String::Join(";", npaths);
	Console::WriteLine("Path (fixed) = {0}", nvalue); 

	env->SetValue("Path", nvalue, RegistryValueKind::ExpandString);
    Console::WriteLine("Path (after) = {0}", env->GetValue("Path"));

    return 0;
}
