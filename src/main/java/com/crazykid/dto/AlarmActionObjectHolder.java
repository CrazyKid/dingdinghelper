package com.crazykid.dto;


/**
 * @author arthur
 * @date 2024/12/23 17:58
 */
public class AlarmActionObjectHolder {
    private Class<? extends AlarmActionObject> clazz;
    private AlarmActionObject instance;

    public Class<? extends AlarmActionObject> getClazz() {
        return clazz;
    }

    public void setClazz(Class<? extends AlarmActionObject> clazz) {
        this.clazz = clazz;
    }

    public AlarmActionObject getInstance() {
        return instance;
    }

    public void setInstance(AlarmActionObject instance) {
        this.instance = instance;
    }
}
