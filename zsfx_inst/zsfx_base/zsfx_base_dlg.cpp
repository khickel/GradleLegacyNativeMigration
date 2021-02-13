
#include "stdafx.h"
#include <winnetwk.h>
#include <ShlObj.h>
#include <io.h>

#include "zsfx_base.h"
#include "zsfx_base_dlg.h"
#include "zsfx_base_inst.h"
#include "parms.h"  // Supplied by zsfx_one or zsfx_two


/////////////////////////////////////////////////////////////////////////////

// c_zsfx_dlg dialog

c_zsfx_dlg::c_zsfx_dlg(CWnd* pParent /*=NULL*/)
    : CDialog(c_zsfx_dlg::IDD, pParent)
{
  //{{AFX_DATA_INIT(c_zsfx_dlg)
  m_installation_directory_value = _T("");
  m_program_description_value = _T(PROGRAM_DESCR);
  m_status_line_value = _T("");
  //}}AFX_DATA_INIT
  // Note that LoadIcon does not require a subsequent DestroyIcon in Win32
}
/////////////////////////////////////////////////////////////////////////////

void c_zsfx_dlg::DoDataExchange(CDataExchange* pDX)
{
  CDialog::DoDataExchange(pDX);
  //{{AFX_DATA_MAP(c_zsfx_dlg)
  DDX_Control(pDX, IDC_PROGRESS1, m_progress_control);
  DDX_Control(pDX, Installation_Directory, m_install_directory_control);
  DDX_Control(pDX, IDCANCEL, m_cancel_control);
  DDX_Control(pDX, Browse, m_browse_control);
  DDX_Control(pDX, Install, m_install_control);
  DDX_Control(pDX, Finish, m_finish_control);
  DDX_Text(pDX, Installation_Directory, m_installation_directory_value);
  DDV_MaxChars(pDX, m_installation_directory_value, 256);
  DDX_Text(pDX, Program_Description, m_program_description_value);
  DDX_Text(pDX, Status_Line, m_status_line_value);
  //}}AFX_DATA_MAP
}

BEGIN_MESSAGE_MAP(c_zsfx_dlg, CDialog)
//{{AFX_MSG_MAP(c_zsfx_dlg)
ON_WM_PAINT()
ON_WM_QUERYDRAGICON()
ON_BN_CLICKED(Finish, OnFinish)
ON_BN_CLICKED(Install, OnInstall)
ON_BN_CLICKED(Browse, OnBrowse)
ON_WM_TIMER()
ON_WM_SHOWWINDOW()
ON_WM_KEYUP()
//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// c_zsfx_dlg message handlers

BOOL c_zsfx_dlg::OnInitDialog()
{
  char message[100];

  CDialog::OnInitDialog();

  // Set the icon for this dialog.  The framework does this automatically
  //  when the application's main window is not a dialog

  SetIcon(m_hIcon, TRUE);         // Set big icon
  SetIcon(m_hIcon, FALSE);      // Set small icon

  sprintf(message, "Version: %s - Build Date: %s %s\n", 
                   PROGRAM_VERSION, __DATE__, __TIME__);

  m_status_line_value = _T(message);
  UpdateData(FALSE);

  if( !init_install_path() )
  {
    AfxMessageBox( "Cannot Determine installation path.\n\nMake sure the installation succeeded." );
    EndDialog(-1);
  }

  return TRUE;  // return TRUE  unless you set the focus to a control
}

/////////////////////////////////////////////////////////////////////////////

// If you add a minimize button to your dialog, you will need the code below
//  to draw the icon.  For MFC applications using the document/view model,
//  this is automatically done for you by the framework.

void c_zsfx_dlg::OnPaint() 
{
  if (IsIconic())
  {
    CPaintDC dc(this); // device context for painting

    SendMessage(WM_ICONERASEBKGND, (WPARAM) dc.GetSafeHdc(), 0);

    // Center icon in client rectangle
    int cxIcon = GetSystemMetrics(SM_CXICON);
    int cyIcon = GetSystemMetrics(SM_CYICON);
    CRect rect;
    GetClientRect(&rect);
    int x = (rect.Width() - cxIcon + 1) / 2;
    int y = (rect.Height() - cyIcon + 1) / 2;

    // Draw the icon
    dc.DrawIcon(x, y, m_hIcon);
  }
  else
  {
    CDialog::OnPaint();
  }
}

