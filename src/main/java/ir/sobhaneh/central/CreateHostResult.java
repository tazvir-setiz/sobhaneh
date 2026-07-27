package ir.sobhaneh.central;

public enum CreateHostResult {
    OK,
    PORT_NUMBER_MUST_BE_AT_LEAST_10000,
    AT_MOST_1000_PORTS_IS_ALLOWED,
    PORT_IN_USE_BY_ANOTHER_HOST
}
