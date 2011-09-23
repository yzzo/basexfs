MNT_DIR=/var/tmp/mnt
INC_JNI_LIN=/usr/lib/jvm/java-6-openjdk/include/
INC_JNI_MAC=/Developer/SDKs/MacOSX10.7.sdk/System/Library/Frameworks/JavaVM.framework/Headers/

# Mac (fuse4x)
# PKG_CONFIG_PATH=/usr/local/lib/pkgconfig/
libbasexfs.jnilib: jnibridge_mac.o basexfs.o
	gcc -dynamiclib -o libbasexfs.jnilib `pkg-config fuse --libs` -framework JavaVM $^

jnibridge_mac.o: jnibridge.c jnibridge.h org_basex_fs_BaseXFS.h
	gcc -Wall -I$(INC_JNI_MAC) -fPIC -o $@ -c $<

# Linux
libbasexfs.so: jnibridge.o basexfs.o
	gcc -Wall -shared -fPIC `pkg-config fuse --libs` -o libbasexfs.so $^


jnibridge.o: jnibridge.c jnibridge.h org_basex_fs_BaseXFS.h
	gcc -Wall -I$(INC_JNI_LIN) -fPIC -c $<

basexfs.o: basexfs.c
	gcc -Wall `pkg-config fuse --cflags` -fPIC -c $<

# phony
mac: libbasexfs.jnilib

linux: libbasexfs.so

clean:
	rm -f *.o
	rm -f basexfs
	rm -f libbasexfs*

mount:
	./basexfs -f $(MNT_DIR)

umount:
	sudo umount $(MNT_DIR)