/////////////////////////////////////////////////////////////////////////////

// The system calls this to obtain the cursor to display while the user drags
//  the minimized window.
HCURSOR c_zsfx_dlg::OnQueryDragIcon()
{
  return (HCURSOR) m_hIcon;
}
/////////////////////////////////////////////////////////////////////////////

void c_zsfx_dlg::OnFinish() 
{
  launch_setup_program();
  PostQuitMessage(0);
  return;
}
/////////////////////////////////////////////////////////////////////////////

void c_zsfx_dlg::OnBrowse() 
{
  BROWSEINFO  browse_info;
  LPITEMIDLIST idlist;
  static char selected_folder[MAX_PATH];

  browse_info.hwndOwner = AfxGetMainWnd () -> GetSafeHwnd();
  browse_info.pidlRoot  =  NULL;
  browse_info.pszDisplayName  = selected_folder;
  browse_info.lpszTitle       = "Select Install Folder";
  browse_info.ulFlags         = 
    BIF_RETURNONLYFSDIRS | BIF_DONTGOBELOWDOMAIN;
  browse_info.lpfn            = NULL;
  browse_info.lParam          = NULL;

  idlist = SHBrowseForFolder(&browse_info);
  SHGetPathFromIDList(idlist, selected_folder);
  if (strlen(selected_folder) == 0)
  {
    // Do nothing. 
  }
  else
  {
    set_install_path(selected_folder);
    m_installation_directory_value = _T(selected_folder);
  }
  UpdateData(FALSE);

  return;
}
///////////////////////////////////////////////////////////////////////

int c_zsfx_dlg::init_install_path(void)
{
  char instpath[MAX_PATH];

  if( !get_install_path(instpath) )
  {
    m_installation_directory_value = _T("WARNING - CANNOT DETERMINE INSTALLATION PATH");
    UpdateData(FALSE);
    return 0;
  }

  m_installation_directory_value = _T(instpath);
  UpdateData(FALSE);
  return 1;
}
///////////////////////////////////////////////////////////////////////

void c_zsfx_dlg::handle_error(char *message)
{
  char buffer[1000];
  strcpy(buffer, "\n\nCANNOT CONTINUE WITH INSTALLATION.\n\n");
  strcat(buffer, message);
  strcat(buffer, "\nCorrect problem and re-execute install when ready.\n");
  strcat(buffer, "\n");
  AfxMessageBox(buffer);

  m_cancel_control.EnableWindow(TRUE);
  m_status_line_value = 
    _T("INSTALLATION FAILED. Click \"Cancel\" To Exit Program.");
  UpdateData(FALSE);
  return;
}
/////////////////////////////////////////////////////////////////////////////

void c_zsfx_dlg::OnInstall() 
{
  char instpath[MAX_PATH];
  m_install_directory_control.GetWindowText(instpath, MAX_PATH);

  set_install_path(instpath);

  m_installation_directory_value = _T(instpath);
  m_finish_control.EnableWindow(FALSE);
  m_install_control.EnableWindow(FALSE);
  m_browse_control.EnableWindow(FALSE);
  m_cancel_control.EnableWindow(FALSE);
  m_install_directory_control.EnableWindow(FALSE);
  m_status_line_value = _T("Running unzip operation...");

  ::SetTimer(m_hWnd, 0, 1000, NULL);

  UpdateData(FALSE);

  return;
   
}
/////////////////////////////////////////////////////////////////////////////

