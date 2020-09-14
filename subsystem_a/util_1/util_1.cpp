#include <windows.h>
#include <stdio.h>
#include <direct.h>
#include <io.h>
#include <time.h>

#include "lib_one.h"

#include "Camelcase.h"

int main(int argc, char *argv[]) {
  printf("util_1.cpp calling lib_one_func_one twice.\n");
  lib_one_func_one(2, "util_1");
}
