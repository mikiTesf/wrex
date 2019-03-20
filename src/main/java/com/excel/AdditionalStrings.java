package com.excel;

import java.util.HashMap;

class AdditionalStrings {
    static final String MEETING_NAME      = "ክርስቲያናዊ ህይወታችንና አገልግሎታችን";
    static final String CHAIRMAN          = "ሊቀመንበር";
    static final String OPENING_PRAYER    = "የመክፈቻ ፀሎት";
    static final String MAIN_HALL         = "በዋናው አዳራሽ";
    static final String SECOND_HALL       = "በሁለተኛው አዳራሽ";
    static final String READER            = "አንባቢ";
    static final String CONCLUDING_PRAYER = "ፀሎት";
    // test string(s)
    static final String BIBLE_READING = "የመጽሐፍ ቅዱስ ንባብ";

    static final HashMap<String, String> MONTHS = new HashMap<>();

    static {
        MONTHS.put("01", "ጥር");
        MONTHS.put("02", "የካቲት");
        MONTHS.put("03", "መጋቢት");
        MONTHS.put("04", "ሚያዝያ");
        MONTHS.put("05", "ግንቦት");
        MONTHS.put("06", "ሰኔ");
        MONTHS.put("07", "ሐምሌ");
        MONTHS.put("08", "ነሐሴ");
        MONTHS.put("09", "መስከረም");
        MONTHS.put("10", "ጥቅምት");
        MONTHS.put("11", "ህዳር");
        MONTHS.put("12", "ታህሳሥ");
    }
}
