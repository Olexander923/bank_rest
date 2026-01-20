package com.example.bankcards.util;

import com.example.bankcards.dto.BlockRequestDTO;
import com.example.bankcards.entity.BlockRequest;
import com.example.bankcards.entity.Card;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class BlockRequestMapper {
    private final CardEncryptionUtil cardEncryptionUtil;

    public BlockRequestDTO requestToDTO(BlockRequest request) {
        Objects.requireNonNull(request,"Block request cannot be null");
        Objects.requireNonNull(request.getCard(),"Card number cannot be null");
        Objects.requireNonNull(request.getUser(),"User cannot be null");

        String originalNumber = cardEncryptionUtil.decrypt(request.getCard().getCardNumber());
        String masked = "**** **** **** " + originalNumber.substring(originalNumber.length() - 4);
        BlockRequestDTO requestDTO = new BlockRequestDTO();
        requestDTO.setUserId(request.getUser().getId());
        requestDTO.setCardId(request.getCard().getId());
        requestDTO.setId(request.getId());
        requestDTO.setMaskedNumber(masked);
        requestDTO.setRequestDate(request.getRequestDate());
        requestDTO.setRequestStatus(request.getRequestStatus());

        return requestDTO;
    }
}
