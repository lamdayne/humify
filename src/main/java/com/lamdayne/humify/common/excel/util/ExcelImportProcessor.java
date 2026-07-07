package com.lamdayne.humify.common.excel.util;

import com.lamdayne.humify.common.exception.AppException;
import com.lamdayne.humify.common.exception.ErrorCode;
import com.lamdayne.humify.common.excel.dto.ImportResult;
import com.lamdayne.humify.common.excel.dto.ImportRow;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

/**
 * Utility chung để import Excel cho bất kỳ entity nào.
 * Không phải Spring bean — dùng như 1 công cụ (gọi static method), tránh mọi vấn đề
 * về circular dependency hay transaction proxy.
 *
 * Cách dùng: mỗi entity tự viết 1 Import service riêng (là Spring bean),
 * inject "creation service" transactional của entity đó, rồi gọi
 * ExcelImportProcessor.process(...) với các hàm map/validate/save tương ứng.
 */
public final class ExcelImportProcessor<T> {

    public interface HeaderValidator {
        void validate(Sheet sheet);
    }
    private HeaderValidator headerValidator;
    public ExcelImportProcessor<T> withHeaderValidator(HeaderValidator validator) {
        this.headerValidator = validator;
        return this;
    }

    /** Map 1 dòng Excel (Row) thành DTO */
    public interface RowMapper<T> {
        T map(Row row);
    }

    /** Validate DTO, throw exception với message rõ ràng nếu invalid */
    public interface RowValidator<T> {
        void validate(T data);
    }

    /**
     * Lưu 1 dòng dữ liệu. PHẢI là method reference tới 1 Spring bean có
     * @Transactional(propagation = REQUIRES_NEW) — ví dụ employeeCreationService::create —
     * để đảm bảo mỗi dòng có transaction độc lập, dòng lỗi không kéo dòng khác rollback.
     */
    public interface RowSaver<T> {
        void save(T data) throws Exception;
    }

    /** Lấy giá trị dùng để check trùng lặp trong cùng file (ví dụ email). Trả null nếu không cần check field đó. */
    public interface DuplicateKeyExtractor<T> {
        String extractKey(T data);
    }

    private static final int DEFAULT_MAX_ROWS = 1000;

    private final List<String> expectedHeaders;
    private final RowMapper<T> mapper;
    private final RowValidator<T> validator;
    private final RowSaver<T> saver;
    private final List<DuplicateCheck<T>> duplicateChecks = new ArrayList<>();
    private int maxRows = DEFAULT_MAX_ROWS;

    private ExcelImportProcessor(List<String> expectedHeaders, RowMapper<T> mapper,
                                 RowValidator<T> validator, RowSaver<T> saver) {
        this.expectedHeaders = expectedHeaders;
        this.mapper = mapper;
        this.validator = validator;
        this.saver = saver;
    }

    public static <T> ExcelImportProcessor<T> builder(
            List<String> expectedHeaders,
            RowMapper<T> mapper,
            RowValidator<T> validator,
            RowSaver<T> saver) {
        return new ExcelImportProcessor<>(expectedHeaders, mapper, validator, saver);
    }

    /** Thêm 1 field cần check trùng trong file (có thể gọi nhiều lần cho nhiều field, ví dụ email + employeeCode) */
    public ExcelImportProcessor<T> withDuplicateCheck(String fieldName, DuplicateKeyExtractor<T> extractor) {
        this.duplicateChecks.add(new DuplicateCheck<>(fieldName, extractor));
        return this;
    }

    public ExcelImportProcessor<T> withMaxRows(int maxRows) {
        this.maxRows = maxRows;
        return this;
    }

    public ImportResult process(MultipartFile file) {
        Workbook workbook = openWorkbook(file);

        try {
            Sheet sheet = workbook.getSheetAt(0);
            validateHeader(sheet);

            int lastRowNum = sheet.getLastRowNum();
            if (lastRowNum > maxRows) {
                throw new AppException(ErrorCode.EXCEL_TOO_MANY_ROWS);
            }

            ImportResult result = new ImportResult();
            result.setTotalRows(lastRowNum);

            List<ImportRow<T>> rows = readRows(sheet, result);
            for (DuplicateCheck<T> check : duplicateChecks) {
                removeDuplicates(rows, result, check);
            }
            saveRows(rows, result);

            return result;
        } finally {
            try {
                workbook.close();
            } catch (IOException ignored) {
                // best-effort
            }
        }
    }

    private Workbook openWorkbook(MultipartFile file) {
        try {
            return ExcelUtil.read(file);
        } catch (Exception e) {
            throw new AppException(ErrorCode.INVALID_EXCEL_FILE);
        }
    }

    private void validateHeader(Sheet sheet) {
        if (headerValidator != null) {
            headerValidator.validate(sheet);
            return;
        }
        Row header = sheet.getRow(0);
        if (header == null) throw new AppException(ErrorCode.INVALID_EXCEL_HEADER);
        for (int i = 0; i < expectedHeaders.size(); i++) {
            if (!expectedHeaders.get(i).equals(CellUtil.getString(header, i))) {
                throw new AppException(ErrorCode.INVALID_EXCEL_HEADER);
            }
        }
    }

    private List<ImportRow<T>> readRows(Sheet sheet, ImportResult result) {
        List<ImportRow<T>> rows = new ArrayList<>();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                T data = mapper.map(row);
                validator.validate(data);
                rows.add(new ImportRow<>(i + 1, data));
            } catch (Exception ex) {
                result.addError(i + 1, "", ex.getMessage());
            }
        }
        return rows;
    }

    private void removeDuplicates(List<ImportRow<T>> rows, ImportResult result, DuplicateCheck<T> check) {
        Map<String, Integer> seen = new HashMap<>();
        Iterator<ImportRow<T>> it = rows.iterator();

        while (it.hasNext()) {
            ImportRow<T> item = it.next();
            String key = check.extractor.extractKey(item.getData());
            if (key == null) continue;

            Integer firstRow = seen.putIfAbsent(key, item.getRowNumber());
            if (firstRow != null) {
                result.addError(
                        item.getRowNumber(),
                        check.fieldName,
                        "Duplicate " + check.fieldName + " with row " + firstRow + " in the same file"
                );
                it.remove();
            }
        }
    }

    private void saveRows(List<ImportRow<T>> rows, ImportResult result) {
        for (ImportRow<T> item : rows) {
            try {
                saver.save(item.getData());
                result.increaseSuccess();
            } catch (Exception ex) {
                result.addError(item.getRowNumber(), "", ex.getMessage());
            }
        }
    }


    private record DuplicateCheck<T>(String fieldName, DuplicateKeyExtractor<T> extractor) {}
}