// This tells the TutIncludes to insert the code for tut_main
// into this class.  G++ Tutorial linking really requires
// everything be in .CPP files for eacy access, so this is my hack.
#define VS_TUT_DEFINE_MAIN
#include <TutIncludes.h>

int
main(int argc, const char* argv[])
{
  int retval = tut_main(argc, argv);
  // We want one test to actually fail here to make
  // sure we're reporting the right values.
  return retval!=1;
}


