/*
 * Copyright (c) 2014, Kustaa Nyholm / SpareTimeLabs
 * Copyright (c) 2018, Nicholas Saney / Chairosoft
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notices, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notices, this
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
package purejavahidapi.shared;

import com.sun.jna.IntegerType;
import com.sun.jna.Structure;
import com.sun.jna.Union;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author nsaney
 */
public class DataDump {
	
	public static List<Map.Entry<String, Object>> getJnaDebugInfo(Structure structure) {
		List<Map.Entry<String, Object>> info = new ArrayList<>();
		for (Field field : structure.getClass().getDeclaredFields()) {
			try {
				String fieldName = field.getName();
				Object fieldValue = field.get(structure);
				if (fieldValue instanceof Union) {
					Field[] subFields = fieldValue.getClass().getDeclaredFields();
					for (Field subField : subFields) {
						String subFieldName = subField.getName();
						Object subFieldValue = subField.get(fieldValue);
						if (subFieldValue instanceof Structure) {
							List<Map.Entry<String, Object>> subInfo = getJnaDebugInfo((Structure)subFieldValue);
							for (Map.Entry<String, Object> infoEntry : subInfo) {
								String subInfoKey = infoEntry.getKey();
								Object subInfoValue = infoEntry.getValue();
								info.add(new AbstractMap.SimpleEntry<>(
									fieldName + "/" + subFieldName + "/" + subInfoKey,
									subInfoValue
								));
							}
						}
						else {
							info.add(new AbstractMap.SimpleEntry<>(
								fieldName + "/" + subFieldName,
								subFieldValue
							));
						}
					}
				}
				else {
					info.add(new AbstractMap.SimpleEntry<>(fieldName, fieldValue));
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return info;
	}
	
	public static void dumpJnaStructures(PrintStream out, Structure... structures) {
		List<List<Map.Entry<String, Object>>> infos = new ArrayList<>();
		for (Structure structure : structures) {
			infos.add(getJnaDebugInfo(structure));
		}
		
		int maxKeyLength = 0;
		int maxValueLength = 10;
		Map<String, List<String>> indexedInfos = new HashMap<>();
		List<Map.Entry<String, List<String>>> pivotedInfos = new ArrayList<>();
		for (List<Map.Entry<String, Object>> info : infos) {
			for (Map.Entry<String, Object> entry : info) {
				String key = entry.getKey();
				Object rawValue = entry.getValue();
				String value;
				if (rawValue instanceof IntegerType || rawValue instanceof Long || rawValue instanceof Integer || rawValue instanceof Short || rawValue instanceof Byte) {
					long longValue = ((Number)rawValue).intValue();
					value = String.format("%5d:%4x", longValue, longValue);
				}
				else if (rawValue != null && rawValue.getClass().isArray()) {
					Class<?> arrayType = rawValue.getClass().getComponentType();
					int arrayLength = Array.getLength(rawValue);
					value = String.format("%s[%d]", arrayType, arrayLength);
				}
				else {
					value = String.valueOf(rawValue);
				}
				if (key.length() > maxKeyLength) { maxKeyLength = key.length(); }
				List<String> values = indexedInfos.get(key);
				if (value.length() > maxValueLength) { value = "..."; }
				if (values == null) {
					values = new ArrayList<>(infos.size());
					pivotedInfos.add(new AbstractMap.SimpleEntry<>(key, values));
					indexedInfos.put(key, values);
				}
				values.add(value);
			}
		}
		
		String keyFormat = String.format("%%-%ds", maxKeyLength);
		String valueFormat = String.format(" %%%ds", maxValueLength);
		out.printf(keyFormat, "");
		for (int i = 0; i < infos.size(); ++i) {
			out.printf(valueFormat, "[" + i + "]");
		}
		out.println();
		for (Map.Entry<String, List<String>> pivotedEntry : pivotedInfos) {
			String key = pivotedEntry.getKey();
			List<String> values = pivotedEntry.getValue();
			out.printf(keyFormat, key);
			for (String value : values) {
				out.printf(valueFormat, value);
			}
			out.println();
		}
	}
	
}
