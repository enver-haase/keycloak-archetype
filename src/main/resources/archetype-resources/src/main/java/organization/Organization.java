package ${package}.organization;

import java.io.Serializable;

public record Organization(String id, String name) implements Serializable {
}
