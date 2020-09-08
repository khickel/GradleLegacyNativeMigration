#include <stdio.h>

#ifdef __cplusplus
extern "C" {
#endif

  int lib_one_func_one(int i, char *descr) {
    int j;
    for(j=0; j < i; j++) {
      printf("lib_one_func_one: idx=%d, descr=%s.\n", j, descr);
    }
    return 0;
  }

#ifdef __cplusplus
}
#endif

