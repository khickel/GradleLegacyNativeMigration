#include "StdAfx.h"
#include <Windows.h>
#include <winreg.h>
#include <winsvc.h>
#include <winnetwk.h>
#include <ShlObj.h>
#include <stdio.h>
#include <winsock.h>
#include <direct.h>
#include <io.h>
#include <objbase.h>
#include <WinNls.h>

#include "zsfx_base.h"
#include "zsfx_base_dlg.h"
#include "zsfx_base_inst.h"
#include "parms.h" // Supplied by build script

static char g_errmsg[500];
static char g_install_path[MAX_PATH];
static char g_temp_exec_path[MAX_PATH];
static int  g_error_count; 
static int getline(FILE *fd, char *line);
static int process_php_file(char *filename, char *shebang);
static void format_and_check_log_file(char *infile, char *outfile);
static int getline(FILE *fd, char *line);
static int stristr(char *str1, char *str2);
static int create_shellink(char *linkfile, char *target);
static PROCESS_INFORMATION g_proc_info;
static HANDLE g_log_handle;
void stop_service_by_short_name(const char *short_name);
void start_service_by_short_name(const char *short_name);

//////////////////////////////////////////////////////////////////////

char *check_access()
{
   SC_HANDLE   handle;
   HKEY   parent_key;
   HKEY   child_key;
   DWORD  reserved = 0;
   int    ret_status;
   char  this_path[MAX_PATH];
   FILE *fd;

   // Check ability to get own program path, required when 
   // extracting files. */

   GetModuleFileName(NULL, this_path, MAX_PATH);
   if ((fd = fopen(this_path, "rb")) == NULL)
   { 
      strcpy(g_errmsg, "ERROR: Cannot Locate Own Program Path.\n");
      strcat(g_errmsg, this_path);
      strcat(g_errmsg, 
      "\nTry saving or moving program to desktop and then re-execute.");
      return(g_errmsg);
   }
   fclose(fd);

   // Verify access to SCM

   handle = OpenSCManager(NULL,  NULL, SC_MANAGER_ALL_ACCESS);
   if (handle)
   {
      CloseServiceHandle(handle);
   }
   else
   {
      strcpy(g_errmsg,
         "NOTE: The installer is not running an an administrator.\n");
      strcat(g_errmsg, 
         "Administrative access is required to continue.\n");
      strcat(g_errmsg, 
         "Try launching program with \"Run as administrator\" option.");
      return(g_errmsg);
   }
   // Check permissions to registry

   parent_key = HKEY_LOCAL_MACHINE;
   ret_status = RegOpenKeyEx ( HKEY_LOCAL_MACHINE, "SOFTWARE",
      reserved, KEY_READ|KEY_WRITE, &child_key);
   if (ret_status != ERROR_SUCCESS)
   {
      strcpy(g_errmsg,
         "You Have No Access To The Windows Registry.\n");
      strcat(g_errmsg,
         "Administrative access is required to continue.\n");
      strcat(g_errmsg,
         "Try launching program with \"Run as administrator\" option.");
      return(g_errmsg);
   }
   RegCloseKey(child_key);
   RegCloseKey(parent_key);
   return(NULL);
}

