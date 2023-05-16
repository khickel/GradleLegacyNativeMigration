#include "lib_one.h"

#include "CamelCase.h"

int c_only(int i) {

  // This is a contrived example, but there are .c files that cannot correctly be compiled in c++ mode.
  // See comment in util_1.gradle for a workaround that involves building your own gradle.
#ifdef __cplusplus
#error This file must be compiled as a C file, not a C++ file.
#endif
  return 0;
}
