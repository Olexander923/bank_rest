package com.example.bankcards.controller;

import com.example.bankcards.dto.TransactionFilterDTO;
import com.example.bankcards.dto.TransactionResponseDTO;
import com.example.bankcards.service.TransactionService;
import com.example.bankcards.util.CustomUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * для user(просмотр истории операций, фильтр по периоду,экспорт выписки по карте)
 */
@RestController
@RequestMapping("/api/user/transactions")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('USER')")
public class UserTransactionController {
    private final TransactionService transactionService;

    @GetMapping//получить всю историю только user транзакций с паганацией
    public ResponseEntity<Page<TransactionResponseDTO>> getUserTransactions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute @Valid TransactionFilterDTO filterDTO,
            @Min(0) @RequestParam(defaultValue = "0") int page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = userDetails.getUserId();
        Pageable pageable = PageRequest.of(page,size);
        Page<TransactionResponseDTO> result = transactionService.getFilteredTransaction(userId,filterDTO,pageable);
        return ResponseEntity.ok(result);
    }

//    //экспорт выписки в CSV
//    @GetMapping("/export")
//    public void exportToCSV(HttpServletResponse response,
//                            @AuthenticationPrincipal CustomUserDetails userDetails,
//                            @ModelAttribute @Valid TransactionFilterDTO filterDTO,
//                            @Min(0) @RequestParam(defaultValue = "0") int page,
//                            @Min(1) @Max(100) @RequestParam(defaultValue = "10") int size
//    ) throws IOException {
//        response.setContentType("text/csv");
//        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
//        String currentDateTime = dateFormatter.format(new Date());
//        String headerKey = "Content-Disposition";
//        String headerValue = "attachment; filename=users_" + currentDateTime + ".csv";
//        response.setHeader(headerKey,headerValue);
//
//        ICsvBeanWriter csvWriter = new CsvBeanWriter(response.getWriter(), CsvPreference.STANDARD_PREFERENCE);
//        String[] csvHeader = {"From Card", "To Card", "Amount", "Date", "Status"};
//        String[] nameMapping = {"fromMaskedCard", "toMaskedCard", "amount", "timeStamp", "transactionStatus"};
//
//        Pageable pageable = PageRequest.of(page,size);
//        Long currentUserId = userDetails.getUserId();
//        Page<TransactionResponseDTO> transactions = transactionService.getFilteredTransaction(currentUserId,filterDTO,pageable);
//
//        //если транзакций нет,вернуть пустой файл с заголовками
//        if (transactions.isEmpty()) {
//            csvWriter.writeHeader(csvHeader);
//            csvWriter.close();
//            return;
//        }
//
//        csvWriter.write(transactions.getContent(),nameMapping);
//        csvWriter.close();
//    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportToCSV(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) throws IOException {
        Pageable pageable = PageRequest.of(page, size);
        Long userId = userDetails.getUserId();
        // Передаём пустой фильтр
        TransactionFilterDTO filterDTO = new TransactionFilterDTO();
        Page<TransactionResponseDTO> result = transactionService.getFilteredTransaction(userId, filterDTO, pageable);

        if (result.isEmpty()) {
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=transactions_empty.csv")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(new byte[0]);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ICsvBeanWriter csvWriter = new CsvBeanWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8),
                CsvPreference.STANDARD_PREFERENCE);

        String[] csvHeader = {"From Card", "To Card", "Amount", "Date", "Status"};
        String[] nameMapping = {"fromMaskedCard", "toMaskedCard", "amount", "timeStamp", "transactionStatus"};

        csvWriter.writeHeader(csvHeader);
        for (TransactionResponseDTO dto : result.getContent()) {
            csvWriter.write(dto, nameMapping);
        }
        csvWriter.close();

        byte[] csvBytes = outputStream.toByteArray();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        System.out.println("Transactions found: " + result.getContent().size());
        System.out.println("CSV bytes: " + csvBytes.length);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=transactions_" + timestamp + ".csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }

    //todo сделать еще экспорт в pdf?
//    @GetMapping("/exportPDF")
}
