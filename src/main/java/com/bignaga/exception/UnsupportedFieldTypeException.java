/**
 * @project lbsgap
 * @file UnsupportedFieldTypeException.java
 * @package com.bignaga.exception
 * @author zhonghao
 * @date 2018/6/5 0:37
 * @copyright bignaga
 */
package com.bignaga.exception;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author zhonghao
 * @date 2018/6/5 0:37
 * @see
 * @since
 */
public class UnsupportedFieldTypeException extends Exception {
    public UnsupportedFieldTypeException() {
        this("UnsupportedFieldTypeException");
    }
    public UnsupportedFieldTypeException(String msg) {
        super(msg);
    }
}
