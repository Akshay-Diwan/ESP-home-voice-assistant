#include "stdlib.h"
#include <stdbool.h>

volatile bool automationEnabled = true;

void setAutomation(bool val){
    automationEnabled = val;
}