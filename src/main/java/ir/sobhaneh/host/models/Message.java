package ir.sobhaneh.host.models;

public record Message(int seq, String from, String type, String body) {}
