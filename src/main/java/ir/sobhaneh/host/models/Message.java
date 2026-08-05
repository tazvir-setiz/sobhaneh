package ir.sobhaneh.host.models;

import java.io.Serializable;

public record Message(int seq, String from, String type, String body) implements Serializable {
}
