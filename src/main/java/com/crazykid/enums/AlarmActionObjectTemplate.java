package com.crazykid.enums;

import com.crazykid.dto.AlarmActionObject;
import com.crazykid.exp.AlarmActionObjectParseClassNotFoundException;

/**
 * @author arthur
 * @date 2024/12/23 17:59
 */
public enum AlarmActionObjectTemplate implements AlarmActionObject {
    /**
     * 如果这里定义的类型,不能满足使用,可以在自己的项目中,
     * 通过实现 {@link AlarmActionObject}, 再拓展
     */
    COMMON(0, "通用"),
    DEVELOPER(1, "开发者"),
    OPERATOR(2, "运营人员");
    private final Integer type;
    private final String desc;

    AlarmActionObjectTemplate(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }

    public String getDesc() {
        return desc;
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
    public AlarmActionObject getByActionObject(Integer actionObject) throws
            AlarmActionObjectParseClassNotFoundException {
        for (AlarmActionObjectTemplate v : values()) {
            if (v.type.equals(actionObject)) {
                return v;
            }
        }
        throw new AlarmActionObjectParseClassNotFoundException("cannot find AlarmActionObject:" + actionObject);
    }

}