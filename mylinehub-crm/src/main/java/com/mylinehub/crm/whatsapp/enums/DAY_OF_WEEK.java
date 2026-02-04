package com.mylinehub.crm.whatsapp.enums;

import java.util.HashMap;
import java.util.Map;

public enum DAY_OF_WEEK {

	MONDAY(1),
	TUESDAY(2),
	WEDNESDAY(3),
	THURSDAY(4),
	FRIDAY(5),
	SATURDAY(6),
	SUNDAY(7);
	
	private int value;
    private static Map<Integer,DAY_OF_WEEK> map = new HashMap<>();


    static {
        for (DAY_OF_WEEK day : DAY_OF_WEEK.values()) {
            map.put(day.value, day);
        }
    }

    public static DAY_OF_WEEK valueOf(int day) {
        return (DAY_OF_WEEK) map.get(day);
    }
    
    private DAY_OF_WEEK(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
	
}
