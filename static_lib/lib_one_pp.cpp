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

#include <boost/lambda/lambda.hpp>
#include <iostream>
#include <iterator>
#include <algorithm>

int lib_one_cpp_func_one(int i) {
  return i*2;
}

int lib_one_cpp_func_two(void)
{
    using namespace boost::lambda;
    typedef std::istream_iterator<int> in;

    std::for_each(
        in(std::cin), in(), std::cout << (_1 * 3) << " " );

    return 0;
}