//////////////////////////////////////////////////////////////////////
char *set_working_directory(char *inst_path)
{
   char test_file[MAX_PATH];
   char install_path_copy[MAX_PATH];
   char temp_path[MAX_PATH] = "";
   char *current_directory;
   FILE *fd;

   // Check installation path.

   strcpy(g_install_path, inst_path);

   if (strlen(inst_path) < 4)
   {
      strcpy(g_errmsg, "Cannot Access Selected Installation Directory.\n");
      strcat(g_errmsg, "An empty installation path was specified.");
      return(g_errmsg);
   }

   if (! isalpha(g_install_path[0]) || g_install_path[1] != ':' || g_install_path[2] != '\\')
   {
      strcpy(g_errmsg, "Invalid Path Specified. A drive letter is required.\n");
      strcat(g_errmsg, "Start path with drive letter (Example: \"C:\\\"");
      return(g_errmsg);
   }

   if (stristr(g_install_path, ".."))
   {
      strcpy(g_errmsg, "Invalid Path Specified. Cannot have \"..\" characters.\n");
      strcat(g_errmsg, "Specify a standard, non-relative path.");
      return(g_errmsg);
   }

   strcpy(install_path_copy, g_install_path);
   current_directory = strtok(install_path_copy, "\\");
   
   if (current_directory == NULL)
   {
	   strcpy(g_errmsg, "Invalid Path Specified. \n");
	   return(g_errmsg);
   }
   else
   {
	   while (current_directory != NULL)
	   {
		   strcat(temp_path, current_directory);
		   strcat(temp_path, "\\");

		   if (access(temp_path, 00) != 0)	// Folder does not exist
		   {
			   if (mkdir(temp_path) != 0)	// Create folder, 0 is success
			   {
				   strcpy(g_errmsg, "Invalid Path Specified. Could not create full path.\n");
				   strcat(g_errmsg, temp_path);
				   return(g_errmsg);
			   }
		   }

		   current_directory = strtok(NULL, "\\");
	   }
   }
   
   if (access(g_install_path, 06) != 0)
   {
      strcpy(g_errmsg,"No Permissions To Installation Directory.\n");
      strcat(g_errmsg, g_install_path);
      strcat(g_errmsg, "\n");
      strcat(g_errmsg, "Check path existence and permissions.");
      return(g_errmsg);
   }
   // Working directory is set here. 
   // Return an error if the working directory cannot be set. 

   if (chdir(g_install_path) != 0)
   {
      strcpy(g_errmsg,
         "Cannot Change Working Directories To Install Directory.\n");
      strcat(g_errmsg, g_install_path);
      strcat(g_errmsg, "\n");
      strcat(g_errmsg, 
         "Check path existence and permissions.");
      return(g_errmsg);
   }
   // Finally, test permissions to the working directory.
   // Create a temporary file, and return if an error occurs. 

   sprintf(test_file, "%s\\.install", g_install_path);
   if ((fd = fopen(test_file, "w")) == NULL)
   {
      strcpy(g_errmsg,
         "Cannot Write To Installation Directory.\n");
      strcat(g_errmsg, g_install_path);
      strcat(g_errmsg, "\n");
      strcat(g_errmsg, 
         "Check path and permissions.");
      return(g_errmsg);
   }
   fclose(fd);
   unlink(test_file);

   return(NULL);
}
//////////////////////////////////////////////////////////////////////

char *extract_archive()
{
   FILE *fdin;
   FILE *fdout;
   char header_found = 0;
   char string[20];
   char marker[20];
   unsigned char c;
   int i, size;
   char this_path[MAX_PATH];

   /* Marker used to separate payload from program. */

   memset(marker, 0, 20);
   memset(string, 0, 20);
   strcpy(marker, "\r\n-#(");
   strcat(marker, "Boundary");
   strcat(marker, ")#-\r\n");

   unlink("./_pkg_.exe");

   GetModuleFileName(NULL, this_path, MAX_PATH);
   if ((fdin = fopen(this_path, "rb")) == NULL)
   { 
      strcpy(g_errmsg, "ERROR: Cannot Locate Own Program Path.\n");
      strcat(g_errmsg, this_path);
      strcat(g_errmsg, "\nCheck system permissions and execution path.");
      return(g_errmsg);
   }
   /* Open up this file */

   strcpy(g_temp_exec_path, "./_pkg_.exe");
   unlink(g_temp_exec_path);

   if ((fdout = fopen(g_temp_exec_path, "wb")) == NULL)
   {
      fclose(fdin);
      strcpy(g_errmsg, "ERROR: Cannot create _pkg_.exe.\n");
      strcat(g_errmsg, g_temp_exec_path);
      strcat(g_errmsg, "\nCheck system permissions and execution path.");
      return(g_errmsg);
   }
   fseek(fdin, 25000, SEEK_SET);
   size = 0;
   while (! feof(fdin) && ! header_found)
   {
      for (i = 0; i < 17; i++) string[i] = string[i + 1];
      c = fgetc(fdin);
      string[17] = c;
     
      if (memcmp(string, marker, 17) == 0)
      {
         header_found = 1;
         while (((c = fgetc(fdin)) == '\r'
            ||  c == '\n' 
            ||  c == ' ')
            && ! feof(fdin));
         ungetc(c, fdin);
      }
   }
   size = 0;
   if (header_found)
   {
      while (! feof(fdin))
      {
         c = fgetc(fdin);
         fputc(c, fdout);
         size++;
      }
   }
   fclose(fdin);
   fclose(fdout);
   if (! header_found) 
   {
      unlink(g_temp_exec_path);
      strcpy(g_errmsg,"ERROR: Package Boundary Not Found.\n");
      strcat(g_errmsg,"Package is corrupt.\n");
      strcat(g_errmsg,"Acquire new installation package.");
      return(g_errmsg);
   }
   else if (size < 1000)
   {
      unlink(g_temp_exec_path);
      strcpy(g_errmsg,"ERROR: Package Size Is Incorrect.\n");
      strcat(g_errmsg,"Package is corrupt.\n");
      strcat(g_errmsg,"Acquire new installation package.");
      return(g_errmsg);
   }
   return(NULL); // Successful
}
//////////////////////////////////////////////////////////////////////

