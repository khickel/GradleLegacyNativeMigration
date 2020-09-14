#include <windows.h>
#include <stdio.h>
#include <direct.h>
#include <io.h>
#include <time.h>

#include <kernelspecs.h>

#include "lib_one.h"
#include "CamelCase.h"

int main(int argc, char *argv[]) {
  printf("server1_1.cpp calling lib_one_func_one three times.\n");
  lib_one_func_one(3, "server_1");
}
