
#ifdef WIN32
#include <windows.h>
#include <direct.h>
#include <io.h>
#endif

#include <stdio.h>
#include <time.h>

#include "lib_one.h"

#ifdef WIN32
// Illustrate the gradle issue on windows where the case of the filename in the
// include statement doesn't match the case on disk.
#include "Camelcase.h"
#else
#include "CamelCase.h"
#endif

int main(int argc, char *argv[]) {
  printf("util_1.cpp calling lib_one_func_one twice.\n");
  lib_one_func_one(2, "util_1");
}