char *extract_files()
{
   char syscmd [1000];

   if (access(g_install_path, 0) != 0)
   {
      char tmppath[MAX_PATH];
      int i;
      strcpy(tmppath, g_install_path);
      for (i = 0; tmppath[i]; i++)
      {
         if (tmppath[i] == '/') tmppath[i] = '\\';
      }
      mkdir (tmppath);
   }
   if (access(g_install_path, 0) != 0)
   {
      unlink(g_temp_exec_path);
      strcpy(g_errmsg,"ERROR: Cannot Make Install Path.\n");
      strcpy(g_errmsg,"Path: ");
      strcat(g_errmsg,g_install_path);
      strcat(g_errmsg,"\nCheck permissions before continuing.");
      return(g_errmsg);
   }
   if (access(g_temp_exec_path, 0) != 0)
   {
      unlink(g_temp_exec_path);
      strcpy(g_errmsg,"ERROR: Cannot Find Package Extract.\n");
      strcat(g_errmsg,"Package is corrupt.\n");
      strcat(g_errmsg,"Acquire new installation package.");
      return(g_errmsg);
   }
   // Run the program here. 
   // Note that the output of the command is sent to the _pkg_.log
   // file, which is later checked and copied to the INSTALL.log 
   // on successful completion.

   sprintf(syscmd, 
      "%s -o -d \"%s\"", g_temp_exec_path, g_install_path);

   return(run_program(syscmd, SW_HIDE, ".\\_pkg_.log"));
}
//////////////////////////////////////////////////////////////////////

void set_install_path(char *string)
{
   char tmpstr[MAX_PATH];
   int len, i;

   // Strip leading trailing blanks. 

   len = 0;
   while (isspace(string[len])) len++;
   if (len >  0)
   {
      strcpy(tmpstr, &string[len]);
      strcpy(string, tmpstr);
   }
   if (*string == '\0')
   {
      return;
   }
   len = strlen(string); len--;
   while (len > 0 && isspace(string[len])) len--;
   string[len + 1] = '\0';
 
   // Convert to back slashes. 

   for (i = 0; string[i]; i++)
   {
      if (string[i] == '/') string[i] = '\\';
   }
   // Chop any terminal slash. 

   len = strlen(string);
   if (len < 4)
   {
      strcpy(g_install_path, DEFAULT_PATH);
      return;
   }
   else if (string[len - 1] == '\\')
   {
      string[len - 1] = '\0';
      len--;
   }
   strcpy(g_install_path, string);
   return;
}
//////////////////////////////////////////////////////////////////////

