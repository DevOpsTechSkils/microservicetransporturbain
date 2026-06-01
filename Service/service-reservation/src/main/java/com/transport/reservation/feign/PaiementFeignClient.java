package com.transport.reservation.feign;

import com.transport.reservation.dto.PaiementRequestDTO;
import com.transport.reservation.dto.PaiementResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "service-paiement")
public interface PaiementFeignClient {

    @PostMapping("/api/paiements/initier")
    PaiementResponseDTO initierPaiement(@RequestBody PaiementRequestDTO request);
}
