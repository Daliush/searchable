package fr.daliush.searchable.processor.service.methods.utils;

public enum TypeEnum {
    STRING("java.lang.String"),
    INTEGER("java.lang.Integer"),
    LONG("java.lang.Long"),
    BIGDECIMAL("java.math.BigDecimal"),
    BOOLEAN("java.lang.Boolean"),
    DATE("java.util.Date"),
    LOCALDATE("java.time.LocalDate"),
    LOCALDATETIME("java.time.LocalDateTime"),
    INSTANT("java.time.Instant");

    private final String value;

    TypeEnum(String value)
    {
        this.value = value;
    }

    public String getValue()
    {
        return value;
    }
}
