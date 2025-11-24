package com.tys.service;

import com.tys.model.Guest;
import com.tys.repository.GuestRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ExcelImportService {

    @Autowired
    private GuestRepository guestRepository;

    public void importGuestsFromExcel(MultipartFile file) throws Exception {

        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Guest guest = new Guest();
            guest.setName(getCellValue(row, 0));
            guest.setSurname(getCellValue(row, 1));
            guest.setPhoneNumber(getCellValue(row, 2));
            guest.setIdentityNumber(getCellValue(row, 3));
            guest.setPlateNumber(getCellValue(row, 4));
            guest.setJob(getCellValue(row, 5));

            guestRepository.save(guest);
        }

        workbook.close();
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
                // sayı ise stringe çevir
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