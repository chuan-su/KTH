DEFINES+=PROJECT_CONF_H=\"project-conf.h\"
all: mqtt-demo

CONTIKI_WITH_IPV6 = 1

APPS += mqtt
APPS += ping6

# We use floating vars. Add library.
#PRINTF_LIB_FLT  = -Wl,-u,vfprintf -lprintf_flt -lm
#PRINTF_LIB_MIN  = -Wl,-u,vfprintf -lprintf_min
#PRINTF_LIB      = $(PRINTF_LIB_FLT)
#CLIBS           = $(PRINTF_LIB) 
#
##CFLAGS += -DPMS5003
##CFLAGS += -DBME280
#
##MODULES += dev/bme280
#
#CUSTOM_RULE_LINK = 1
#%.$(TARGET): %.co $(PROJECT_OBJECTFILES) $(PROJECT_LIBRARIES) contiki-$(TARGET).a 
#	$(LD) $(LDFLAGS) $(TARGET_STARTFILES) ${filter-out %.a,$^} ${filter %.a,$^} $(TARGET_LIBFILES) -o $@ $(CLIBS)


CONTIKI=../../../..
include $(CONTIKI)/Makefile.include
