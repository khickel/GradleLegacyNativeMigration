#include <stdio.h>
#include "lib_one.h"

int main(int argc, char *argv[]) {
  printf("util_1.cpp calling lib_one_func_one twice.\n");
  lib_one_func_one(2, "util_1");
}
