#ifdef WIN32
#include <windows.h>
#include <stdio.h>
#include <direct.h>
#include <io.h>
#endif

#include <stdio.h>
#include <time.h>

#include "lib_One.h"
#include "CamelCase.h"

int lib_one_cpp_func_one(int i) {
  return i*2;
}
