#include <TutIncludes.h>

/*
 * A "Simple" test that just makes sure the Tut framework compiles
 * and runs correctly.  If this test fails for any reason, the framework
 * is to blame, not you, poor developer.
 */
VS_TUT_TEST_DECLARATION_START
  struct TrueTest
  {
  };

  VS_TUT_TEST_REGISTER_OBJECT(TrueTest);

  VS_TUT_TEST_START(TrueTest, 1, "Simple Always True Test");
  {
    VS_TUT_ENSURE("failure", true);
    VS_TUT_ENSURE_EQUALS("failure", 1, 1);
    VS_TUT_ENSURE_DISTANCE("failure", 1, 2, 3);

  }
  VS_TUT_TEST_END(TrueTest)

  VS_TUT_TEST_START(TrueTest, 2, "Simple Always False Test");
  {
    bool failed = false;
    try {
      VS_TUT_ENSURE("failure", false);
    } catch (...) {
      failed = true;
    }
    if (!failed)
    {
      fail("we totally biffed it... our macros may be bust");
    }

    failed = false;
    try {
      VS_TUT_ENSURE_EQUALS("failure", 1, 0);
    } catch (...) {
      failed = true;
    }
    if (!failed)
    {
      fail("we totally biffed it... our macros may be bust");
    }

    failed = false;
    try {
      VS_TUT_ENSURE_DISTANCE("failure", 10, 0, 1);
    } catch (...) {
      failed = true;
    }
    if (!failed)
    {
      fail("we totally biffed it... our macros may be bust");
    }

  }
  VS_TUT_TEST_END(TrueTest)
  
  VS_TUT_TEST_START(TrueTest, 3, "Simple Always Fail Test");
  {
    VS_TUT_ENSURE("This is actually an expected failure and can be ignored", false);
  }
  VS_TUT_TEST_END(TrueTest)

VS_TUT_TEST_DECLARATION_END