char *run_program(
        char *syscmd, 
        int show_window, 
        char *logfile )
{
    // Run the specified program, redirecting standard output to 
    // the specified log file, and either displaying or hiding the 
    // background process.

    // If logfile is NULL, no output is generated.

    static char cmdline[1024];
    int status;
    STARTUPINFO         startup_info;
    SECURITY_ATTRIBUTES security_attrib;

    // Setup the following streams and environmental values. 
    // Note that if a logfile is specified, some of these values are
    // later overwritten. 

    GetStartupInfo(&startup_info);
    startup_info.dwFlags = STARTF_USESHOWWINDOW;
    startup_info.wShowWindow = show_window;
    startup_info.hStdInput   = NULL;
    startup_info.hStdOutput  = NULL;
    startup_info.hStdError   = NULL;

    security_attrib.nLength = sizeof(SECURITY_ATTRIBUTES);
    security_attrib.lpSecurityDescriptor   = NULL;
    security_attrib.bInheritHandle = TRUE;

    if (logfile != NULL)
    {
        // Output to the specified log file. 

        unlink(logfile);
        startup_info.dwFlags = STARTF_USESHOWWINDOW | STARTF_USESTDHANDLES;
        g_log_handle = CreateFile 
            (
             logfile, 
             GENERIC_READ | GENERIC_WRITE,
             FILE_SHARE_READ | FILE_SHARE_WRITE,
             &security_attrib,
             CREATE_ALWAYS,
             FILE_ATTRIBUTE_NORMAL,
             NULL
            );
        startup_info.hStdOutput  = g_log_handle;
        startup_info.hStdError   = g_log_handle;
    }

    strcpy(cmdline, syscmd);
    status = CreateProcess(
            NULL,                     /* Executable module. */
            cmdline,
            NULL,                     /* Security attributes. */
            NULL,                     /* Thread Attributes. */
            TRUE,                     /* Inherit handles. */
            CREATE_NEW_CONSOLE,
            NULL,                     /* Environment. */
            g_install_path,          /* Working directory. */
            &startup_info,
            &g_proc_info);

    // Process is launched.

    if (status != TRUE)
    {
        // Bad status. The program will terminate. 
        cleanup_run_program();
        strcpy(g_errmsg,"ERROR: Cannot Execute Self Extractor.\n");
        strcat(g_errmsg,"Command Failed. Package may be corrupt.\n");
        strcat(g_errmsg,"Acquire new installation package.");
        return(g_errmsg);
    }

    return(NULL);
}
//////////////////////////////////////////////////////////////////////

char *run_program_wait(char *syscmd)
{
   // Similar to the above, but wait for the program to complete. 
   // We wait a max of 60 seconds, which is normally sufficient 
   // to stop services. 

   char *message;
   int retry;

   if ((message = run_program(syscmd, SW_HIDE, NULL)) != NULL)
   {
      cleanup_run_program();
      return(message);
   }   
   for (retry = 0; retry < 60; retry++)
   {
      if (program_is_finished())
      {
         break;
      }      
      Sleep(1000);
   }
   cleanup_run_program();
   if (retry >= 60)
   {
      strcpy(g_errmsg,"ERROR: Cannot Execute System Command.\n");
      strcat(g_errmsg,syscmd);
      strcat(g_errmsg,"\nCheck paths and permissions.");
      return(g_errmsg);
   }
   return(NULL);
}
//////////////////////////////////////////////////////////////////////

int program_is_finished()
{
   // Return 1 when finished. Otherwise, return zero

   unsigned long exit_code;

   GetExitCodeProcess(g_proc_info.hProcess, &exit_code);
   if (exit_code == STILL_ACTIVE)
   {
      return(0);
   }
   return(1); // Program is finished.
}
//////////////////////////////////////////////////////////////////////

void cleanup_run_program()
{
   // Cleanup.

   if (g_proc_info.hProcess > (HANDLE) 0)
   {
      CloseHandle(g_proc_info.hProcess);
   }
   if (g_proc_info.hThread  > (HANDLE) 0)
   {
      CloseHandle(g_proc_info.hThread);
   }
   if (g_temp_exec_path[0] != '\0')
   {
      unlink(g_temp_exec_path);
   }
   if (g_log_handle != NULL)
   {
      CloseHandle(g_log_handle);
      g_log_handle = NULL;
      format_and_check_log_file("_pkg_.log", "INSTALL.log");
      unlink("_pkg_.log");
   }
   g_proc_info.hProcess = (HANDLE) 0;
   g_proc_info.hThread  = (HANDLE) 0;
   return;
}
//////////////////////////////////////////////////////////////////////

