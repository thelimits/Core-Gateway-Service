package com.s8.demoservice.convertor;

import java.io.IOException;

@FunctionalInterface
public interface Convertor<I, R> {
    I convert(R input) throws IOException;
}
