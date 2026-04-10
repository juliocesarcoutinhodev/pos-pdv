package br.com.topone.backend.application.usecase.supplier;

import br.com.topone.backend.domain.exception.SupplierNotFoundException;
import br.com.topone.backend.domain.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeactivateSupplierUseCase {

    private final SupplierRepository supplierRepository;

    @Transactional
    public DeactivateSupplierResult execute(DeactivateSupplierCommand command) {
        var supplier = supplierRepository.findById(command.id())
                .orElseThrow(SupplierNotFoundException::new);

        supplier.deactivate();
        supplierRepository.save(supplier);
        log.info("Supplier deactivated | id={}", supplier.getId());

        return new DeactivateSupplierResult(supplier.getId());
    }
}
