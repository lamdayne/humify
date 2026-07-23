package com.lamdayne.humify.payroll.util;


import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Tính toán Bảo hiểm bắt buộc & Thuế TNCN lũy tiến từng phần theo quy định Việt Nam.
 * Các mốc tiền là hằng số nghiệp vụ hiện hành — cân nhắc đưa ra bảng cấu hình (tax_brackets)
 * nếu cần thay đổi theo từng năm tài chính mà không phải build lại ứng dụng.
 */
public final class PayrollTaxCalculator {

    private PayrollTaxCalculator() {
    }

    public static final BigDecimal SOCIAL_INSURANCE_RATE = new BigDecimal("0.08");   // BHXH 8%
    public static final BigDecimal HEALTH_INSURANCE_RATE = new BigDecimal("0.015");  // BHYT 1.5%
    public static final BigDecimal UNEMPLOYMENT_INSURANCE_RATE = new BigDecimal("0.01"); // BHTN 1%

    public static final BigDecimal SELF_DEDUCTION = new BigDecimal("11000000");
    public static final BigDecimal DEPENDENT_DEDUCTION = new BigDecimal("4400000");

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private record TaxBracket(BigDecimal upperBound, BigDecimal rate, BigDecimal quickDeduction) {
    }

    /** Biểu thuế lũy tiến 7 bậc, tính theo phương pháp trừ nhanh: PIT = TNTT * rate - quickDeduction. */
    private static final TaxBracket[] BRACKETS = new TaxBracket[]{
            new TaxBracket(new BigDecimal("5000000"), new BigDecimal("0.05"), BigDecimal.ZERO),
            new TaxBracket(new BigDecimal("10000000"), new BigDecimal("0.10"), new BigDecimal("250000")),
            new TaxBracket(new BigDecimal("18000000"), new BigDecimal("0.15"), new BigDecimal("750000")),
            new TaxBracket(new BigDecimal("32000000"), new BigDecimal("0.20"), new BigDecimal("1650000")),
            new TaxBracket(new BigDecimal("52000000"), new BigDecimal("0.25"), new BigDecimal("3250000")),
            new TaxBracket(new BigDecimal("80000000"), new BigDecimal("0.30"), new BigDecimal("5850000")),
            new TaxBracket(null, new BigDecimal("0.35"), new BigDecimal("9850000")), // > 80 triệu
    };

    public static BigDecimal calculateSocialInsurance(BigDecimal insuranceSalary) {
        return safeMultiply(insuranceSalary, SOCIAL_INSURANCE_RATE);
    }

    public static BigDecimal calculateHealthInsurance(BigDecimal insuranceSalary) {
        return safeMultiply(insuranceSalary, HEALTH_INSURANCE_RATE);
    }

    public static BigDecimal calculateUnemploymentInsurance(BigDecimal insuranceSalary) {
        return safeMultiply(insuranceSalary, UNEMPLOYMENT_INSURANCE_RATE);
    }

    /** TNTT = Gross - (giảm trừ bản thân + giảm trừ người phụ thuộc + bảo hiểm bắt buộc đã nộp). Âm -> 0. */
    public static BigDecimal calculateTaxableIncome(
            BigDecimal grossIncome,
            int taxableDependents,
            BigDecimal totalCompulsoryInsurance
    ) {
        BigDecimal dependentDeduction = DEPENDENT_DEDUCTION.multiply(BigDecimal.valueOf(Math.max(taxableDependents, 0)));
        BigDecimal totalDeduction = SELF_DEDUCTION.add(dependentDeduction).add(nvl(totalCompulsoryInsurance));
        BigDecimal taxable = nvl(grossIncome).subtract(totalDeduction);
        return taxable.max(BigDecimal.ZERO).setScale(SCALE, ROUNDING);
    }

    public static BigDecimal calculatePersonalIncomeTax(BigDecimal taxableIncome) {
        if (taxableIncome == null || taxableIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(SCALE, ROUNDING);
        }
        for (TaxBracket bracket : BRACKETS) {
            if (bracket.upperBound() == null || taxableIncome.compareTo(bracket.upperBound()) <= 0) {
                BigDecimal tax = taxableIncome.multiply(bracket.rate()).subtract(bracket.quickDeduction());
                return tax.max(BigDecimal.ZERO).setScale(SCALE, ROUNDING);
            }
        }
        throw new IllegalStateException("Không xác định được bậc thuế cho taxableIncome=" + taxableIncome);
    }

    private static BigDecimal safeMultiply(BigDecimal value, BigDecimal rate) {
        return nvl(value).multiply(rate).setScale(SCALE, ROUNDING);
    }

    private static BigDecimal nvl(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}