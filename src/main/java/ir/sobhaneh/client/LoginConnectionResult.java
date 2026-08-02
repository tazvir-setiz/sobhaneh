package ir.sobhaneh.client;

import ir.sobhaneh.common.Connection;


public record LoginConnectionResult(Connection connection, String errorMessage) {
}
