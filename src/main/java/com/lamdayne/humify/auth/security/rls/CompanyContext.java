package com.lamdayne.humify.auth.security.rls;

public class CompanyContext {

    private CompanyContext() {}

    private static final ThreadLocal<Long> currentCompanyId = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> isAdmin = new ThreadLocal<>();

    public static void setCompanyId(Long companyId) {
        currentCompanyId.set(companyId);
    }

    public static Long getCompanyId() {
        return currentCompanyId.get();
    }

    public static void setAdmin(boolean admin) {
        isAdmin.set(admin);
    }

    public static boolean isAdmin() {
        Boolean admin = isAdmin.get();
        return admin != null && admin;
    }

    public static void clear() {
        currentCompanyId.remove();
        isAdmin.remove();
    }

}
