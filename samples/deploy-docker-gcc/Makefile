CXX := g++
DBGFLAGS := -g

# path macros
BIN_PATH := bin
OBJ_PATH := obj
SRC_PATH := src

#


CXXFLAGS := -D_GLIBCXX_USE_CXX11_ABI=0 -std=c++11
LDFLAGS := 

LIBS := 

INCLUDES := 

CCOBJFLAGS := $(CXXFLAGS) -c

# compile macros
TARGET_NAME := main
TARGET := $(BIN_PATH)/$(TARGET_NAME)

# src files & obj files
SRC := $(foreach x, $(SRC_PATH), $(wildcard $(addprefix $(x)/*,.c*)))
OBJ := $(addprefix $(OBJ_PATH)/, $(addsuffix .o, $(notdir $(basename $(SRC)))))

# clean files list
DISTCLEAN_LIST := $(OBJ) \
                  $(OBJ_DEBUG)
CLEAN_LIST := $(TARGET) \
                  $(TARGET_DEBUG) \
                          $(DISTCLEAN_LIST)

# default rule
default: makedir all

# non-phony targets
$(TARGET): $(OBJ)
	$(CXX) $(LDFLAGS) $(CXXFLAGS) -o $@ $(OBJ) $(LIBS)

$(OBJ_PATH)/%.o: $(SRC_PATH)/%.c*
	$(CXX) $(INCLUDES) $(CCOBJFLAGS) -o $@ $<


$(TARGET_DEBUG): $(OBJ_DEBUG)
	$(CXX) $(CXXFLAGS) $(DBGFLAGS) $(OBJ_DEBUG) -o $@

# phony rules
.PHONY: makedir
makedir:
	@mkdir -p $(BIN_PATH) $(OBJ_PATH)

.PHONY: all
all: $(TARGET)


.PHONY: clean
clean:
	@echo CLEAN $(CLEAN_LIST)
	@rm -f $(CLEAN_LIST)

.PHONY: distclean
distclean:
	@echo CLEAN $(CLEAN_LIST)
	@rm -f $(DISTCLEAN_LIST)

