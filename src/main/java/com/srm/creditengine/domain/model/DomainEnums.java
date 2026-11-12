package com.srm.creditengine.domain.model;

public final class DomainEnums {

    private DomainEnums() {
    }

    public enum ReceivableType {
        DUPLICATA_MERCANTIL,
        CHEQUE_PRE_DATADO
    }

    public enum Currency {
        BRL,
        USD
    }

    public enum ReceivableStatus {
        OPEN,
        SETTLED
    }
}
