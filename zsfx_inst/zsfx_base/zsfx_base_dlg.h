class c_zsfx_dlg : public CDialog
{
// Construction
public:
   c_zsfx_dlg(CWnd* pParent = NULL);   // standard constructor

// Dialog Data
   //{{AFX_DATA(c_zsfx_dlg)
   enum { IDD = IDD_ZSFX_BASE_DIALOG };
   CProgressCtrl   m_progress_control;
   CEdit   m_install_directory_control;
   CButton   m_cancel_control;
   CButton   m_browse_control;
   CButton   m_install_control;
   CButton   m_finish_control;
   CButton   m_click_control;
// BOOL   m_click_value;
   CString   m_installation_directory_value;
   CString   m_program_description_value;
   CString   m_status_line_value;
   //}}AFX_DATA

   // ClassWizard generated virtual function overrides
   //{{AFX_VIRTUAL(c_zsfx_dlg)
   protected:
   virtual void DoDataExchange(CDataExchange* pDX);   // DDX/DDV support
   //}}AFX_VIRTUAL

// Implementation
protected:
   HICON m_hIcon;

   // Generated message map functions
   //{{AFX_MSG(c_zsfx_dlg)
   virtual BOOL OnInitDialog();
   afx_msg void OnPaint();
   afx_msg HCURSOR OnQueryDragIcon();
   afx_msg void OnFinish();
   afx_msg void OnInstall();
   afx_msg void OnBrowse();
   afx_msg void OnTimer(UINT nIDEvent);
   afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
   afx_msg void OnKeyUp(UINT nChar, UINT nRepCnt, UINT nFlags);
   //}}AFX_MSG
   DECLARE_MESSAGE_MAP()
   int init_install_path(void);
   void handle_error(char *message);
};
