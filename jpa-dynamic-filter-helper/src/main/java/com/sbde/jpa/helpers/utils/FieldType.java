package com.sbde.jpa.helpers.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
public enum FieldType {

    BOOLEAN {
        public Object parse(String value) {
            return Boolean.valueOf(value);
        }
    },

    CHAR {
        public Object parse(String value) {
            return value.charAt(0);
        }
    },

    DATE {
        public Object parse(String value) {
            Object date = null;
            try {
                date = LocalDate.parse(value);
            } catch (Exception e) {
                log.error("Failed parse field type DATE {}", e.getMessage());
            }

            return date;
        }
    },

    DATE_TIME {
        public Object parse(String value) {
            Object date = null;
            try {
                date = LocalDateTime.parse(value);
            } catch (Exception e) {
                log.error("Failed parse field type DATE_TIME {}", e.getMessage());
            }

            return date;
        }
    },

    DOUBLE {
        public Object parse(String value) {
            return Double.valueOf(value);
        }
    },

    INTEGER {
        public Object parse(String value) {
            return Integer.valueOf(value);
        }
    },

    LONG {
        public Object parse(String value) {
            return Long.valueOf(value);
        }
    },

    STRING {
        public Object parse(String value) {
            return value;
        }
    },

    ENUM {
        @Override
        public Object parse(String value) {
            return value;
        }
    };

    public abstract Object parse(String value);
}
