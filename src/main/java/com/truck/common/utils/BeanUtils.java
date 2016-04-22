package com.truck.common.utils;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.*;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 扩展Apache Commons BeanUtils, 提供一些反射方面缺失的封装.
 */

public class BeanUtils extends org.apache.commons.beanutils.BeanUtils {

    protected static Logger logger = Logger.getLogger(BeanUtils.class);

    /**
     * 覆写org.apache.commons.beanutils.BeanUtils的copyProperties方法
     */
    public static void copyProperties(Object dest, Object orig){
        try {
            registerConverter();
            BeanUtilsBean.getInstance().copyProperties(dest, orig);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            logger.error(e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            logger.error(e);
        }
    }

    /**
     * 注册类型转换，可以避免两个问题：
     *
     * 1、对于数值型，当源对象中的字段为null，避免转换成0等；
     *
     */
    public static void registerConverter() {
        ConvertUtils.register(new IntegerConverter(null), Integer.class);
        ConvertUtils.register(new LongConverter(null), Long.class);
        ConvertUtils.register(new FloatConverter(null), Float.class);
        ConvertUtils.register(new DoubleConverter(null), Double.class);
        ConvertUtils.register(new BigDecimalConverter(null), BigDecimal.class);
        ConvertUtils.register(new SqlDateConverter(null), java.sql.Date.class);
        ConvertUtils.register(new SqlTimestampConverter(null), Timestamp.class);
        ConvertUtils.register(new DateConverter(null), Date.class);
    }

    /**
     * 直接读取对象属性值,无视private/protected修饰符,不经过getter函数.
     */
    public static Object getFieldValue(Object object, String fieldName) throws NoSuchFieldException {
        Field field = getDeclaredField(object, fieldName);
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }

        Object result = null;
        try {
            result = field.get(object);
        } catch (IllegalAccessException e) {
            logger.error("不可能抛出的异常{}", e);
        }
        return result;
    }

