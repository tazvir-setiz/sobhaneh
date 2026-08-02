package ir.sobhaneh.central.models;

public record Message(int seq, String from, String type, String body) {}
