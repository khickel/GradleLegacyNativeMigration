#ifdef WIN32
#include <windows.h>
#include <direct.h>
#include <io.h>
#include <kernelspecs.h>
#endif

#include <stdio.h>
#include <time.h>

#include "lib_one.h"
#include "CamelCase.h"

int main(int argc, char *argv[]) {
  printf("server1_1.cpp calling lib_one_func_one three times.\n");
  lib_one_func_one(3, "server_1");
}
