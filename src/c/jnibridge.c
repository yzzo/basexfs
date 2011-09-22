#include <jni.h>
#include "jnibridge.h"
#include "basexfs.h"
#include "org_basex_fs_BaseXFS.h" 

/* Reference Java Virtual Machine (on System.loadLibrary('basexfs')). */
JavaVM *cached_jvm;
/* Reference org.basex.fs.BaseXFS class on System.loadLibrary('basexfs')). */
jclass Class_BaseXFS;
/* Reference to BaseXFS object (for c2j callbacks) on j2cMount(). */
jobject OBJ_C2J_BaseXFS;
/* Reference org.basex.fs.BaseXFS:c2jDummyCallback(). */
jmethodID MID_c2jDummyCallback;

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

	cls = (*env)->FindClass(env, "org/basex/fs/BaseXFS");
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
	/* Compute and cache the method ID */
	MID_c2jDummyCallback = (*env)->GetMethodID(env, cls, "c2jDummyCallback", "()V");
	if (MID_c2jDummyCallback == NULL) {
		fprintf(stderr, "[jnibridge] Can not ref c2jDummyCallback.\n");
		return JNI_ERR;
	}

	return JNI_VERSION_1_2;
}

/*
 * Using the cached JavaVM interface pointer allow native code to obtain the
 * JNIEnv interface pointer for the current thread.
 */
static JNIEnv *jni_get_env(void);
static JNIEnv *jni_get_env()
{
	JNIEnv *env;
	(*cached_jvm)->AttachCurrentThread(cached_jvm, (void **)&env, NULL);
	if (env == NULL) {
		fprintf(stderr, "[jnibridge] Can not get env.\n");
	}
	return env;
}


/* -------------------------------------------------------------------------- */
/* ---- From platform independent code to native part (j2c) ----------------- */
/* -------------------------------------------------------------------------- */

JNIEXPORT jboolean JNICALL Java_org_basex_fs_BaseXFS_j2cMount
  (JNIEnv *env, jobject o, jobjectArray a)
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
	char *argv[] = { "basexfs", "-f", "/var/tmp/mnt" }; 
	int rc = basexfs_mount(3, argv);
	if (rc != 0)
		return JNI_FALSE;
	return JNI_TRUE;
}

/* -------------------------------------------------------------------------- */
JNIEXPORT void JNICALL Java_org_basex_fs_BaseXFS_j2cInfo
  (JNIEnv *e, jobject o)
{
	printf("[j2c_info] Huhu. A shy hello from native basexfs library.\n");
}
/* -------------------------------------------------------------------------- */


/* -------------------------------------------------------------------------- */
/* ---- From native to platform independent code (c2j) ---------------------- */
/* -------------------------------------------------------------------------- */

int c2j_symlink(const char *from, const char *to)
{
	JNIEnv *env;

	env = jni_get_env();
	fprintf(stderr, "%-20s from: '%s' to: '%s'\n", "[c2j_symlink]", from, to);
	
	(*env)->CallVoidMethod(env, OBJ_C2J_BaseXFS, MID_c2jDummyCallback);
	// XXX: exceptions? p74 
	
	return 0;
}
