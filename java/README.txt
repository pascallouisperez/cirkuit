.: CirKuit 2D :.
----------------

If you have the binary files you can launch the game with:

> java -jar CirKuit2D.jar

the editor with:

> java -cp CirKuit2D.jar cirkuit.CirKuitEdit

and the server with:

> java -cp CirKuit2D.jar cirkuit.CirKuitServer [port]

If you have the source files you can compile them using ant (http://ant.apache.org)

> ant         -> to compile
> ant jar     -> to make the jar file
> ant doc     -> to make the javadoc
> ant dist    -> generates two distribution files placed in the dist directory
                 and a windows setup file with InnoSetup.

If you want to generate the Windows launchers:

> make
