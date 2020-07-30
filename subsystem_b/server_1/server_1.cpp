#include <stdio.h>

#include <kernelspecs.h>

#include "lib_one.h"

int main(int argc, char *argv[]) {
  printf("server1_1.cpp calling lib_one_func_one three times.\n");
  lib_one_func_one(3, "server_1");
}
