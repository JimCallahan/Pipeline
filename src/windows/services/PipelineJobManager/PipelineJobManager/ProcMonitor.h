#pragma once

using namespace System;
using namespace System::IO;
using namespace System::ComponentModel;
using namespace System::Threading;
using namespace System::Diagnostics;

namespace PipelineJobManager {
	ref class ProcMonitor
	{
	public:
		ProcMonitor(Process^ proc)
		{
			pProc = proc;
		}

		void CollectOutput()
		{
			File::WriteAllText("C:\\TEMP\\PipelineJobManager.stdout", pProc->StandardOutput->ReadToEnd());
			pProc->WaitForExit();
		}

	private:
		Process^ pProc;
	};
}