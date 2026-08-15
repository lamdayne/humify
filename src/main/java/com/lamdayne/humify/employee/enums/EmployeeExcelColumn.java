package com.lamdayne.humify.employee.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum EmployeeExcelColumn {
    BRANCH("Branch", true, 0, "Chi nhánh"),
    DEPARTMENT("Department", true, 1, "Phòng ban"),
    EMAIL("Email", true, 2, "Email"),
    FULL_NAME("Full Name", true, 3, "Họ và tên"),
    POSITION("Position", true, 4, "Vị trí"),
    DATE_OF_BIRTH("Date of Birth", true, 5, "Ngày sinh"),
    GENDER("Gender", true, 6, "Giới tính"),
    START_DATE("Start Date", true, 7, "Ngày bắt đầu làm việc"),
    STATUS("Status", true, 8, "Trạng thái"),
    DEGREE("Degree", true, 9, "Bằng cấp"),
    SCHOOL("School", true, 10, "Trường"),
    MAJOR("Major", true, 11, "Chuyên ngành"),
    START_YEAR("Start Year", true, 12, "Năm học bắt đầu"),
    END_YEAR("End Year", true, 13, "Năm học kết thúc"),
    GPA("GPA", true, 14, "GPA"),

    ;

    private final String defaultHeaderName;
    private final boolean required;
    private final int defaultIndex;
    private final List<String> aliases;

    EmployeeExcelColumn(String defaultHeaderName, boolean required, int defaultIndex, String... aliases) {
        this.defaultHeaderName = defaultHeaderName;
        this.required = required;
        this.defaultIndex = defaultIndex;
        this.aliases = Arrays.stream(aliases).map(String::toLowerCase).toList();
    }

    public boolean matches(String cellText) {
        if (cellText == null) return false;
        String clean = cellText.trim().toLowerCase();
        return clean.equals(defaultHeaderName.toLowerCase()) ||
                aliases.stream().anyMatch(alias -> alias.contains(clean));
    }
}
