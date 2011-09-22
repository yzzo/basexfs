MNT_DIR=/var/tmp/mnt
INC_JNI=/usr/lib/jvm/java-6-openjdk/include/
LIBBASEXFS=libbasexfs.so.1.0.1

# $(LIBBASEXFS): basexfs.o jnibridge.o
# 	gcc -Wall -fPIC -shared -Wl,-e,info,-soname,libbasexfs.so.1 -o $(LIBBASEXFS) $^ `pkg-config fuse --libs` -lc
# 
# basexfs: basexfs.o jnibridge.o
# 	gcc -Wall `pkg-config fuse --libs` $^ -o $@
# 
libbasexfs.so: jnibridge.o basexfs.o
	gcc -Wall -shared -fPIC `pkg-config fuse --libs` -o libbasexfs.so $^

jnibridge.o: jnibridge.c jnibridge.h org_basex_fs_BaseXFS.h
	gcc -Wall -I$(INC_JNI) -fPIC -c $<

basexfs.o: basexfs.c
	gcc -Wall `pkg-config fuse --cflags` -fPIC -c $<

test: test.c libbasexfs
	gcc -Wall -o test -I$(INC_JNI) -L. -lbasexfs $<

# phony

clean:
	rm -f *.o
	rm -f basexfs
	rm -f libbasexfs*

mount:
	./basexfs -f $(MNT_DIR)

umount:
	sudo umount $(MNT_DIR)
