#include <windows.h>
#include <stdio.h>
#include <direct.h>
#include <io.h>
#include <time.h>

#include "lib_One.h"
#include "CamelCase.h"

#ifdef __cplusplus
extern "C" {
#endif


  int lib_one_func_one(int i, char *descr) {
    int j;
    for(j=0; j < i; j++) {
      printf("%s: idx=%d, descr=%s.\n", __FUNCTION__, j, descr);
    }
    return 0;
  }

  int lib_one_camel_case(int i, char *descr) {
    int j;
    for(j=0; j < i; j++) {
      printf("%s: idx=%d, descr=%s.\n", __FUNCTION__, j, descr);
    }
    return 0;
  }

#ifdef __cplusplus
}
#endif

