package br.com.topone.backend.application.usecase.supplier;

import br.com.topone.backend.domain.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListSuppliersUseCase {

    private final SupplierRepository supplierRepository;

    public ListSuppliersResult execute(ListSuppliersCommand command) {
        var pageResult = supplierRepository.findAll(
                command.filter(),
                command.page(),
                command.size(),
                command.sort()
        );

        log.debug("List suppliers | page={} | size={} | total={}",
                command.page(), command.size(), pageResult.totalElements());

        var summaries = pageResult.content().stream()
                .map(supplier -> new ListSuppliersResult.SupplierSummary(
                        supplier.getId(),
                        supplier.getName(),
                        supplier.getTaxId(),
                        supplier.getEmail(),
                        supplier.getPhone(),
                        supplier.getCreatedAt(),
                        supplier.getUpdatedAt(),
                        supplier.isActive()
                ))
                .toList();

        return new ListSuppliersResult(
                summaries,
                pageResult.page(),
                pageResult.size(),
                pageResult.totalElements(),
                pageResult.totalPages()
        );
    }
}
