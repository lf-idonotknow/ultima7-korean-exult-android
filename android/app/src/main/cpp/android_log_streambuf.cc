/*
 *  Copyright (C) 2021-2022  The Exult Team
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

#include "android_log_streambuf.h"

#include <SDL2/SDL_system.h>
#include <android/log.h>

#include <iostream>
#include <unistd.h>
#include <cstdlib>
#include <cstdio>

AndroidLog_streambuf::AndroidLog_streambuf(int priority, const char* tag)
		: m_priority{priority}, m_tag{tag} {
	jnithread = std::this_thread::get_id();

	m_jniEnv      = static_cast<JNIEnv*>(SDL_AndroidGetJNIEnv());
	auto* jclass  = m_jniEnv->FindClass("info/exult/ExultActivity");
    if (!jclass) { m_jniEnv->ExceptionClear(); return; }
	auto* jmethod = m_jniEnv->GetStaticMethodID(
			jclass, "instance", "()Linfo/exult/ExultActivity;");
    if (!jmethod) { m_jniEnv->ExceptionClear(); m_jniEnv->DeleteLocalRef(jclass); return; }
	m_exultActivityObject  = m_jniEnv->CallStaticObjectMethod(jclass, jmethod);
    if (m_jniEnv->ExceptionCheck() || !m_exultActivityObject) {
        m_jniEnv->ExceptionClear(); m_jniEnv->DeleteLocalRef(jclass); return;
    }
	m_writeToConsoleMethod = m_jniEnv->GetMethodID(
			jclass, "writeToConsoleBytes", "([B)V");
    if (m_jniEnv->ExceptionCheck()) m_jniEnv->ExceptionClear();
    m_jniEnv->DeleteLocalRef(jclass);
}

std::streambuf::int_type AndroidLog_streambuf::overflow(int_type ch) {
	if ('\n' == ch || traits_type::eof() == ch) {
        const char* diagnostic = std::getenv("EXULT_DIAG_FD");
        if (diagnostic) {
            int fd = std::atoi(diagnostic);
            dprintf(fd,"CONSOLE_BEGIN bytes=%zu\n",m_lineBuf.size());
            (void)!write(fd,m_lineBuf.data(),m_lineBuf.size());
            (void)!write(fd,"\n",1);
        }
		__android_log_write(m_priority, m_tag.c_str(), m_lineBuf.c_str());

		// Can only call into jni if we are on the main thread
		if (jnithread == std::this_thread::get_id() && m_writeToConsoleMethod) {
            // Korean engine logs contain CP949, not JNI Modified UTF-8.
            // Transfer bytes unchanged and decode only the console display in Java.
            jbyteArray line = m_jniEnv->NewByteArray(static_cast<jsize>(m_lineBuf.size()));
            if (line) {
                m_jniEnv->SetByteArrayRegion(line, 0, static_cast<jsize>(m_lineBuf.size()),
                        reinterpret_cast<const jbyte*>(m_lineBuf.data()));
                if (!m_jniEnv->ExceptionCheck())
                    m_jniEnv->CallVoidMethod(m_exultActivityObject, m_writeToConsoleMethod, line);
                m_jniEnv->DeleteLocalRef(line);
            }
            // A console display failure must not poison subsequent game JNI calls.
            if (m_jniEnv->ExceptionCheck()) {
                m_jniEnv->ExceptionDescribe();
                m_jniEnv->ExceptionClear();
            }
		}
		m_lineBuf.clear();
        if (diagnostic) dprintf(std::atoi(diagnostic),"CONSOLE_END\n");
	} else {
		m_lineBuf += static_cast<char>(ch);
	}
	return ch;
}
