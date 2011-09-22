.PHONY: all java header lib start

all:	java header lib

# build platform independant part
java:
	(cd ./java/ && mvn package)
	@echo '==== Built java/target/basexfs-1.0-SNAPSHOT.jar'

# generate native header from platform independant part 
header:
	javah -jni -d ./c -classpath ./java/target/classes/ org.basex.fs.BaseXFS 
	@echo '==== Built c/org_basex_fs_BaseXFS.h'

# compile libbasexfs
lib:
	(cd ./c/ && make)
	@echo '==== Built c/libbasexfs.so'

# start BaseXFS database client, which loads libbasexfs.
start:
	java -Djava.library.path=./c/ -cp java/target/basexfs-1.0-SNAPSHOT.jar org.basex.fs.Shell

clean:
	(cd java && mvn clean)
	(cd c && make clean)	