void format_and_check_log_file(char *infile, char *outfile)
{
   FILE *fdin;
   FILE *fdout;
   char date_time[50];
   time_t ctime;
   char line[1000];

   if ((fdin  = fopen(infile, "r")) == NULL) 
   {
      return;
   }
   if ((fdout = fopen(outfile, "w")) == NULL) 
   {
      fclose(fdin);
      return;
   }
   time(&ctime);
   strftime(date_time, 50, "%a %c", localtime(&ctime));
   fprintf(fdout, "%s\n", PROGRAM_DESCR);
   fprintf(fdout, "%s\n", "FILE EXTRACTION LOG");
   fprintf(fdout, "Version: %s - Build Date: %s %s\n", 
      PROGRAM_VERSION, __DATE__, __TIME__);
   fprintf(fdout, "Installed: %s\n", date_time);

   g_error_count = 0;
   while (getline(fdin, line) != EOF)
   {
      fprintf(fdout, "%s\n", line);
      if (stristr(line, "error: ")) 
      {
         g_error_count++;
      }
      else if (stristr(line, "cannot create")) 
      {
         g_error_count++;
      }
   }
   fprintf(fdout, "END OF DATA\n");
   fclose(fdin);
   fclose(fdout);

   return;
}
//////////////////////////////////////////////////////////////////////

int get_install_path(char *instpath)
{
    // Check if we can query for an existing location for this program 
    if( get_registered_location(instpath) != 0 )
    {
        //
        // could not find a registry entry, use the default.
        //
        strcpy(instpath, DEFAULT_PATH);
    }

    return 1;
}
//////////////////////////////////////////////////////////////////////