void c_zsfx_dlg::OnTimer(UINT nIDEvent) 
{
  static int state = 0;
  char *message;
  char this_msg[1000];
  static int pos = 0;

  if (state == 0)
  {
    const char* inst_path = (LPCTSTR) m_installation_directory_value;

    state++;
    m_status_line_value = _T("Checking system...");
    UpdateData(FALSE);
    Sleep(1000);
    message = check_access();
    if (message != NULL)
    {
      KillTimer(nIDEvent);
      handle_error(message);
    } 
    // Check and use the configuration path that now exists in 
    // the browse dialog. Set the working directory, and display
    // and error message if this fails. 
   
    message = set_working_directory ((char*) inst_path);
    if (message != NULL)
    {
      KillTimer(nIDEvent);
      handle_error(message);
    } 
    return;
  }   
  else if (state == 1)
  {
    // Extract the archive. 

    const char* inst_path = (LPCTSTR) m_installation_directory_value;

    state++;
    if (stricmp(SERVICE_NAME, "none") == 0)
    {
      m_status_line_value = _T("Checking existing services. Wait...");
      UpdateData(FALSE);
      Sleep(1000);
    }
    else if (access((char*) inst_path, 0) == 0)
    {
      // Existing installation
   
      m_status_line_value = _T("Stopping existing installation. Wait...");
      UpdateData(FALSE);
      stop_existing_services();
    }
    else
    {
      // Existing installation, but no install path.
      m_status_line_value = _T("Note - New installation will be created.");
      UpdateData(FALSE);
      Sleep(1000);
    }
  }  
  else if (state == 2)
  {
    // Extract the archive package using the boundary marker. Note 
    // this step extracts the Info-Zip SFX file, which is executed
    // in the next step to actually extract files. 

    state++;

    m_status_line_value = _T("Extracting archive Zip package - Wait...");
    UpdateData(FALSE);
    message = extract_archive();
    if (message != NULL)
    {
      Sleep(1000);
      KillTimer(nIDEvent);
      handle_error(message);
    }
    return;
  }
  else if (state == 3)
  {
    // Unzip files

    state++;

    // Enable the progress bar here. 

    m_progress_control.ShowWindow(SW_SHOW);
    m_progress_control.SetPos(10);

    // Begin unzip operation in background

    m_status_line_value = _T("Unzipping and installing files - Wait..");
    UpdateData(FALSE);
    Sleep(1000);

    // This actuall performs the work of extracting files

    message = extract_files();
    if (message != NULL)
    {
      KillTimer(nIDEvent);
      handle_error(message);
    }
    return;
  }
  else if (state < 300)
  {
    // Wait a max of five seconds for files to be extracted

    state++;
    if (pos < 90)
    {
      pos += 10;
    }
    m_progress_control.SetPos(pos);
    UpdateData(FALSE);
    Sleep(1000);

    if (program_is_finished() && pos == 90 )
    {
      m_progress_control.SetPos(100);
      state = 99999; // Exit this loop.
      UpdateData(FALSE);
      Sleep(1000);
    }
    else if (state > 60)
    {
      m_status_line_value = 
        _T("Operation is delayed/slow - Wait..");
    }
    return;
  }
  else if (! program_is_finished())
  {
    // We waited for five minutes. 

    Sleep(1000);
    KillTimer(nIDEvent);
    message = "Operation took too long.";
    handle_error(message);
    state = 99999; // Exit this loop.
    UpdateData(FALSE);
    Sleep(1000);
  }
  else
  {
    // Finish the procedure.

    KillTimer(nIDEvent);
    state++;
    cleanup_run_program();

#     ifdef RESTART_SERVICE

    // If this is in the Parm file, restart the service before 
    // finishing the program. 

    m_status_line_value = _T("Restarting Services...");
    start_services();
    UpdateData(FALSE);
    Sleep(1000);
#     endif

    m_finish_control.EnableWindow(TRUE);
    m_install_control.EnableWindow(FALSE);
    m_browse_control.EnableWindow(FALSE);
    m_cancel_control.EnableWindow(FALSE);
    m_progress_control.ShowWindow(SW_HIDE);

    if ((message = check_successful_finish()) != NULL)
    {
      handle_error(message);
      return;
    }
    strcpy(this_msg, "Files successfully unzipped - ");
    strcat(this_msg, "Click \"Next\" to continue.");
    m_status_line_value = _T(this_msg);
    UpdateData(FALSE);

    return;
  }
  return; // Not reached. 
}
///////////////////////////////////////////////////////////////////////

void c_zsfx_dlg::OnShowWindow(BOOL bShow, UINT nStatus) 
{
  return;
}
///////////////////////////////////////////////////////////////////////

void c_zsfx_dlg::OnKeyUp(UINT nChar, UINT nRepCnt, UINT nFlags) 
{
  return;
}
