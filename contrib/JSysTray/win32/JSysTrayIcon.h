/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class JSysTrayIcon */

#ifndef _Included_JSysTrayIcon
#define _Included_JSysTrayIcon
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     JSysTrayIcon
 * Method:    nativeShowWindow
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_JSysTrayIcon_nativeShowWindow
  (JNIEnv *, jobject, jstring, jint);

/*
 * Class:     JSysTrayIcon
 * Method:    nativeCreateSystrayIcon
 * Signature: (ILjava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_JSysTrayIcon_nativeCreateSystrayIcon
  (JNIEnv *, jobject, jint, jstring, jstring);

/*
 * Class:     JSysTrayIcon
 * Method:    nativeModifySystrayIcon
 * Signature: (IILjava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_JSysTrayIcon_nativeModifySystrayIcon
  (JNIEnv *, jobject, jint, jint, jstring, jstring);

/*
 * Class:     JSysTrayIcon
 * Method:    nativeDeleteSystrayIcon
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_JSysTrayIcon_nativeDeleteSystrayIcon
  (JNIEnv *, jobject, jint);

#ifdef __cplusplus
}
#endif
#endif