int get_registered_location(char *install_location)
{
   BYTE   key_value[256];
   DWORD  key_type;
   DWORD  reserved = 0;
   DWORD  value_size = 256;
   HKEY   child_key; 
   HKEY   parent_key;
   char   reg_key_location[256];
   int    errcount = 0;
   int    ret_status;

   strcpy(install_location, "");

   strcpy(reg_key_location, 
      "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\");

   strcat(reg_key_location, PROGRAM_NAME);
   
   parent_key = HKEY_LOCAL_MACHINE;

   ret_status = RegOpenKeyEx (
      parent_key, 
      reg_key_location, 
      reserved,
      KEY_QUERY_VALUE,
      &child_key );

   if (ret_status != ERROR_SUCCESS)
   {
      errcount++;
      return(errcount);
   }

   parent_key = child_key;
   child_key = NULL;   

   ret_status = RegQueryValueEx (
      parent_key,
      "InstallLocation",
      NULL,
      &key_type,
      key_value,
      &value_size); 

   if (ret_status != ERROR_SUCCESS)
   {
      errcount++;
      strcpy(install_location, "");
   }
   else
   {
      // Strip "/system" path from key value. */

      int i;
      int lastslash = 0;

      for (i = 0; key_value[i]; i++)
      {
         if (key_value[i] == '\\') lastslash = i;
      }
      if (key_value[lastslash] == '\\')
      {
         key_value[lastslash] = '\0';
      }
      strcpy(install_location, (char*) key_value);
   }

   RegCloseKey(parent_key);
   return(errcount);
}  
//////////////////////////////////////////////////////////////////////

void stop_existing_services()
{
   char syscmd[200];
   if (strlen(SERVICE_NAME) == 0 || stricmp(SERVICE_NAME, "none") == 0)
   {
      // No service name was configured. Do not try to stop 
      // any service. Just return.

      return;
   }
   stop_service_by_short_name(SERVICE_NAME);

   // Also use the "net stop" command.
   sprintf(syscmd, "net stop %s", SERVICE_NAME);
   run_program_wait(syscmd);
   return;
}
//////////////////////////////////////////////////////////////////////

void start_services()
{
   if (strlen(SERVICE_NAME) == 0 || stricmp(SERVICE_NAME, "none") == 0)
   {
      // No service name was configured. Do not try to start
      // any service. Just return.

      return;
   }
   start_service_by_short_name(SERVICE_NAME);

   // Service was restarted.

   return;
}
//////////////////////////////////////////////////////////////////////

int getline(FILE *fd, char *line)
{
   char c; int i;;
   i = 0; line[i] = 0;
   if (fd == NULL) return(EOF);
   if (feof(fd))
   {
      return(EOF);
   }
   while ((int) (c = fgetc(fd)) != EOF && c != '\n')
   {
      if (c == '\r') 
      {
         continue;
      }
      else if  (i < 998)
      {
         line[i] = c; i++; line[i] = '\0';
      }
   }
   if (feof(fd))
   {
      return(EOF);
   }
   return(0);
}
//////////////////////////////////////////////////////////////////////

int stristr(char *String, char *Pattern)
{
   /* http://opensource.apple.com/ */
   /* source/CyrusIMAP/CyrusIMAP-188/cyrus_imap/lib/stristr.c */

   char *pptr, *sptr, *start;
   int  slen, plen;

   for (start = (char *)String,
      pptr  = (char *)Pattern,
      slen  = strlen(String),
      plen  = strlen(Pattern);

      /* While string length not shorter than pattern length */

      slen >= plen;

      start++, slen--)
   {
      /* Find the start of pattern in string */

      while (toupper(*start) != toupper(*Pattern))
      {
         start++;
         slen--;

         if (slen < plen)
         {
            /* The pattern is longer than the string */

            return(NULL);
         }
      }

      sptr = start;
      pptr = (char *)Pattern;

      while (toupper(*sptr) == toupper(*pptr))
      {
         sptr++;
         pptr++;

         if ('\0' == *pptr)
         {
            /* The end of pattern was found */

            return ((int) start);
         }
      }
   }
   return(NULL);
}
//////////////////////////////////////////////////////////////////////

char *check_successful_finish()
{
   if (access("INSTALL.log", 0) != 0)
   {
      strcpy(g_errmsg, "ERROR: Cannot Find Install Log.\n");
      strcat(g_errmsg, RUN_PROGRAM);
      strcat(g_errmsg, "\nCheck paths and permissions.");
      return(g_errmsg);
   }
   else if (g_error_count  != 0)
   {
      strcpy(g_errmsg, "ERRORS ENCOUNTERED DURING FILE EXTRACTION:\n");
      strcat(g_errmsg, "Check 'INSTALL.log' in install folder for details.");
      return(g_errmsg);
   }

   return(NULL);
}
//////////////////////////////////////////////////////////////////////

void launch_setup_program()
{
   char syscmd[MAX_PATH];
   sprintf(syscmd, "%s\\%s", g_install_path, RUN_PROGRAM);
   run_program(syscmd, SW_SHOW, "ServiceInstaller.log");
}
//////////////////////////////////////////////////////////////////////

void stop_service_by_short_name(const char *short_name)
{
   SC_HANDLE   handle_svc;
   SC_HANDLE   handle_mgr;
   SERVICE_STATUS service_status;
   int retry;

   handle_mgr = OpenSCManager(
      NULL,
      NULL,
      SC_MANAGER_ALL_ACCESS
      );

   if (handle_mgr == NULL)
   {
      return;
   }
   handle_svc = OpenService
      (handle_mgr, TEXT(short_name), SERVICE_ALL_ACCESS);

   if (handle_svc == NULL)
   {
      CloseServiceHandle(handle_mgr);
      return;
   }
   // Try to stop the named service 

   retry = 0;
   if ( ControlService( handle_svc,
      SERVICE_CONTROL_STOP, &service_status ) )
   {
      Sleep(500);

      // Wait for the service to stop.

      while( QueryServiceStatus( handle_svc, &service_status ) 
         && retry < 120)
      {
         if ( service_status.dwCurrentState ==
            SERVICE_STOP_PENDING )
         {
            retry++;
            Sleep(500);
         }
         else 
         {
            break;
         }
      }
   }
   CloseServiceHandle(handle_svc);
   CloseServiceHandle(handle_mgr);
   return;
}
//////////////////////////////////////////////////////////////////////

void start_service_by_short_name(const char *short_name)
{
   SC_HANDLE   handle_svc;
   SC_HANDLE   handle_mgr;

   handle_mgr = OpenSCManager(
      NULL,
      NULL,
      SC_MANAGER_ALL_ACCESS
      );

   if (handle_mgr == NULL)
   {
      return;
   }
   handle_svc = OpenService
      (handle_mgr, TEXT(short_name), SERVICE_ALL_ACCESS);

   if (handle_svc == NULL)
   {
      CloseServiceHandle(handle_mgr);
      return;
   }
   StartService(handle_svc, 0, NULL);
   CloseServiceHandle(handle_svc);
   CloseServiceHandle(handle_mgr);
   return;
}
