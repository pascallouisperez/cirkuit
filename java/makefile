all: CirKuit CirKuitEditor CirKuitServer clean

CirKuit: CirKuit_o resource_res 
	gcc CirKuit.o resource.res -o CirKuit -mwindows -lgdi32
	
CirKuit_o:
	gcc -c ./src/CirKuit.c -o CirKuit.o

CirKuitEditor: CirKuitEditor_o resource_edit_res 
	gcc CirKuitEditor.o resource_edit.res -o CirKuitEditor -mwindows -lgdi32
	
CirKuitEditor_o:
	gcc -c ./src/CirKuitEditor.c -o CirKuitEditor.o

CirKuitServer: CirKuitServer_o resource_server_res 
	gcc CirKuitServer.o resource_server.res -o CirKuitServer
	
CirKuitServer_o:
	gcc -c ./src/CirKuitServer.c -o CirKuitServer.o
	
resource_res:
	windres ./src/resource.rc --include-dir ./src -O coff -o resource.res

resource_edit_res:
	windres ./src/resource_edit.rc --include-dir ./src -O coff -o resource_edit.res
    
resource_server_res:
	windres ./src/resource_server.rc --include-dir ./src -O coff -o resource_server.res
    
clean:
	@-erase *.o *.res
#	@-rm *.o *.res
