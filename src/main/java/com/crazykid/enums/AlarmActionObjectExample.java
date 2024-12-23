package com.crazykid.enums;

import com.crazykid.config.DingMultiProperties;
import com.crazykid.dto.AlarmActionObject;
import com.crazykid.exp.AlarmActionObjectParseClassNotFoundException;

/**
 * 给了个例子, 可以在自己项目中, 自己写一个例如这样的枚举类,
 * 然后把 类名配置到 {@link DingMultiProperties#getActionObjectClassName()} 上
 * 这个不要使用, 如果要用,使用默认的配置 {@link AlarmActionObjectTemplate}
 * @author arthur
 * @date 2024/12/23 17:57
 */
public enum AlarmActionObjectExample implements AlarmActionObject {
    COMMON(3, "通用报警机器人"),
    DEVELOPER(4, "开发者报警机器人"),
    OPERATOR(5, "运营人员报警机器人");
    private final Integer type;
    private final String desc;

    AlarmActionObjectExample(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    @Override
    public Integer getActionObject() {
        return type;
    }

    @Override
    public String getActionObjectName() {
        return type + "";
    }

    @Override
    public String getActionObjectDesc() {
        return desc;
    }

    @Override
    public AlarmActionObject getByActionObject(Integer actionObject)
            throws AlarmActionObjectParseClassNotFoundException {
        for (AlarmActionObjectExample v : values()) {
            if (v.type.equals(actionObject)) {
                return v;
            }
        }
        throw new AlarmActionObjectParseClassNotFoundException("cannot find AlarmActionObject:" + actionObject);

    }
}
