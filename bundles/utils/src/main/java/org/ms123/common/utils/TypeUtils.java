/**
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2017] [Manfred Sattler] <manfred@ms123.org>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ms123.common.utils;

import org.apache.commons.beanutils.*;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collection;
import java.lang.reflect.*;
import java.lang.annotation.*;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.Key;
import javax.jdo.annotations.Value;
import org.ms123.common.utils.*;
import org.ms123.common.nucleus.api.NucleusService;
import org.ms123.common.store.StoreDesc;
import java.lang.annotation.Annotation;

@SuppressWarnings("unchecked")
public class TypeUtils {

	protected static Inflector m_inflector = Inflector.getInstance();

	public static boolean isPrimitiveType(Class type) {
		if (type.equals(byte[].class) || type.equals(Long.class) || type.equals(String.class) || type.equals(Float.class) || type.equals(Number.class) || type.equals(Date.class) || type.equals(Double.class) || type.equals(Integer.class) || type.equals(Boolean.class)) {
			return true;
		}
		return false;
	}
	public static boolean isBooleanType(Class type) {
		if (type.equals(Boolean.class)) {
			return true;
		}
		return false;
	}

	public static boolean isDateType(Class type) {
		if (type.equals(Date.class)) {
			return true;
		}
		return false;
	}

	public static boolean isLongType(Class type) {
		if (type.equals(Long.class)) {
			return true;
		}
		return false;
	}

	public static boolean isIntegerType(Class type) {
		if (type.equals(Integer.class)) {
			return true;
		}
		return false;
	}

	public static boolean isFloatType(Class type) {
		if (type.equals(Float.class)) {
			return true;
		}
		return false;
	}

	public static boolean isDoubleType(Class type) {
		if (type.equals(Double.class)) {
			return true;
		}
		return false;
	}

	public static boolean isStringType(Class type) {
		if (type.equals(String.class)) {
			return true;
		}
		return false;
	}

	public static boolean isNumberType(Class type) {
		if (type.equals(Long.class) || type.equals(Float.class) || type.equals(Number.class) || type.equals(Double.class) || type.equals(Integer.class)) {
			return true;
		}
		return false;
	}

	public static String getEntityForPath(NucleusService nucleus, StoreDesc sdesc, String _path) {
		Class clazz = null;
		try {
			int dollar = _path.lastIndexOf("$");
			if (dollar != -1) {
				String[] path = _path.split("\\$");
				String className = m_inflector.getClassName(path[0]);
				clazz = nucleus.getClass(sdesc, className);
				for (int i = 1; i < path.length; i++) {
					Field field = clazz.getDeclaredField(path[i]);
					clazz = getTypeForField(field);
				}
				className = clazz.getName();
				int dot = className.lastIndexOf(".");
				return m_inflector.getEntityName(className.substring(dot + 1));
			} else {
				return _path;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("getEntityForPath(" + _path + "):" + e);
		}
	}

	public static  boolean isMap(Object o, String fieldName) throws Exception {
		try {
			Field field = null;
			if( o instanceof Class){
				field = ((Class)o).getDeclaredField(fieldName);
			}else{
				field = o.getClass().getDeclaredField(fieldName);
			}
			Annotation ann = field.getAnnotation(Element.class);
			if (ann == null) {
				Annotation ann_key = field.getAnnotation(Key.class);
				ann = field.getAnnotation(Value.class);
				if (ann != null && ann_key!=null) {
					return true;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}

	public static  Class getTypeForField(Object o, String fieldName) throws Exception {
		try {
			if( o instanceof Class){
				Field field = ((Class)o).getDeclaredField(fieldName);
				return getTypeForField(field);
			}else{
				Field field = o.getClass().getDeclaredField(fieldName);
				return getTypeForField(field);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	public static  Class getTypeForField(Field field) throws Exception {
		try {
			Annotation ann = field.getAnnotation(Element.class);
			if (ann == null) {
				Annotation ann_key = field.getAnnotation(Key.class);
				ann = field.getAnnotation(Value.class);
				if (ann == null || ann_key==null) {
					return field.getType();
				}
			}
			Class atype = ann.annotationType();
			Method methTypes = atype.getDeclaredMethod("types");
			Class[] clazzType = (Class[]) methTypes.invoke(ann, new Object[0]);
			if (clazzType != null && clazzType.length > 0) {
				return clazzType[0];
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	public static List<Collection> getCandidateLists(Object related, Object to, String parentName) {
		List<Collection> list = new ArrayList<Collection>();
		Object o = null;
		try {
			BeanMap beanMap = new BeanMap(related);
			Iterator itv = beanMap.keyIterator();
			while (itv.hasNext()) {
				String prop = (String) itv.next();
				if (List.class.equals(beanMap.getType(prop)) || Set.class.equals(beanMap.getType(prop))) {
					Class lt = TypeUtils.getTypeForField(related, prop);
					if (lt.equals(to.getClass())) {
						list.add((Collection) beanMap.get(prop));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	public static  String getPrimaryKey(Class clazz) throws Exception {
		System.out.print("----->getPrimaryKey.clazz:" + clazz +" -> ");
		Field[] fields = clazz.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			java.lang.annotation.Annotation[] anns = fields[i].getDeclaredAnnotations();
			for (int j = 0; j < anns.length; j++) {
				try {
					Class atype = anns[j].annotationType();
					if (!(anns[j] instanceof javax.jdo.annotations.PrimaryKey)) {
						continue;
					}
					System.out.println(fields[i].getName());
					return fields[i].getName();
				} catch (Exception e) {
					System.out.println("getPrimaryKey.e:" + e);
				}
			}
		}
		throw new RuntimeException("TypeUtils.getPrimaryKey("+clazz+"):no_primary_key");
	}
}
