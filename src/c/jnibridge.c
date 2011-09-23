#include <jni.h>
#include <err.h>
#include <string.h>
#include <stdlib.h>
#include "jnibridge.h"
#include "basexfs.h"
#include "org_basex_fs_fuse_BaseXFS.h" 

/* Reference Java Virtual Machine (on System.loadLibrary('basexfs')). */
JavaVM *cached_jvm;
/* Reference org.basex.fs.fuse.BaseXFS class on System.loadLibrary('basexfs')). */
jclass Class_BaseXFS;

/* Reference to BaseXFS object (for c2j callbacks) on j2cMount(). */
jobject OBJ_C2J_BaseXFS;
/* Reference to callback in org.basex.fs.fuse.BaseXFS. c2j<XXX>(). */
jmethodID MID_c2jMkdir;

/* -------------------------------------------------------------------------- */
/* ---- JNI utility functions ----------------------------------------------- */
/* -------------------------------------------------------------------------- */

/*
 * Store reference to Java Virtual Machine BaseXServer is running on.
 *
 * When System.loadLibrary('basexfs') is called by the platform independent
 * part, the virtual machine searches for the following exported entry in the
 * native library:
 * 	JNIEXPORT jint JNICALL JNI_OnLoad(Java VM* jvm, void *reserved);
 * We use it to store the reference to the virtual machine for later callbacks.  
 */
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* jvm, void *reserved)
{
	JNIEnv *env;
	jclass cls;
	cached_jvm = jvm; /* cache the JavaVM pointer */
	if ((*jvm)->GetEnv(jvm, (void **)&env, JNI_VERSION_1_2)) {
		fprintf(stderr, "[jnibridge] JavaVM not supported.\n");
		return JNI_ERR; /*JNI Version not supported */
	}
	fprintf(stderr, "[jnibridge] JavaVM stored at %p\n", cached_jvm);

	/* Create reference to BaseXFS class. */
	cls = (*env)->FindClass(env, "org/basex/fs/fuse/BaseXFS");
	if (cls == NULL) {
		fprintf(stderr, "[jnibridge] Can not find class BaseXFS.\n");
		return JNI_ERR;
	}
	/* Use weak global ref to allow BaseXFS class to be unloaded. */
	/* XXX: Hmm, I do not think we want it to be unloaded. */
	Class_BaseXFS = (*env)->NewWeakGlobalRef(env, cls);
	if (Class_BaseXFS == NULL) {
		fprintf(stderr, "[jnibridge] Can not create ref to BaseXFS.\n");
		return JNI_ERR;
	}

	/* Compute and cache various method IDs (utility and c2j callbacks) */
	MID_c2jMkdir = (*env)->GetMethodID(env, Class_BaseXFS, "c2jMkdir", "([B[B)V");
	if (MID_c2jMkdir == NULL) {
		fprintf(stderr, "[jnibridge] Can not ref c2jMkdir.\n");
		return JNI_ERR;
	}

	return JNI_VERSION_1_2;
}

/*
 * Using the cached JavaVM interface pointer allow native code to obtain the
 * JNIEnv interface pointer for the current thread.
 */
static JNIEnv *JNU_GetEnv(void);
static JNIEnv *JNU_GetEnv()
{
	JNIEnv *env;
	(*cached_jvm)->AttachCurrentThread(cached_jvm, (void **)&env, NULL);
	if (env == NULL) {
		fprintf(stderr, "[jnibridge] Can not get env.\n");
	}
	return env;
}

/* Creates a jbyteArray from a locale-specific native C string. */
static jstring JNU_JByteArrayNative(JNIEnv *env, const char *str);
static jstring JNU_JByteArrayNative(JNIEnv *env, const char *str)
{
	jbyteArray bytes = 0;
	int len;

	if ((*env)->EnsureLocalCapacity(env, 2) < 0) {
		err(1, "No memory.");
	}
	len = strlen(str);
	bytes = (*env)->NewByteArray(env, len);
	if (bytes != NULL) {
		(*env)->SetByteArrayRegion(env, bytes, 0, len, (jbyte *)str);
		if ((*env)->ExceptionCheck(env)) {
			(*env)->ExceptionDescribe(env);
			(*env)->ExceptionClear(env);
			errx(1, "Failed to convert c string to jbyteArray: '%s'", str);
		}
		return bytes;
	} /* else fall through */
	return NULL;
}

