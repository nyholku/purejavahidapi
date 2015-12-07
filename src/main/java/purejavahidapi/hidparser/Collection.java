/*
 * Copyright (c) 2014, Kustaa Nyholm / SpareTimeLabs
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list 
 * of conditions and the following disclaimer.
 * 
 * Redistributions in binary form must reproduce the above copyright notice, this 
 * list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 *  
 * Neither the name of the Kustaa Nyholm or SpareTimeLabs nor the names of its 
 * contributors may be used to endorse or promote products derived from this software 
 * without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 */
package purejavahidapi.hidparser;

import java.util.LinkedList;

public final class Collection {
	Collection m_Parent;
	LinkedList<Collection> m_Children;
	LinkedList<Field> m_Fields;
	int m_Usage;
	int m_Type;

	Collection(Collection parent, int usage, int type) {
		m_Parent = parent;
		m_Usage = usage;
		m_Type = type;
		m_Children = new LinkedList();
		if (parent != null)
			parent.m_Children.add(this);
		m_Fields = new LinkedList();
	}

	void add(Field field) {
		m_Fields.add(field);
	}

	void dump(String tab) {
		if (m_Parent != null) {
			HidParser.printf(tab + "collection  type %d  usage 0x%04X:0x%04X\n", m_Type, (m_Usage >> 16) & 0xFFFF, m_Usage & 0xFFFF);
			tab += "   ";
		}
		for (Collection c : m_Children)
			c.dump(tab);
		for (Field f : m_Fields)
			f.dump(tab);
	}
}