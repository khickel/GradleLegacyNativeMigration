#ifdef WIN32
#include <windows.h>
#include <stdio.h>
#include <direct.h>
#include <io.h>
#endif

#include <stdio.h>
#include <time.h>

#include "lib_one.h"

#include "CamelCase.h"

int main(int argc, char *argv[]) {
  printf("util_1.cpp calling lib_one_func_one twice.\n");
  lib_one_func_one(2, "util_1");
}
