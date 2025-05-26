package com.test.batchsystempractice;

public enum Extension {
    TXT("txt"),
    CSV("csv"),
    XML("xml"),
    JSON("json");

    private final String extension;

    Extension(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    @Override
    public String toString() {
        return extension;
    }
}
