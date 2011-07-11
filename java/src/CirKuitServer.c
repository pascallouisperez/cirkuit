#include <stdlib.h>
#include <string.h>
#include <stdio.h>

int main(int argc, char * argv[])  {
    char * str = malloc(FILENAME_MAX*sizeof(char));
    strncpy(str, "java -cp CirKuit2D.jar cirkuit.CirKuitServer ", FILENAME_MAX-6);
    if (argc > 1)
        strncat(str , argv[1], 5);
    str[FILENAME_MAX-1] = '\0';
    system(str);
    free(str);
    return 0;
}