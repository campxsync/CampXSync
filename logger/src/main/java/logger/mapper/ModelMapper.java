package logger.mapper;

import logger.utilities.JsonUtils;
import java.util.List;
import java.util.stream.Collectors;

public final class ModelMapper {
    private ModelMapper() {
        // Prevent instantiation
    }

    /**
     * Maps a source object to a target class.
     */
    public static <S, T> T map(S source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            // Jackson map conversion: convertValue is extremely fast and respects annotations/configs
            return JsonUtils.getMapper().convertValue(source, targetClass);
        } catch (IllegalArgumentException e) {
            // Fallback to serialization/deserialization if there is conversion mismatch
            String json = JsonUtils.toJson(source);
            return JsonUtils.fromJson(json, targetClass);
        }
    }

    /**
     * Maps a list of source objects to a list of target class instances.
     */
    public static <S, T> List<T> mapList(List<S> sourceList, Class<T> targetClass) {
        if (sourceList == null) {
            return null;
        }
        return sourceList.stream()
                .map(source -> map(source, targetClass))
                .collect(Collectors.toList());
    }
}
