#ifndef TUTINCLUDES_H_
#define TUTINCLUDES_H_

#include "tut.h"
#include <string>

/*
 * Some hackery to make stringifying tokens work.
 */
#define VS_TUT_TOSTRING( X ) #X
#define VS_TUT_STRINGIFY( X ) VS_TUT_TOSTRING(X)


/*
 * This must be at the START of every test file.
 */
#define VS_TUT_TEST_DECLARATION_START namespace tut {

/*
 * And this must be at the END of every test file.
 */
#define VS_TUT_TEST_DECLARATION_END }

#define VS_TUT_TEST_REGISTER_OBJECT(__object) \
  tut::test_group<tut::__object> vs_test_factory_ ## __object( VS_TUT_STRINGIFY(VS_CPP_NAMESPACE) "::" VS_TUT_STRINGIFY(__object))

#define VS_TUT_TEST_START(__object, __num, __descr) \
  template <> template <> void tut::test_group<tut::__object>::object::test<__num>() \
  { \
    set_test_name(__descr); \
    try { \

#define VS_TUT_TEST_END(__object) \
    } catch ( std::exception & __tut_e ) { \
      VS_TUT_ENSURE(false, __tut_e.what() ); \
      throw; \
    } catch (...) { \
      VS_TUT_ENSURE(false, "Got unexpected exception"); \
      throw; \
    } \
  }

#define VS_TUT_ENSURE(__msg, __expr) \
    do { \
      std::stringstream __tut__ss; \
      __tut__ss << __msg; \
      __tut__ss << " (at "; \
      __tut__ss <<__FILE__; \
      __tut__ss <<" : "; \
      __tut__ss << __LINE__; \
      __tut__ss << ")"; \
      tut::ensure(__tut__ss.str().c_str(), (__expr)); \
    } while (0)

#define VS_TUT_ENSURE_EQUALS(msg, __a, __b) \
    do { \
      std::stringstream __tut__ss; \
      __tut__ss << msg; \
      __tut__ss << " (at "; \
      __tut__ss <<__FILE__; \
      __tut__ss <<" : "; \
      __tut__ss << __LINE__; \
      __tut__ss << ")"; \
      tut::ensure_equals(__tut__ss.str().c_str(), (__a), (__b)); \
    } while (0)

#define VS_TUT_ENSURE_DISTANCE(msg, __a, __b, __delta) \
    do { \
      std::stringstream __tut__ss; \
      __tut__ss << msg; \
      __tut__ss << " (at "; \
      __tut__ss <<__FILE__; \
      __tut__ss <<" : "; \
      __tut__ss << __LINE__; \
      __tut__ss << ")"; \
      tut::ensure_distance(__tut__ss.str().c_str(), (__a), (__b), (__delta)); \
    } while (0)

/*
 * If you define this variable BEFORE including this file
 * then the Preprocessor will insert this into your cpp file
 * with the effect that it is now the instantiatitor of
 * tut_main.
 */
#ifdef VS_TUT_DEFINE_MAIN
#include <tut/tut.hpp>
#include <tut/tut_reporter.hpp>
#include <iostream>

using std::exception;
using std::string;
using std::cout;
using std::cerr;
using std::endl;

using tut::reporter;
using tut::groupnames;

namespace tut
{
  extern test_runner_singleton runner;
}

int tut_main(int argc, const char* argv[])
{
  int retval = -1;
  reporter visi;

  if ((argc < 1) ||
      (argc > 3) ||
      (argc == 2 && (
          (string(argv[1]) == "-h") ||
          (string(argv[1]) == "-help")
          )))

  {
    cout << "Test Suite For: " VS_TUT_STRINGIFY(VS_CPP_NAMESPACE) << endl;
    cout << "Usage: " << argv[0] << " [--help] [regression] | [list] | [ group] [test]" << endl;
    cout << "  List all groups: " << argv[0] << " list" << endl;
    cout << "  Run all tests: " << argv[0] << " regression" << endl;
    cout << "  Run one group: " << argv[0] << " group_name " << endl;
    cout << "  Run one test: " << argv[0] << " group_name 3" << endl;
    cout << "  --help: print this message" << endl;

  } else {
    tut::runner.get().set_callback(&visi);

    try
    {
      if (argc == 1 || (argc == 2 && string(argv[1]) == "regression"))
      {
        tut::runner.get().run_tests();
      }
      else if (argc == 2 && string(argv[1]) == "list")
      {
        cout << "registered test groups:" << endl;
        groupnames gl = tut::runner.get().list_groups();
        groupnames::const_iterator i = gl.begin();
        groupnames::const_iterator e = gl.end();
        while(i != e)
        {
          cout << "  " << *i << endl;
          ++i;
        }
      }
      else if (argc == 2 && string(argv[1]) != "regression")
      {
        tut::runner.get().run_tests(argv[1]);
      }
      else if (argc == 3)
      {
        tut::runner.get().run_test(argv[1],::atoi(argv[2]));
      }
      retval = !visi.all_ok();
    }
    catch (const exception& ex)
    {
      cerr << "tut raised ex: " << ex.what() << endl;
      retval = -1;
    }
  }

  return retval;
}

#endif // VS_TUT_DEFINE_MAIN

#endif /*TUTINCLUDES_H_*/
