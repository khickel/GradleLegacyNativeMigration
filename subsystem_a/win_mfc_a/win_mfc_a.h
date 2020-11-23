
// win_mfc_a.h : main header file for the PROJECT_NAME application
//

#pragma once

#ifndef __AFXWIN_H__
	#error "include 'pch.h' before including this file for PCH"
#endif

#include "resource.h"		// main symbols


// CwinmfcaApp:
// See win_mfc_a.cpp for the implementation of this class
//

class CwinmfcaApp : public CWinApp
{
public:
	CwinmfcaApp();

// Overrides
public:
	virtual BOOL InitInstance();

// Implementation

	DECLARE_MESSAGE_MAP()
};

extern CwinmfcaApp theApp;