    /**
     * 直接设置对象属性值,无视private/protected修饰符,不经过setter函数.
     */
    public static void setFieldValue(Object object, String fieldName, Object value) throws NoSuchFieldException {
        Field field = getDeclaredField(object, fieldName);
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            logger.error("不可能抛出的异常:{}");
        }
    }

    /**
     * 循环向上转型,获取对象的DeclaredField.
     */
    public static Field getDeclaredField(Object object, String fieldName) throws NoSuchFieldException {
        Assert.notNull(object);
        return getDeclaredField(object.getClass(), fieldName);
    }

    /**
     * 循环向上转型,获取类的DeclaredField.
     */
    public static Field getDeclaredField(Class clazz, String fieldName) throws NoSuchFieldException {
        Assert.notNull(clazz);
        Assert.hasText(fieldName);
        for (Class superClass = clazz; superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                return superClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                // Field不在当前类定义,继续向上转型
            }
        }
        throw new NoSuchFieldException("No such field: " + clazz.getName() + '.' + fieldName);
    }

    /**
     * 判断object是否为空(object == null或object的所有属性均为null)
     *
     * @param obj
     * @return
     */
    public static boolean isObjectNull(Object obj) {
        if (obj == null)
            return true;
        Field[] fieldArr = obj.getClass().getDeclaredFields();
        if (fieldArr != null) {
            for (int i = 0; i < fieldArr.length; i++) {
                Field field = fieldArr[i];
                String fieldName = field.getName();
                if ("UUID".equals(fieldName)) {
                    continue;
                }
                try {
                    Object val = BeanUtils.getFieldValue(obj, fieldName);
                    if ("".equals(val)) {
                        continue;
                    }
                    if (val != null) {
                        return false;
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage());
                    throw new RuntimeException(ex.getMessage());
                }
            }
        }
        return true;
    }

    /**
     * 判断object是否为空(object == null或object的所有属性均为null或“”)
     *
     * @param obj
     * @return
     */
    public static boolean isObjectEmpty(Object obj) {
        if (obj == null)
            return true;
        Field[] fieldArr = obj.getClass().getDeclaredFields();
        if (fieldArr != null) {
            for (int i = 0; i < fieldArr.length; i++) {
                Field field = fieldArr[i];
                String fieldName = field.getName();
                if ("UUID".equals(fieldName)) {
                    continue;
                }
                try {
                    Object val = BeanUtils.getFieldValue(obj, fieldName);
                    if (val != null && !"".equals(val)) {
                        return false;
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage());
                    throw new RuntimeException(ex.getMessage());
                }
            }
        }
        return true;
    }

    /**
     * 设置主表的关联表的外键
     *
     * @param obj 主表对象
     * @param foreignKey 外键属性名
     * @return
     * @throws NoSuchFieldException
     */
    public static void setForeign(Object obj, String foreignKey) throws NoSuchFieldException {
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (!String.class.equals(field.getType()) && !Integer.class.equals(field.getType())
                    && !Long.class.equals(field.getType()) && !Timestamp.class.equals(field.getType())
                    && !Date.class.equals(field.getType()) && !Set.class.equals(field.getType())
                    && !Short.class.equals(field.getType()) && !Double.class.equals(field.getType())
                    && !BigDecimal.class.equals(field.getType())) {
                String fieldName = field.getName();
                Object val = BeanUtils.getFieldValue(obj, fieldName);
                if ("UUID".equals(field.getName())) {
                    continue;
                } else if (val != null) {
                    if (List.class.equals(field.getType())) {
                        for (Object o : (List) val) {
                            BeanUtils.setFieldValue(o, foreignKey, BeanUtils.getFieldValue(obj, foreignKey));
                        }
                    } else {
                        BeanUtils.setFieldValue(val, foreignKey, BeanUtils.getFieldValue(obj, foreignKey));
                    }
                }
            }
        }
    }

    /**
     * 拷贝非空属性
     *
     * @param target 目标对象
     * @param source 原对象
     * @return
     * @throws NoSuchFieldException
     */
    public static void copyNotNullProps(Object target, Object source) {
        Field[] fieldArr = source.getClass().getDeclaredFields();
        if (fieldArr != null) {
            for (int i = 0; i < fieldArr.length; i++) {
                Field field = fieldArr[i];
                String fieldName = field.getName();
                if (fieldName.equals("UUID")||fieldName.equals("id")) {
                    continue;
                }
                try {
                    Object val = BeanUtils.getFieldValue(source, fieldName);
                    if (val != null && ifExistField(target, fieldName)) {
                        BeanUtils.setFieldValue(target, fieldName, val);
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage());
                    throw new RuntimeException(ex.getMessage());
                }
            }
        }
    }

    /**
     * 拷贝非空属性,过滤掉不需要拷贝的属性
     *
     * @param target
     * @param source
     * @param skipList
     */
    public static void copyNotNullProps(Object target, Object source, List skipList) {
        Field[] fieldArr = source.getClass().getDeclaredFields();
        if (fieldArr != null) {
            for (int i = 0; i < fieldArr.length; i++) {
                Field field = fieldArr[i];
                String fieldName = field.getName();
                if (skipList.contains(fieldName)||"UUID".equals(fieldName)||fieldName.equals("id")) {
                    continue;
                }
                try {
                    Object val = BeanUtils.getFieldValue(source, fieldName);
                    if (val != null && ifExistField(target, fieldName)) {
                        BeanUtils.setFieldValue(target, fieldName, val);
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage());
                    throw new RuntimeException(ex.getMessage());
                }
            }
        }
    }

    /**
     * 判断属性是否属于当前BEAN对象
     *
     * @param bean
     * @param fieldName
     * @return
     */
    private static boolean ifExistField(Object bean, String fieldName) {
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            String name = field.getName();
            if (name.equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

}
