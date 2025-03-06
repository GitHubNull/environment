def modify(Map<String, String> oldNameAndValue) {
    StringBuilder result = new StringBuilder()
    oldNameAndValue.each { key, value ->
        result.append("Key: $key, Value: $value")
    }
    return result.toString()
}