# How deep down the source tree are we.
DEPTH = ../../../../..
# This is a testing code
VS_TESTING=1

# Defining this variable lets us set up sensible DLLs (.so in Linux)
# archive and executable name variables.  $(PRODUCTDLL) will be the DLL
# for this product (correctly formatted for your OS), $(PRODUCTLIB) will
# be the archive (.a on Linux), and $(PRODUCTEXE) will be the excutable
# name.
VS_PRODUCT=test_utils
include $(DEPTH)/mk/Config.mk

# Now, define an all and a clean
# In your all, give the target you care about.
all: $(PRODUCTTESTEXE)

.PHONY: clean
clean: inherited_clean ;

.PHONY: clobber
clobber: inherited_clobber ;

# And finally, include the rules we'll use to build all the above
include $(DEPTH)/mk/Rules.mk
