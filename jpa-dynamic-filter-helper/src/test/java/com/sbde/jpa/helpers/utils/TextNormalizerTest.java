package com.sbde.jpa.helpers.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextNormalizerTest {

    @Test
    void foldsCaseAndAccents() {
        assertThat(TextNormalizer.normalize("Élève Çà ŒUVRE Straße")).isEqualTo("eleve ca oeuvre strasse");
    }

    @Test
    void foldsDecomposedAccents() {
        assertThat(TextNormalizer.normalize("Cre\u0301ation")).isEqualTo("creation");
    }
}
