package grails.views.utils

import groovy.transform.CompileStatic

/**
 * Utility methods for the views project
 * @author Graeme Rocher
 * @since 1.1
 */
@CompileStatic
class ViewUtils {

    /**
     * Retrieves a boolean value from a Map for the given key
     *
     * @param key The key that references the boolean value
     * @param map The map to look in
     * @return A boolean value which will be false if the map is null, the map doesn't contain the key or the value is false
     */
    public static boolean getBooleanFromMap(String key, Map<?, ?> map, boolean defaultValue = false) {
        if (map == null) return defaultValue
        if (map.containsKey(key)) {
            Object o = map.get(key)
            if (o == null)return defaultValue
            if (o instanceof Boolean) {
                return (Boolean)o
            }
            return Boolean.valueOf(o.toString())
        }
        return defaultValue
    }
}
