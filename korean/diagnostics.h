#pragma once
#ifdef ANDROID
#include <unistd.h>
#include <cstdlib>
#include <cstdio>
#include <atomic>
// ASCII-only records bypass Java string conversion and userspace stream buffering.
inline int korean_diag_fd() {
    static int fd = [] { const char* s = std::getenv("EXULT_DIAG_FD"); return s ? std::atoi(s) : -1; }();
    return fd;
}
struct KoreanTrace {
    const char* name; unsigned long id;
    KoreanTrace(const char* n, const char* text=nullptr, int length=-1):name(n) {
        static std::atomic<unsigned long> counter{0}; id=++counter;
        int fd=korean_diag_fd(); if(fd<0 || id>200000) return;
        char b[640]; int used=snprintf(b,sizeof(b),"ENTER %lu %s length=%d hex=",id,name,length);
        if(text) for(int i=0;i<128 && (length<0 || i<length);++i) {
            unsigned char c=static_cast<unsigned char>(text[i]); if(length<0 && !c) break;
            used+=snprintf(b+used,sizeof(b)-used,"%02x",c);
        }
        b[used++]='\n'; (void)!write(fd,b,used);
    }
    ~KoreanTrace(){int fd=korean_diag_fd();if(fd>=0 && id<=200000)dprintf(fd,"LEAVE %lu %s\n",id,name);}
};
#else
struct KoreanTrace { KoreanTrace(const char*,const char* =nullptr,int=-1){} };
#endif
