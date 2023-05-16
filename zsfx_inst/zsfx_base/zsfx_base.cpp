// License Apache 2.0

#include "stdafx.h"
#include "zsfx_base.h"
#include "zsfx_base_dlg.h"
#include "zsfx_base_inst.h"
#include "parms.h" // Supplied by zsfx_one or zsfx_two

BEGIN_MESSAGE_MAP(c_zsfx_app, CWinApp)
   //{{AFX_MSG_MAP(c_zsfx_app)
      // NOTE - the ClassWizard will add and remove mapping macros here.
      //    DO NOT EDIT what you see in these blocks of generated code!
   //}}AFX_MSG
   ON_COMMAND(ID_HELP, CWinApp::OnHelp)
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// c_zsfx_app construction

c_zsfx_app::c_zsfx_app()
{
   // Place all significant initialization in InitInstance
}

/////////////////////////////////////////////////////////////////////////////
// The one and only c_zsfx_app object

c_zsfx_app theApp;

/////////////////////////////////////////////////////////////////////////////
// c_zsfx_app initialization

BOOL c_zsfx_app::InitInstance()
{
   // Standard initialization

   char *errmsg;

   c_zsfx_dlg dlg;
   m_pMainWnd = &dlg;

   if ((errmsg = check_access()) != NULL)
   {
      // An installation error exists. */

      AfxMessageBox(errmsg);
      exit(0);
      return FALSE;
   }

   int nResponse = dlg.DoModal();

   // This performs various checks of the installation. */

   if (nResponse == IDOK)
   {
      // Place code here to handle when the dialog is
      // dismissed with OK
   }
   else if (nResponse == IDCANCEL)
   {
      // Place code here to handle when the dialog is
      // dismissed with Cancel
   }

   // Since the dialog has been closed, return FALSE so that we exit the
   // application, rather than start the application's message pump.

   return FALSE;
}
