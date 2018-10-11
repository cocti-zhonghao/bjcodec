/**
 * @project lbsgap
 * @file MessageElementType.java
 * @package com.bignaga.codec
 * @author zhonghao
 * @date 2018/6/1 16:05
 * @copyright bignaga
 */
package com.bignaga.codec;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * @author zhonghao
 * @date 2018/6/1 16:05
 * @see
 * @since
 */
public enum MessageElementType
{
    U8("1字节无符号数"),
    U16("2字节无符号数"),
    U32("4字节无符号数"),
    S8("1字节有符号数"),
    S16("2字节有符号数"),
    S32("4字节有符号数"),
    LONG("long"),
    BYTES("字符串"),
    RESERVE("保留字段"),
    STRUCT("复合类型");

    MessageElementType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {return desc;}

    private String desc;
}
