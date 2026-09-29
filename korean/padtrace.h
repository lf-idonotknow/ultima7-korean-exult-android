#pragma once
#include <cstdio>
#include <cstdarg>
#ifdef ANDROID
#include <SDL_system.h>
#include <jni.h>
#endif
#if defined(__GNUC__)
__attribute__((format(printf, 1, 2)))
#endif
inline void korean_pad_log(const char* format, ...) {
#ifdef ANDROID
    char text[512]; va_list args;va_start(args,format);vsnprintf(text,sizeof(text),format,args);va_end(args);
    auto* env=static_cast<JNIEnv*>(SDL_AndroidGetJNIEnv());
    if(!env || env->ExceptionCheck()) return;
    auto cls=env->FindClass("info/exult/ExultActivity");
    if(!cls){env->ExceptionClear();return;}
    auto method=env->GetStaticMethodID(cls,"logPadEffect","(Ljava/lang/String;)V");
    if(!method){env->ExceptionClear();env->DeleteLocalRef(cls);return;}
    auto message=env->NewStringUTF(text); // Only ASCII event names/numbers; never game text.
    if(message){env->CallStaticVoidMethod(cls,method,message);env->DeleteLocalRef(message);}
    if(env->ExceptionCheck())env->ExceptionClear();
    env->DeleteLocalRef(cls);
#else
    (void)format;
#endif
}
