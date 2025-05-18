package cn.bincker.common.data;

public record Pair<T, V> (
    T first,
    V second
){}
