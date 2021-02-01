
#define PROGRAM_VERSION "1.1.00"

#define UNINSTALL_DISPLAY_NAME_STANDARD "GLM UnInstall Std"
#define UNINSTALL_DISPLAY_NAME_WINTOOLS "GLM UnInstall WT"
#define UNINSTALL_PROGRAM_NAME "douinst.exe"

#define PROGRAM_NAME          UNINSTALL_DISPLAY_NAME_STANDARD
#define AGENT_PROGRAM_NAME    UNINSTALL_DISPLAY_NAME_WINTOOLS

#ifndef CSIDL_COMMON_DESKTOPDIRECTORY
#define CSIDL_COMMON_DESKTOPDIRECTORY 0x0019
#endif

char *check_access();
char *set_working_directory(char *inst_path);
char *extract_archive();
char *extract_files();
void set_install_path(char *path);
int get_install_path(char *path);
char *run_program(char *syscmd, int show_window, char *logfile );
char *run_program_wait(char *syscmd);
int program_is_finished();
void cleanup_run_program();
void stop_existing_services();
void start_services();
int get_registered_location(char *install_location);
char *check_successful_finish();
void launch_setup_program();
