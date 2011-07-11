#include <windows.h>

int APIENTRY WinMain(HINSTANCE hInstance, HINSTANCE hPrevInstance, LPSTR lpCmdLine, int  nCmdShow) {
   /*
    * execute the Java VM with corresponding arguments, MessageBox if javaw.exe
    * is not found or fails for other reasons
    */
    if(ShellExecute(NULL, NULL, "javaw", "-cp CirKuit2D.jar cirkuit.CirKuitEdit", ".", SW_SHOWNORMAL) <= (HINSTANCE)32) {
        MessageBox(NULL, "Could not find JRE!\n\n Download from: http://java.sun.com", "CirKuit error", MB_OK | MB_ICONASTERISK);
        return 2;
    }
    return 0;
}