static char *JNU_CStringFromJByteArray(JNIEnv *env, jbyteArray bytes);
static char *JNU_CStringFromJByteArray(JNIEnv *env, jbyteArray bytes)
{
	char *result = NULL;

	jint len = (*env)->GetArrayLength(env, bytes);
	result = (char *)malloc(len + 1);
	if (result == 0) {
	//     	JNU_ThrowByName(env, "java/lang/OutOfMemoryError", 0);
	//	(*env)->DeleteLocalRef(env, bytes);
		errx(1, "Out of memory");
	}
	(*env)->GetByteArrayRegion(env, bytes, 0, len, (jbyte *)result);
	if ((*env)->ExceptionCheck(env)) {
		(*env)->ExceptionDescribe(env);
		errx(1, "Failed to convert jbyteArray to c string.");
	}
	result[len] = '\0'; /* terminate c string */

	return result;
}

/* -------------------------------------------------------------------------- */
/* ---- From platform independent code to native part (j2c) ----------------- */
/* -------------------------------------------------------------------------- */

JNIEXPORT jboolean JNICALL Java_org_basex_fs_fuse_BaseXFS_j2cMount
  (JNIEnv *env, jobject o, jint jargc, jobjectArray jargv)
{
	/* Create global reference to BaseXFS object for subsequent callbacks
         * (c2j). */
	OBJ_C2J_BaseXFS = (*env)->NewGlobalRef(env, o);
	if (OBJ_C2J_BaseXFS == NULL) {
		fprintf(stderr, "Can not create global ref to BaseXFS object.");
		return JNI_FALSE;
	}

	/* Mount filesystem in foreground (otherwise the complete Java process
 	 * is terminated). */ 
	//char *argv[] = { "basexfs", "-f", "/var/tmp/mnt" }; 
	int i;
	char** argv = malloc(jargc * sizeof(char *));
	if (argv == NULL)
		errx(1, "Out of memory");
	for (i = 0; i < jargc; i++) {
		/* fetch an <element> from an array of <element>s */
		jbyteArray jbytes = (*env)->GetObjectArrayElement(env, jargv, i);
		if ((*env)->ExceptionCheck(env)) {
			(*env)->ExceptionDescribe(env);
			errx(1, "Failed to get jbyteArray from jargv array.");
		}
		/* convert jbyteArray to c string */
		argv[i] = JNU_CStringFromJByteArray(env, jbytes);
	}
	int rc = basexfs_mount(jargc, argv);
	for (i = 0; i < jargc; i++) {
		fprintf(stderr, "> %s\n", argv[i]);
		free(argv[i]);
	}
	free(argv);
	if (rc != 0)
		return JNI_FALSE;
	return JNI_TRUE;
}

/* -------------------------------------------------------------------------- */
JNIEXPORT void JNICALL Java_org_basex_fs_fuse_BaseXFS_j2cInfo
  (JNIEnv *e, jobject o)
{
	printf("[j2c_info] Huhu. A shy hello from native basexfs library.\n");
}
/* -------------------------------------------------------------------------- */


/* -------------------------------------------------------------------------- */
/* ---- From native to platform independent code (c2j) ---------------------- */
/* -------------------------------------------------------------------------- */
int c2j_mkdir(const char *path, const char *mode)
{
	JNIEnv *env;
	jbyteArray arg1;
	jbyteArray arg2;

	env = JNU_GetEnv();
	fprintf(stderr, "%-20s path: '%s' mode: '%s'\n", "[c2j_mkdir]", path, mode);

	arg1 = JNU_JByteArrayNative(env, path);
	arg2 = JNU_JByteArrayNative(env, mode);
	if (arg1 == NULL || arg2 == NULL)
		errx(1, "Failed to convert c string to jbyteArray.");

	(*env)->CallVoidMethod(env, OBJ_C2J_BaseXFS, MID_c2jMkdir, arg1, arg2);
	if ((*env)->ExceptionCheck(env)) {
		(*env)->ExceptionDescribe(env);
		errx(1, "Failed to call org_basex_fs_BaseXFS_c2jMkdir.");
	}
	
	return 0;
}

int c2j_symlink(const char *from, const char *to)
{
	JNIEnv *env;

	env = JNU_GetEnv();
	fprintf(stderr, "%-20s from: '%s' to: '%s'\n", "[c2j_symlink]", from, to);
	
	return 0;
}

