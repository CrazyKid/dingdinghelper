package com.crazykid.dto;

import com.crazykid.exp.AlarmActionObjectParseClassNotFoundException;

/**
 * 通过实现这个接口, 可以增加报警的作用对象
 * 在一个项目中, 每一个报警的作用对象, 都有且只有一个唯一(与其余作用对象不重复的)指定链接的报警机器人与之对应
 *
 * @author arthur
 * @date 2024/12/23 17:57
 */
public interface AlarmActionObject {
    /**
     * 获取报警作用对象的类型
     * 这个在一个项目中, 必须是保证唯一的,
     * 通过这个类型,能定位到唯一一个报警作用对象
     *
     * @return
     */
    Integer getActionObject();

    /**
     * 获取报警作用对象的名字
     * 这个在一个项目中, 必须是保证唯一的,
     * 通过这个名字,能定位到唯一一个报警作用对象
     *
     * @return
     */
    String getActionObjectName();

    /**
     * 获取报警作用对象的描述
     * 一般就是介绍下这个对象的身份, 以及这类报警是什么性质的
     *
     * @return
     */
    String getActionObjectDesc();

    /**
     * 获取作用对象, 找不到时抛出异常
     *
     * @param actionObject
     * @return
     * @throws AlarmActionObjectParseClassNotFoundException
     */
    AlarmActionObject getByActionObject(Integer actionObject) throws AlarmActionObjectParseClassNotFoundException;

}
