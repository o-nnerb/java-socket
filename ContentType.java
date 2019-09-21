import java.util.Optional;

public enum ContentType {
    data(1), string(2);

    final Integer rawValue;
    private ContentType(Integer rawValue) {
        this.rawValue = rawValue;
    }

    public static Optional<ContentType> init(Integer rawValue) {
        for(ContentType c: ContentType.values()) {
            if (c.rawValue == rawValue) {
                return Optional.of(c);
            }
        }

        return Optional.empty();
    }
}