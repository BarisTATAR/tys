package com.tys.service;

import com.tys.model.Company;
import com.tys.model.Guest;
import com.tys.repository.CompanyRepository;
import com.tys.repository.GuestRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ExcelImportService {

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Transactional
    public void importGuestsFromExcel(MultipartFile file, Long companyId) throws Exception {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));

        List<Guest> guestsToImport = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                Guest guest = new Guest();
                guest.setName(getCellValue(row, 0));
                guest.setSurname(getCellValue(row, 1));
                guest.setPhoneNumber(getCellValue(row, 2));
                guest.setIdentityNumber(getCellValue(row, 3));
                guest.setPlateNumber(getCellValue(row, 4));
                guest.setJob(getCellValue(row, 5));
                guest.setCompany(company);

                guestsToImport.add(guest);
            }
        }

        assertNoDuplicateIdentityNumbers(guestsToImport);

        for (Guest guest : guestsToImport) {
            guestRepository.save(guest);
        }
    }

    private void assertNoDuplicateIdentityNumbers(List<Guest> guests) {
        Set<String> seenInFile = new HashSet<>();

        for (Guest guest : guests) {
            String identityNumber = normalizeIdentityNumber(guest.getIdentityNumber());
            if (identityNumber == null) {
                continue;
            }

            guest.setIdentityNumber(identityNumber);

            if (!seenInFile.add(identityNumber)) {
                throw new IllegalArgumentException(
                        "Excel dosyasında aynı TC kimlik numarası birden fazla kez geçiyor: " + identityNumber);
            }

            if (guestRepository.existsByIdentityNumber(identityNumber)) {
                throw new IllegalArgumentException(
                        "Bu TC kimlik numarası sistemde zaten kayıtlı: " + identityNumber);
            }
        }
    }

    private static String normalizeIdentityNumber(String identityNumber) {
        if (identityNumber == null) {
            return null;
        }
        String normalized = identityNumber.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i <= 5; i++) {
            String value = getCellValue(row, i);
            if (value != null && !value.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String getCellValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);

        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();

            case NUMERIC:
                return String.valueOf((long) cell.getNumericCellValue());

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case BLANK:
                return null;

            default:
                return null;
        }
    }
}
