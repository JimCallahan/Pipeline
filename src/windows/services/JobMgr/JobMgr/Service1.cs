using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Diagnostics;
using System.Linq;
using System.ServiceProcess;
using System.Text;

namespace JobMgr
{
    public partial class Service1 : ServiceBase
    {
        public Service1()
        {
            InitializeComponent();
        }

        protected override void OnStart(string[] args)
        {
            this.process1.Start();
            this.process1.PriorityClass = ProcessPriorityClass.AboveNormal;
        }

        protected override void OnStop()
        {
            this.process1.Kill();
            this.process1.WaitForExit(30000); // 30-sec
        }
    }
}
